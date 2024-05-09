package cn.eros.staffjoy.account;

import org.junit.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

/**
 * @author Eros
 * @date 2022/2/11 15:52
 */
public class MyTest {
    @Test
    public void test() throws NoSuchAlgorithmException, InvalidKeyException {
        // 企业代码
        String corpCode = "guizhouhuizhi";
        // appId
        String appId = "RtPagRRJyHhq8Lzg";
        // 时间戳
        long timestamp = System.currentTimeMillis() / 1000;
        // appSecret
        String appSecret = UUID.randomUUID().toString();

        // 构建要加密的字符串
        String text = "\n" + corpCode + "\n" + appId + "\n" + timestamp + "\n";

        byte[] keyBytes = appSecret.getBytes(StandardCharsets.UTF_8);
        // 使用appSecret作为加密向量，并指定加密算法
        Key hmacKey = new SecretKeySpec(keyBytes, "HmacSHA512");
        Mac hmacSha512 = Mac.getInstance("HmacSHA512");
        hmacSha512.init(hmacKey);

        // 加密数据
        byte[] macData = hmacSha512.doFinal(text.getBytes(StandardCharsets.UTF_8));
        // 将加密数据转换为字符串
        String sign = Base64.getEncoder().encodeToString(macData);

        System.out.println(sign);
        System.out.println(sign.length());

        System.out.println("=================");

        text = "\n" + corpCode + "\n" + appId + "\n" + timestamp + "\n" + appSecret + "\n";

        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        byte[] encodedHash = digest.digest(text.getBytes(StandardCharsets.UTF_8));

        // 生成签名
        sign = Base64.getEncoder().encodeToString(encodedHash);

        System.out.println(sign);
        System.out.println(sign.length());
    }
}
