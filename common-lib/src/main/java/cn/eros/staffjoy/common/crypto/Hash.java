package cn.eros.staffjoy.common.crypto;


import org.bouncycastle.util.encoders.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @author 周光兵
 * @date 2021/10/18 23:23
 */
public class Hash {
    public static String encode(String key, String data) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac sha256HMac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256HMac.init(secretKey);

        return Hex.toHexString(sha256HMac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }
}
