package cn.eros.staffjoy.common.crypto;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 周光兵
 * @date 2021/8/3 13:11
 */
public class Sign {
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_USER_ID = "userid";
    public static final String CLAIM_SUPPORT = "support";

    private static final Map<String, JWTVerifier> VERIFIER_MAP = new HashMap<>();
    private static Map<String, Algorithm> algorithmMap = new HashMap<>();

    public static DecodedJWT verifySessionToken(String tokenString, String signingToken) {
        return verifyToken(tokenString, signingToken);
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
