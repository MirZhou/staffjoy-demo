package cn.eros.staffjoy.company.service.helper;

import cn.eros.staffjoy.account.client.AccountClient;
import cn.eros.staffjoy.account.dto.TrackEventRequest;
import cn.eros.staffjoy.bot.client.BotClient;
import cn.eros.staffjoy.bot.dto.*;
import cn.eros.staffjoy.common.api.BaseResponse;
import cn.eros.staffjoy.common.auth.AuthContext;
import cn.eros.staffjoy.common.env.EnvConfig;
import cn.eros.staffjoy.common.error.ServiceException;
import cn.eros.staffjoy.company.config.AppConfig;
import cn.eros.staffjoy.company.dto.ShiftDto;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import io.sentry.SentryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;

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

        BaseResponse response;

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

    @Async(AppConfig.ASYNC_EXECUTOR_NAME)
    public void alertNewShiftAsync(AlertNewShiftRequest alertNewShiftRequest) {
        BaseResponse baseResponse;

        try {
            baseResponse = this.botClient.alertNewShift(alertNewShiftRequest);
        } catch (Exception ex) {
            String errMsg = "Failed to alert worker about new shift";

            this.handleErrorAndThrowException(LOGGER, ex, errMsg);
            throw new ServiceException(errMsg, ex);
        }

        if (!baseResponse.isSuccess()) {
            this.handleErrorAndThrowException(LOGGER, baseResponse.getMessage());
            throw new ServiceException(baseResponse.getMessage());
        }
    }

    public void alertRemovedShiftAsync(AlertRemovedShiftRequest alertNewShiftRequest) {
        BaseResponse baseResponse;
        try {
            baseResponse = this.botClient.alertRemovedShift(alertNewShiftRequest);
        } catch (Exception ex) {
            String errMsg = "Failed to alert worker about removed shift";

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

    @Async(AppConfig.ASYNC_EXECUTOR_NAME)
    public void updateShiftNotificationAsync(ShiftDto originalShift, ShiftDto shiftDtoToUpdate) {
        if (!originalShift.isPublished() && shiftDtoToUpdate.isPublished()) {
            if (shiftDtoToUpdate.getStart().isAfter(Instant.now()) &&
                !StringUtils.isEmpty(shiftDtoToUpdate.getUserId())) {
                // looks like a new shift
                AlertNewShiftRequest alertNewShiftRequest = AlertNewShiftRequest.builder()
                    .userId(shiftDtoToUpdate.getUserId())
                    .newShift(shiftDtoToUpdate)
                    .build();

                BaseResponse baseResponse;

                try {
                    baseResponse = this.botClient.alertNewShift(alertNewShiftRequest);
                } catch (Exception ex) {
                    String errMsg = "failed to alert worker about new shift";

                    this.handleErrorAndThrowException(LOGGER, ex, errMsg);
                    throw new ServiceException(errMsg, ex);
                }

                if (baseResponse.isSuccess()) {
                    this.handleErrorAndThrowException(LOGGER, baseResponse.getMessage());
                    throw new ServiceException(baseResponse.getMessage());
                }
            }

            return;
        }

        if (originalShift.isPublished() && !shiftDtoToUpdate.isPublished()) {
            if (shiftDtoToUpdate.getStart().isAfter(Instant.now()) &&
                !StringUtils.isEmpty(shiftDtoToUpdate.getUserId())) {
                // removed a shift
                AlertRemovedShiftRequest alertRemovedShiftRequest = AlertRemovedShiftRequest.builder()
                    .userId(shiftDtoToUpdate.getUserId())
                    .oldShift(originalShift)
                    .build();

                BaseResponse baseResponse;

                try {
                    baseResponse = this.botClient.alertRemovedShift(alertRemovedShiftRequest);
                } catch (Exception ex) {
                    String errMsg = "failed to alert worker about removed shift";

                    this.handleErrorAndThrowException(LOGGER, ex, errMsg);
                    throw new ServiceException(errMsg, ex);
                }

                if (baseResponse.isSuccess()) {
                    this.handleErrorAndThrowException(LOGGER, baseResponse.getMessage());
                    throw new ServiceException(baseResponse.getMessage());
                }
            }

            return;
        }

        if (!originalShift.isPublished() && !shiftDtoToUpdate.isPublished()) {
            // NOOP - basically return
            return;
        }

        if (!StringUtils.isEmpty(originalShift.getUserId()) &&
            originalShift.getUserId().equals(shiftDtoToUpdate.getUserId())) {
            if (shiftDtoToUpdate.getStart().isAfter(Instant.now())) {
                AlertChangedShiftRequest alertChangedShiftRequest = AlertChangedShiftRequest.builder()
                    .userId(shiftDtoToUpdate.getUserId())
                    .oldShift(originalShift)
                    .newShift(shiftDtoToUpdate)
                    .build();

                BaseResponse baseResponse;

                try {
                    baseResponse = this.botClient.alertChangedShift(alertChangedShiftRequest);
                } catch (Exception ex) {
                    String errMsg = "failed to alert worker about changed shift";

                    this.handleErrorAndThrowException(LOGGER, ex, errMsg);
                    throw new ServiceException(errMsg, ex);
                }

                if (baseResponse.isSuccess()) {
                    this.handleErrorAndThrowException(LOGGER, baseResponse.getMessage());
                    throw new ServiceException(baseResponse.getMessage());
                }
            }

            return;
        }

        if (!originalShift.getUserId().equals(shiftDtoToUpdate.getUserId())) {
            if (!StringUtils.isEmpty(originalShift.getUserId()) && originalShift.getStart().isAfter(Instant.now())) {
                AlertRemovedShiftRequest alertRemovedShiftRequest = AlertRemovedShiftRequest.builder()
                    .userId(originalShift.getUserId())
                    .oldShift(originalShift)
                    .build();
                BaseResponse baseResponse;
                try {
                    baseResponse = botClient.alertRemovedShift(alertRemovedShiftRequest);
                } catch (Exception ex) {
                    String errMsg = "failed to alert worker about removed shift";
                    this.handleErrorAndThrowException(LOGGER, ex, errMsg);
                    throw new ServiceException(errMsg, ex);
                }
                if (!baseResponse.isSuccess()) {
                    this.handleErrorAndThrowException(LOGGER, baseResponse.getMessage());
                    throw new ServiceException(baseResponse.getMessage());
                }
            }

            if (!StringUtils.isEmpty(shiftDtoToUpdate.getUserId()) && shiftDtoToUpdate.getStart().isAfter(Instant.now())) {
                AlertNewShiftRequest alertNewShiftRequest = AlertNewShiftRequest.builder()
                    .userId(shiftDtoToUpdate.getUserId())
                    .newShift(shiftDtoToUpdate)
                    .build();
                BaseResponse baseResponse;
                try {
                    baseResponse = botClient.alertNewShift(alertNewShiftRequest);
                } catch (Exception ex) {
                    String errMsg = "failed to alert worker about new shift";
                    this.handleErrorAndThrowException(LOGGER, ex, errMsg);
                    throw new ServiceException(errMsg, ex);
                }
                if (!baseResponse.isSuccess()) {
                    this.handleErrorAndThrowException(LOGGER, baseResponse.getMessage());
                    throw new ServiceException(baseResponse.getMessage());
                }
            }

            return;
        }

        LOGGER.error(String.format("unable to determine updated shift messaging - original %s new %s", originalShift, shiftDtoToUpdate));
    }

    @Async(AppConfig.ASYNC_EXECUTOR_NAME)
    public void buildShiftNotificationAsync(Map<String, List<ShiftDto>> notificationShifts, boolean published) {
        for (Map.Entry<String, List<ShiftDto>> entry : notificationShifts.entrySet()) {
            String userId = entry.getKey();
            List<ShiftDto> shiftDtoList = entry.getValue();

            if (published) {
                // alert published
                AlertNewShiftsRequest alertNewShiftRequest = AlertNewShiftsRequest.builder()
                    .userId(userId)
                    .newShifts(shiftDtoList)
                    .build();

                BaseResponse baseResponse;
                try {
                    baseResponse = this.botClient.alertNewShifts(alertNewShiftRequest);
                } catch (Exception ex) {
                    String errMsg = "failed to alert worker about new shifts";

                    this.handleErrorAndThrowException(LOGGER, ex, errMsg);
                    throw new ServiceException(errMsg, ex);
                }

                if (!baseResponse.isSuccess()) {
                    this.handleErrorAndThrowException(LOGGER, baseResponse.getMessage());
                    throw new ServiceException(baseResponse.getMessage());
                }
            } else {
                // alert removed
                AlertRemovedShiftsRequest alertRemovedShiftsRequest = AlertRemovedShiftsRequest.builder()
                    .userId(userId)
                    .oldShifts(shiftDtoList)
                    .build();

                BaseResponse baseResponse;
                try {
                    baseResponse = this.botClient.alertRemovedShifts(alertRemovedShiftsRequest);
                } catch (Exception ex) {
                    String errMsg = "failed to alert worker about removed shifts";

                    this.handleErrorAndThrowException(LOGGER, ex, errMsg);
                    throw new ServiceException(errMsg, ex);
                }

                if (!baseResponse.isSuccess()) {
                    this.handleErrorAndThrowException(LOGGER, baseResponse.getMessage());
                    throw new ServiceException(baseResponse.getMessage());
                }
            }
        }
    }
}
