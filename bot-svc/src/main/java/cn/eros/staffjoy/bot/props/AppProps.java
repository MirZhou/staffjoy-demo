package cn.eros.staffjoy.bot.props;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Eros
 * @date 2021-08-18 13:40
 */
@Component
@ConfigurationProperties(prefix = "staffjoy")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppProps {
    private boolean forceEmailPreference;
}
