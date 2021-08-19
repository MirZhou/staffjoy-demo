package cn.eros.staffjoy.bot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @author 周光兵
 * @date 2021/8/19 12:56
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnboardWorkerRequest {
    @NotBlank
    private String companyId;
    @NotBlank
    private String userId;
}
