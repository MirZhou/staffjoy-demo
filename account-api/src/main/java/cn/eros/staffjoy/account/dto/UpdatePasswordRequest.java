package cn.eros.staffjoy.account.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @author 周光兵
 * @date 2021/7/21 09:07
 */
@Data
@NoArgsConstructor
@Builder
public class UpdatePasswordRequest {
    @NotBlank
    private String userid;
    @NotBlank
    @Size(min = 6)
    private String password;
}
