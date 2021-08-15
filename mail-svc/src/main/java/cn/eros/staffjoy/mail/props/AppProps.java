package cn.eros.staffjoy.mail.props;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

/**
 * <p>create time：2021-08-15 22:11
 *
 * @author Eros
 */
@Component
@ConfigurationProperties(prefix = "staffjoy")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppProps {
    /**
     * aliyun direct mail props
     */
    @NotNull
    private String aliyunAccessKey;
    @NotNull
    private String aliyunAccessSecret;
}
