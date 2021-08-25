package cn.eros.staffjoy.bot.service;

import cn.eros.staffjoy.account.dto.AccountDto;
import cn.eros.staffjoy.bot.BotConstant;
import cn.eros.staffjoy.bot.dto.*;
import cn.eros.staffjoy.common.api.ResultCode;
import cn.eros.staffjoy.common.auth.AuthConstant;
import cn.eros.staffjoy.common.auth.AuthContext;
import cn.eros.staffjoy.common.error.ServiceException;
import cn.eros.staffjoy.company.client.CompanyClient;
import cn.eros.staffjoy.company.dto.*;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import io.sentry.SentryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.json.Json;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * @author 周光兵
 * @date 2021/8/19 22:23
 */
@Service
public class AlertService {
    private static final ILogger LOGGER = SLoggerFactory.getLogger(AlertService.class);

    @Autowired
    private CompanyClient companyClient;
    @Autowired
    private HelperService helperService;
    @Autowired
    private SentryClient sentryClient;

    public void alertNewShift(AlertNewShiftRequest request) {
        String companyId = request.getNewShift().getCompanyId();
        String teamId = request.getNewShift().getTeamId();

        AccountDto account = this.helperService.getAccountById(request.getUserId());
        DispatchPreference dispatchPreference = this.helperService.getPreferredDispatch(account);
        if (dispatchPreference.equals(DispatchPreference.DISPATCH_UNAVAILABLE)) {
            return;
        }

        CompanyDto company = this.helperService.getCompanyById(companyId);
        TeamDto teamDto = this.getTeamByCompanyIdAndTeamId(companyId, teamId);

        String newShiftMsg = this.printShiftSmsMsg(request.getNewShift(), teamDto.getTimezone());
        String jobName = this.getJobName(companyId, teamId, request.getNewShift().getJobId());

        // Format name with leading space
        if (!StringUtils.isEmpty(jobName)) {
            jobName = " " + jobName;
        }

        String greet = HelperService.getGreet(account.getName());
        String companyName = company.getName();

        if (dispatchPreference.equals(DispatchPreference.DISPATCH_EMAIL)) {
            String htmlBody = String.format(BotConstant.ALERT_NEW_SHIFT_EMAIL_TEMPLATE,
                greet, company, jobName, newShiftMsg);
            String subject = "New Shift Alert";
            String email = account.getEmail();
            String name = account.getName();

            this.helperService.sendMail(email, name, subject, htmlBody);
        } else {
            // sms
            String templateParam = Json.createObjectBuilder()
                .add("greet", greet)
                .add("company_name", companyName)
                .add("job_name", jobName)
                .add("shift_msg", newShiftMsg)
                .build()
                .toString();

            String phoneNumber = account.getPhoneNumber();

            this.helperService.sendSms(phoneNumber, BotConstant.ALERT_NEW_SHIFT_SMS_TEMPLATE_CODE, templateParam);
        }

    }

