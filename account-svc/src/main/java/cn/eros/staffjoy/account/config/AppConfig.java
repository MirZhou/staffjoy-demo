package cn.eros.staffjoy.account.config;

import cn.eros.staffjoy.common.async.ContextCopyingDecorator;
import cn.eros.staffjoy.common.config.StaffjoyRestConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.concurrent.Executor;

/**
 * @author 周光兵
 * @date 2021/7/23 13:27
 */
@Configuration
@EnableAsync
@Import(value = {StaffjoyRestConfig.class})
public class AppConfig {
    public static final String ASYNC_EXECUTOR_NAME = "asyncExecutor";

    @Bean
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // for passing in request scope context
        executor.setTaskDecorator(new ContextCopyingDecorator());
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setThreadNamePrefix("AsyncThread-");
        executor.initialize();

        return executor;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
