package cn.eros.staffjoy.account.controller;

import cn.eros.staffjoy.account.TestConfig;
import cn.eros.staffjoy.account.client.AccountClient;
import cn.eros.staffjoy.account.dto.*;
import cn.eros.staffjoy.account.model.Account;
import cn.eros.staffjoy.account.repo.AccountRepo;
import cn.eros.staffjoy.account.repo.AccountSecretRepo;
import cn.eros.staffjoy.bot.client.BotClient;
import cn.eros.staffjoy.common.api.BaseResponse;
import cn.eros.staffjoy.common.api.ResultCode;
import cn.eros.staffjoy.common.auth.AuthConstant;
import cn.eros.staffjoy.common.env.EnvConfig;
import cn.eros.staffjoy.mail.client.MailClient;
import cn.eros.staffjoy.mail.dto.EmailRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@EnableFeignClients(basePackages = {"cn.eros.staffjoy.account.client"})
@Import(TestConfig.class)
@Slf4j
public class AccountControllerTest {
    @Autowired
    private AccountClient accountClient;

    @Autowired
    private EnvConfig envConfig;

    @MockBean
    private MailClient mailClient;

    @MockBean
    private BotClient botClient;

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private AccountSecretRepo accountSecretRepo;

    private Account newAccount;

    @Before
    public void setUp() {
        // sanity check
        this.accountRepo.deleteAll();
        // clear CURRENT_USER_HEADER for testing
        TestConfig.TEST_USER_ID = null;
    }

    @Test
    public void testChangeEmail() {
        // arrange mock
        when(this.mailClient.send(any(EmailRequest.class))).thenReturn(BaseResponse.builder().message("email sent").build());

        String name = "testAccount";
        String email = "test@staffjoy.xyz";
        String phoneNumber = "18001801236";
        CreateAccountRequest createAccountRequest = CreateAccountRequest.builder()
            .name(name)
            .email(email)
            .phoneNumber(phoneNumber)
            .build();
        // create one account
        GenericAccountResponse genericAccountResponse = this.accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();
        AccountDto accountDto = genericAccountResponse.getAccount();
        assertThat(accountDto.isConfirmedAndActive()).isFalse();

        // change email
        String changedEmail = "test123@staffjoy.xyz";
        EmailConfirmationRequest emailConfirmationRequest = EmailConfirmationRequest.builder()
            .userid(accountDto.getId())
            .email(changedEmail)
            .build();
        BaseResponse baseResponse = this.accountClient.changeEmail(AuthConstant.AUTHORIZATION_WWW_SERVICE, emailConfirmationRequest);
        log.info(baseResponse.toString());
        assertThat(baseResponse.isSuccess()).isTrue();

        // verify email changed and account activated
        GetOrCreateRequest getOrCreateRequest = GetOrCreateRequest.builder()
            .email(changedEmail)
            .build();
        genericAccountResponse = this.accountClient.getOrCreateAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, getOrCreateRequest);
        AccountDto foundAccountDto = genericAccountResponse.getAccount();
        assertThat(foundAccountDto.getEmail()).isEqualTo(changedEmail);
        assertThat(foundAccountDto.isConfirmedAndActive()).isTrue();

