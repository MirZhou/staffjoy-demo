package cn.eros.staffjoy.company.dto;

import cn.eros.staffjoy.common.validation.DayOfWeek;
import cn.eros.staffjoy.common.validation.Timezone;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * @author 周光兵
 * @date 2021/8/26 13:08
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTeamRequest {
    @NotBlank
    private String companyId;
    @NotBlank
    private String name;
    @Timezone
    private String timezone;
    @DayOfWeek
    private String dayWeekStarts;
    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")
    @NotEmpty
    private String color;
}
