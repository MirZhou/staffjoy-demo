package cn.eros.staffjoy.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

/**
 * @author 周光兵
 * @date 2021/7/21 00:26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailChangeRequest {
    @NotBlank
    private String userid;
    @NotEmpty
    @Email
    private String email;
}
