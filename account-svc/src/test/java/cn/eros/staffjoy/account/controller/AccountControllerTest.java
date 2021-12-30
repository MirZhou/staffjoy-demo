package cn.eros.staffjoy.account.controller;

import cn.eros.staffjoy.account.TestConfig;
import cn.eros.staffjoy.account.client.AccountClient;
import cn.eros.staffjoy.account.dto.*;
import cn.eros.staffjoy.account.model.Account;
import cn.eros.staffjoy.account.repo.AccountRepo;
import cn.eros.staffjoy.account.repo.AccountSecretRepo;
import cn.eros.staffjoy.bot.client.BotClient;
import cn.eros.staffjoy.bot.dto.GreetingRequest;
import cn.eros.staffjoy.common.api.BaseResponse;
import cn.eros.staffjoy.common.api.ResultCode;
import cn.eros.staffjoy.common.auth.AuthConstant;
import cn.eros.staffjoy.common.env.EnvConfig;
import cn.eros.staffjoy.mail.client.MailClient;
import cn.eros.staffjoy.mail.dto.EmailRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
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

import java.time.Instant;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@EnableFeignClients(basePackages = { "cn.eros.staffjoy.account.client" })
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
        when(this.mailClient.send(any(EmailRequest.class)))
                .thenReturn(BaseResponse.builder().message("email sent").build());

        String name = "testAccount";
        String email = "test@staffjoy.xyz";
        String phoneNumber = "18001801236";
        CreateAccountRequest createAccountRequest = CreateAccountRequest.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        // create one account
        GenericAccountResponse genericAccountResponse = this.accountClient
                .createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();
        AccountDto accountDto = genericAccountResponse.getAccount();
        assertThat(accountDto.isConfirmedAndActive()).isFalse();

        // change email
        String changedEmail = "test123@staffjoy.xyz";
        EmailConfirmationRequest emailConfirmationRequest = EmailConfirmationRequest.builder()
                .userid(accountDto.getId())
                .email(changedEmail)
                .build();
        BaseResponse baseResponse = this.accountClient.changeEmail(AuthConstant.AUTHORIZATION_WWW_SERVICE,
                emailConfirmationRequest);
        log.info(baseResponse.toString());
        assertThat(baseResponse.isSuccess()).isTrue();

        // verify email changed and account activated
        GetOrCreateRequest getOrCreateRequest = GetOrCreateRequest.builder()
                .email(changedEmail)
                .build();
        genericAccountResponse = this.accountClient.getOrCreateAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE,
                getOrCreateRequest);
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
        GenericAccountResponse genericAccountResponse = this.accountClient
                .createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();

        AccountDto accountDto = genericAccountResponse.getAccount();

        // request email change
        String updatedEmail = "test111@staffjoy.xyz";
        EmailChangeRequest emailChangeRequest = EmailChangeRequest.builder()
                .email(updatedEmail)
                .userid(accountDto.getId())
                .build();
        BaseResponse baseResponse = this.accountClient.requestEmailChange(AuthConstant.AUTHORIZATION_SUPPORT_USER,
                emailChangeRequest);
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
        assertThat(StringUtils.countMatches(emailRequest.getHtmlBody(), "http://www." + externalApex + "/activate/"))
                .isEqualTo(3);
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
                createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();

        AccountDto accountDto = genericAccountResponse.getAccount();

        // request password reset
        PasswordResetRequest passwordResetRequest = PasswordResetRequest.builder()
                .email(email)
                .build();

        BaseResponse baseResponse = this.accountClient.requestPasswordReset(
                AuthConstant.AUTHORIZATION_WWW_SERVICE,
                passwordResetRequest);
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
        assertThat(StringUtils.countMatches(emailRequest.getHtmlBody(), "http://www." + externalApex + "/activate/"))
                .isEqualTo(3);
        assertThat(StringUtils.countMatches(emailRequest.getHtmlBody(), name)).isEqualTo(1);
        assertThat(emailRequest.getHtmlBody()).startsWith("<div><p>Hi");

        // activate the account
        accountDto.setConfirmedAndActive(true);
        genericAccountResponse = this.accountClient.updateAccount(
                AuthConstant.AUTHORIZATION_WWW_SERVICE,
                accountDto);
        assertThat(genericAccountResponse.isSuccess()).isTrue();

        // request password reset
        passwordResetRequest = PasswordResetRequest.builder()
                .email(email)
                .build();
        baseResponse = this.accountClient.requestPasswordReset(
                AuthConstant.AUTHORIZATION_WWW_SERVICE,
                passwordResetRequest);
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
        assertThat(emailRequest.getHtmlBody())
                .startsWith("<div>We received a request to reset the password on your account");
    }

    @Test
    public void testUpdateAndVerifyPasswordValidation() {
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
                createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();

        AccountDto accountDto = genericAccountResponse.getAccount();

        // update password too short
        String password = "pass";
        UpdatePasswordRequest updatePasswordRequest = UpdatePasswordRequest.builder()
                .userid(accountDto.getId())
                .password(password)
                .build();
        BaseResponse baseResponse = this.accountClient.updatePassword(
                AuthConstant.AUTHORIZATION_WWW_SERVICE, updatePasswordRequest);
        log.info(baseResponse.toString());
        assertThat(baseResponse.isSuccess()).isFalse();
        assertThat(baseResponse.getCode()).isEqualTo(ResultCode.PARAM_VALID_ERROR);

        // update password success
        password = "pass123456";
        updatePasswordRequest = UpdatePasswordRequest.builder()
                .userid(accountDto.getId())
                .password(password)
                .build();
        baseResponse = this.accountClient.updatePassword(
                AuthConstant.AUTHORIZATION_WWW_SERVICE, updatePasswordRequest);
        log.info(baseResponse.toString());
        assertThat(baseResponse.isSuccess()).isTrue();

        // verify not found
        VerifyPasswordRequest verifyPasswordRequest = VerifyPasswordRequest.builder()
                .password(password)
                .email("test000@staffjoy.xyz")
                .build();
        genericAccountResponse = this.accountClient.verifyPassword(
                AuthConstant.AUTHORIZATION_WWW_SERVICE, verifyPasswordRequest);
        log.info(genericAccountResponse.toString());
        assertThat(genericAccountResponse.isSuccess()).isFalse();
        assertThat(genericAccountResponse.getCode()).isEqualTo(ResultCode.NOT_FOUND);

        // verify account not active
        verifyPasswordRequest = VerifyPasswordRequest.builder()
                .password(password)
                .email(email)
                .build();
        genericAccountResponse = this.accountClient.verifyPassword(
                AuthConstant.AUTHORIZATION_WWW_SERVICE, verifyPasswordRequest);
        log.info(genericAccountResponse.toString());
        assertThat(genericAccountResponse.isSuccess()).isFalse();
        assertThat(genericAccountResponse.getCode()).isEqualTo(ResultCode.REQ_REJECT);

        // activate the account
        accountDto.setConfirmedAndActive(true);
        genericAccountResponse = this.accountClient.updateAccount(
                AuthConstant.AUTHORIZATION_WWW_SERVICE, accountDto);
        assertThat(genericAccountResponse.isSuccess()).isTrue();

        // verify wrong password
        verifyPasswordRequest = VerifyPasswordRequest.builder()
                .email(email)
                .password("wrong_password")
                .build();
        genericAccountResponse = this.accountClient.verifyPassword(
                AuthConstant.AUTHORIZATION_WWW_SERVICE, verifyPasswordRequest);
        log.info(genericAccountResponse.toString());
        assertThat(genericAccountResponse.isSuccess()).isFalse();
        assertThat(genericAccountResponse.getCode()).isEqualTo(ResultCode.UNAUTHORIZED);

        // verify correct password
        verifyPasswordRequest = VerifyPasswordRequest.builder()
                .email(email)
                .password(password)
                .build();
        genericAccountResponse = this.accountClient.verifyPassword(
                AuthConstant.AUTHORIZATION_WWW_SERVICE, verifyPasswordRequest);
        log.info(genericAccountResponse.toString());
        assertThat(genericAccountResponse.isSuccess()).isTrue();
        assertThat(genericAccountResponse.getAccount()).isEqualTo(accountDto);
    }

    @Test
    public void testUpdateAndVerifyPassword() {
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
                createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();

        AccountDto accountDto = genericAccountResponse.getAccount();

        // update password success
        String password = "pass123456";
        UpdatePasswordRequest updatePasswordRequest = UpdatePasswordRequest.builder()
                .userid(accountDto.getId())
                .password(password)
                .build();
        BaseResponse baseResponse = this.accountClient.updatePassword(
                AuthConstant.AUTHORIZATION_WWW_SERVICE, updatePasswordRequest);
        log.info(baseResponse.toString());
        assertThat(baseResponse.isSuccess()).isTrue();

        // activate the account
        accountDto.setConfirmedAndActive(true);
        genericAccountResponse = this.accountClient.updateAccount(
                AuthConstant.AUTHORIZATION_WWW_SERVICE, accountDto);
        assertThat(genericAccountResponse.isSuccess()).isTrue();

        // verify correct password
        VerifyPasswordRequest verifyPasswordRequest = VerifyPasswordRequest.builder()
                .email(email)
                .password(password)
                .build();
        genericAccountResponse = this.accountClient.verifyPassword(
                AuthConstant.AUTHORIZATION_WWW_SERVICE, verifyPasswordRequest);
        log.info(genericAccountResponse.toString());
        assertThat(genericAccountResponse.isSuccess()).isTrue();
        assertThat(genericAccountResponse.getAccount()).isEqualTo(accountDto);
    }

    @Test
    public void testUpdateAccountValidation() {
        // arrange mock
        when(this.mailClient.send(any(EmailRequest.class)))
                .thenReturn(BaseResponse.builder().message("message sent").build());
        when(this.botClient.sendSmsGreeting(any(GreetingRequest.class)))
                .thenReturn(BaseResponse.builder().message("sms sent").build());

        // create first account
        String name = "testAccount001";
        String email = "test001@staffjoy.xyz";
        String phoneNumber = "18012344321";
        CreateAccountRequest createAccountRequest = CreateAccountRequest.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        // create
        GenericAccountResponse genericAccountResponse = this.accountClient.createAccount(
                AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();

        // create second account
        name = "testAccount002";
        email = "test002@staffjoy.xyz";
        phoneNumber = "18012344322";

        String subject = "Confirm Your New Email Address";
        String externalApex = "staffjoy-v2.local";

        createAccountRequest = CreateAccountRequest.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        // create
        genericAccountResponse = this.accountClient.createAccount(
                AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();
        AccountDto accountDto = genericAccountResponse.getAccount();

        // update
        String updateName = "testAccountUpdate";
        accountDto.setName(updateName);
        accountDto.setPhoneNumber("18012344323");
        // no current user id
        genericAccountResponse = this.accountClient.updateAccount(
                AuthConstant.AUTHORIZATION_AUTHENTICATED_USER, accountDto);
        log.info(genericAccountResponse.toString());
        assertThat(genericAccountResponse.isSuccess()).isFalse();
        assertThat(genericAccountResponse.getCode()).isEqualTo(ResultCode.FAILURE);

        // set user id for testing
        TestConfig.TEST_USER_ID = accountDto.getId();
        GenericAccountResponse genericAccountResponse1 = this.accountClient.updateAccount(
                AuthConstant.AUTHORIZATION_WWW_SERVICE, accountDto);
        log.info(genericAccountResponse1.toString());
        assertThat(genericAccountResponse1.isSuccess()).isTrue();

        String oldId = accountDto.getId();
        // can't update not existing account
        accountDto.setId("not_existing_id");
        genericAccountResponse1 = this.accountClient.updateAccount(
                AuthConstant.AUTHORIZATION_WWW_SERVICE, accountDto);
        log.info(genericAccountResponse1.toString());
        assertThat(genericAccountResponse1.isSuccess()).isFalse();
        assertThat(genericAccountResponse1.getCode()).isEqualTo(ResultCode.NOT_FOUND);

        // reset id
        accountDto.setId(oldId);
        // can't update member since
        Instant oldMemberSince = accountDto.getMemberSince();
        accountDto.setMemberSince(oldMemberSince.minusSeconds(5L));
        genericAccountResponse1 = this.accountClient.updateAccount(
                AuthConstant.AUTHORIZATION_WWW_SERVICE, accountDto);
        log.info(genericAccountResponse1.toString());
        assertThat(genericAccountResponse1.isSuccess()).isFalse();
        assertThat(genericAccountResponse1.getCode()).isEqualTo(ResultCode.REQ_REJECT);

        // reset member since
        accountDto.setMemberSince(oldMemberSince);
        // can't update to existing email
        String oldEmail = accountDto.getEmail();
        accountDto.setEmail("test001@staffjoy.xyz");
        genericAccountResponse1 = this.accountClient.updateAccount(
                AuthConstant.AUTHORIZATION_WWW_SERVICE, accountDto);
        log.info(genericAccountResponse1.toString());
        assertThat(genericAccountResponse1.isSuccess()).isFalse();
        assertThat(genericAccountResponse1.getCode()).isEqualTo(ResultCode.REQ_REJECT);

        // reset email
        accountDto.setEmail(oldEmail);
        // can't update to existing phoneNumber
        String oldPhoneNumber = accountDto.getPhoneNumber();
        accountDto.setPhoneNumber("18012344321");
        genericAccountResponse1 = this.accountClient.updateAccount(
                AuthConstant.AUTHORIZATION_WWW_SERVICE, accountDto);
        log.info(genericAccountResponse1.toString());
        assertThat(genericAccountResponse1.isSuccess()).isFalse();
        assertThat(genericAccountResponse1.getCode()).isEqualTo(ResultCode.REQ_REJECT);

        // reset phoneNumber
        accountDto.setPhoneNumber(oldPhoneNumber);
        // user can't activate him/herself
        accountDto.setConfirmedAndActive(true);
        genericAccountResponse1 = this.accountClient.updateAccount(
                AuthConstant.AUTHORIZATION_AUTHENTICATED_USER, accountDto);
        log.info(genericAccountResponse1.toString());
        assertThat(genericAccountResponse1.isSuccess()).isFalse();
        assertThat(genericAccountResponse1.getCode()).isEqualTo(ResultCode.REQ_REJECT);

        // reset confirm&active
        accountDto.setConfirmedAndActive(false);
        // user can't change support parameter
        accountDto.setSupport(true);
        genericAccountResponse1 = this.accountClient.updateAccount(
                AuthConstant.AUTHORIZATION_AUTHENTICATED_USER, accountDto);
        log.info(genericAccountResponse1.toString());
        assertThat(genericAccountResponse1.isSuccess()).isFalse();
        assertThat(genericAccountResponse1.getCode()).isEqualTo(ResultCode.REQ_REJECT);

        // reset support
        accountDto.setSupport(false);
        // user can't change photo url
        String photoUrl = accountDto.getPhotoUrl();
        accountDto.setPhotoUrl("updated_photo_url");
        genericAccountResponse1 = this.accountClient.updateAccount(
                AuthConstant.AUTHORIZATION_AUTHENTICATED_USER, accountDto);
        log.info(genericAccountResponse1.toString());
        assertThat(genericAccountResponse1.isSuccess()).isFalse();
        assertThat(genericAccountResponse1.getCode()).isEqualTo(ResultCode.REQ_REJECT);

        // reset photo url
        accountDto.setPhotoUrl(photoUrl);
        // user updated his/her account successfully
        genericAccountResponse1 = this.accountClient.updateAccount(
                AuthConstant.AUTHORIZATION_AUTHENTICATED_USER, accountDto);
        log.info(genericAccountResponse1.toString());
        assertThat(genericAccountResponse1.isSuccess()).isTrue();

        // user can change his/her email
        oldEmail = accountDto.getEmail();
        String updatedEmail = "test003@staffjoy.xyz";
        accountDto.setEmail(updatedEmail);
        genericAccountResponse1 = this.accountClient.updateAccount(
                AuthConstant.AUTHORIZATION_AUTHENTICATED_USER, accountDto);
        log.info(genericAccountResponse1.toString());
        assertThat(genericAccountResponse1.isSuccess()).isTrue();
        accountDto = genericAccountResponse1.getAccount();
        // email should be restored to original one
        assertThat(accountDto.getEmail()).isEqualTo(oldEmail);

        // verify email change mail sent
        ArgumentCaptor<EmailRequest> argEmailRequest = ArgumentCaptor.forClass(EmailRequest.class);
        // 3 times, 2 for account creation, 1 for email update
        verify(this.mailClient, times(3)).send(argEmailRequest.capture());
        EmailRequest emailRequest = argEmailRequest.getValue();
        log.info(emailRequest.toString());
        assertThat(emailRequest.getTo()).isEqualTo(updatedEmail);
        assertThat(emailRequest.getSubject()).isEqualTo(subject);
        assertThat(emailRequest.getName()).isEqualTo(updateName);
        assertThat(StringUtils.countMatches(emailRequest.getHtmlBody(), "http://www." + externalApex + "/activate/"))
                .isEqualTo(3);
        assertThat(StringUtils.countMatches(emailRequest.getHtmlBody(), updateName)).isEqualTo(1);
        assertThat(emailRequest.getHtmlBody()).startsWith("<div>Hi");

    }

    @Test
    public void testUpdateAccount() {
        // arrange mock
        when(this.mailClient.send(any(EmailRequest.class)))
                .thenReturn(BaseResponse.builder().message("message sent").build());
        when(this.botClient.sendSmsGreeting(any(GreetingRequest.class)))
                .thenReturn(BaseResponse.builder().message("sms sent").build());

        // create first account
        String name = "testAccount001";
        String email = "test001@staffjoy.xyz";
        String phoneNumber = "18012344321";
        CreateAccountRequest createAccountRequest = CreateAccountRequest.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        // create
        GenericAccountResponse genericAccountResponse = this.accountClient.createAccount(
                AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();

        AccountDto accountDto = genericAccountResponse.getAccount();

        // update
        accountDto.setName("testAccountUpdate");
        accountDto.setConfirmedAndActive(true);
        accountDto.setPhoneNumber("18612341122");
        GenericAccountResponse genericAccountResponse1 = this.accountClient
                .updateAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, accountDto);
        log.info(genericAccountResponse1.toString());
        assertThat(genericAccountResponse1.isSuccess()).isTrue();
        AccountDto updatedAccountDto = genericAccountResponse1.getAccount();
        assertThat(updatedAccountDto).isEqualTo(accountDto);

        // capture and verify
        ArgumentCaptor<GreetingRequest> argGreetingRequest = ArgumentCaptor.forClass(GreetingRequest.class);
        verify(this.botClient, times(1)).sendSmsGreeting(argGreetingRequest.capture());
        GreetingRequest greetingRequest = argGreetingRequest.getValue();
        assertThat(greetingRequest.getUserId()).isEqualTo(accountDto.getId());
    }

    @Test
    public void testGetAccount() {
        // arrange mock
        when(this.mailClient.send(any(EmailRequest.class)))
                .thenReturn(BaseResponse.builder().message("email sent").build());

        // first account
        String name = "testAccount001";
        String email = "test001@staffjoy.xyz";
        String phoneNumber = "18012344321";
        CreateAccountRequest createAccountRequest = CreateAccountRequest.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        // create
        GenericAccountResponse genericAccountResponse = this.accountClient.createAccount(
                AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();

        AccountDto accountDto = genericAccountResponse.getAccount();

        // get account fail
        genericAccountResponse = this.accountClient.getAccount(AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
                accountDto.getId());
        log.info(genericAccountResponse.toString());
        assertThat(genericAccountResponse.isSuccess()).isFalse();
        assertThat(genericAccountResponse.getCode()).isEqualTo(ResultCode.FAILURE);

        // get account success
        genericAccountResponse = this.accountClient.getAccount(AuthConstant.AUTHORIZATION_WHOAMI_SERVICE,
                accountDto.getId());
        log.info(genericAccountResponse.toString());
        assertThat(genericAccountResponse.isSuccess()).isTrue();
        AccountDto gotAccountDto = genericAccountResponse.getAccount();
        assertThat(accountDto).isEqualTo(gotAccountDto);

    }

    @Test
    public void testListAccounts() {
        // arrange mock
        when(this.mailClient.send(any(EmailRequest.class)))
                .thenReturn(BaseResponse.builder().message("email sent").build());

        // first account
        String name = "testAccount001";
        String email = "test001@staffjoy.xyz";
        String phoneNumber = "18012344321";
        CreateAccountRequest createAccountRequest = CreateAccountRequest.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        // create
        GenericAccountResponse genericAccountResponse = this.accountClient.createAccount(
                AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();

        // list account and verify
        ListAccountResponse listAccountResponse = this.accountClient
                .listAccounts(AuthConstant.AUTHORIZATION_SUPPORT_USER, 0, 2);
        log.info(listAccountResponse.toString());
        assertThat(listAccountResponse.isSuccess()).isTrue();
        AccountList accountList = listAccountResponse.getAccountList();
        assertThat(accountList.getAccounts()).hasSize(1);
        assertThat(accountList.getLimit()).isEqualTo(2);
        assertThat(accountList.getOffset()).isZero();

        // second account
        name = "testAccount002";
        email = "test002@staffjoy.xyz";
        phoneNumber = "18012344322";
        createAccountRequest = CreateAccountRequest.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        // create account
        genericAccountResponse = this.accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE,
                createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();

        // list and verify
        listAccountResponse = this.accountClient.listAccounts(AuthConstant.AUTHORIZATION_SUPPORT_USER, 0, 2);
        log.info(listAccountResponse.toString());
        assertThat(listAccountResponse.isSuccess()).isTrue();
        accountList = listAccountResponse.getAccountList();
        assertThat(accountList.getAccounts()).hasSize(2);
        assertThat(accountList.getLimit()).isEqualTo(2);
        assertThat(accountList.getOffset()).isZero();

        // third account
        name = "testAccount003";
        email = "test003@staffjoy.xyz";
        phoneNumber = "18012344323";
        createAccountRequest = CreateAccountRequest.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        // create account
        genericAccountResponse = this.accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE,
                createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();

        // list and verify
        listAccountResponse = this.accountClient.listAccounts(AuthConstant.AUTHORIZATION_SUPPORT_USER, 1, 2);
        log.info(listAccountResponse.toString());
        assertThat(listAccountResponse.isSuccess()).isTrue();
        accountList = listAccountResponse.getAccountList();
        assertThat(accountList.getAccounts()).hasSize(1);
        assertThat(accountList.getLimit()).isEqualTo(2);
        assertThat(accountList.getOffset()).isEqualTo(1);
    }

    @Test
    public void testCreateAccountValidation() {
        String phoneNumber = "18012340000";
        // empty request
        CreateAccountRequest createAccountRequest = CreateAccountRequest.builder()
                .build();
        GenericAccountResponse createAccountResponse = this.accountClient
                .createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        log.info(createAccountResponse.toString());
        assertThat(createAccountResponse.isSuccess()).isFalse();
        assertThat(createAccountResponse.getCode()).isEqualTo(ResultCode.PARAM_VALID_ERROR);

        // invalid email
        createAccountRequest = CreateAccountRequest.builder().email("invalid_email").build();
        createAccountResponse = this.accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE,
                createAccountRequest);
        log.info(createAccountResponse.toString());
        assertThat(createAccountResponse.isSuccess()).isFalse();
        assertThat(createAccountResponse.getCode()).isEqualTo(ResultCode.PARAM_VALID_ERROR);

        // invalid phone number
        createAccountRequest = CreateAccountRequest.builder().phoneNumber("invalid_phone_number").build();
        createAccountResponse = this.accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE,
                createAccountRequest);
        log.info(createAccountResponse.toString());
        assertThat(createAccountResponse.isSuccess()).isFalse();
        assertThat(createAccountResponse.getCode()).isEqualTo(ResultCode.PARAM_VALID_ERROR);

        // invalid auth
        createAccountRequest = CreateAccountRequest.builder().phoneNumber(phoneNumber).build();
        createAccountResponse = this.accountClient.createAccount(AuthConstant.AUTHORIZATION_ANONYMOUS_WEB,
                createAccountRequest);
        log.info(createAccountResponse.toString());
        assertThat(createAccountResponse.isSuccess()).isFalse();
        assertThat(createAccountResponse.getCode()).isEqualTo(ResultCode.UNAUTHORIZED);
    }

    @Test
    public void testGetAccountByPhoneNumber() {
        // arrange mock
        when(this.mailClient.send(any(EmailRequest.class)))
                .thenReturn(BaseResponse.builder().message("email sent").build());

        // first account
        String name = "testAccount001";
        String email = "test001@staffjoy.xyz";
        String phoneNumber = "18012344321";
        CreateAccountRequest createAccountRequest = CreateAccountRequest.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        // create
        GenericAccountResponse genericAccountResponse = this.accountClient.createAccount(
                AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();

        // get account by phoneNumber
        genericAccountResponse = this.accountClient.getAccountByPhoneNumber(AuthConstant.AUTHORIZATION_SUPPORT_USER,
                phoneNumber);
        assertThat(genericAccountResponse.isSuccess()).isTrue();
        AccountDto accountDto = genericAccountResponse.getAccount();
        assertThat(accountDto.getId()).isNotNull();
        assertThat(accountDto.getName()).isEqualTo(name);
        assertThat(accountDto.getEmail()).isEqualTo(email);
        assertThat(accountDto.getPhotoUrl()).isNotNull();
        assertThat(accountDto.getMemberSince()).isBeforeOrEqualTo(Instant.now());
        assertThat(accountDto.isSupport()).isFalse();
        assertThat(accountDto.isConfirmedAndActive()).isFalse();

        // invalid phone number
        genericAccountResponse = this.accountClient.getAccountByPhoneNumber(AuthConstant.AUTHORIZATION_SUPPORT_USER,
                "invalid_phone_number");
        log.info(genericAccountResponse.toString());
        assertThat(genericAccountResponse.isSuccess()).isFalse();
        assertThat(genericAccountResponse.getCode()).isEqualTo(ResultCode.PARAM_VALID_ERROR);

        // phoneNumber not exists
        genericAccountResponse = this.accountClient.getAccountByPhoneNumber(AuthConstant.AUTHORIZATION_SUPPORT_USER,
                "18000001111");
        log.info(genericAccountResponse.toString());
        assertThat(genericAccountResponse.isSuccess()).isFalse();
        assertThat(genericAccountResponse.getCode()).isEqualTo(ResultCode.NOT_FOUND);
    }

    @Test
    public void testCreateAccountSuccess() {
        // arrange mock
        when(this.mailClient.send(any(EmailRequest.class)))
                .thenReturn(BaseResponse.builder().message("email sent").build());

        // first account
        String name = "testAccount001";
        String email = "test001@staffjoy.xyz";
        String phoneNumber = "18012344321";
        String subject = "Activate your Staffjoy account";
        CreateAccountRequest createAccountRequest = CreateAccountRequest.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        // create and verify
        GenericAccountResponse genericAccountResponse = this.accountClient.createAccount(
                AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();

        AccountDto accountDto = genericAccountResponse.getAccount();
        assertThat(accountDto.getId()).isNotNull();
        assertThat(accountDto.getName()).isEqualTo(name);
        assertThat(accountDto.getEmail()).isEqualTo(email);
        assertThat(accountDto.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(accountDto.getPhotoUrl()).isNotNull();
        assertThat(accountDto.getMemberSince()).isBeforeOrEqualTo(Instant.now());
        assertThat(accountDto.isSupport()).isFalse();
        assertThat(accountDto.isConfirmedAndActive()).isFalse();

        // capture and verify
        ArgumentCaptor<EmailRequest> argEmailRequest = ArgumentCaptor.forClass(EmailRequest.class);
        verify(this.mailClient, times(1)).send(argEmailRequest.capture());
        EmailRequest emailRequest = argEmailRequest.getValue();
        assertThat(emailRequest.getTo()).isEqualTo(email);
        assertThat(emailRequest.getSubject()).isEqualTo(subject);
        assertThat(emailRequest.getName()).isEqualTo(name);
        assertThat(StringUtils.countMatches(emailRequest.getHtmlBody(),
                "http://www." + this.envConfig.getExternalApex() + "/activate")).isEqualTo(3);
        assertThat(StringUtils.countMatches(emailRequest.getHtmlBody(), name)).isEqualTo(1);
        assertThat(emailRequest.getHtmlBody()).startsWith("<div><p>Hi");
    }

    @Test
    public void testCreateAccountDuplicate() {
        // arrange mock
        when(this.mailClient.send(any(EmailRequest.class)))
                .thenReturn(BaseResponse.builder().message("email sent").build());

        // first account
        String name = "testAccount001";
        String email = "test001@staffjoy.xyz";
        String phoneNumber = "18012344321";
        CreateAccountRequest createAccountRequest = CreateAccountRequest.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        // create and verify
        GenericAccountResponse genericAccountResponse = this.accountClient.createAccount(
                AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();

        // email duplicate
        createAccountRequest = CreateAccountRequest.builder()
                .name("testAccount002")
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        genericAccountResponse = this.accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE,
                createAccountRequest);
        log.info(genericAccountResponse.toString());
        assertThat(genericAccountResponse.isSuccess()).isFalse();
        assertThat(genericAccountResponse.getCode()).isEqualTo(ResultCode.FAILURE);

        // phone number duplicate
        createAccountRequest = CreateAccountRequest.builder()
                .name("testAccount002")
                .email("test002@staffjoy.xyz")
                .phoneNumber(phoneNumber)
                .build();
        genericAccountResponse = this.accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE,
                createAccountRequest);
        log.info(genericAccountResponse.toString());
        assertThat(genericAccountResponse.isSuccess()).isFalse();
        assertThat(genericAccountResponse.getCode()).isEqualTo(ResultCode.FAILURE);

    }

    @After
    public void destroy() {
        this.accountRepo.deleteAll();
    }
}