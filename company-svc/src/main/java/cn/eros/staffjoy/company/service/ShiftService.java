package cn.eros.staffjoy.company.service;

import cn.eros.staffjoy.bot.dto.AlertNewShiftRequest;
import cn.eros.staffjoy.bot.dto.AlertRemovedShiftRequest;
import cn.eros.staffjoy.common.auditlog.LogEntry;
import cn.eros.staffjoy.common.auth.AuthContext;
import cn.eros.staffjoy.common.error.ServiceException;
import cn.eros.staffjoy.company.dto.*;
import cn.eros.staffjoy.company.model.Shift;
import cn.eros.staffjoy.company.repo.ShiftRepository;
import cn.eros.staffjoy.company.service.helper.ServiceHelper;
import cn.eros.staffjoy.company.service.helper.ShiftHelper;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.*;

/**
 * @author 周光兵
 * @date 2021/10/11 13:20
 */
@Service
public class ShiftService {
    private static final ILogger LOGGER = SLoggerFactory.getLogger(ShiftService.class);

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private TeamService teamService;

    @Autowired
    private JobService jobService;

    @Autowired
    private DirectoryService directoryService;

    @Autowired
    private ServiceHelper serviceHelper;

    @Autowired
    private ShiftHelper shiftHelper;

    public ShiftDto createShift(CreateShiftRequest request) {
        // validate and will throw exception if not exist
        this.teamService.getTeamWithCompanyIdValidation(request.getCompanyId(), request.getTeamId());

        if (!StringUtils.isEmpty(request.getJobId())) {
            // validate and will throw exception if not exist
            this.jobService.getJob(request.getJobId(), request.getCompanyId(), request.getTeamId());
        }

        if (!StringUtils.isEmpty(request.getUserId())) {
            this.directoryService.getDirectoryEntry(request.getCompanyId(), request.getUserId());
        }

        Shift shift = Shift.builder()
            .teamId(request.getTeamId())
            .jobId(request.getJobId())
            .start(request.getStart())
            .stop(request.getStop())
            .published(request.isPublished())
            .userId(request.getUserId())
            .build();

        try {
            this.shiftRepository.save(shift);
        } catch (Exception ex) {
            String errMsg = "Could not create shift";

            this.serviceHelper.handleErrorAndThrowException(LOGGER, ex, errMsg);
            throw new ServiceException(errMsg, ex);
        }

        LogEntry auditLog = LogEntry.builder()
            .currentUserId(AuthContext.getUserId())
            .authorization(AuthContext.getAuthz())
            .targetType("shift")
            .companyId(request.getCompanyId())
            .teamId(request.getTeamId())
            .updatedContents(shift.toString())
            .build();

        LOGGER.info("created shift", auditLog);

        ShiftDto shiftDto = this.shiftHelper.convertToDto(shift);
        shiftDto.setCompanyId(request.getCompanyId());

        if (!StringUtils.isEmpty(shift.getUserId()) && shift.isPublished()) {
            AlertNewShiftRequest alertNewShiftRequest = AlertNewShiftRequest.builder()
                .userId(shift.getUserId())
                .newShift(shiftDto)
                .build();

            this.serviceHelper.alertNewShiftAsync(alertNewShiftRequest);
        }

        this.serviceHelper.trackEventAsync("shift_created");

        if (request.isPublished()) {
            this.serviceHelper.trackEventAsync("shift_published");
        }

        return shiftDto;
    }

    public ShiftList listWorkerShifts(WorkerShiftListRequest request) {
        // validate and will throw exception if not exist
        this.teamService.getTeamWithCompanyIdValidation(request.getCompanyId(), request.getTeamId());

        ShiftList shiftList = ShiftList.builder()
            .shiftStartAfter(request.getShiftStartAfter())
            .shiftStartBefore(request.getShiftStartBefore())
            .build();

        List<Shift> shifts = this.shiftRepository.listWorkerShifts(
            request.getTeamId(),
            request.getWorkerId(),
            request.getShiftStartAfter(),
            request.getShiftStartBefore());

        return this.convertToShiftList(shiftList, shifts, request.getCompanyId());
    }

    public ShiftList listShifts(ShiftListRequest request) {
        // validate and will throw exception if not exist
        this.teamService.getTeamWithCompanyIdValidation(request.getCompanyId(), request.getTeamId());

        ShiftList shiftList = ShiftList.builder()
            .shiftStartAfter(request.getShiftStartAfter())
            .shiftStartBefore(request.getShiftStartBefore())
            .build();

        List<Shift> shifts = null;

        if (!StringUtils.isEmpty(request.getUserId()) && StringUtils.isEmpty(request.getJobId())) {
            shifts = this.shiftRepository.listWorkerShifts(
                request.getTeamId(),
                request.getUserId(),
                request.getShiftStartAfter(),
                request.getShiftStartBefore());
        }

        if (!StringUtils.isEmpty(request.getJobId()) && StringUtils.isEmpty(request.getUserId())) {
            shifts = this.shiftRepository.listShiftByJobId(
                request.getTeamId(),
                request.getJobId(),
                request.getShiftStartAfter(),
                request.getShiftStartBefore());
        }

        if (!StringUtils.isEmpty(request.getJobId()) && !StringUtils.isEmpty(request.getUserId())) {
            shifts = this.shiftRepository.listShiftByUserIdAndJobId(
                request.getTeamId(),
                request.getUserId(),
                request.getJobId(),
                request.getShiftStartAfter(),
                request.getShiftStartBefore());
        }

        if (StringUtils.isEmpty(request.getJobId()) && StringUtils.isEmpty(request.getUserId())) {
            shifts = this.shiftRepository.listShiftByTeamIdOnly(
                request.getTeamId(),
                request.getShiftStartAfter(),
                request.getShiftStartBefore()
            );
        }

        if (Objects.isNull(shifts)) {
            shifts = Collections.emptyList();
        }

        return this.convertToShiftList(shiftList, shifts, request.getCompanyId());
    }

