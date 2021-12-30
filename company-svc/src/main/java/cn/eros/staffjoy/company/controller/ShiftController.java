package cn.eros.staffjoy.company.controller;

import cn.eros.staffjoy.common.api.BaseResponse;
import cn.eros.staffjoy.common.auth.AuthConstant;
import cn.eros.staffjoy.common.auth.AuthContext;
import cn.eros.staffjoy.common.auth.Authorize;
import cn.eros.staffjoy.company.dto.*;
import cn.eros.staffjoy.company.service.PermissionService;
import cn.eros.staffjoy.company.service.ShiftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author 周光兵
 * @date 2021/9/30 16:14
 */
@RestController
@RequestMapping("/v1/company/shift")
@Validated
public class ShiftController {
    @Autowired
    private PermissionService permissionService;

    @Autowired
    private ShiftService shiftService;

    @PostMapping("/create")
    @Authorize({
        AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
        AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public GenericShiftResponse createShift(@RequestBody @Validated CreateShiftRequest request) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            this.permissionService.checkPermissionCompanyAdmin(request.getCompanyId());
        }

        return new GenericShiftResponse(this.shiftService.createShift(request));
    }

    @PostMapping("/list_worker_shifts")
    @Authorize({
        AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
        AuthConstant.AUTHORIZATION_SUPPORT_USER,
        AuthConstant.AUTHORIZATION_BOT_SERVICE,
        AuthConstant.AUTHORIZATION_ICAL_SERVICE
    })
    public GenericShiftListResponse listWorkerShifts(@RequestBody @Validated WorkerShiftListRequest request) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            this.permissionService.checkPermissionTeamWorker(request.getCompanyId(), request.getTeamId());
        }

        return new GenericShiftListResponse(this.shiftService.listWorkerShifts(request));
    }

    @PostMapping("/list")
    @Authorize({
        AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
        AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public GenericShiftListResponse listShifts(@RequestBody @Validated ShiftListRequest request) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            this.permissionService.checkPermissionTeamWorker(request.getCompanyId(), request.getTeamId());
        }

        return new GenericShiftListResponse(this.shiftService.listShifts(request));
    }

    @PostMapping("/bulk_publish")
    @Authorize({
        AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
        AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public GenericShiftListResponse bulkPublishShifts(@RequestBody @Validated BulkPublishShiftsRequest request) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            this.permissionService.checkPermissionTeamWorker(request.getCompanyId(), request.getTeamId());
        }

        return new GenericShiftListResponse(this.shiftService.bulkPublishShifts(request));
    }

    @GetMapping("/get")
    @Authorize({
        AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
        AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public GenericShiftResponse getShift(@RequestParam String shiftId,
                                         @RequestParam String teamId,
                                         @RequestParam String companyId) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            this.permissionService.checkPermissionTeamWorker(companyId, teamId);
        }

        return new GenericShiftResponse(this.shiftService.getShift(shiftId, teamId, companyId));
    }

    @PutMapping("/update")
    @Authorize({
        AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
        AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public GenericShiftResponse updateShift(@RequestBody @Validated ShiftDto shiftDto) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            this.permissionService.checkPermissionCompanyAdmin(shiftDto.getCompanyId());
        }

        return new GenericShiftResponse(this.shiftService.updateShift(shiftDto));
    }

    @DeleteMapping("/delete")
    @Authorize({
        AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
        AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public BaseResponse deleteShift(@RequestParam String shiftId,
                                    @RequestParam String teamId,
                                    @RequestParam String companyId) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            this.permissionService.checkPermissionTeamWorker(companyId, teamId);
        }

        this.shiftService.deleteShift(shiftId, teamId, companyId);

        return BaseResponse.builder()
            .message("shift deleted")
            .build();
    }
}
