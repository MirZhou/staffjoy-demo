package cn.eros.staffjoy.bot.dto;

import cn.eros.staffjoy.company.dto.ShiftDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 周光兵
 * @date 2021/8/19 13:44
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertRemovedShiftsRequest {
    @NotBlank
    private String userId;
    @NotNull
    @Builder.Default
    private List<ShiftDto> oldShifts = new ArrayList<>();
}
