package cn.eros.staffjoy.common.auth;

import cn.eros.staffjoy.common.crypto.Sign;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author 周光兵
 * @date 2021/8/3 13:25
 */
public class Sessions {
    public static final long SHORT_SESSION = TimeUnit.HOURS.toMillis(12L);
    public static final long LONG_SESSION = TimeUnit.DAYS.toMillis(30L);

    public static void loginUser(String userid,
                                 boolean support,
                                 boolean rememberMe,
                                 String signingSecret,
                                 String externalApex,
                                 HttpServletResponse response) {
        long duration = rememberMe ? LONG_SESSION : SHORT_SESSION;
        int maxAge = (int) (duration / 1000);

        String token = Sign.generateSessionToken(userid, signingSecret, support, duration);

        Cookie cookie = new Cookie(AuthConstant.COOKIE_NAME, token);
        cookie.setPath("/");
        cookie.setDomain(externalApex);
        cookie.setMaxAge(maxAge);
        cookie.setHttpOnly(true);

        response.addCookie(cookie);

    }

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
