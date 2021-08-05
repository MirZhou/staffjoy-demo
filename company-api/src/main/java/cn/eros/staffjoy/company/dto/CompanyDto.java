package cn.eros.staffjoy.company.dto;

import cn.eros.staffjoy.common.validation.DayOfWeek;
import cn.eros.staffjoy.common.validation.Group1;
import cn.eros.staffjoy.common.validation.Group2;
import cn.eros.staffjoy.common.validation.Timezone;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @author 周光兵
 * @date 2021/8/5 23:04
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyDto {
    @NotBlank(groups = Group1.class)
    private String id;
    @NotBlank(groups = {Group1.class, Group2.class})
    private String name;
    private boolean archived;
    @Timezone(groups = {Group1.class, Group2.class})
    @NotBlank(groups = {Group1.class, Group2.class})
    private String defaultTimezone;
    @DayOfWeek(groups = {Group1.class, Group2.class})
    @NotBlank(groups = {Group1.class, Group2.class})
    private String defaultDayWeekStarts;
}
