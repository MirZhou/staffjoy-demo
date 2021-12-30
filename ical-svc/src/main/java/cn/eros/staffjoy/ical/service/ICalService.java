package cn.eros.staffjoy.ical.service;

import cn.eros.staffjoy.common.error.ServiceException;
import cn.eros.staffjoy.company.client.CompanyClient;
import cn.eros.staffjoy.company.dto.*;
import cn.eros.staffjoy.ical.model.Cal;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import io.sentry.SentryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static cn.eros.staffjoy.common.auth.AuthConstant.AUTHORIZATION_ICAL_SERVICE;

/**
 * @author 周光兵
 * @date 2021/10/14 09:57
 */
@Service
public class ICalService {
    private static final ILogger LOGGER = SLoggerFactory.getLogger(ICalService.class);

    @Autowired
    private CompanyClient companyClient;

    @Autowired
    private SentryClient sentryClient;

    public Cal getCalByUserId(String userId) {
        GenericWorkerResponse workerResponse = null;
        try {
            workerResponse = this.companyClient.getWorkerTeamInfo(AUTHORIZATION_ICAL_SERVICE, null, userId);
        } catch (Exception ex) {
            String errMsg = "unable to get team info";

            this.handleErrorAndThrowException(ex, errMsg);
        }

        if (!workerResponse.isSuccess()) {
            this.handleErrorAndThrowException(workerResponse.getMessage());
        }

        WorkerDto workerDto = workerResponse.getWorkerDto();

        GenericCompanyResponse companyResponse = null;
        try {
            companyResponse = this.companyClient.getCompany(AUTHORIZATION_ICAL_SERVICE, workerDto.getCompanyId());
        } catch (Exception ex) {
            String errMsg = "unable to get company";

            this.handleErrorAndThrowException(ex, errMsg);
        }

        if (!companyResponse.isSuccess()) {
            this.handleErrorAndThrowException(companyResponse.getMessage());
        }

        CompanyDto companyDto = companyResponse.getCompany();

        WorkerShiftListRequest workerShiftListRequest = WorkerShiftListRequest.builder()
            .companyId(workerDto.getCompanyId())
            .teamId(workerDto.getTeamId())
            .workerId(workerDto.getUserId())
            .shiftStartAfter(Instant.now().minus(30, ChronoUnit.DAYS))
            .shiftStartBefore(Instant.now().minus(90, ChronoUnit.DAYS))
            .build();

        GenericShiftListResponse shiftListResponse = null;

        try {
            shiftListResponse = this.companyClient.listWorkerShifts(AUTHORIZATION_ICAL_SERVICE, workerShiftListRequest);
        } catch (Exception ex) {
            String errMsg = "unable to get worker shifts";
            this.handleErrorAndThrowException(ex, errMsg);
        }

        if (!shiftListResponse.isSuccess()) {
            this.handleErrorAndThrowException(shiftListResponse.getMessage());
        }

        ShiftList shiftList = shiftListResponse.getShiftList();

        return Cal.builder()
            .companyName(companyDto.getName())
            .shiftList(shiftList.getShifts())
            .build();
    }

    private void handleErrorAndThrowException(String errMsg) {
        LOGGER.error(errMsg);
        this.sentryClient.sendMessage(errMsg);
        throw new ServiceException(errMsg);
    }

    private void handleErrorAndThrowException(Exception ex, String errMsg) {
        LOGGER.error(errMsg, ex);
        this.sentryClient.sendException(ex);
        throw new ServiceException(errMsg, ex);
    }
}
