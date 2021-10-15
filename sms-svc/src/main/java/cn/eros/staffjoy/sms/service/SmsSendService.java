package cn.eros.staffjoy.sms.service;

import cn.eros.staffjoy.sms.config.AppConfig;
import cn.eros.staffjoy.sms.dto.SmsRequest;
import cn.eros.staffjoy.sms.props.AppProps;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import io.sentry.SentryClient;
import io.sentry.context.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author 周光兵
 * @date 2021/10/15 09:19
 */
@Service
public class SmsSendService {
    private static final ILogger LOGGER = SLoggerFactory.getLogger(SmsSendService.class);

    @Autowired
    private AppProps appProps;

    @Autowired
    private IAcsClient acsClient;

    @Autowired
    private SentryClient sentryClient;

    @Async(AppConfig.ASYNC_EXECUTOR_NAME)
    public void sendSmsAsync(SmsRequest smsRequest) {
        SendSmsRequest request = new SendSmsRequest();
        request.setPhoneNumbers(smsRequest.getTo());
        request.setSignName(this.appProps.getAliyunSmsSignName());
        request.setTemplateCode(smsRequest.getTemplateCode());
        request.setTemplateParam(smsRequest.getTemplateParam());

        try {
            SendSmsResponse response = this.acsClient.getAcsResponse(request);

            if ("OK".equals(response.getCode())) {
                LOGGER.info("SMS sent - " + response.getRequestId(),
                    "to", smsRequest.getTo(),
                    "template_code", smsRequest.getTemplateCode(),
                    "template_param", smsRequest.getTemplateParam());
            } else {
                Context sentryContext = this.sentryClient.getContext();
                sentryContext.addTag("to", smsRequest.getTo());
                sentryContext.addTag("template_code", smsRequest.getTemplateCode());
                sentryClient.sendMessage("bad aliyun sms reponse " + response.getCode());

                LOGGER.error("failed to send: bad aliyun sms response " + response.getCode());

            }
        } catch (ClientException ex) {
            Context sentryContext = this.sentryClient.getContext();
            sentryContext.addTag("to", smsRequest.getTo());
            sentryContext.addTag("template_code", smsRequest.getTemplateCode());
            sentryClient.sendException(ex);

            LOGGER.error("failed to make aliyun sms request", ex);
        }
    }
}
