package cn.eros.staffjoy.company.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 周光兵
 * @date 2021/8/22 22:41
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftList {
    @Builder.Default
    private List<ShiftDto> shifts = new ArrayList<>();
    private Instant shiftStartAfter;
    private Instant shiftStartBefore;
}
