package cn.eros.staffjoy.common.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @author 周光兵
 * @date 2021/7/21 00:29
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BaseResponse {
    private String message;
    @Builder.Default
    private ResultCode code = ResultCode.SUCCESS;

    public boolean isSuccess() {
        return code == ResultCode.SUCCESS;
    }
}
