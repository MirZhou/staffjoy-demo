package cn.eros.staffjoy.company.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 周光兵
 * @date 2021/8/6 13:42
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminOfList {
    private String userid;
    @Builder.Default
    private List<CompanyDto> companies = new ArrayList<>();
}
