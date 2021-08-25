package cn.eros.staffjoy.company.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * @author 周光兵
 * @date 2021/8/19 22:29
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateShiftRequest {
    protected static final long MAX_SHIFT_DURATION = TimeUnit.HOURS.toMillis(23L);

    @NotBlank
    private String companyId;
    @NotBlank
    private String teamId;
    @NotNull
    private Instant start;
    @NotNull
    private Instant stop;
    @Builder.Default
    private String userId = "";
    @Builder.Default
    private String jobId = "";
    @NotNull
    private boolean published;

    @AssertTrue(message = "stop must be after start")
    private boolean shopIsAfterStart() {
        long duration = stop.toEpochMilli() - start.toEpochMilli();

        return duration > 0;
    }

    @AssertTrue(message = "Shifts exceed max allowed hour duration")
    private boolean withInMaxDuration() {
        long duration = stop.toEpochMilli() - start.toEpochMilli();

        return duration <= MAX_SHIFT_DURATION;
    }

}
