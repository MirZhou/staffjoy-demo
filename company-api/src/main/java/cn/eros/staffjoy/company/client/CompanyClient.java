package cn.eros.staffjoy.company.client;

import cn.eros.staffjoy.common.auth.AuthConstant;
import cn.eros.staffjoy.company.CompanyConstant;
import cn.eros.staffjoy.company.dto.GenericCompanyResponse;
import cn.eros.staffjoy.company.dto.GetAdminOfResponse;
import cn.eros.staffjoy.company.dto.GetWorkerOfResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author 周光兵
 * @date 2021/8/6 08:34
 */
@FeignClient(name = CompanyConstant.SERVICE_NAME, path = "/v1/company", url = "${staffjoy.company-service-endpoint")
public interface CompanyClient {
    @GetMapping("/get")
    GenericCompanyResponse getCompany(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam("company_id") String companyId);

    @GetMapping("/worker/get_worker_of")
    GetWorkerOfResponse getWorkerOf(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam String userid);

    @GetMapping("/admin/admin_of")
    GetAdminOfResponse getAdminOf(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam String userid);
}
