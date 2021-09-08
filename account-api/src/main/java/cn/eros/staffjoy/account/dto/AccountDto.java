package cn.eros.staffjoy.account.dto;

import cn.eros.staffjoy.common.validation.PhoneNumber;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.Instant;

/**
 * @author 周光兵
 * @date 2021/7/21 00:12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDto {
    @NotBlank
    private String id;
    private String name;
    @Email(message = "Invalid email")
    private String email;
    private boolean confirmedAndActive;
    @NotNull
    private Instant memberSince;
    private boolean support;
    @PhoneNumber
    private String phoneNumber;
    @NotEmpty
    private String photoUrl;
}
