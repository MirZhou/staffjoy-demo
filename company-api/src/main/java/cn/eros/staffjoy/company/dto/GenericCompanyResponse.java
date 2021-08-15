package cn.eros.staffjoy.company.dto;

import cn.eros.staffjoy.common.api.BaseResponse;
import lombok.*;

/**
 * @author 周光兵
 * @date 2021/8/6 13:22
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenericCompanyResponse extends BaseResponse {
    private CompanyDto company;
}
