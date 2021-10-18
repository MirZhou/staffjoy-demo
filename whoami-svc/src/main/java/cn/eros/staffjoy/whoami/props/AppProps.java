package cn.eros.staffjoy.whoami.props;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author 周光兵
 * @date 2021/10/18 13:18
 */
@Component
@Data
@ConfigurationProperties(prefix = "staffjoy")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppProps {
    private String intercomAppId;
    private String intercomSigningSecret;
}
