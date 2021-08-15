package cn.eros.staffjoy.account.controller;

import cn.eros.staffjoy.account.TestConfig;
import cn.eros.staffjoy.account.client.AccountClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@EnableFeignClients(basePackages = {"cn.eros.staffjoy.account.client"})
@Import(TestConfig.class)
@Slf4j
public class AccountControllerTest {
    @Autowired
    private AccountClient accountClient;
}