    public void alertNewShifts(AlertNewShiftsRequest request) {
        List<ShiftDto> shifts = request.getNewShifts();
        if (shifts.size() == 0) {
            throw new ServiceException(ResultCode.PARAM_MISS, "empty shifts list in request");
        }

        String companyId = shifts.get(0).getCompanyId();
        String teamId = shifts.get(0).getTeamId();

        AccountDto account = this.helperService.getAccountById(request.getUserId());
        DispatchPreference dispatchPreference = this.helperService.getPreferredDispatch(account);
        if (dispatchPreference.equals(DispatchPreference.DISPATCH_UNAVAILABLE)) {
            return;
        }

        CompanyDto company = this.helperService.getCompanyById(companyId);
        TeamDto team = this.getTeamByCompanyIdAndTeamId(companyId, teamId);

        StringBuilder newShiftsMsg = new StringBuilder();
        String separator = (dispatchPreference.equals(DispatchPreference.DISPATCH_SMS)) ? "\n" : "<br/><br/>";

        for (ShiftDto shift : shifts) {
            String newShiftMsg = this.printShiftSmsMsg(shift, team.getTimezone());

            String jobName = this.getJobName(companyId, teamId, shift.getJobId());

            // Format name with leading space
            if (StringUtils.hasText(jobName)) {
                jobName = " " + jobName;
            }

            newShiftsMsg.append(String.format("%s%s%s", newShiftMsg, jobName, separator));
        }

        String greet = HelperService.getGreet(account.getName());
        String companyName = company.getName();
        int numberOfShifts = shifts.size();

        if (dispatchPreference.equals(DispatchPreference.DISPATCH_EMAIL)) {
            String htmlBody = String.format(BotConstant.ALERT_NEW_SHIFTS_EMAIL_TEMPLATE,
                greet, companyName, numberOfShifts, newShiftsMsg);
            String subject = "New Shifts Alert";
            String email = account.getEmail();
            String name = account.getName();

            this.helperService.sendMail(email, name, subject, htmlBody);
        } else {
            // sms

            String templateParam = Json.createObjectBuilder()
                .add("greet", greet)
                .add("company_name", companyName)
                .add("shifts_size", numberOfShifts)
                .add("shifts_msg", newShiftsMsg.toString())
                .build().toString();

            String phoneNumber = account.getPhoneNumber();
            this.helperService.sendSms(phoneNumber, BotConstant.ALERT_NEW_SHIFTS_SMS_TEMPLATE_CODE, templateParam);
        }

    }

    public void alertRemovedShift(AlertRemovedShiftRequest request) {
        String companyId = request.getOldShift().getCompanyId();
        String teamId = request.getOldShift().getTeamId();

        AccountDto account = this.helperService.getAccountById(request.getUserId());
        DispatchPreference dispatchPreference = this.helperService.getPreferredDispatch(account);
        if (dispatchPreference == DispatchPreference.DISPATCH_UNAVAILABLE) {
            return;
        }

        CompanyDto company = this.helperService.getCompanyById(companyId);
        TeamDto team = this.getTeamByCompanyIdAndTeamId(companyId, teamId);

        WorkerShiftListRequest workerShiftListRequest = WorkerShiftListRequest.builder()
            .companyId(companyId)
            .teamId(teamId)
            .workerId(AuthContext.getUserId())
            .shiftStartAfter(Instant.now())
            .shiftStartBefore(Instant.now().plus(BotConstant.SHIFT_WINDOW, ChronoUnit.DAYS))
            .build();

        ShiftList shiftList = this.listWorkerShifts(workerShiftListRequest);

        StringBuilder newShiftsMsg = new StringBuilder();
        String separator = (dispatchPreference == DispatchPreference.DISPATCH_SMS) ? "\n" : "<br /><br />";
        for (ShiftDto shiftDto : shiftList.getShifts()) {
            newShiftsMsg
                .append(this.printShiftSmsMsg(shiftDto, team.getTimezone()))
                .append(separator);
        }

        String greet = HelperService.getGreet(account.getName());
        String companyName = company.getName();

        if (dispatchPreference == DispatchPreference.DISPATCH_EMAIL) {
            String htmlBody = String.format(BotConstant.ALERT_REMOVED_SHIFT_EMAIL_TEMPLATE,
                greet, companyName, newShiftsMsg);
            String subject = "Removed Shift Alert";
            String email = account.getEmail();
            String name = account.getName();

            this.helperService.sendMail(email, name, subject, htmlBody);
        } else {
            String templateParam = Json.createObjectBuilder()
                .add("greet", greet)
                .add("company_name", companyName)
                .add("shifts_msg", newShiftsMsg.toString())
                .build()
                .toString();

            this.helperService.sendSms(account.getPhoneNumber(), BotConstant.ALERT_REMOVED_SHIFT_EMAIL_TEMPLATE, templateParam);
        }
    }

