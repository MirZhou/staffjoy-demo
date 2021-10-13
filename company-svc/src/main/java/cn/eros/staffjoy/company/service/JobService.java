package cn.eros.staffjoy.company.service;

import cn.eros.staffjoy.common.api.ResultCode;
import cn.eros.staffjoy.common.auditlog.LogEntry;
import cn.eros.staffjoy.common.auth.AuthContext;
import cn.eros.staffjoy.common.error.ServiceException;
import cn.eros.staffjoy.company.dto.CreateJobRequest;
import cn.eros.staffjoy.company.dto.JobDto;
import cn.eros.staffjoy.company.dto.JobList;
import cn.eros.staffjoy.company.model.Job;
import cn.eros.staffjoy.company.repo.JobRepository;
import cn.eros.staffjoy.company.service.helper.ServiceHelper;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author 周光兵
 * @date 2021/9/23 22:25
 */
@Service
public class JobService {
    private static final ILogger LOGGER = SLoggerFactory.getLogger(JobService.class);

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private TeamService teamService;

    @Autowired
    private ServiceHelper serviceHelper;

    @Autowired
    private ModelMapper modelMapper;

    public JobDto createJob(CreateJobRequest request) {
        // validate and will throw exception if not exist
        this.teamService.getTeamWithCompanyIdValidation(request.getCompanyId(), request.getTeamId());

        Job job = Job.builder()
            .name(request.getName())
            .color(request.getColor())
            .teamId(request.getTeamId())
            .build();

        try {
            this.jobRepository.save(job);
        } catch (Exception ex) {
            String errMsg = "Could not create job";
            this.serviceHelper.handleErrorAndThrowException(LOGGER, ex, errMsg);
            throw new ServiceException(errMsg, ex);
        }

        LogEntry auditLog = LogEntry.builder()
            .currentUserId(AuthContext.getUserId())
            .authorization(AuthContext.getAuthz())
            .targetType("job")
            .targetId(job.getId())
            .companyId(request.getCompanyId())
            .teamId(job.getTeamId())
            .updatedContents(job.toString())
            .build();

        LOGGER.info("created job", auditLog);

        this.serviceHelper.trackEventAsync("job_created");

        JobDto jobDto = this.convertToDto(job);
        jobDto.setCompanyId(request.getCompanyId());

        return jobDto;
    }

    public JobList listJobs(String companyId, String teamId) {
        // validate and will throw exception if not exist
        this.teamService.getTeamWithCompanyIdValidation(companyId, teamId);

        JobList jobList = JobList.builder().build();
        this.jobRepository.findByTeamId(teamId).forEach(job -> {
            JobDto jobDto = this.convertToDto(job);

            jobDto.setCompanyId(companyId);

            jobList.getJobs().add(jobDto);
        });

        return jobList;
    }

    public JobDto getJob(String jobId, String companyId, String teamId) {
        // validate and will throw exception if not exist
        this.teamService.getTeamWithCompanyIdValidation(companyId, teamId);

        Optional<Job> job = this.jobRepository.findById(jobId);
        if (!job.isPresent()) {
            throw new ServiceException(ResultCode.NOT_FOUND, "job not found");
        }

        JobDto jobDto = this.convertToDto(job.get());
        jobDto.setCompanyId(companyId);

        return jobDto;
    }

    public JobDto updateJob(JobDto jobDto) {
        // validate and will throw exception if not exist
        this.teamService.getTeamWithCompanyIdValidation(jobDto.getCompanyId(), jobDto.getTeamId());

        JobDto originalJobDto = this.getJob(jobDto.getId(), jobDto.getCompanyId(), jobDto.getTeamId());
        Job jobToUpdate = this.convertToModel(originalJobDto);

        try {
            this.jobRepository.save(jobToUpdate);
        } catch (Exception e) {
            String errMsg = "could not update job";

            this.serviceHelper.handleErrorAndThrowException(LOGGER, errMsg);
            throw new ServiceException(errMsg, e);
        }

        LogEntry auditLog = LogEntry.builder()
            .currentUserId(AuthContext.getUserId())
            .authorization(AuthContext.getAuthz())
            .targetType("job")
            .targetId(jobDto.getId())
            .companyId(jobDto.getCompanyId())
            .teamId(jobDto.getTeamId())
            .originalContents(originalJobDto.toString())
            .updatedContents(jobToUpdate.toString())
            .build();

        LOGGER.info("updated job", auditLog);

        this.serviceHelper.trackEventAsync("job_updated");

        return jobDto;
    }

    private JobDto convertToDto(Job job) {
        return this.modelMapper.map(job, JobDto.class);
    }

    private Job convertToModel(JobDto jobDto) {
        return this.modelMapper.map(jobDto, Job.class);
    }
}
