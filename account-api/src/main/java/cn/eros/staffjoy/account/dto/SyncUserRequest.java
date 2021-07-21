package cn.eros.staffjoy.account.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @author 周光兵
 * @date 2021/7/21 09:06
 */
@Data
@NoArgsConstructor
@Builder
public class SyncUserRequest {
    @NotBlank
    private String userid;
}
