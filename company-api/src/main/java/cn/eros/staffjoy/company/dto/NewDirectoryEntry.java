package cn.eros.staffjoy.company.dto;

import cn.eros.staffjoy.common.validation.PhoneNumber;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

/**
 * @author 周光兵
 * @date 2021/8/25 21:57
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewDirectoryEntry {
    @NotBlank
    private String companyId;
    @Builder.Default
    private String name = "";
    @Email
    private String email;
    @PhoneNumber
    private String phoneNumber;
    @Builder.Default
    private String internalId = "";
}
