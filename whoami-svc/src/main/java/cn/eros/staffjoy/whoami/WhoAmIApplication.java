package cn.eros.staffjoy.whoami;

import cn.eros.staffjoy.common.config.StaffjoyRestConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

/**
 * @author 周光兵
 * @date 2021/10/18 13:15
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@Import(StaffjoyRestConfig.class)
@EnableFeignClients(basePackages = {"cn.eros.staffjoy.company", "cn.eros.staffjoy.account"})
public class WhoAmIApplication {
    public static void main(String[] args) {
        SpringApplication.run(WhoAmIApplication.class, args);
    }
}
