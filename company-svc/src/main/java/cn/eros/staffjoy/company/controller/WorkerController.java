package cn.eros.staffjoy.company.controller;

import cn.eros.staffjoy.common.api.BaseResponse;
import cn.eros.staffjoy.common.auth.AuthConstant;
import cn.eros.staffjoy.common.auth.AuthContext;
import cn.eros.staffjoy.common.auth.Authorize;
import cn.eros.staffjoy.company.dto.GenericDirectoryResponse;
import cn.eros.staffjoy.company.dto.GetWorkerOfResponse;
import cn.eros.staffjoy.company.dto.ListWorkerResponse;
import cn.eros.staffjoy.company.dto.WorkerDto;
import cn.eros.staffjoy.company.service.PermissionService;
import cn.eros.staffjoy.company.service.WorkerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author 周光兵
 * @date 2021/09/10 13:32
 */
@RestController
@RequestMapping("/v1/company/worker")
public class WorkerController {
    @Autowired
    private WorkerService workerService;

    @Autowired
    private PermissionService permissionService;

    @PostMapping("/create")
    @Authorize({
        AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
        AuthConstant.AUTHORIZATION_SUPPORT_USER,
        AuthConstant.AUTHORIZATION_WWW_SERVICE,
        AuthConstant.AUTHORIZATION_WHOAMI_SERVICE
    })
    public GenericDirectoryResponse createWorker(@RequestBody @Validated WorkerDto request) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            this.permissionService.checkPermissionCompanyAdmin(request.getCompanyId());
        }

        return new GenericDirectoryResponse(this.workerService.createDirectory(request));
    }

    @GetMapping("/list")
    @Authorize({
        AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
        AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public ListWorkerResponse listWorkers(@RequestParam String companyId,
                                          @RequestParam String teamId) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            this.permissionService.checkPermissionCompanyAdmin(companyId);
        }

        return ListWorkerResponse.builder()
            .workerEntries(this.workerService.listWorkers(companyId, teamId))
            .build();
    }

    @GetMapping("/get")
    @Authorize({
        AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
        AuthConstant.AUTHORIZATION_SUPPORT_USER,
        AuthConstant.AUTHORIZATION_WWW_SERVICE
    })
    public GenericDirectoryResponse getWorker(@RequestParam String companyId,
                                              @RequestParam String teamId,
                                              @RequestParam String userId) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            this.permissionService.checkPermissionCompanyAdmin(companyId);
        }

        return GenericDirectoryResponse.builder()
            .directoryEntry(this.workerService.getWorker(companyId, teamId, userId))
            .build();
    }

    @DeleteMapping("/delete")
    @Authorize({
        AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
        AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public BaseResponse deleteWorker(@RequestBody @Validated WorkerDto workerDto) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            this.permissionService.checkPermissionCompanyAdmin(workerDto.getCompanyId());
        }

        this.workerService.deleteWorker(workerDto.getCompanyId(), workerDto.getTeamId(), workerDto.getUserId());

        return BaseResponse.builder()
            .message("worker has been deleted")
            .build();
    }

    @GetMapping("/get_worker_of")
    @Authorize({
        AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
        AuthConstant.AUTHORIZATION_SUPPORT_USER,
        AuthConstant.AUTHORIZATION_ACCOUNT_SERVICE,
        AuthConstant.AUTHORIZATION_WWW_SERVICE,
        // This is an internal endpoint
        AuthConstant.AUTHORIZATION_WHOAMI_SERVICE
    })
    public GetWorkerOfResponse getWorkerOf(@RequestParam String userId) {
        return GetWorkerOfResponse.builder()
            .workerOfList(this.workerService.getWorkerOf(userId))
            .build();
    }

}