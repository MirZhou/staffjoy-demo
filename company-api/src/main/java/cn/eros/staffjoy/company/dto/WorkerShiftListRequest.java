package cn.eros.staffjoy.company.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;

/**
 * @author 周光兵
 * @date 2021/8/22 22:36
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerShiftListRequest {
    @NotBlank
    private String companyId;
    @NotBlank
    private String teamId;
    @NotBlank
    private String workerId;
    @NotNull
    private Instant shiftStartAfter;
    @NotNull
    private Instant shiftStartBefore;

    @AssertTrue(message = "shift_start_after must be before shift_start_before")
    private boolean correctAfterAndBefore() {
        return shiftStartBefore.isBefore(shiftStartAfter);
    }
}
