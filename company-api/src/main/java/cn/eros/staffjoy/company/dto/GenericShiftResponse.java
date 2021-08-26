package cn.eros.staffjoy.company.dto;

import cn.eros.staffjoy.common.api.BaseResponse;
import lombok.*;

/**
 * @author 周光兵
 * @date 2021/8/26 13:31
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenericShiftResponse extends BaseResponse {
    private ShiftDto shiftDto;
}
