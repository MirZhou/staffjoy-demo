package cn.eros.staffjoy.common.services;

/**
 * @author 周光兵
 * @date 2021/8/4 22:20
 */
public class SecurityConstant {
    /**
     * Public security means a user may be logged out or logged in.
     */
    public static final int SEC_PUBLIC = 0;
    /**
     * Authenticated security means a user bust be logged in.
     */
    public static final int SEC_AUTHENTICATED = 1;
    /**
     * Admin security means a user must be both logged in and have sudo flag.
     */
    public static final int SEC_ADMIN = 2;
}
