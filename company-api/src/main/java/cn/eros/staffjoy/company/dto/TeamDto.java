package cn.eros.staffjoy.company.dto;

import cn.eros.staffjoy.common.validation.DayOfWeek;
import cn.eros.staffjoy.common.validation.Timezone;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * @author 周光兵
 * @date 2021/8/5 23:23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamDto {
    @NotBlank
    private String id;
    @NotBlank
    private String companyId;
    @NotBlank
    private String name;
    private boolean archived;
    @Timezone
    @NotBlank
    private String timezone;
    @DayOfWeek
    @NotBlank
    private String dayWeekStarts;
    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")
    @NotBlank
    private String color;
}
