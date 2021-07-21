package cn.eros.staffjoy.account.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

/**
 * @author 周光兵
 * @date 2021/7/21 09:09
 */
@Data
@NoArgsConstructor
@Builder
public class VerifyPasswordRequest {
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String password;
}
