package cn.eros.staffjoy.company.service;

import cn.eros.staffjoy.common.api.ResultCode;
import cn.eros.staffjoy.common.auditlog.LogEntry;
import cn.eros.staffjoy.common.auth.AuthContext;
import cn.eros.staffjoy.common.error.ServiceException;
import cn.eros.staffjoy.company.dto.CreateTeamRequest;
import cn.eros.staffjoy.company.dto.TeamDto;
import cn.eros.staffjoy.company.dto.TeamList;
import cn.eros.staffjoy.company.dto.WorkerDto;
import cn.eros.staffjoy.company.model.Company;
import cn.eros.staffjoy.company.model.Team;
import cn.eros.staffjoy.company.model.Worker;
import cn.eros.staffjoy.company.repo.CompanyRepository;
import cn.eros.staffjoy.company.repo.TeamRepository;
import cn.eros.staffjoy.company.repo.WorkerRepository;
import cn.eros.staffjoy.company.service.helper.ServiceHelper;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import io.micrometer.core.instrument.util.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @author 周光兵
 * @date 2021/09/08 13:31
 */
@Service
public class TeamService {
    private static final ILogger LOGGER = SLoggerFactory.getLogger(TeamService.class);

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private WorkerRepository workerRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ServiceHelper serviceHelper;

    public TeamDto createTeam(CreateTeamRequest request) {
        Optional<Company> company = this.companyRepository.findById(request.getCompanyId());
        if (!company.isPresent()) {
            throw new ServiceException(ResultCode.NOT_FOUND, "Company with specified id not found");
        }

        // sanitize
        if (StringUtils.isEmpty(request.getDayWeekStarts())) {
            request.setDayWeekStarts(company.get().getDefaultDayWeekStarts());
        }

        if (StringUtils.isEmpty(request.getTimezone())) {
            request.setTimezone(company.get().getDefaultTimezone());
        }

        Team team = Team.builder()
            .companyId(request.getCompanyId())
            .name(request.getName())
            .dayWeekStarts(request.getDayWeekStarts())
            .timezone(request.getTimezone())
            .color(request.getColor())
            .build();

        try {
            this.teamRepository.save(team);
        } catch (Exception ex) {
            String errMsg = "Could not create team";
            this.serviceHelper.handleErrorAndThrowException(LOGGER, ex, errMsg);
            throw new ServiceException(errMsg, ex);
        }

        LogEntry auditLog = LogEntry.builder()
            .currentUserId(AuthContext.getUserId())
            .authorization(AuthContext.getAuthz())
            .targetType("team")
            .targetId(team.getId())
            .companyId(request.getCompanyId())
            .teamId(team.getId())
            .updatedContents(team.toString())
            .build();

        LOGGER.info("created team", auditLog);

        this.serviceHelper.trackEventAsync("team_created");

        return this.convertToDto(team);
    }

    public TeamDto updateTeam(TeamDto teamDto) {
        TeamDto orig = this.getTeamWithCompanyIdValidation(teamDto.getCompanyId(), teamDto.getId());
        Team team = this.convertToModel(teamDto);

        try {
            this.teamRepository.save(team);
        } catch (Exception ex) {
            String errMsg = "Could not update the team";

            this.serviceHelper.handleErrorAndThrowException(LOGGER, ex, errMsg);
            throw new ServiceException(errMsg, ex);
        }

        LogEntry auditLog = LogEntry.builder()
            .currentUserId(AuthContext.getUserId())
            .authorization(AuthContext.getAuthz())
            .targetType("team")
            .targetId(orig.getId())
            .companyId(teamDto.getCompanyId())
            .teamId(orig.getId())
            .originalContents(orig.toString())
            .updatedContents(teamDto.toString())
            .build();

        LOGGER.info("updated team", auditLog);

        this.serviceHelper.trackEventAsync("team_updated");

        return teamDto;
    }

    public TeamList listTeams(String companyId) {
        Optional<Company> company = this.companyRepository.findById(companyId);
        if (!company.isPresent()) {
            throw new ServiceException(ResultCode.NOT_FOUND, "Company with specified id not found");
        }

        List<Team> teams = this.teamRepository.findByCompanyId(companyId);

        TeamList teamList = TeamList.builder().build();
        for (Team team : teams) {
            TeamDto teamDto = this.getTeamWithCompanyIdValidation(team.getCompanyId(), team.getId());

            teamList.getTeams().add(teamDto);
        }

        return teamList;
    }

    public TeamDto getTeam(String teamId) {
        Optional<Team> team = this.teamRepository.findById(teamId);

        if (!team.isPresent()) {
            throw new ServiceException(ResultCode.NOT_FOUND, "team with specified id not found");
        }

        return this.convertToDto(team.get());
    }

    public WorkerDto getWorkerTeamInfo(String userId) {
        List<Worker> workers = this.workerRepository.findByUserId(userId);

        if (workers.isEmpty()) {
            throw new ServiceException(ResultCode.NOT_FOUND, "worker with specified user id not found");
        }

        Worker worker = workers.get(0);
        TeamDto team = this.getTeam(worker.getTeamId());

        return WorkerDto.builder()
            .teamId(worker.getTeamId())
            .userId(worker.getUserId())
            .companyId(team.getCompanyId())
            .build();
    }

    public TeamDto getTeamWithCompanyIdValidation(String companyId, String teamId) {
        Optional<Company> company = this.companyRepository.findById(companyId);
        if (!company.isPresent()) {
            throw new ServiceException(ResultCode.NOT_FOUND, "Company with specified id not found");
        }

        return this.getTeam(teamId);
    }

    private Team convertToModel(TeamDto teamDto) {
        return this.modelMapper.map(teamDto, Team.class);
    }

    private TeamDto convertToDto(Team team) {
        return this.modelMapper.map(team, TeamDto.class);
    }
}
