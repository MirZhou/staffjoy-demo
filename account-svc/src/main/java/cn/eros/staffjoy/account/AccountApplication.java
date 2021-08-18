package cn.eros.staffjoy.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author 周光兵
 * @date 2021/8/6 17:09
 */
@SpringBootApplication
@EnableFeignClients(basePackages = {"cn.eros.staffjoy.company", "cn.eros.staffjoy.bot", "cn.eros.staffjoy.mail"})
public class AccountApplication {
    public static void main(String[] args) {
        SpringApplication.run(AccountApplication.class, args);
    }
}
