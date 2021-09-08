package cn.eros.staffjoy.company.controller;

import cn.eros.staffjoy.common.auth.AuthConstant;
import cn.eros.staffjoy.common.auth.AuthContext;
import cn.eros.staffjoy.common.auth.Authorize;
import cn.eros.staffjoy.company.dto.DirectoryEntryDto;
import cn.eros.staffjoy.company.dto.GenericDirectoryResponse;
import cn.eros.staffjoy.company.dto.GetAssociationResponse;
import cn.eros.staffjoy.company.dto.ListDirectoryResponse;
import cn.eros.staffjoy.company.dto.NewDirectoryEntry;
import cn.eros.staffjoy.company.service.DirectoryService;
import cn.eros.staffjoy.company.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author 周光兵
 * @date 2021/9/2 13:15
 */
@RestController
@RequestMapping("/v1/company/directory")
@Validated
public class DirectoryController {
    @Autowired
    private DirectoryService directoryService;

    @Autowired
    private PermissionService permissionService;

    @PostMapping("/create")
    @Authorize({ AuthConstant.AUTHORIZATION_AUTHENTICATED_USER, AuthConstant.AUTHORIZATION_SUPPORT_USER,
            AuthConstant.AUTHORIZATION_WWW_SERVICE })
    public GenericDirectoryResponse createDirectory(@RequestBody @Validated NewDirectoryEntry request) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            this.permissionService.checkPermissionCompanyAdmin(request.getCompanyId());
        }

        return new GenericDirectoryResponse(this.directoryService.createDirectory(request));
    }

    @GetMapping("/list")
    @Authorize({ AuthConstant.AUTHORIZATION_AUTHENTICATED_USER, AuthConstant.AUTHORIZATION_SUPPORT_USER })
    public ListDirectoryResponse listDirectories(@RequestParam String companyId,
            @RequestParam(defaultValue = "0") int offset, @RequestParam(defaultValue = "0") int limit) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            this.permissionService.checkPermissionCompanyAdmin(companyId);
        }

        return new ListDirectoryResponse(this.directoryService.listDirectory(companyId, offset, limit));
    }

    @GetMapping("/get")
    @Authorize({ AuthConstant.AUTHORIZATION_AUTHENTICATED_USER, AuthConstant.AUTHORIZATION_SUPPORT_USER,
            AuthConstant.AUTHORIZATION_WHOAMI_SERVICE, AuthConstant.AUTHORIZATION_WWW_SERVICE })
    public GenericDirectoryResponse getDirectoryEntry(@RequestParam String companyId, @RequestParam String userId) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            // user can access their own entry
            if (!userId.equals(AuthContext.getUserId())) {
                this.permissionService.checkPermissionCompanyAdmin(companyId);
            }
        }

        return new GenericDirectoryResponse(this.directoryService.getDirectoryEntry(companyId, userId));
    }

    @PutMapping("/update")
    @Authorize({ AuthConstant.AUTHORIZATION_AUTHENTICATED_USER, AuthConstant.AUTHORIZATION_SUPPORT_USER })
    public GenericDirectoryResponse updateDirectoryEntry(@RequestBody @Validated DirectoryEntryDto request) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            this.permissionService.checkPermissionCompanyAdmin(request.getCompanyId());
        }

        return new GenericDirectoryResponse(this.directoryService.updateDirectoryEntry(request));
    }

    @GetMapping("/get_associations")
    @Authorize({ AuthConstant.AUTHORIZATION_AUTHENTICATED_USER, AuthConstant.AUTHORIZATION_SUPPORT_USER })
    public GetAssociationResponse getAssociations(@RequestParam String companyId,
            @RequestParam(defaultValue = "0") int offset, @RequestParam(defaultValue = "0") int limit) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            this.permissionService.checkPermissionCompanyAdmin(companyId);
        }

        return new GetAssociationResponse(this.directoryService.getAssociations(companyId, offset, limit));
    }
}
