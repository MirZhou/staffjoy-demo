package cn.eros.staffjoy.mail.service;

import cn.eros.staffjoy.common.env.EnvConfig;
import cn.eros.staffjoy.common.env.EnvConstant;
import cn.eros.staffjoy.mail.MailConstant;
import cn.eros.staffjoy.mail.config.AppConfig;
import cn.eros.staffjoy.mail.dto.EmailRequest;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dm.model.v20151123.SingleSendMailRequest;
import com.aliyuncs.dm.model.v20151123.SingleSendMailResponse;
import com.aliyuncs.exceptions.ClientException;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.IToLog;
import com.github.structlog4j.SLoggerFactory;
import io.sentry.SentryClient;
import io.sentry.context.Context;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * <p>create time：2021-08-16 08:53
 *
 * @author Eros
 */
@Service
@RequiredArgsConstructor
public class MailSendService {
    private final ILogger LOGGER = SLoggerFactory.getLogger(MailSendService.class);

    private final EnvConfig envConfig;
    private final IAcsClient acsClient;
    private final SentryClient sentryClient;

    @Async(AppConfig.ASYNC_EXECUTOR_NAME)
    public void syncSendEmail(EmailRequest request) {
        IToLog logContext = () -> new Object[]{
                "subject", request.getSubject(),
                "to", request.getTo(),
                "html_body", request.getHtmlBody()
        };

        if (!EnvConstant.ENV_PROD.equals(this.envConfig.getName())) {
            String subject = String.format("[%s]%s", this.envConfig.getName(), request.getSubject());
            request.setSubject(subject);

            if (!request.getTo().endsWith(MailConstant.STAFFJOY_EMAIL_SUFFIX)) {
                LOGGER.warn("Intercepted sending due to non-production environment");
                return;
            }

            SingleSendMailRequest mailRequest = new SingleSendMailRequest();
            mailRequest.setAccountName(MailConstant.FROM);
            mailRequest.setFromAlias(MailConstant.FROM_NAME);
            mailRequest.setAddressType(1);
            mailRequest.setToAddress(request.getTo());
            mailRequest.setReplyToAddress(false);
            mailRequest.setSubject(request.getSubject());
            mailRequest.setHtmlBody(request.getHtmlBody());

            try {
                SingleSendMailResponse mailResponse = this.acsClient.getAcsResponse(mailRequest);
                LOGGER.info("Successfully sent email - request id: " + mailResponse.getRequestId());
            } catch (ClientException e) {
                Context sentryContext = this.sentryClient.getContext();
                sentryContext.addTag("subject", request.getSubject());
                sentryContext.addTag("to", request.getTo());

                this.sentryClient.sendException(e);

                LOGGER.error("Unable to send email ", e, logContext);
            }
        }
    }
}
