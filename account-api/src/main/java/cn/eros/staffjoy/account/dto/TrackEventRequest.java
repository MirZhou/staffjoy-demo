package cn.eros.staffjoy.account.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @author 周光兵
 * @date 2021/7/21 09:07
 */
@Data
@NoArgsConstructor
@Builder
public class TrackEventRequest {
    @NotBlank
    private String userid;
    @NotBlank
    private String event;
}