        // account not found
        emailConfirmationRequest = EmailConfirmationRequest.builder()
            .userid("not_existing_id")
            .email(changedEmail)
            .build();
        baseResponse = this.accountClient.changeEmail(AuthConstant.AUTHORIZATION_WWW_SERVICE, emailConfirmationRequest);
        log.info(baseResponse.toString());
        assertThat(baseResponse.isSuccess()).isFalse();
        assertThat(baseResponse.getCode()).isEqualTo(ResultCode.NOT_FOUND);
    }

    @Test
    public void testRequestEmailChange() {
        // arrange mock
        when(this.mailClient.send(any(EmailRequest.class)))
            .thenReturn(BaseResponse.builder()
                .message("email sent")
                .build());

        String name = "testAccount";
        String email = "test@staffjoy.xyz";
        String phoneNumber = "18012344321";
        CreateAccountRequest createAccountRequest = CreateAccountRequest.builder()
            .name(name)
            .email(email)
            .phoneNumber(phoneNumber)
            .build();

        // create one account
        GenericAccountResponse genericAccountResponse = this.accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();

        AccountDto accountDto = genericAccountResponse.getAccount();

        // request email change
        String updatedEmail = "test111@staffjoy.xyz";
        EmailChangeRequest emailChangeRequest = EmailChangeRequest.builder()
            .email(updatedEmail)
            .userid(accountDto.getId())
            .build();
        BaseResponse baseResponse = this.accountClient.requestEmailChange(AuthConstant.AUTHORIZATION_SUPPORT_USER, emailChangeRequest);
        assertThat(baseResponse.isSuccess()).isTrue();

        // capture and verify email sent
        String externalApex = "staffjoy-v2.local";
        String subject = "Confirm Your New Email Address";
        ArgumentCaptor<EmailRequest> argEmailRequest = ArgumentCaptor.forClass(EmailRequest.class);
        verify(this.mailClient, times(2)).send(argEmailRequest.capture());

        EmailRequest emailRequest = argEmailRequest.getAllValues().get(1);
        log.info(emailRequest.toString());
        assertThat(emailRequest.getTo()).isEqualTo(updatedEmail);
        assertThat(emailRequest.getSubject()).isEqualTo(subject);
        assertThat(emailRequest.getName()).isEqualTo(name);
        assertThat(StringUtils.countMatches(emailRequest.getHtmlBody(), "http://www." + externalApex + "/activate/")).isEqualTo(3);
        assertThat(StringUtils.countMatches(emailRequest.getHtmlBody(), name)).isEqualTo(1);
        assertThat(emailRequest.getHtmlBody()).startsWith("<div>Hi");
    }

    @Test
    public void testRequestPasswordReset() {
        // arrange mock
        when(this.mailClient.send(any(EmailRequest.class)))
            .thenReturn(BaseResponse.builder()
                .message("email sent").build());

        String name = "testAccount";
        String email = "test@staffjoy.xyz";
        String phoneNumber = "18012344321";

        CreateAccountRequest createAccountRequest = CreateAccountRequest.builder()
            .name(name)
            .email(email)
            .phoneNumber(phoneNumber)
            .build();

        // create one account
        GenericAccountResponse genericAccountResponse = this.accountClient.createAccount(
            AuthConstant.AUTHORIZATION_WWW_SERVICE,
            createAccountRequest
        );
        assertThat(genericAccountResponse.isSuccess()).isTrue();

        AccountDto accountDto = genericAccountResponse.getAccount();

        // request password reset
        PasswordResetRequest passwordResetRequest = PasswordResetRequest.builder()
            .email(email)
            .build();

        BaseResponse baseResponse = this.accountClient.requestPasswordReset(
            AuthConstant.AUTHORIZATION_WWW_SERVICE,
            passwordResetRequest
        );
        assertThat(baseResponse.isSuccess()).isTrue();

        // capture and verify
        String subject = "Activate your Staffjoy account";
        String externalApex = "staffjoy-v2.local";

        ArgumentCaptor<EmailRequest> argEmailRequest = ArgumentCaptor.forClass(EmailRequest.class);
        // 2 times, 1 for account creation, 1 for password reset
        verify(this.mailClient, times(2)).send(argEmailRequest.capture());
        EmailRequest emailRequest = argEmailRequest.getAllValues().get(1);
        log.info(emailRequest.toString());
        assertThat(emailRequest.getTo()).isEqualTo(email);
        assertThat(emailRequest.getSubject()).isEqualTo(subject);
        assertThat(emailRequest.getName()).isEqualTo(name);
        assertThat(StringUtils.countMatches(emailRequest.getHtmlBody(), "http://www." + externalApex + "/activate/")).isEqualTo(3);
        assertThat(StringUtils.countMatches(emailRequest.getHtmlBody(), name)).isEqualTo(1);
        assertThat(emailRequest.getHtmlBody()).startsWith("<div><p>Hi");

        // activate the account
        accountDto.setConfirmedAndActive(true);
        genericAccountResponse = this.accountClient.updateAccount(
            AuthConstant.AUTHORIZATION_WWW_SERVICE,
            accountDto
        );
        assertThat(genericAccountResponse.isSuccess()).isTrue();

        // request password reset
        passwordResetRequest = PasswordResetRequest.builder()
            .email(email)
            .build();
        baseResponse = this.accountClient.requestPasswordReset(
            AuthConstant.AUTHORIZATION_WWW_SERVICE,
            passwordResetRequest
        );
        log.info(baseResponse.toString());
        assertThat(baseResponse.isSuccess()).isTrue();

        // capture and verify
        subject = "Reset your Staffjoy password";
        argEmailRequest = ArgumentCaptor.forClass(EmailRequest.class);
        // 3 times, 1 for account creation, 2 for password reset
        verify(this.mailClient, times(3)).send(argEmailRequest.capture());
        emailRequest = argEmailRequest.getAllValues().get(2);
        log.info(emailRequest.toString());
        assertThat(emailRequest.getTo()).isEqualTo(email);
        assertThat(emailRequest.getSubject()).isEqualTo(subject);
        assertThat(emailRequest.getName()).isEqualTo(name);
        assertThat(StringUtils.countMatches(emailRequest.getHtmlBody(), "http://www." + externalApex + "/reset"))
            .isEqualTo(2);
        assertThat(emailRequest.getHtmlBody()).startsWith("<div>We received a request to reset the password on your account");
    }
}