    public void alertRemovedShifts(AlertRemovedShiftsRequest request) {
        List<ShiftDto> shiftDtos = request.getOldShifts();
        if (shiftDtos.isEmpty()) {
            throw new ServiceException(ResultCode.PARAM_MISS, "empty shifts list in request");
        }

        String companyId = shiftDtos.get(0).getCompanyId();
        String teamId = shiftDtos.get(0).getTeamId();

        AccountDto account = this.helperService.getAccountById(request.getUserId());
        DispatchPreference dispatchPreference = this.helperService.getPreferredDispatch(account);
        if (DispatchPreference.DISPATCH_UNAVAILABLE.equals(dispatchPreference)) {
            return;
        }

        CompanyDto company = this.helperService.getCompanyById(companyId);
        TeamDto team = this.getTeamByCompanyIdAndTeamId(companyId, teamId);

        WorkerShiftListRequest workerShiftListRequest = WorkerShiftListRequest.builder()
            .companyId(companyId)
            .teamId(teamId)
            .workerId(request.getUserId())
            .shiftStartAfter(Instant.now())
            .shiftStartBefore(Instant.now().plus(BotConstant.SHIFT_WINDOW, ChronoUnit.DAYS))
            .build();

        ShiftList shiftList = this.listWorkerShifts(workerShiftListRequest);

        StringBuilder newShiftsMsg = new StringBuilder();
        String separator = DispatchPreference.DISPATCH_SMS.equals(dispatchPreference) ? "\n" : "<br /><br />";
        for (ShiftDto shiftDto : shiftList.getShifts()) {
            newShiftsMsg.append(this.printShiftSmsMsg(shiftDto, team.getTimezone()))
                .append(separator);
        }

        String greet = HelperService.getGreet(account.getName());
        String companyName = company.getName();
        int numberOfShifts = shiftDtos.size();

        if (DispatchPreference.DISPATCH_EMAIL.equals(dispatchPreference)) {
            String htmlBody = String.format(BotConstant.ALERT_REMOVED_SHIFTS_EMAIL_TEMPLATE,
                greet, companyName, numberOfShifts, newShiftsMsg);
            String subject = "Removed Shifts Alert";
            String email = account.getEmail();
            String name = account.getName();

            this.helperService.sendMail(email, name, subject, htmlBody);
        } else {
            String templateParam = Json.createObjectBuilder()
                .add("greet", greet)
                .add("company_name", companyName)
                .add("shifts_size", numberOfShifts)
                .add("shifts_msg", newShiftsMsg.toString())
                .build()
                .toString();
            String phoneNumber = account.getPhoneNumber();

            this.helperService.sendSms(phoneNumber, BotConstant.ALERT_REMOVED_SHIFTS_SMS_TEMPLATE_CODE, templateParam);
        }
    }

    public void alertChangedShift(AlertChangedShiftRequest request) {
        String companyId = request.getOldShift().getCompanyId();
        String teamId = request.getOldShift().getTeamId();

        AccountDto account = this.helperService.getAccountById(request.getUserId());
        DispatchPreference dispatchPreference = this.helperService.getPreferredDispatch(account);
        if (DispatchPreference.DISPATCH_UNAVAILABLE.equals(dispatchPreference)) {
            return;
        }

        CompanyDto company = this.helperService.getCompanyById(companyId);
        TeamDto team = this.getTeamByCompanyIdAndTeamId(companyId, teamId);

        String oldShiftMsg = this.printShiftSmsMsg(request.getOldShift(), team.getTimezone());
        String oldJobName = this.getJobName(companyId, teamId, request.getOldShift().getJobId());

        // Format name with leading space.
        if (!StringUtils.isEmpty(oldJobName)) {
            oldShiftMsg += String.format(" (%s)", oldJobName);
        }

        String newShiftMsg = this.printShiftSmsMsg(request.getNewShift(), team.getTimezone());
        String newJobName = this.getJobName(companyId, teamId, request.getNewShift().getJobId());

        if (StringUtils.hasText(newJobName)) {
            newShiftMsg += String.format(" (%s)", newJobName);
        }

        String greet = HelperService.getGreet(account.getName());
        String companyName = company.getName();

        if (DispatchPreference.DISPATCH_EMAIL.equals(dispatchPreference)) {
            String htmlBody = String.format(BotConstant.ALERT_CHANGED_SHIFT_EMAIL_TEMPLATE,
                greet, companyName, oldShiftMsg, newShiftMsg);
            String subject = "Changed Shift Alert";
            String email = account.getEmail();
            String name = account.getName();

            this.helperService.sendMail(email, name, subject, htmlBody);
        } else {
            String templateParam = Json.createObjectBuilder()
                .add("greet", greet)
                .add("company_name", companyName)
                .add("old_shift_msg", oldShiftMsg)
                .add("new_shift_msg", newShiftMsg)
                .build()
                .toString();
            String phoneNumber = account.getPhoneNumber();

            this.helperService.sendSms(phoneNumber, BotConstant.ALERT_CHANGED_SHIFT_SMS_TEMPLATE_CODE, templateParam);
        }
    }

