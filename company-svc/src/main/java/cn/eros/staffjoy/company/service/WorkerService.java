package cn.eros.staffjoy.company.service;

import cn.eros.staffjoy.common.api.ResultCode;
import cn.eros.staffjoy.common.auditlog.LogEntry;
import cn.eros.staffjoy.common.auth.AuthContext;
import cn.eros.staffjoy.common.error.ServiceException;
import cn.eros.staffjoy.company.dto.*;
import cn.eros.staffjoy.company.model.Worker;
import cn.eros.staffjoy.company.repo.WorkerRepository;
import cn.eros.staffjoy.company.service.helper.ServiceHelper;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author 周光兵
 * @date 2021/09/08 13:24
 */
@Service
public class WorkerService {
    private static final ILogger LOGGER = SLoggerFactory.getLogger(WorkerService.class);

    @Autowired
    private WorkerRepository workerRepository;

    @Autowired
    private TeamService teamService;

    @Autowired
    private DirectoryService directoryService;

    @Autowired
    private ServiceHelper serviceHelper;

    public DirectoryEntryDto createDirectory(WorkerDto workerDto) {
        // validate and will throw exception if not found
        this.teamService.getTeamWithCompanyIdValidation(workerDto.getCompanyId(), workerDto.getTeamId());

        DirectoryEntryDto directoryEntryDto = this.directoryService.getDirectoryEntry(
            workerDto.getCompanyId(),
            workerDto.getUserId()
        );

        Worker worker = this.workerRepository.findByTeamIdAndUserId(workerDto.getTeamId(), workerDto.getUserId());
        if (Objects.nonNull(worker)) {
            throw new ServiceException("User is already a worker");
        }

        try {
            Worker workerToCreate = Worker.builder()
                .teamId(workerDto.getTeamId())
                .userId(workerDto.getUserId())
                .build();

            this.workerRepository.save(workerToCreate);
        } catch (Exception ex) {
            String errMsg = "failed to create worker in database";
            this.serviceHelper.handleErrorAndThrowException(LOGGER, ex, errMsg);
            throw new ServiceException(errMsg, ex);
        }

        LogEntry auditLog = LogEntry.builder()
            .currentUserId(AuthContext.getUserId())
            .authorization(AuthContext.getAuthz())
            .targetType("worker")
            .targetId(workerDto.getUserId())
            .companyId(workerDto.getCompanyId())
            .teamId(workerDto.getTeamId())
            .build();

        LOGGER.info("added worker", auditLog);

        this.serviceHelper.trackEventAsync("worker_created");

        return directoryEntryDto;
    }

    public DirectoryEntryDto getWorker(String companyId, String teamId, String userId) {
        // validate and will throw exception if not exist
        this.teamService.getTeamWithCompanyIdValidation(companyId, teamId);

        Worker worker = this.workerRepository.findByTeamIdAndUserId(teamId, userId);
        if (Objects.isNull(worker)) {
            throw new ServiceException(ResultCode.NOT_FOUND, "worker relationship not found");
        }

        return this.directoryService.getDirectoryEntry(companyId, userId);
    }

    public WorkerOfList getWorkerOf(String userId) {
        List<Worker> workerList = this.workerRepository.findByUserId(userId);

        WorkerOfList workerOfList = WorkerOfList.builder().userid(userId).build();

        for (Worker worker : workerList) {
            TeamDto teamDto = this.teamService.getTeam(worker.getTeamId());
            workerOfList.getTeams().add(teamDto);
        }

        return workerOfList;
    }

    public WorkerEntries listWorkers(String companyId, String teamId) {
        // validate and will throw exception if not exist
        this.teamService.getTeamWithCompanyIdValidation(companyId, teamId);

        List<Worker> workerList = this.workerRepository.findByTeamId(teamId);

        WorkerEntries workerEntries = WorkerEntries.builder()
            .companyId(companyId)
            .teamId(teamId)
            .build();

        for (Worker worker : workerList) {
            DirectoryEntryDto directoryEntryDto = this.directoryService.getDirectoryEntry(companyId, worker.getUserId());
            workerEntries.getWorkers().add(directoryEntryDto);
        }

        return workerEntries;
    }

    public void deleteWorker(String companyId, String teamId, String userId) {
        // validate and will throw exception if not exist
        this.teamService.getTeamWithCompanyIdValidation(companyId, teamId);

        try {
            this.workerRepository.deleteWorker(teamId, userId);
        } catch (Exception ex) {
            String errMsg = "failed to delete worker in database";
            this.serviceHelper.handleErrorAndThrowException(LOGGER, ex, errMsg);
            throw new ServiceException(errMsg, ex);
        }

        LogEntry auditLog = LogEntry.builder()
            .currentUserId(AuthContext.getUserId())
            .authorization(AuthContext.getAuthz())
            .targetType("worker")
            .targetId(userId)
            .companyId(companyId)
            .teamId(teamId)
            .build();

        LOGGER.info("removed worker", auditLog);

        this.serviceHelper.trackEventAsync("worker_deleted");
    }
}
