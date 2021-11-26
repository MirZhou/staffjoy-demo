package cn.eros.staffjoy.common.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotBlank;

/**
 * @author 周光兵
 * @date 2021/7/22 12:54
 */
@ConfigurationProperties(prefix = "staffjoy.common")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffjoyProps {
    @NotBlank
    private String sentryDsn;
    /**
     * DeployEnvVar is set by Kubernetes during a new deployment so we can identify the code version
     */
    @NotBlank
    private String deployEnv;
}
