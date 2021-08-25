package cn.eros.staffjoy.company.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * @author 周光兵
 * @date 2021/8/20 13:38
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobDto {
    @NotBlank
    private String id;
    @NotBlank
    private String companyId;
    @NotBlank
    private String teamId;
    @NotBlank
    private String name;
    private boolean archived;
    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")
    @NotBlank
    private String color;
}
