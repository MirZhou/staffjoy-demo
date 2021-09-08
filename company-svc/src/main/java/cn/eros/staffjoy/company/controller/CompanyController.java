package cn.eros.staffjoy.company.controller;

import cn.eros.staffjoy.common.auth.AuthConstant;
import cn.eros.staffjoy.common.auth.AuthContext;
import cn.eros.staffjoy.common.auth.Authorize;
import cn.eros.staffjoy.common.validation.Group1;
import cn.eros.staffjoy.common.validation.Group2;
import cn.eros.staffjoy.company.dto.CompanyDto;
import cn.eros.staffjoy.company.dto.GenericCompanyResponse;
import cn.eros.staffjoy.company.dto.ListCompanyResponse;
import cn.eros.staffjoy.company.service.CompanyService;
import cn.eros.staffjoy.company.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author 周光兵
 * @date 2021/8/30 22:32
 */
@RestController
@RequestMapping("/v1/company")
@Validated
public class CompanyController {
    @Autowired
    private CompanyService companyService;

    @Autowired
    private PermissionService permissionService;

    @PostMapping("/create")
    @Authorize({
        AuthConstant.AUTHORIZATION_SUPPORT_USER,
        AuthConstant.AUTHORIZATION_WWW_SERVICE
    })
    public GenericCompanyResponse createCompany(@RequestBody @Validated(Group2.class) CompanyDto companyDto) {
        CompanyDto newCompany = this.companyService.createCompany(companyDto);

        return GenericCompanyResponse.builder()
            .company(newCompany)
            .build();
    }

    @GetMapping("/list")
    @Authorize({AuthConstant.AUTHORIZATION_SUPPORT_USER})
    public ListCompanyResponse listCompanies(@RequestParam int offset,
                                             @RequestParam int limit) {
        return new ListCompanyResponse(this.companyService.listCompanies(offset, limit));
    }

    @GetMapping("/get")
    @Authorize({
        AuthConstant.AUTHORIZATION_ACCOUNT_SERVICE,
        AuthConstant.AUTHORIZATION_BOT_SERVICE,
        AuthConstant.AUTHORIZATION_WHOAMI_SERVICE,
        AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
        AuthConstant.AUTHORIZATION_SUPPORT_USER,
        AuthConstant.AUTHORIZATION_WWW_SERVICE,
        AuthConstant.AUTHORIZATION_ICAL_SERVICE
    })
    public GenericCompanyResponse getCompany(@RequestParam("company_id") String companyId) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            this.permissionService.checkPermissionCompanyAdmin(companyId);
        }

        return GenericCompanyResponse.builder()
            .company(this.companyService.getCompany(companyId))
            .build();
    }

    @PutMapping("/update")
    @Authorize({
        AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
        AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public GenericCompanyResponse updateCompany(@RequestBody @Validated({Group1.class}) CompanyDto companyDto) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            this.permissionService.checkPermissionCompanyAdmin(companyDto.getId());
        }

        return new GenericCompanyResponse(this.companyService.updateCompany(companyDto));
    }
}
