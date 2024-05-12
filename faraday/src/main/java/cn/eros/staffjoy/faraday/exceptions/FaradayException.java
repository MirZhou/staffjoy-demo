package cn.eros.staffjoy.faraday.exceptions;

/**
 * @author Eros
 * @since 2024/5/12
 */
public class FaradayException extends RuntimeException {
    public FaradayException(String message) {
        super(message);
    }

    public FaradayException(String message, Throwable cause) {
        super(message, cause);
    }
}
