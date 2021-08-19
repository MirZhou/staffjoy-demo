package cn.eros.staffjoy.bot.dto;

import cn.eros.staffjoy.company.dto.ShiftDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author 周光兵
 * @date 2021/8/19 13:43
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertChangedShiftRequest {
    @NotBlank
    private String userId;
    @NotNull
    private ShiftDto oldShift;
    @NotNull
    private ShiftDto newShift;
}
