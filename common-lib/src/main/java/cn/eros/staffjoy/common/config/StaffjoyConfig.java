package cn.eros.staffjoy.common.config;

import cn.eros.staffjoy.common.auth.AuthorizeInterceptor;
import cn.eros.staffjoy.common.auth.FeignRequestHeaderInterceptor;
import cn.eros.staffjoy.common.env.EnvConfig;
import com.github.structlog4j.StructLog4J;
import com.github.structlog4j.json.JsonFormatter;
import feign.RequestInterceptor;
import io.sentry.Sentry;
import io.sentry.SentryClient;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author 周光兵
 * @date 2021/7/23 13:28
 */
@Configuration
@EnableConfigurationProperties(StaffjoyProps.class)
public class StaffjoyConfig implements WebMvcConfigurer {
    @Value("${spring.profiles.active:NA")
    private String activeProfile;

    @Value("${spring.application.name:NA")
    private String appName;

    @Autowired
    private StaffjoyProps staffjoyProps;

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public EnvConfig envConfig() {
        return EnvConfig.getEnvConfig(activeProfile);
    }

    @Bean
    public SentryClient sentryClient() {
        SentryClient sentryClient = Sentry.init(staffjoyProps.getSentryDsn());
        sentryClient.setEnvironment(activeProfile);
        sentryClient.setRelease(staffjoyProps.getDeployEnv());
        sentryClient.addTag("service", this.appName);

        return sentryClient;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthorizeInterceptor());
    }

    @Bean
    public RequestInterceptor feignRequestInterceptor() {
        return new FeignRequestHeaderInterceptor();
    }

    @PostConstruct
    public void init() {
        StructLog4J.setFormatter(JsonFormatter.getInstance());

        StructLog4J.setMandatoryContextSupplier(() -> new Object[]{
            "env", activeProfile,
            "service", appName
        });
    }

    @PreDestroy
    public void destroy() {
        this.sentryClient().closeConnection();
    }
}
