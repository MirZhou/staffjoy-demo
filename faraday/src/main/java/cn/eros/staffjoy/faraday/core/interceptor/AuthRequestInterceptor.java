package cn.eros.staffjoy.faraday.core.interceptor;

import cn.eros.staffjoy.common.auth.AuthConstant;
import cn.eros.staffjoy.common.auth.Sessions;
import cn.eros.staffjoy.common.crypto.Sign;
import cn.eros.staffjoy.common.env.EnvConfig;
import cn.eros.staffjoy.common.services.SecurityConstant;
import cn.eros.staffjoy.common.services.Service;
import cn.eros.staffjoy.common.services.ServiceDirectory;
import cn.eros.staffjoy.faraday.config.MappingProperties;
import cn.eros.staffjoy.faraday.core.http.RequestData;
import cn.eros.staffjoy.faraday.exceptions.FaradayException;
import cn.eros.staffjoy.faraday.exceptions.ForbiddenException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 周光兵
 * @date 2021/8/3 13:21
 */
public class AuthRequestInterceptor implements PreForwardRequestInterceptor {
    private final static ILogger LOGGER = SLoggerFactory.getLogger(AuthRequestInterceptor.class);

    private final String signingSecret;
    private final EnvConfig envConfig;

    /**
     * Use a map for constant time lookups. Value doesn't matter
     * Hypothetically these should be universally unique, so we don't have to limit by env
     */
    private final Map<String, String> bannedUsers = new HashMap<String, String>() {
        private static final long serialVersionUID = -6312512887572513089L;

        {
            put("d7b9dbed-9719-4856-5f19-23da2d0e3dec", "hidden");
        }
    };

    public AuthRequestInterceptor(String signingSecret, EnvConfig envConfig) {
        this.signingSecret = signingSecret;
        this.envConfig = envConfig;
    }

    @Override
    public void intercept(RequestData data, MappingProperties mapping) {
        // sanitize incoming requests and set authorization information
        String authorization = this.setAuthHeader(data, mapping);

        this.validateRestrict(mapping);
        this.validateSecurity(data, mapping, authorization);
    }

    private String setAuthHeader(RequestData data, MappingProperties mapping) {
        // default to anonymous web when prove otherwise
        String authorization = AuthConstant.AUTHORIZATION_ANONYMOUS_WEB;

        HttpHeaders headers = data.getHeaders();

        Session session = this.getSession(data.getOriginRequest());

        if (session != null) {
            authorization = session.isSupport() ? AuthConstant.AUTHORIZATION_SUPPORT_USER : AuthConstant.AUTHORIZATION_AUTHENTICATED_USER;

            this.checkBannedUsers(session.getUserid());

            headers.set(AuthConstant.CURRENT_USER_HEADER, session.getUserid());

        } else {
            headers.remove(AuthConstant.CURRENT_USER_HEADER);
        }

        headers.set(AuthConstant.AUTHORIZATION_HEADER, authorization);

        return authorization;
    }

    private Service getService(MappingProperties mapping) {
        String host = mapping.getHost();
        String subdomain = host.replace("." + this.envConfig.getExternalApex(), "");

        Service service = ServiceDirectory.getMapping().get(subdomain.toLowerCase());

        if (service == null) {
            throw new FaradayException("Unsupported sub-domain " + subdomain);
        }

        return service;
    }

    private void validateRestrict(MappingProperties mapping) {
        Service service = this.getService(mapping);

        if (service.isRestrictDev() && !envConfig.isDebug()) {
            throw new FaradayException("This service is restrict to dev and test environment only");
        }
    }

    /**
     * check response Authorization and see if it's ok with the requested service
     */
    private void validateSecurity(RequestData data, MappingProperties mapping, String authorization) {
        // Check perimeter authorization
        if (AuthConstant.AUTHORIZATION_ANONYMOUS_WEB.equals(authorization)) {
            Service service = this.getService(mapping);

            if (SecurityConstant.SEC_PUBLIC != service.getSecurity()) {
                LOGGER.info("Anonymous user want ot access secure service, redirect to login");

                // send to login
                String schema = "https";
                if (this.envConfig.isDebug()) {
                    schema = "http";
                }

                int port = data.getOriginRequest().getServerPort();

                try {
                    URI redirectUrl = new URI(schema,
                        null,
                        "www." + this.envConfig.getExternalApex(),
                        port,
                        "/login", null, null);

                    String returnTo = data.getHost() + data.getUri();
                    String fullRedirectUrl = redirectUrl + "?return_to=" + returnTo;

                    data.setNeedRedirect(true);
                    data.setRedirectUrl(fullRedirectUrl);
                } catch (URISyntaxException e) {
                    LOGGER.error("Fail to build redirect url", e);
                }
            }
        }
    }

    private void checkBannedUsers(String userid) {
        if (bannedUsers.containsKey(userid)) {
            LOGGER.warn(String.format("Banned user accessing service - user %s", userid));

            throw new ForbiddenException("Banned user forbidden");
        }
    }

    private Session getSession(HttpServletRequest request) {
        String token = Sessions.getToken(request);

        if (token == null) {
            return null;
        }

        try {
            DecodedJWT decodedJwt = Sign.verifySessionToken(token, signingSecret);

            String userid = decodedJwt.getClaim(Sign.CLAIM_USER_ID).asString();
            boolean support = decodedJwt.getClaim(Sign.CLAIM_SUPPORT).asBoolean();

            return Session.builder()
                .userid(userid)
                .support(support)
                .build();
        } catch (Exception e) {
            LOGGER.error("fail to verify token", "token", token, e);
            return null;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    private static class Session {
        private String userid;
        private boolean support;
    }
}
