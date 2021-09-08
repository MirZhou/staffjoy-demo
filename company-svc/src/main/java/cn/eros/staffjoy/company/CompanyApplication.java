package cn.eros.staffjoy.company;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author 周光兵
 * @date 2021/8/26 20:27
 */
@EnableFeignClients({"cn.eros.staffjoy.account", "cn.eros.staffjoy.bot"})
@SpringBootApplication
public class CompanyApplication {
    public static void main(String[] args) {
        SpringApplication.run(CompanyApplication.class, args);
    }
}
