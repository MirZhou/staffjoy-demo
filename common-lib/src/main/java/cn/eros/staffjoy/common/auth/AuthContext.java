package cn.eros.staffjoy.common.auth;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 周光兵
 * @date 2021/7/23 13:46
 */
public class AuthContext {
    private static String getRequestHeader(String headerName) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

            return request.getHeader(headerName);
        }

        return null;
    }

    public static String getUserId() {
        return getRequestHeader(AuthConstant.CURRENT_USER_HEADER);
    }

    public static String getAuthz() {
        return getRequestHeader(AuthConstant.AUTHORIZATION_HEADER);
    }
}
