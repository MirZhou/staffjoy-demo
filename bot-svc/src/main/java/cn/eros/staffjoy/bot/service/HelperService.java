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
import cn.eros.staffjoy.company.dto.CompanyDto;
import cn.eros.staffjoy.company.dto.GenericCompanyResponse;
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

import javax.json.Json;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

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

    static String getGreet(String firstName) {
        return String.format(standardGreetings[ThreadLocalRandom.current().nextInt(standardGreetings.length)], firstName);
    }

    static String getFirstName(String name) {
        if (StringUtils.isEmpty(name)) {
            return "there";
        }

        String[] names = name.split(" ");
        return names[0];
    }

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

    public CompanyDto getCompanyById(String companyId) {
        GenericCompanyResponse response;

        try {
            response = this.companyClient.getCompany(AuthConstant.AUTHORIZATION_BOT_SERVICE, companyId);
        } catch (Exception ex) {
            String errMsg = "fail to get company";

            LOGGER.error(errMsg, ex);
            this.sentryClient.sendException(ex);
            throw new ServiceException(errMsg, ex);
        }

        if (!response.isSuccess()) {
            LOGGER.error(response.getMessage());
            this.sentryClient.sendMessage(response.getMessage());
            throw new ServiceException(response.getMessage());
        }

        return response.getCompany();
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
    public void smsOnboardAsync(AccountDto account, CompanyDto company) {
        URI icalUri;
        try {
            icalUri = new URI(this.envConfig.getScheme(), "ical." + this.envConfig.getExternalApex(),
                String.format("/%s.ics", account.getId()), null);
        } catch (URISyntaxException e) {
            throw new ServiceException("Fail to build URI", e);
        }

        String templateParam1 = Json.createObjectBuilder()
            .add("greet", HelperService.getGreet(HelperService.getFirstName(account.getName())))
            .add("company_name", company.getName())
            .build()
            .toString();

        String templateParam3 = Json.createObjectBuilder()
            .add("ical_url", icalUri.toString())
            .build()
            .toString();

        Map<String, String> onBoardingMessageMap = new HashMap<>(3);
        onBoardingMessageMap.put(BotConstant.ONBOARDING_SMS_TEMPLATE_CODE_1, templateParam1);
        onBoardingMessageMap.put(BotConstant.ONBOARDING_SMS_TEMPLATE_CODE_2, "");
        onBoardingMessageMap.put(BotConstant.ONBOARDING_SMS_TEMPLATE_CODE_3, templateParam3);

        for (Map.Entry<String, String> entry : onBoardingMessageMap.entrySet()) {
            String templateCode = entry.getKey();
            String templateParam = entry.getValue();

            this.sendSms(account.getPhoneNumber(), templateCode, templateParam);

            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(4));
            } catch (InterruptedException e) {
                LOGGER.warn("InterruptedException", e);
            }
        }

        // todo - check if upcoming shifts, and if there are - send them
        LOGGER.info(String.format("onboarded worker %s (%s) for company %s (%s)",
            account.getId(), account.getName(),
            company.getId(), company.getName()));
    }

    @Async(AppConfig.ASYNC_EXECUTOR_NAME)
    public void mailGreetingAsync(AccountDto accountDto) {
        String email = accountDto.getEmail();
        String name = accountDto.getName();
        String subject = "Staffjoy Greeting";
        String htmlBody = BotConstant.GREETING_EMAIL_TEMPLATE;

        this.sendMail(email, name, subject, htmlBody);
    }

    @Async(AppConfig.ASYNC_EXECUTOR_NAME)
    public void mailOnBoardAsync(AccountDto account, CompanyDto company) {
        URI icalUri;
        try {
            icalUri = new URI(this.envConfig.getScheme(), "ical." + this.envConfig.getExternalApex(),
                String.format("/%s.ics", account.getId()), null);
        } catch (URISyntaxException e) {
            throw new ServiceException("Fail to build URI", e);
        }

        String greet = HelperService.getGreet(HelperService.getFirstName(account.getName()));
        String companyName = company.getName();
        String icalUrl = icalUri.toString();
        String email = account.getEmail();
        String name = account.getName();

        String htmlBody = String.format(BotConstant.ONBOARDING_EMAIL_TEMPLATE, greet, companyName, icalUrl);
        String subject = "On Boarding Message";

        this.sendMail(email, name, subject, htmlBody);

        // todo - check if upcoming shifts, and if there are - send them
        LOGGER.info(String.format("onboarded worker %s (%s) for company %s (%s)",
            account.getId(), account.getName(),
            company.getId(), company.getName()));
    }

    public void sendSms(String phoneNumber, String templateCode, String templateParam) {
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

    public void sendMail(String email, String name, String subject, String htmlBody) {
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
