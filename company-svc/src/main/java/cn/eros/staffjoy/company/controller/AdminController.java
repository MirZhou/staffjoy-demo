package cn.eros.staffjoy.company.controller;

import cn.eros.staffjoy.common.api.BaseResponse;
import cn.eros.staffjoy.common.api.ResultCode;
import cn.eros.staffjoy.common.auth.AuthConstant;
import cn.eros.staffjoy.common.auth.AuthContext;
import cn.eros.staffjoy.common.auth.Authorize;
import cn.eros.staffjoy.common.auth.PermissionDeniedException;
import cn.eros.staffjoy.common.error.ServiceException;
import cn.eros.staffjoy.company.dto.*;
import cn.eros.staffjoy.company.service.AdminService;
import cn.eros.staffjoy.company.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Objects;

/**
 * @author 周光兵
 * @date 2021/8/26 21:52
 */
@RestController
@RequestMapping("/v1/company/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;
    @Autowired
    private PermissionService permissionService;

    @GetMapping("/list")
    @Authorize({
        AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
        AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public ListAdminResponse listAdmins(@RequestParam String companyId) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            this.permissionService.checkPermissionCompanyAdmin(companyId);
        }

        AdminEntries adminEntries = this.adminService.listAdmins(companyId);

        return new ListAdminResponse(adminEntries);
    }

    @GetMapping("/get")
    @Authorize({
        AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
        AuthConstant.AUTHORIZATION_SUPPORT_USER,
        AuthConstant.AUTHORIZATION_WWW_SERVICE
    })
    public GenericDirectoryResponse getAdmin(@RequestParam String companyId,
                                             @RequestParam String userId) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            this.permissionService.checkPermissionCompanyAdmin(companyId);
        }

        DirectoryEntryDto directoryEntryDto = this.adminService.getAdmin(companyId, userId);

        if (Objects.isNull(directoryEntryDto)) {
            throw new ServiceException(ResultCode.NOT_FOUND, "Admin relationship not found");
        }

        return new GenericDirectoryResponse(directoryEntryDto);
    }

    @PostMapping("/create")
    @Authorize({
        AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
        AuthConstant.AUTHORIZATION_SUPPORT_USER,
        AuthConstant.AUTHORIZATION_WWW_SERVICE
    })
    public GenericDirectoryResponse createAdmin(@RequestBody @Validated DirectoryEntryRequest request) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            this.permissionService.checkPermissionCompanyAdmin(request.getCompanyId());
        }

        DirectoryEntryDto directoryEntryDto = this.adminService.createAdmin(request.getCompanyId(), request.getUserId());

        return new GenericDirectoryResponse(directoryEntryDto);

    }

    @DeleteMapping("/delete")
    @Authorize({
        AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
        AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public BaseResponse deleteAdmin(@RequestBody @Valid DirectoryEntryRequest request) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            this.permissionService.checkPermissionCompanyAdmin(request.getCompanyId());
        }

        this.adminService.deleteAdmin(request.getCompanyId(), request.getUserId());

        return BaseResponse.builder().build();
    }

    @GetMapping("/admin_of")
    @Authorize({
        AuthConstant.AUTHORIZATION_ACCOUNT_SERVICE,
        AuthConstant.AUTHORIZATION_WHOAMI_SERVICE,
        AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
        AuthConstant.AUTHORIZATION_SUPPORT_USER,
        AuthConstant.AUTHORIZATION_WWW_SERVICE
    })
    public GetAdminOfResponse getAdminOf(@RequestParam String userId) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            if (!userId.equals(AuthContext.getUserId())) {
                throw new PermissionDeniedException("You don't have access to this service");
            }
        }

        return GetAdminOfResponse.builder()
            .adminOfList(this.adminService.getAdminOf(userId))
            .build();
    }
}
