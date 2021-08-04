package cn.eros.staffjoy.common.crypto;

import cn.eros.staffjoy.common.error.ServiceException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author 周光兵
 * @date 2021/8/3 13:11
 */
public class Sign {
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_USER_ID = "userid";
    public static final String CLAIM_SUPPORT = "support";

    private static final Map<String, JWTVerifier> VERIFIER_MAP = new HashMap<>();
    private static final Map<String, Algorithm> ALGORITHM_MAP = new HashMap<>();

    public static String generateEmailConfirmationToken(String userid, String email, String signingToken) {
        Algorithm algorithm = getAlgorithm(signingToken);

        return JWT.create()
            .withClaim(CLAIM_EMAIL, email)
            .withClaim(CLAIM_USER_ID, userid)
            .withExpiresAt(new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(2L)))
            .sign(algorithm);
    }

    public static String generateSessionToken(String userid, String signingToken, boolean support, long duration) {
        if (StringUtils.isEmpty(signingToken)) {
            throw new ServiceException("No signing token present");
        }

        Algorithm algorithm = getAlgorithm(signingToken);

        return JWT.create()
            .withClaim(CLAIM_USER_ID, userid)
            .withClaim(CLAIM_SUPPORT, support)
            .withExpiresAt(new Date(System.currentTimeMillis() + duration))
            .sign(algorithm);
    }

    public static DecodedJWT verifySessionToken(String tokenString, String signingToken) {
        return verifyToken(tokenString, signingToken);
    }

    private static Algorithm getAlgorithm(String signingToken) {
        Algorithm algorithm = ALGORITHM_MAP.get(signingToken);

        if (algorithm == null) {
            synchronized (ALGORITHM_MAP) {
                algorithm = ALGORITHM_MAP.get(signingToken);

                if (algorithm == null) {
                    algorithm = Algorithm.HMAC512(signingToken);
                    ALGORITHM_MAP.put(signingToken, algorithm);
                }
            }
        }

        return algorithm;
    }

    private static DecodedJWT verifyToken(String tokenString, String signingToken) {
        JWTVerifier verifier = VERIFIER_MAP.get(signingToken);

        if (verifier == null) {
            synchronized (VERIFIER_MAP) {
                verifier = VERIFIER_MAP.get(signingToken);

                if (verifier == null) {
                    Algorithm algorithm = Algorithm.HMAC512(signingToken);
                    verifier = JWT.require(algorithm).build();
                    VERIFIER_MAP.put(signingToken, verifier);
                }
            }
        }

        return verifier.verify(tokenString);
    }
}