    private TeamDto getTeamByCompanyIdAndTeamId(String companyId, String teamId) {
        GenericTeamResponse teamResponse;
        try {
            teamResponse = this.companyClient.getTeam(AuthConstant.AUTHORIZATION_BOT_SERVICE, companyId, teamId);
        } catch (Exception ex) {
            String errMsg = "fail to get team";
            LOGGER.error(errMsg, ex);
            this.sentryClient.sendException(ex);
            throw new ServiceException(errMsg, ex);
        }

        if (!teamResponse.isSuccess()) {
            LOGGER.error(teamResponse.getMessage());
            this.sentryClient.sendMessage(teamResponse.getMessage());
            throw new ServiceException(teamResponse.getMessage());
        }

        return teamResponse.getTeam();
    }

    private ShiftList listWorkerShifts(WorkerShiftListRequest workerShiftListRequest) {
        GenericShiftListResponse response;

        try {
            response = this.companyClient.listWorkerShifts(AuthConstant.AUTHORIZATION_BOT_SERVICE, workerShiftListRequest);
        } catch (Exception ex) {
            String errMsg = "fail to list worker shifts";
            LOGGER.error(errMsg, ex);
            this.sentryClient.sendException(ex);
            throw new ServiceException(errMsg, ex);
        }

        if (!response.isSuccess()) {
            LOGGER.error(response.getMessage());
            this.sentryClient.sendMessage(response.getMessage());
            throw new ServiceException(response.getMessage());
        }

        return response.getShiftList();
    }

    private String printShiftSmsMsg(ShiftDto shift, String timezone) {
        DateTimeFormatter startTimeFormatter = DateTimeFormatter.ofPattern(BotConstant.SMS_START_TIME_FORMAT)
            .withZone(ZoneId.of(timezone));

        DateTimeFormatter stopTimeFormatter = DateTimeFormatter.ofPattern(BotConstant.SMS_STOP_TIME_FORMAT)
            .withZone(ZoneId.of(timezone));

        String startTime = startTimeFormatter.format(shift.getStart());
        String stopTime = stopTimeFormatter.format(shift.getStop());

        return String.format(BotConstant.SMS_SHIFT_FORMAT, startTime, stopTime);
    }

    /**
     * JobName returns the name of a job, given it's id
     *
     * @param companyId
     * @param teamId
     * @param jobId
     * @return
     */
    private String getJobName(String companyId, String teamId, String jobId) {
        if (StringUtils.isEmpty(jobId)) {
            return "";
        }

        GenericJobResponse jobResponse;
        try {
            jobResponse = this.companyClient.getJob(AuthConstant.AUTHORIZATION_BOT_SERVICE, jobId, companyId, teamId);
        } catch (Exception ex) {
            String errMsg = "fail to get job";
            LOGGER.error(errMsg, ex);
            this.sentryClient.sendException(ex);
            throw new ServiceException(errMsg, ex);
        }

        if (!jobResponse.isSuccess()) {
            LOGGER.error(jobResponse.getMessage());
            this.sentryClient.sendMessage(jobResponse.getMessage());
            throw new ServiceException(jobResponse.getMessage());
        }

        return jobResponse.getJob().getName();
    }
}
