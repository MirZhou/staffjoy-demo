package cn.eros.staffjoy.faraday.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 周光兵
 * @date 2021/7/27 13:43
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TracingProperties {
    /**
     * Flag for enabling and disabling tracing HTTP requests proxying processes.
     */
    private boolean enabled;
}
