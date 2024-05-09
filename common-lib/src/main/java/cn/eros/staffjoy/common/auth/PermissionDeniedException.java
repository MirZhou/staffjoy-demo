package cn.eros.staffjoy.common.auth;

import cn.eros.staffjoy.common.api.ResultCode;
import lombok.Getter;

/**
 * @author Eros
 * @since 2024/5/9
 */
@Getter
public class PermissionDeniedException extends RuntimeException {
    private static final long serialVersionUID = -2207389052905683145L;

    private final ResultCode resultCode;

    public PermissionDeniedException(String message) {
        super(message);
        this.resultCode = ResultCode.UN_AUTHORIZED;
    }

    public PermissionDeniedException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.resultCode = resultCode;
    }

    public PermissionDeniedException(ResultCode resultCode, Throwable cause) {
        super(cause);
        this.resultCode = resultCode;
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
