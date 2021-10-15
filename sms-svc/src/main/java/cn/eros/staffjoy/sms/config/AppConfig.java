package cn.eros.staffjoy.sms.config;

import cn.eros.staffjoy.common.config.StaffjoyRestConfig;
import cn.eros.staffjoy.sms.SmsConstant;
import cn.eros.staffjoy.sms.props.AppProps;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import io.sentry.SentryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * @author 周光兵
 * @date 2021/10/15 09:23
 */
@Configuration
@EnableAsync
@Import(StaffjoyRestConfig.class)
public class AppConfig {
    public static final String ASYNC_EXECUTOR_NAME = "asyncExecutor";

    private static final ILogger LOGGER = SLoggerFactory.getLogger(AppConfig.class);

    @Autowired
    private AppProps appProps;

    @Bean
    public IAcsClient acsClient(@Autowired SentryClient sentryClient) {
        IClientProfile profile = DefaultProfile.getProfile(
            SmsConstant.ALIYUN_REGION_ID,
            this.appProps.getAliyunAccessKey(),
            this.appProps.getAliyunAccessSecret());

        try {
            DefaultProfile.addEndpoint(
                SmsConstant.ALIYUN_SMS_ENDPOINT_NAME,
                SmsConstant.ALIYUN_REGION_ID,
                SmsConstant.ALIYUN_SMS_PRODUCT,
                SmsConstant.ALIYUN_SMS_DOMAIN);
        } catch (ClientException ex) {
            sentryClient.sendException(ex);
            LOGGER.error("Fail to create acsClient", ex);
        }

        return new DefaultAcsClient(profile);
    }

    @Bean(name = ASYNC_EXECUTOR_NAME)
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(this.appProps.getConcurrency());
        executor.setMaxPoolSize(this.appProps.getConcurrency());
        executor.setQueueCapacity(SmsConstant.DEFAULT_EXECUTOR_QUEUE_CAPACITY);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setThreadNamePrefix("AsyncThread-");
        executor.initialize();

        return executor;
    }
}
