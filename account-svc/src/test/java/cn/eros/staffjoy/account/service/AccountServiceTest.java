package cn.eros.staffjoy.account.service;

import cn.eros.staffjoy.account.AccountConstant;
import cn.eros.staffjoy.account.dto.AccountDto;
import cn.eros.staffjoy.account.model.Account;
import cn.eros.staffjoy.account.props.AppProps;
import cn.eros.staffjoy.common.api.BaseResponse;
import cn.eros.staffjoy.common.env.EnvConfig;
import cn.eros.staffjoy.mail.client.MailClient;
import cn.eros.staffjoy.mail.dto.EmailRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.modelmapper.ModelMapper;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author 周光兵
 * @date 2021/12/1 22:38
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class AccountServiceTest {
    @Mock
    private AppProps appProps;

    @Mock
    private EnvConfig envConfig;

    @Mock
    private MailClient mailClient;

    @InjectMocks
    private AccountService accountService;

    @Test
    public void testSendEmail() {
        String externalApex = "staffjoy-v2.local";

        when(this.appProps.getSigningSecret()).thenReturn("test_secret");
        when(this.envConfig.getExternalApex()).thenReturn(externalApex);
        when(this.mailClient.send(any(EmailRequest.class)))
            .thenReturn(BaseResponse.builder()
                .message("email sent")
                .build());

        String userId = UUID.randomUUID().toString();
        String email = "test@jskillcloud.com";
        String name = "test_name";
        String subject = "Activate your Staffjoy account";
        String template = AccountConstant.ACTIVATE_ACCOUNT_TMPL;

        this.accountService.sendEmail(userId, email, name, subject, template, true);

        ArgumentCaptor<EmailRequest> argEmailRequest = ArgumentCaptor.forClass(EmailRequest.class);
        verify(this.mailClient, times(1)).send(argEmailRequest.capture());
        EmailRequest emailRequest = argEmailRequest.getValue();
        log.info(emailRequest.toString());
        assertThat(emailRequest.getTo()).isEqualTo(email);
        assertThat(emailRequest.getSubject()).isEqualTo(subject);
        assertThat(emailRequest.getName()).isEqualTo(name);
        assertThat(StringUtils.countMatches(emailRequest.getHtmlBody(), "http://www." + externalApex + "/activate/"))
            .isEqualTo(3);
        assertThat(StringUtils.countMatches(emailRequest.getHtmlBody(), name)).isEqualTo(1);
        assertThat(emailRequest.getHtmlBody()).startsWith("<div><p>Hi");

        template = AccountConstant.CONFIRM_EMAIL_TMPL;
        this.accountService.sendEmail(userId, email, name, subject, template, true);

        verify(this.mailClient, times(2)).send(argEmailRequest.capture());
        emailRequest = argEmailRequest.getValue();
        log.info(emailRequest.toString());
        assertThat(emailRequest.getTo()).isEqualTo(email);
        assertThat(emailRequest.getSubject()).isEqualTo(subject);
        assertThat(emailRequest.getName()).isEqualTo(name);
        assertThat(StringUtils.countMatches(emailRequest.getHtmlBody(), "http://www." + externalApex + "/activate/"))
            .isEqualTo(3);
        assertThat(StringUtils.countMatches(emailRequest.getHtmlBody(), name)).isEqualTo(1);
        assertThat(emailRequest.getHtmlBody()).startsWith("<div>Hi");

        subject = "Reset your Staffjoy password";
        template = AccountConstant.RESET_PASSWORD_TMPL;
        this.accountService.sendEmail(userId, email, name, subject, template, false);

        verify(this.mailClient, times(3)).send(argEmailRequest.capture());
        emailRequest = argEmailRequest.getValue();
        log.info(emailRequest.toString());
        assertThat(emailRequest.getTo()).isEqualTo(email);
        assertThat(emailRequest.getSubject()).isEqualTo(subject);
        assertThat(emailRequest.getName()).isEqualTo(name);
        assertThat(StringUtils.countMatches(emailRequest.getHtmlBody(), "http://www." + externalApex + "/reset/"))
            .isEqualTo(2);
        assertThat(emailRequest.getHtmlBody()).startsWith("<div>We received a request to reset the password on your account.");
    }

    @Test
    public void testModelMapper() {
        ModelMapper mapper = new ModelMapper();

        Account account = Account.builder()
            .name("testAccount")
            .email("test@staffjoy.net")
            .memberSince(Instant.now())
            .confirmedAndActive(true)
            .photoUrl("https://staffjoy.xyz/photo/test.png")
            .phoneNumber("18001801266")
            .support(false)
            .build();

        AccountDto accountDto = mapper.map(account, AccountDto.class);
        this.validateAccount(accountDto, account);

        Account account1 = mapper.map(accountDto, Account.class);
        this.validateAccount(accountDto, account1);
    }

    private void validateAccount(AccountDto accountDto, Account account) {
        assertThat(account.getId()).isEqualTo(accountDto.getId());
        assertThat(account.getName()).isEqualTo(accountDto.getName());
        assertThat(account.getEmail()).isEqualTo(accountDto.getEmail());
        assertThat(account.getMemberSince()).isEqualTo(accountDto.getMemberSince());
        assertThat(account.isConfirmedAndActive()).isEqualTo(accountDto.isConfirmedAndActive());
        assertThat(account.getPhotoUrl()).isEqualTo(accountDto.getPhotoUrl());
        assertThat(account.getPhoneNumber()).isEqualTo(accountDto.getPhoneNumber());
        assertThat(account.isSupport()).isEqualTo(accountDto.isSupport());
    }
}
