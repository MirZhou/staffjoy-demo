package cn.eros.staffjoy.sms.props;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

/**
 * @author 周光兵
 * @date 2021/10/14 16:10
 */
@Component
@ConfigurationProperties(prefix = "staffjoy")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppProps {
    @NotNull
    private String aliyunAccessKey;
    @NotNull
    private String aliyunAccessSecret;
    @NotNull
    private String aliyunSmsSignName;

    private boolean whiteListOnly;
    private String whiteListPhoneNumbers;
    private int concurrency;
}
