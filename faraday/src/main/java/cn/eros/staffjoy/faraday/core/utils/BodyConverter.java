package cn.eros.staffjoy.faraday.core.utils;

import java.nio.charset.StandardCharsets;

/**
 * @author 周光兵
 * @date 2021/7/27 22:08
 */
public class BodyConverter {
    public static String convertBodyToString(byte[] body) {
        if (body == null) {
            return null;
        }

        return new String(body, StandardCharsets.UTF_8);
    }

    public static byte[] convertStringToBody(String body) {
        if (body == null) {
            return null;
        }

        return body.getBytes(StandardCharsets.UTF_8);
    }
}
