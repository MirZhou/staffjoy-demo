package cn.eros.staffjoy.whoami.service;

import cn.eros.staffjoy.account.client.AccountClient;
import cn.eros.staffjoy.account.dto.AccountDto;
import cn.eros.staffjoy.account.dto.GenericAccountResponse;
import cn.eros.staffjoy.common.auth.AuthConstant;
import cn.eros.staffjoy.common.crypto.Hash;
import cn.eros.staffjoy.common.error.ServiceException;
import cn.eros.staffjoy.company.client.CompanyClient;
import cn.eros.staffjoy.company.dto.AdminOfList;
import cn.eros.staffjoy.company.dto.GetAdminOfResponse;
import cn.eros.staffjoy.company.dto.GetWorkerOfResponse;
import cn.eros.staffjoy.company.dto.WorkerOfList;
import cn.eros.staffjoy.whoami.dto.IAmDto;
import cn.eros.staffjoy.whoami.dto.IntercomSettingsDto;
import cn.eros.staffjoy.whoami.props.AppProps;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import io.sentry.SentryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author 周光兵
 * @date 2021/10/18 22:46
 */
@Service
public class WhoAmIService {
    private static final ILogger LOGGER = SLoggerFactory.getLogger(WhoAmIService.class);

    @Autowired
    private AppProps appProps;

    @Autowired
    private CompanyClient companyClient;

    @Autowired
    private AccountClient accountClient;

    @Autowired
    private SentryClient sentryClient;

    public IAmDto findWhoIAm(String userId) {
        IAmDto iAmDto = IAmDto.builder()
            .userId(userId)
            .build();

        GetWorkerOfResponse workerOfResponse = null;
        try {
            workerOfResponse = this.companyClient.getWorkerOf(AuthConstant.AUTHORIZATION_WHOAMI_SERVICE, userId);
        } catch (Exception ex) {
            String errMsg = "unable to get worker of list";
            this.handleErrorAndThrowException(ex, errMsg);
        }

        if (!workerOfResponse.isSuccess()) {
            this.handleErrorAndThrowException(workerOfResponse.getMessage());
        }

        WorkerOfList workerOfList = workerOfResponse.getWorkerOfList();
        iAmDto.setWorkerOfList(workerOfList);

        GetAdminOfResponse getAdminOfResponse = null;
        try {
            getAdminOfResponse = this.companyClient.getAdminOf(AuthConstant.AUTHORIZATION_WHOAMI_SERVICE, userId);
        } catch (Exception ex) {
            String errMsg = "unable to get admin of list";
            this.handleErrorAndThrowException(ex, errMsg);
        }

        if (!getAdminOfResponse.isSuccess()) {
            this.handleErrorAndThrowException(getAdminOfResponse.getMessage());
        }

        AdminOfList adminOfList = getAdminOfResponse.getAdminOfList();
        iAmDto.setAdminOfList(adminOfList);

        return iAmDto;
    }

    public IntercomSettingsDto findIntercomSettings(String userId) {
        IntercomSettingsDto intercomSettingsDto = IntercomSettingsDto.builder()
            .appId(this.appProps.getIntercomAppId())
            .userId(userId)
            .build();

        GenericAccountResponse genericAccountResponse = null;
        try {
            genericAccountResponse = this.accountClient.getAccount(AuthConstant.AUTHORIZATION_WHOAMI_SERVICE, userId);
        } catch (Exception ex) {
            String errMsg = "unable to get account";
            this.handleErrorAndThrowException(ex, errMsg);
        }

        if (!genericAccountResponse.isSuccess()) {
            this.handleErrorAndThrowException(genericAccountResponse.getMessage());
        }

        AccountDto accountDto = genericAccountResponse.getAccount();
        intercomSettingsDto.setName(accountDto.getName());
        intercomSettingsDto.setEmail(accountDto.getEmail());
        intercomSettingsDto.setCreatedAt(accountDto.getMemberSince());

        try {
            String userHash = Hash.encode(this.appProps.getIntercomSigningSecret(), userId);
            intercomSettingsDto.setUserHash(userHash);
        } catch (Exception ex) {
            String errMsg = "fail to compute user hash";
            this.handleErrorAndThrowException(ex, errMsg);
        }

        return intercomSettingsDto;
    }

    void handleErrorAndThrowException(String errMsg) {
        LOGGER.error(errMsg);
        this.sentryClient.sendMessage(errMsg);
        throw new ServiceException(errMsg);
    }

    void handleErrorAndThrowException(Exception ex, String errMsg) {
        LOGGER.error(errMsg, ex);
        this.sentryClient.sendException(ex);
        throw new ServiceException(errMsg, ex);
    }
}
