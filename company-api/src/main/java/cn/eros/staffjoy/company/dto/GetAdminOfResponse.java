package cn.eros.staffjoy.company.dto;

import cn.eros.staffjoy.common.api.BaseResponse;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * @author 周光兵
 * @date 2021/8/6 13:41
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class GetAdminOfResponse extends BaseResponse {
    private AdminOfList adminOfList;
}
