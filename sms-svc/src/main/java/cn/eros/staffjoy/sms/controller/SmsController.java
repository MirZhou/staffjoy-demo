package cn.eros.staffjoy.sms.controller;

import cn.eros.staffjoy.common.api.BaseResponse;
import cn.eros.staffjoy.common.auth.AuthConstant;
import cn.eros.staffjoy.common.auth.Authorize;
import cn.eros.staffjoy.sms.dto.SmsRequest;
import cn.eros.staffjoy.sms.props.AppProps;
import cn.eros.staffjoy.sms.service.SmsSendService;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author 周光兵
 * @date 2021/10/14 16:09
 */
@RestController
@RequestMapping("/v1")
@Validated
public class SmsController {
    private static final ILogger LOGGER = SLoggerFactory.getLogger(SmsController.class);

    @Autowired
    private AppProps appProps;

    @Autowired
    private SmsSendService smsSendService;

    @PostMapping("/queue_send")
    @Authorize({
        AuthConstant.AUTHORIZATION_COMPANY_SERVICE,
        AuthConstant.AUTHORIZATION_ACCOUNT_SERVICE,
        AuthConstant.AUTHORIZATION_BOT_SERVICE
    })
    public BaseResponse send(@RequestBody @Valid SmsRequest smsRequest) {
        if (this.appProps.isWhiteListOnly()) {
            String whiteList = this.appProps.getWhiteListPhoneNumbers();
            boolean allowToSend = !StringUtils.isEmpty(whiteList)
                && whiteList.contains(smsRequest.getTo());

            if (!allowToSend) {
                String msg = String.format("prevented sending to number %s due to whitelist", smsRequest.getTo());
                LOGGER.warn(msg);
                return BaseResponse.builder().message(msg).build();
            }
        }

        this.smsSendService.sendSmsAsync(smsRequest);

        String msg = String.format("sent message to %s. async", smsRequest.getTo());

        LOGGER.debug(msg);

        return BaseResponse.builder()
            .message("")
            .build();
    }
}
