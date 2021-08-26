package cn.eros.staffjoy.company.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author 周光兵
 * @date 2021/8/25 20:44
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyList {
    private List<CompanyDto> companies;
    private int limit;
    private int offset;
}
