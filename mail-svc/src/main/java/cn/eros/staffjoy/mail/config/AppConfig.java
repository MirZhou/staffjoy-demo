package cn.eros.staffjoy.mail.config;

import cn.eros.staffjoy.common.config.StaffjoyRestConfig;
import cn.eros.staffjoy.mail.MailConstant;
import cn.eros.staffjoy.mail.props.AppProps;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * <p>create time：2021-08-15 22:08
 *
 * @author Eros
 */
@Configuration
@EnableAsync
@Import(StaffjoyRestConfig.class)
@SuppressWarnings("Duplicates")
public class AppConfig {
    public static final String ASYNC_EXECUTOR_NAME = "asyncExecutor";

    @Autowired
    private AppProps appProps;

    @Bean
    public IAcsClient ascClient() {
        IClientProfile profile = DefaultProfile.getProfile(MailConstant.ALIYUN_REGION_ID,
                this.appProps.getAliyunAccessKey(),
                this.appProps.getAliyunAccessSecret());

        return new DefaultAcsClient(profile);
    }

    @Bean(name = ASYNC_EXECUTOR_NAME)
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setThreadNamePrefix("AsyncThread-");
        executor.initialize();

        return executor;
    }
}
