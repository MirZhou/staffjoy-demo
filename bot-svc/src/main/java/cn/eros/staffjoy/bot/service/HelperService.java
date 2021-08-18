package cn.eros.staffjoy.bot.service;

import cn.eros.staffjoy.account.client.AccountClient;
import cn.eros.staffjoy.account.dto.AccountDto;
import cn.eros.staffjoy.account.dto.GenericAccountResponse;
import cn.eros.staffjoy.bot.BotConstant;
import cn.eros.staffjoy.bot.config.AppConfig;
import cn.eros.staffjoy.bot.props.AppProps;
import cn.eros.staffjoy.common.api.BaseResponse;
import cn.eros.staffjoy.common.auth.AuthConstant;
import cn.eros.staffjoy.common.env.EnvConfig;
import cn.eros.staffjoy.common.error.ServiceException;
import cn.eros.staffjoy.company.client.CompanyClient;
import cn.eros.staffjoy.mail.client.MailClient;
import cn.eros.staffjoy.mail.dto.EmailRequest;
import cn.eros.staffjoy.sms.client.SmsClient;
import cn.eros.staffjoy.sms.dto.SmsRequest;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import io.sentry.SentryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @author Eros
 * @date 2021-08-18 13:47
 */
@Service
public class HelperService {
    private final ILogger LOGGER = SLoggerFactory.getLogger(HelperService.class);

    @Autowired
    private EnvConfig envConfig;

    @Autowired
    private MailClient mailClient;
    @Autowired
    private AccountClient accountClient;
    @Autowired
    private CompanyClient companyClient;
    @Autowired
    private SmsClient smsClient;

    @Autowired
    private AppProps appProps;

    @Autowired
    private SentryClient sentryClient;

    private static final String[] standardGreetings = {
        "Hi %s!",
        "Hey %s -",
            "Hello %s.",
            "Hey. %s -!",
    };

    public AccountDto getAccountById(String userId) {
        GenericAccountResponse response;
        try {
            response = this.accountClient.getAccount(AuthConstant.AUTHORIZATION_BOT_SERVICE, userId);
        } catch (Exception e) {
            String errMsg = "fail to get user";

            LOGGER.error(errMsg, e);
            this.sentryClient.sendException(e);
            throw new ServiceException(errMsg, e);
        }

        if (!response.isSuccess()) {
            LOGGER.error(response.getMessage());
            this.sentryClient.sendMessage(response.getMessage());
            throw new ServiceException(response.getMessage());
        }
        return response.getAccount();
    }

    public DispatchPreference getPreferredDispatch(AccountDto account) {
        if (this.appProps.isForceEmailPreference()) {
            return DispatchPreference.DISPATCH_EMAIL;
        }

        if (StringUtils.hasText(account.getPhoneNumber())) {
            return DispatchPreference.DISPATCH_SMS;
        }

        if (StringUtils.hasText(account.getEmail())) {
            return DispatchPreference.DISPATCH_EMAIL;
        }

        return DispatchPreference.DISPATCH_UNAVAILABLE;
    }

    @Async(AppConfig.ASYNC_EXECUTOR_NAME)
    public void smsGreetingAsync(String phoneNumber) {
        String templateCode = BotConstant.GREETING_SMS_TEMPLATE_CODE;
        String templateParam = "";

        this.sendSms(phoneNumber, templateCode, templateParam);
    }

    @Async(AppConfig.ASYNC_EXECUTOR_NAME)
    public void mailGreetingAsync(AccountDto accountDto) {
        String email = accountDto.getEmail();
        String name = accountDto.getName();
        String subject = "Staffjoy Greeting";
        String htmlBody = BotConstant.GREETING_EMAIL_TEMPLATE;

        this.sendMail(email, name, subject, htmlBody);
    }

    private void sendSms(String phoneNumber, String templateCode, String templateParam) {
        SmsRequest smsRequest = SmsRequest.builder()
            .to(phoneNumber)
            .templateCode(templateCode)
            .templateParam(templateParam)
            .build();

        BaseResponse response;
        try {
            response = this.smsClient.send(AuthConstant.AUTHORIZATION_BOT_SERVICE, smsRequest);
        } catch (Exception exception) {
            String errMsg = "could not send sms";
            LOGGER.error(errMsg, exception);
            this.sentryClient.sendException(exception);
            throw new ServiceException(errMsg, exception);
        }

        if (!response.isSuccess()) {
            LOGGER.error(response.getMessage());
            this.sentryClient.sendMessage(response.getMessage());
            throw new ServiceException(response.getMessage());
        }

    }

    private void sendMail(String email, String name, String subject, String htmlBody) {
        EmailRequest emailRequest = EmailRequest.builder()
            .to(email)
            .name(name)
            .subject(subject)
            .htmlBody(htmlBody)
            .build();

        BaseResponse response;
        try {
            response = this.mailClient.send(emailRequest);
        } catch (Exception exception) {
            String errMsg = "Unable to send email";
            LOGGER.error(errMsg, exception);
            this.sentryClient.sendException(exception);
            throw new ServiceException(errMsg, exception);
        }

        if (!response.isSuccess()) {
            LOGGER.error(response.getMessage());
            this.sentryClient.sendMessage(response.getMessage());
            throw new ServiceException(response.getMessage());
        }
    }
}
