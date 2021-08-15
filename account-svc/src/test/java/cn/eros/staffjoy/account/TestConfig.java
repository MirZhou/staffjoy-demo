package cn.eros.staffjoy.account;

import cn.eros.staffjoy.common.auth.AuthConstant;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * @author 周光兵
 * @date 2021/8/6 17:04
 */
@Configuration
public class TestConfig {
    public static String TEST_USER_ID = UUID.randomUUID().toString();

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            if (!StringUtils.isEmpty(TEST_USER_ID)) {
                requestTemplate.header(AuthConstant.CURRENT_USER_HEADER, TEST_USER_ID);
            }
        };
    }
}
