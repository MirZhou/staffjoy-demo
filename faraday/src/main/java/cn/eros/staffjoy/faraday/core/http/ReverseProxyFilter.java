package cn.eros.staffjoy.faraday.core.http;

import cn.eros.staffjoy.faraday.config.MappingProperties;
import cn.eros.staffjoy.faraday.core.interceptor.PreForwardRequestInterceptor;
import cn.eros.staffjoy.faraday.core.mappings.MappingProvider;
import cn.eros.staffjoy.faraday.core.trace.ProxyingTraceInterceptor;
import cn.eros.staffjoy.faraday.exceptions.FaradayException;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.valueOf;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * @author 周光兵
 * @date 2021/7/26 13:51
 */
public class ReverseProxyFilter extends OncePerRequestFilter {
    protected static final String X_FORWARDED_FOR_HEADER = "X-Forwarded-For";
    protected static final String X_FORWARDED_PROTO_HEADER = "X-Forwarded-Proto";
    protected static final String X_FORWARDED_HOST_HEADER = "X-Forwarded-Host";
    protected static final String X_FORWARDED_PORT_HEADER = "X-Forwarded-Port";

    private static final ILogger log = SLoggerFactory.getLogger(ReverseProxyFilter.class);

    protected final RequestDataExtractor extractor;
    protected final MappingProvider mappingProvider;
    protected final RequestForwarder requestForwarder;
    protected final ProxyingTraceInterceptor traceInterceptor;
    protected final PreForwardRequestInterceptor preForwardRequestInterceptor;

    public ReverseProxyFilter(RequestDataExtractor extractor,
                              MappingProvider mappingProvider,
                              RequestForwarder requestForwarder,
                              ProxyingTraceInterceptor traceInterceptor,
                              PreForwardRequestInterceptor preForwardRequestInterceptor) {
        this.extractor = extractor;
        this.mappingProvider = mappingProvider;
        this.requestForwarder = requestForwarder;
        this.traceInterceptor = traceInterceptor;
        this.preForwardRequestInterceptor = preForwardRequestInterceptor;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException {
        String originUri = this.extractor.extractUri(request);
        String originHost = this.extractor.extractHost(request);

        log.debug("Incoming request", "method", request.getMethod(),
            "host", originHost,
            "uri", originUri);

        HttpHeaders headers = extractor.extractHttpHeaders(request);
        HttpMethod method = extractor.extractMethod(request);

        String traceId = traceInterceptor.generateTraceId();

        this.traceInterceptor.onRequestReceived(traceId, method, originHost, originUri, headers);

        MappingProperties mapping = mappingProvider.resolveMapping(originHost, request);
        if (mapping == null) {
            this.traceInterceptor.onNoMappingFound(traceId, method, originHost, originUri, headers);

            log.debug(String.format("Forwarding: %s %s %s -> no mapping found", method, originHost, originUri));

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Unsupported domain");
            return;
        } else {
            log.debug(String.format("Forwarding: %s %s %s -> %s", method, originHost, originUri, mapping.getDestinations()));
        }

        byte[] body = this.extractor.extractBody(request);
        addForwardHeaders(request, headers);

        RequestData dataToForward = new RequestData(method, originHost, originUri, headers, body, request);
        preForwardRequestInterceptor.intercept(dataToForward, mapping);

        if (dataToForward.isNeedRedirect() && !isBlank(dataToForward.getRedirectUrl())) {
            log.debug(String.format("Redirecting to -> %s", dataToForward.getRedirectUrl()));
            response.sendRedirect(dataToForward.getRedirectUrl());
            return;
        }

        ResponseEntity<byte[]> responseEntity = requestForwarder.forwardHttpRequest(dataToForward, traceId, mapping);

        this.processResponse(response, responseEntity);
    }

    protected void addForwardHeaders(HttpServletRequest request, HttpHeaders headers) {
        List<String> forwardedFor = headers.get(X_FORWARDED_FOR_HEADER);
        if (isEmpty(forwardedFor)) {
            forwardedFor = new ArrayList<>(1);
        }

        forwardedFor.add(request.getRemoteAddr());

        headers.put(X_FORWARDED_FOR_HEADER, forwardedFor);
        headers.set(X_FORWARDED_PROTO_HEADER, request.getScheme());
        headers.set(X_FORWARDED_HOST_HEADER, request.getServerName());
        headers.set(X_FORWARDED_PORT_HEADER, valueOf(request.getServerPort()));
    }

    protected void processResponse(HttpServletResponse response, ResponseEntity<byte[]> responseEntity) {
        response.setStatus(responseEntity.getStatusCodeValue());
        responseEntity.getHeaders().forEach((name, values) ->
            values.forEach(value -> response.addHeader(name, value)));

        if (responseEntity.getBody() != null) {
            try {
                response.getOutputStream().write(responseEntity.getBody());
            } catch (IOException e) {
                throw new FaradayException("Error writing body of HTTP response", e);
            }
        }
    }
}
