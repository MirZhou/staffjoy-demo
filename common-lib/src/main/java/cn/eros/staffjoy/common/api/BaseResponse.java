package cn.eros.staffjoy.common.api;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 周光兵
 * @date 2021/7/21 00:29
 */
@Data
@NoArgsConstructor
@Builder
public class BaseResponse {
    private String message;
    @Builder.Default
    private ResultCode code = ResultCode.SUCCESS;

    public boolean isSuccess() {
        return code == ResultCode.SUCCESS;
    }
}
