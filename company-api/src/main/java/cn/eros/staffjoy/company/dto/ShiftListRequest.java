package cn.eros.staffjoy.company.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import java.time.Instant;

/**
 * @author 周光兵
 * @date 2021/8/26 13:36
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftListRequest {
    private String companyId;
    private String teamId;
    private String userId;
    private String jobId;
    @NotNull
    private Instant shiftStartAfter;
    @NotNull
    private Instant shiftStartBefore;

    @AssertTrue(message = "shift_start_after must be before shift_start_before")
    private boolean correctAfterAndBefore() {
        return shiftStartAfter.isBefore(shiftStartBefore);
    }
}