    public ShiftList bulkPublishShifts(BulkPublishShiftsRequest request) {
        long startTime = System.currentTimeMillis();
        LOGGER.info(String.format("time so far %d", this.quickTime(startTime)));

        ShiftListRequest shiftListRequest = ShiftListRequest.builder()
            .companyId(request.getCompanyId())
            .teamId(request.getTeamId())
            .userId(request.getUserId())
            .jobId(request.getJobId())
            .shiftStartAfter(request.getShiftStartAfter())
            .shiftStartBefore(request.getShiftStartBefore())
            .build();

        ShiftList originalShiftList = this.listShifts(shiftListRequest);

        ShiftList shiftList = ShiftList.builder()
            .shiftStartAfter(request.getShiftStartAfter())
            .shiftStartBefore(request.getShiftStartBefore())
            .build();

        // Keep track of notifications - user to original shift
        Map<String, List<ShiftDto>> notificationShifts = new HashMap<>(6);

        LOGGER.info(String.format("before shifts update %d", quickTime(startTime)));

        for (ShiftDto shiftDto : originalShiftList.getShifts()) {
            // Keep track of what changed for messaging purpose
            if (!StringUtils.isEmpty(shiftDto.getUserId()) &&
                shiftDto.isPublished() != request.isPublished() &&
                shiftDto.getStart().isAfter(Instant.now())) {
                List<ShiftDto> shiftDtos = notificationShifts.get(shiftDto.getUserId());
                if (Objects.isNull(shiftDtos)) {
                    shiftDtos = new ArrayList<>();
                    notificationShifts.put(shiftDto.getUserId(), shiftDtos);
                }
                shiftDtos.add(shiftDto.toBuilder().build());
            }

            // do the change
            shiftDto.setPublished(request.isPublished());

            this.shiftHelper.updateShift(shiftDto, true);
            shiftList.getShifts().add(shiftDto);
        }

        LOGGER.info(String.format("before shifts notifications %d", this.quickTime(startTime)));

        this.serviceHelper.buildShiftNotificationAsync(notificationShifts, request.isPublished());
        LOGGER.info(String.format("total time %d", this.quickTime(startTime)));

        return shiftList;
    }

    public ShiftDto getShift(String shiftId, String teamId, String companyId) {
        return this.shiftHelper.getShift(shiftId, teamId, companyId);
    }

    public ShiftDto updateShift(ShiftDto shiftDto) {
        return this.shiftHelper.updateShift(shiftDto, false);
    }

    public void deleteShift(String shiftId, String teamId, String companyId) {
        ShiftDto originalShiftDto = this.getShift(shiftId, teamId, companyId);

        try {
            this.shiftRepository.deleteById(shiftId);
        } catch (Exception e) {
            String errMsg = "failed to delete shift";

            this.serviceHelper.handleErrorAndThrowException(LOGGER, e, errMsg);
            throw new ServiceException(errMsg, e);
        }

        LogEntry auditLog = LogEntry.builder()
            .currentUserId(AuthContext.getUserId())
            .authorization(AuthContext.getAuthz())
            .targetType("shift")
            .targetId(shiftId)
            .companyId(companyId)
            .teamId(teamId)
            .originalContents(originalShiftDto.toString())
            .build();

        LOGGER.info("deleted shift", auditLog);

        if (!StringUtils.isEmpty((originalShiftDto.getUserId()))
            && originalShiftDto.isPublished()
            && originalShiftDto.getStart().isAfter(Instant.now())) {
            AlertRemovedShiftRequest alertNewShiftRequest = AlertRemovedShiftRequest.builder()
                .userId(originalShiftDto.getUserId())
                .oldShift(originalShiftDto)
                .build();

            this.serviceHelper.alertRemovedShiftAsync(alertNewShiftRequest);
        }

        this.serviceHelper.trackEventAsync("shift_deleted");
    }

    private long quickTime(long startTime) {
        long endTime = System.currentTimeMillis();
        return (endTime - startTime) / 1000;
    }

    private ShiftList convertToShiftList(ShiftList shiftList, List<Shift> shifts, String companyId) {
        shifts.forEach(shift -> {
            ShiftDto shiftDto = this.shiftHelper.convertToDto(shift);
            shiftDto.setCompanyId(companyId);

            shiftList.getShifts().add(shiftDto);
        });

        return shiftList;
    }
}
