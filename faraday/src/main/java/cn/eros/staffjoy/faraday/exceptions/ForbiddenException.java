package cn.eros.staffjoy.faraday.exceptions;

/**
 * @author 周光兵
 * @date 2021/8/4 13:14
 */
public class ForbiddenException extends RuntimeException {
    private static final long serialVersionUID = 5256066170593157919L;

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
