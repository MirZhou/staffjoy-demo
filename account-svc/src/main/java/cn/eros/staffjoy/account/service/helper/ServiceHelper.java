package cn.eros.staffjoy.account.service.helper;

import cn.eros.staffjoy.account.config.AppConfig;
import cn.eros.staffjoy.account.model.Account;
import cn.eros.staffjoy.account.repo.AccountRepo;
import cn.eros.staffjoy.bot.client.BotClient;
import cn.eros.staffjoy.bot.dto.GreetingRequest;
import cn.eros.staffjoy.common.api.BaseResponse;
import cn.eros.staffjoy.common.api.ResultCode;
import cn.eros.staffjoy.common.auth.AuthConstant;
import cn.eros.staffjoy.common.env.EnvConfig;
import cn.eros.staffjoy.common.error.ServiceException;
import cn.eros.staffjoy.company.client.CompanyClient;
import cn.eros.staffjoy.company.dto.*;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import io.intercom.api.*;
import io.sentry.SentryClient;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author 周光兵
 * @date 2021/8/5 13:54
 */
@Component
@RequiredArgsConstructor
public class ServiceHelper {
    private static final ILogger LOGGER = SLoggerFactory.getLogger(ServiceHelper.class);

    private final AccountRepo accountRepo;

    private final CompanyClient companyClient;
    private final BotClient botClient;

    private final SentryClient sentryClient;

    private final EnvConfig envConfig;

    @Async(AppConfig.ASYNC_EXECUTOR_NAME)
    public void syncUserAsync(String userId) {
        if (this.envConfig.isDebug()) {
            LOGGER.debug("intercom disabled in dev & test environment");
            return;
        }
        Optional<Account> optionalAccount = this.accountRepo.findById(userId);

        if (!optionalAccount.isPresent()) {
            throw new ServiceException(ResultCode.NOT_FOUND, String.format("User with id %s not found", userId));
        }

        Account account = optionalAccount.get();

        if (StringUtils.isEmpty(account.getPhoneNumber()) && StringUtils.isEmpty(account.getEmail())) {
            LOGGER.info(String.format("skipping sync for user %s because not email or phone number", account.getId()));
            return;
        }

        // use a map to de-dupe
        Map<String, CompanyDto> memberships = new HashMap<>(16);

        GetWorkerOfResponse workerOfResponse;

        try {
            workerOfResponse = this.companyClient.getWorkerOf(AuthConstant.AUTHORIZATION_ACCOUNT_SERVICE, userId);
        } catch (Exception ex) {
            String errMsg = "could not fetch workOfList";
            handleException(ex, errMsg);
            throw new ServiceException(errMsg, ex);
        }

        if (!workerOfResponse.isSuccess()) {
            this.handleError(workerOfResponse.getMessage());
            throw new ServiceException(workerOfResponse.getMessage());
        }


        WorkerOfList workerOfList = workerOfResponse.getWorkerOfList();

        boolean isWorker = workerOfList.getTeams().size() > 0;

        for (TeamDto team : workerOfList.getTeams()) {
            GenericCompanyResponse genericCompanyResponse;

            try {
                genericCompanyResponse = this.companyClient.getCompany(AuthConstant.AUTHORIZATION_ACCOUNT_SERVICE, team.getCompanyId());
            } catch (Exception ex) {
                String errMsg = "could not fetch companyDto from teamDto";
                this.handleException(ex, errMsg);
                throw new ServiceException(errMsg, ex);
            }

            if (!genericCompanyResponse.isSuccess()) {
                this.handleError(genericCompanyResponse.getMessage());
                throw new ServiceException(genericCompanyResponse.getMessage());
            }

            CompanyDto companyDto = genericCompanyResponse.getCompany();

            memberships.put(companyDto.getId(), companyDto);
        }

        GetAdminOfResponse getAdminOfResponse;
        try {
            getAdminOfResponse = this.companyClient.getAdminOf(AuthConstant.AUTHORIZATION_ACCOUNT_SERVICE, userId);
        } catch (Exception exception) {
            String errMsg = "could not fetch adminOfList";
            this.handleException(exception, errMsg);
            throw new ServiceException(errMsg, exception);
        }

        if (!getAdminOfResponse.isSuccess()) {
            this.handleError(getAdminOfResponse.getMessage());
            throw new ServiceException(getAdminOfResponse.getMessage());
        }

        AdminOfList adminOfList = getAdminOfResponse.getAdminOfList();

        boolean isAdmin = adminOfList.getCompanies().size() > 0;
        for (CompanyDto companyDto : adminOfList.getCompanies()) {
            memberships.put(companyDto.getId(), companyDto);
        }

        User user = new User();
        user.setUserId(userId);
        user.setEmail(account.getEmail());
        user.setName(account.getName());
        user.setSignedUpAt(account.getMemberSince().toEpochMilli());
        user.setAvatar(new Avatar().setImageURL(account.getPhotoUrl()));
        user.setLastRequestAt(Instant.now().toEpochMilli());

        user.addCustomAttribute(CustomAttribute.newBooleanAttribute("v2", true));
        user.addCustomAttribute(CustomAttribute.newStringAttribute("phone_number", account.getPhoneNumber()));
        user.addCustomAttribute(CustomAttribute.newBooleanAttribute("confirmed_and_active", account.isConfirmedAndActive()));
        user.addCustomAttribute(CustomAttribute.newBooleanAttribute("is_worker", isWorker));
        user.addCustomAttribute(CustomAttribute.newBooleanAttribute("is_admin", isAdmin));
        user.addCustomAttribute(CustomAttribute.newBooleanAttribute("is_staffjoy_account", account.isSupport()));

        for (CompanyDto companyDto : memberships.values()) {
            user.addCompany(new Company().setCompanyID(companyDto.getId()).setName(companyDto.getName()));
        }

        this.syncUserWithIntercom(user, account.getId());
    }

