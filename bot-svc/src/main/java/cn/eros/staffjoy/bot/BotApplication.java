package cn.eros.staffjoy.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * <p>create time：2021-08-17 13:59
 *
 * @author Eros
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableFeignClients({"cn.eros.staffjoy.account", "cn.eros.staffjoy.company", "cn.eros.staffjoy.mail", "cn.eros.staffjoy.sms"})
public class BotApplication {
    public static void main(String[] args) {
        SpringApplication.run(BotApplication.class, args);
    }
}
