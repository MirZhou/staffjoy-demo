package cn.eros.staffjoy.common.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.servlet.http.HttpServletResponse;

/**
 * Result code enumeration.
 *
 * @author Eros
 * @since 2024/5/9
 */
@Getter
@AllArgsConstructor
public enum ResultCode {
    /**
     * Operation is Successful
     */
    SUCCESS(HttpServletResponse.SC_OK, "Operation is Successful"),
    /**
     * Biz Exception
     */
    FAILURE(HttpServletResponse.SC_BAD_REQUEST, "Biz Exception"),
    /**
     * Unauthorized
     */
    UN_AUTHORIZED(HttpServletResponse.SC_UNAUTHORIZED, "Request Unauthorized"),
    /**
     * Not Found
     */
    NOT_FOUND(HttpServletResponse.SC_NOT_FOUND, "404 Not Found"),
    /**
     * Message Can't be Read
     */
    MSG_NOT_READABLE(HttpServletResponse.SC_BAD_REQUEST, "Message Can't be Read"),
    /**
     * Method Not Supported
     */
    METHOD_NOT_SUPPORTED(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Method Not Supported"),
    /**
     * Media Type Not Supported
     */
    MEDIA_TYPE_NOT_SUPPORTED(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Media Type Not Supported"),
    /**
     * Request Rejected
     */
    REQ_REJECT(HttpServletResponse.SC_FORBIDDEN, "Request Rejected"),
    /**
     * Internal Server Error
     */
    INTERNAL_SERVER_ERROR(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error"),
    /**
     * Missing Required Parameter
     */
    PARAM_MISS(HttpServletResponse.SC_BAD_REQUEST, "Missing Required Parameter"),
    /**
     * Parameter Type Mismatch
     */
    PARAM_TYPE_ERROR(HttpServletResponse.SC_BAD_REQUEST, "Parameter Type Mismatch"),
    /**
     * Parameter Binding Error
     */
    PARAM_BIND_ERROR(HttpServletResponse.SC_BAD_REQUEST, "Parameter Binding Error"),
    /**
     * Parameter Validation Error
     */
    PARAM_VALID_ERROR(HttpServletResponse.SC_BAD_REQUEST, "Parameter Validation Error");

    private final int code;
    private final String msg;
}
