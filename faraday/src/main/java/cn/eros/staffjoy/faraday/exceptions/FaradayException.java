package cn.eros.staffjoy.faraday.exceptions;

/**
 * @author 周光兵
 * @date 2021/7/27 14:40
 */
public class FaradayException extends RuntimeException {
    private static final long serialVersionUID = -1944986691841974878L;

    public FaradayException(String message) {
        super(message);
    }

    public FaradayException(String message, Throwable cause) {
        super(message, cause);
    }
}
