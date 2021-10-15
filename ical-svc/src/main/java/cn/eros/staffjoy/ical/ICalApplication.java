package cn.eros.staffjoy.ical;

import cn.eros.staffjoy.common.config.StaffjoyWebConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

/**
 * @author 周光兵
 * @date 2021/10/13 23:08
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableFeignClients(basePackages = {"cn.eros.staffjoy"})
@Import(value = StaffjoyWebConfig.class)
public class ICalApplication {
    public static void main(String[] args) {
        SpringApplication.run(ICalApplication.class, args);
    }
}
