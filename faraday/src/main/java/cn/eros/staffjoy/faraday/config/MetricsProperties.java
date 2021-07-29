package cn.eros.staffjoy.faraday.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 周光兵
 * @date 2021/7/27 22:33
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetricsProperties {
    /**
     * Global metrics names prefix
     */
    private String namesPrefix = "faraday";
}
