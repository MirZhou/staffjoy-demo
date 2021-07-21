package cn.eros.staffjoy.account.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

/**
 * @author 周光兵
 * @date 2021/7/21 00:46
 */
@Data
@NoArgsConstructor
@Builder
public class PasswordResetRequest {
    @Email(message = "Invalid email")
    @NotEmpty
    private String email;
}
