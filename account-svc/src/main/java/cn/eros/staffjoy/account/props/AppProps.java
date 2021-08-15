package cn.eros.staffjoy.account.props;

/**
 * <p>create time：2021-08-14 22:17
 *
 * @author Eros
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

@Component
@ConfigurationProperties(prefix = "staffjoy")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppProps {
    @NotNull
    private String intercomAccessToken;
    @NotNull
    private String signingSecret;
}
