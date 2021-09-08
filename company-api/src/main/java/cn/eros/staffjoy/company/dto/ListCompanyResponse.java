package cn.eros.staffjoy.company.dto;

import cn.eros.staffjoy.common.api.BaseResponse;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * @author 周光兵
 * @date 2021/8/25 20:43
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ListCompanyResponse extends BaseResponse {
    private CompanyList companyList;
}
