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
 * @date 2021/8/25 21:10
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DirectoryEntryDto {
    @NotBlank
    private String userId;
    @NotBlank
    private String internalId;
    @NotBlank
    private String companyId;
    @NotBlank
    @Builder.Default
    private String name = "";
    @NotBlank
    @Email
    private String email;
    private boolean confirmedAndActive;
    @NotBlank
    @PhoneNumber
    private String phoneNumber;
    private String photoUrl;
}
