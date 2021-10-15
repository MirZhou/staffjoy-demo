package cn.eros.staffjoy.sms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @author 周光兵
 * @date 2021/10/14 16:00
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class SmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmsApplication.class, args);
    }
}
