package cn.eros.staffjoy.company.config;

import cn.eros.staffjoy.common.async.ContextCopyingDecorator;
import cn.eros.staffjoy.common.config.StaffjoyRestConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * @author 周光兵
 * @date 2021/8/26 20:29
 */
@Configuration
@EnableAsync
@Import({StaffjoyRestConfig.class})
public class AppConfig {
    public static final String ASYNC_EXECUTOR_NAME = "asyncExecutor";

    @Bean(ASYNC_EXECUTOR_NAME)
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setTaskDecorator(new ContextCopyingDecorator());
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setThreadNamePrefix("AsyncThread-");
        executor.initialize();

        return executor;
    }

}
