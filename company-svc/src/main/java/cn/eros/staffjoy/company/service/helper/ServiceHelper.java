package cn.eros.staffjoy.company.service.helper;

import cn.eros.staffjoy.account.client.AccountClient;
import cn.eros.staffjoy.account.dto.TrackEventRequest;
import cn.eros.staffjoy.bot.client.BotClient;
import cn.eros.staffjoy.bot.dto.OnboardWorkerRequest;
import cn.eros.staffjoy.common.api.BaseResponse;
import cn.eros.staffjoy.common.auth.AuthContext;
import cn.eros.staffjoy.common.env.EnvConfig;
import cn.eros.staffjoy.common.error.ServiceException;
import cn.eros.staffjoy.company.config.AppConfig;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import io.sentry.SentryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author 周光兵
 * @date 2021/8/26 22:06
 */
@Component
public class ServiceHelper {
    private static final ILogger LOGGER = SLoggerFactory.getLogger(ServiceHelper.class);

    @Autowired
    private SentryClient sentryClient;
    @Autowired
    private AccountClient accountClient;
    @Autowired
    private BotClient botClient;
    @Autowired
    private EnvConfig envConfig;

    @Async(AppConfig.ASYNC_EXECUTOR_NAME)
    public void trackEventAsync(String event) {
        String userId = AuthContext.getUserId();
        if (StringUtils.isEmpty(userId)) {
            return;
        }

        TrackEventRequest trackEventRequest = TrackEventRequest.builder()
            .userid(userId)
            .event(event)
            .build();

        BaseResponse response = null;

        try {
            response = this.accountClient.trackEvent(trackEventRequest);
        } catch (Exception ex) {
            String errMsg = "Fail to trackEvent through accountClient";
            this.handleErrorAndThrowException(LOGGER, ex, errMsg);
            throw new ServiceException(errMsg, ex);
        }

        if (!response.isSuccess()) {
            this.handleErrorAndThrowException(LOGGER, response.getMessage());
            throw new ServiceException(response.getMessage());
        }
    }

    @Async(AppConfig.ASYNC_EXECUTOR_NAME)
    public void onboardWorkerAsync(OnboardWorkerRequest onboardWorkerRequest) {
        BaseResponse baseResponse;
        try {
            baseResponse = this.botClient.onboardWorker(onboardWorkerRequest);
        } catch (Exception ex) {
            String errMsg = "Fail to call onboardWorker through botClient";
            this.handleErrorAndThrowException(LOGGER, ex, errMsg);
            throw new ServiceException(errMsg, ex);
        }

        if (!baseResponse.isSuccess()) {
            this.handleErrorAndThrowException(LOGGER, baseResponse.getMessage());
            throw new ServiceException(baseResponse.getMessage());
        }
    }

    public void handleErrorAndThrowException(ILogger logger, String errMsg) {
        logger.error(errMsg);

        if (!this.envConfig.isDebug()) {
            this.sentryClient.sendMessage(errMsg);
        }
    }

    public void handleErrorAndThrowException(ILogger logger, Exception ex, String errMsg) {
        logger.error(errMsg, ex);

        if (!this.envConfig.isDebug()) {
            this.sentryClient.sendException(ex);
        }
    }
}
