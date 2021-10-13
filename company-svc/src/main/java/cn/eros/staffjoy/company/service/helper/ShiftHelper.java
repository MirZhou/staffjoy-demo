package cn.eros.staffjoy.company.service.helper;

import cn.eros.staffjoy.common.api.ResultCode;
import cn.eros.staffjoy.common.auditlog.LogEntry;
import cn.eros.staffjoy.common.auth.AuthContext;
import cn.eros.staffjoy.common.error.ServiceException;
import cn.eros.staffjoy.company.dto.ShiftDto;
import cn.eros.staffjoy.company.model.Shift;
import cn.eros.staffjoy.company.repo.ShiftRepository;
import cn.eros.staffjoy.company.service.DirectoryService;
import cn.eros.staffjoy.company.service.JobService;
import cn.eros.staffjoy.company.service.TeamService;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * @author 周光兵
 * @date 2021/10/12 22:27
 */
@Component
public class ShiftHelper {
    private static final ILogger LOGGER = SLoggerFactory.getLogger(ShiftHelper.class);

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private TeamService teamService;

    @Autowired
    private DirectoryService directoryService;

    @Autowired
    private JobService jobService;

    @Autowired
    private ServiceHelper serviceHelper;

    @Autowired
    private ModelMapper modelMapper;

    public ShiftDto updateShift(ShiftDto shiftDtoToUpdate, boolean suppressNotification) {
        // validate and will throw exception if not exist
        ShiftDto originalShift = this.getShift(
            shiftDtoToUpdate.getId(),
            shiftDtoToUpdate.getTeamId(),
            shiftDtoToUpdate.getCompanyId());

        if (originalShift.equals(shiftDtoToUpdate)) {
            // no change
            return shiftDtoToUpdate;
        }

        if (!StringUtils.isEmpty(shiftDtoToUpdate.getUserId())) {
            // validate and will throw exception if not exist
            this.directoryService.getDirectoryEntry(shiftDtoToUpdate.getCompanyId(), shiftDtoToUpdate.getUserId());
        }

        if (!StringUtils.isEmpty(shiftDtoToUpdate.getJobId())) {
            // validate and will throw exception if not exist
            this.jobService.getJob(shiftDtoToUpdate.getJobId(), shiftDtoToUpdate.getCompanyId(), shiftDtoToUpdate.getTeamId());
        }

        Shift shiftToUpdate = this.convertToModel(shiftDtoToUpdate);

        try {
            this.shiftRepository.save(shiftToUpdate);
        } catch (Exception ex) {
            String errMsg = "Could not update the shift";

            this.serviceHelper.handleErrorAndThrowException(LOGGER, ex, errMsg);
            throw new ServiceException(errMsg, ex);
        }

        LogEntry auditLog = LogEntry.builder()
            .currentUserId(AuthContext.getUserId())
            .authorization(AuthContext.getAuthz())
            .targetType("shift")
            .targetId(shiftToUpdate.getId())
            .companyId(shiftDtoToUpdate.getCompanyId())
            .teamId(shiftDtoToUpdate.getTeamId())
            .originalContents(originalShift.toString())
            .updatedContents(shiftToUpdate.toString())
            .build();

        LOGGER.info("updated shift", auditLog);

        this.serviceHelper.trackEventAsync("shift_updated");
        if (!originalShift.isPublished() && shiftToUpdate.isPublished()) {
            this.serviceHelper.trackEventAsync("shift_published");
        }

        if (!suppressNotification) {
            this.serviceHelper.updateShiftNotificationAsync(originalShift, shiftDtoToUpdate);
        }

        return shiftDtoToUpdate;
    }

    public ShiftDto getShift(String shiftId, String teamId, String companyId) {
        // validate and will throw exception if not exist
        this.teamService.getTeamWithCompanyIdValidation(companyId, teamId);

        Optional<Shift> shift = this.shiftRepository.findById(shiftId);
        if (!shift.isPresent()) {
            throw new ServiceException(ResultCode.NOT_FOUND, "shift with specified id not found");
        }

        ShiftDto shiftDto = this.convertToDto(shift.get());
        shiftDto.setCompanyId(companyId);

        return shiftDto;
    }

    public ShiftDto convertToDto(Shift shift) {
        return this.modelMapper.map(shift, ShiftDto.class);
    }

    public Shift convertToModel(ShiftDto shiftDto) {
        return this.modelMapper.map(shiftDto, Shift.class);
    }
}
