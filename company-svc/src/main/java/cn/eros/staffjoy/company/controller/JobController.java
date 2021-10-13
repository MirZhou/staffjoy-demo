package cn.eros.staffjoy.company.controller;

import cn.eros.staffjoy.common.auth.AuthConstant;
import cn.eros.staffjoy.common.auth.AuthContext;
import cn.eros.staffjoy.common.auth.Authorize;
import cn.eros.staffjoy.company.dto.CreateJobRequest;
import cn.eros.staffjoy.company.dto.GenericJobResponse;
import cn.eros.staffjoy.company.dto.JobDto;
import cn.eros.staffjoy.company.dto.ListJobResponse;
import cn.eros.staffjoy.company.service.JobService;
import cn.eros.staffjoy.company.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author 周光兵
 * @date 2021/9/23 22:23
 */
@RestController
@RequestMapping("/v1/company/job")
@Validated
public class JobController {
    @Autowired
    private JobService jobService;

    @Autowired
    private PermissionService permissionService;

    @PostMapping("/create")
    @Authorize({
        AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
        AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public GenericJobResponse createJob(@RequestBody @Validated CreateJobRequest request) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            this.permissionService.checkPermissionCompanyAdmin(request.getCompanyId());
        }

        return GenericJobResponse.builder()
            .job(this.jobService.createJob(request))
            .build();
    }

    @GetMapping("/list")
    @Authorize({
        AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
        AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public ListJobResponse listJob(@RequestParam String companyId,
                                   @RequestParam String teamId) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            this.permissionService.checkPermissionTeamWorker(companyId, teamId);
        }

        return new ListJobResponse(this.jobService.listJobs(companyId, teamId));
    }

    @GetMapping("/get")
    @Authorize({
        AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
        AuthConstant.AUTHORIZATION_SUPPORT_USER,
        AuthConstant.AUTHORIZATION_BOT_SERVICE
    })
    public GenericJobResponse getJob(@RequestParam String jobId,
                                     @RequestParam String companyId,
                                     @RequestParam String teamId) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            this.permissionService.checkPermissionTeamWorker(companyId, teamId);
        }

        return new GenericJobResponse(this.jobService.getJob(jobId, companyId, teamId));
    }

    @PutMapping("/update")
    @Authorize({
        AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
        AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public GenericJobResponse updateJob(@RequestBody @Validated JobDto jobDto) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            this.permissionService.checkPermissionCompanyAdmin(jobDto.getCompanyId());
        }

        return new GenericJobResponse(this.jobService.updateJob(jobDto));
    }
}