    @Async(AppConfig.ASYNC_EXECUTOR_NAME)
    public void trackEventAsync(String userId, String eventName) {
        if (envConfig.isDebug()) {
            LOGGER.debug("intercom disabled in dev & dev environment");
            return;
        }

        Event event = new Event()
                .setUserID(userId)
                .setEventName("v2_" + eventName)
                .setCreatedAt(System.currentTimeMillis());

        try {
            Event.create(event);

            LOGGER.debug("updated intercom");
        } catch (Exception ex) {
            String errMsg = "fail to create event to Intercom";
            handleException(ex, errMsg);
            throw new ServiceException(errMsg, ex);
        }
    }

    public void sendSmsGreeting(String userId) {
        BaseResponse baseResponse;
        try {
            GreetingRequest greetingRequest = GreetingRequest.builder().userId(userId).build();
            baseResponse = this.botClient.sendSmsGreeting(greetingRequest);
        } catch (Exception ex) {
            String errMsg = "could not send welcome sms";
            this.handleException(ex, errMsg);
            throw new ServiceException(errMsg, ex);
        }

        if (!baseResponse.isSuccess()) {
            this.handleError(baseResponse.getMessage());
            throw new ServiceException(baseResponse.getMessage());
        }
    }

    /**
     * for time diff < 2s, treat them as almost same
     *
     * @param dt1 time1
     * @param dt2 time2
     * @return result
     */
    public boolean isAlmostSameInstant(Instant dt1, Instant dt2) {
        long diff = Math.abs(dt1.toEpochMilli() - dt2.toEpochMilli());

        return diff < TimeUnit.SECONDS.toMillis(1L);

    }

    public void handleError(ILogger log, String errMsg) {
        log.error(errMsg);

        if (!this.envConfig.isDebug()) {
            this.sentryClient.sendMessage(errMsg);
        }
    }

    public void handleException(ILogger log, Exception ex, String errMsg) {
        log.error(errMsg, ex);
        if (!this.envConfig.isDebug()) {
            this.sentryClient.sendException(ex);
        }
    }

    public void syncUserWithIntercom(User user, String userid) {
        try {
            Map<String, String> params = new HashMap<>(1);
            params.put("user_id", userid);

            User existing = User.find(params);

            if (existing != null) {
                User.update(user);
            } else {
                User.create(user);
            }

            LOGGER.debug("updated intercom");
        } catch (Exception exception) {
            String errMsg = "fail to create/update user on Intercom";
            handleException(exception, errMsg);
            throw new ServiceException(errMsg, exception);
        }

    }

    private void handleError(String errMsg) {
        this.handleError(LOGGER, errMsg);
    }

    private void handleException(Exception ex, String errMsg) {
        this.handleException(LOGGER, ex, errMsg);
    }
}
