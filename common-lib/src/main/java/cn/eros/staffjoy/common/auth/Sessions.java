package cn.eros.staffjoy.common.auth;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * @author 周光兵
 * @date 2021/8/3 13:25
 */
public class Sessions {
    public static String getToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null || cookies.length == 0) {
            return null;
        }

        return Arrays.stream(cookies)
            .filter(cookie -> AuthConstant.COOKIE_NAME.equals(cookie.getName()))
            .findAny()
            .map(Cookie::getValue).orElse(null);
    }
}
