package cn.eros.staffjoy.common.utils;

/**
 * <p>create time：2021-08-14 22:22
 *
 * @author Eros
 */
public class Helper {
    public static String generateGravatarUrl(String email) {
        String hash = MD5Util.md5Hex(email);

        return String.format("https://www.gravatar.com/avatar/%s.jpg?s=400&d=identicon", hash);
    }
}
