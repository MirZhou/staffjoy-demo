package cn.eros.staffjoy.company.client;

import cn.eros.staffjoy.common.auth.AuthConstant;
import cn.eros.staffjoy.company.CompanyConstant;
import cn.eros.staffjoy.company.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    // Team Apis

    @GetMapping("/team/get")
    GenericTeamResponse getTeam(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz,
                                @RequestParam String companyId,
                                @RequestParam String teamId);

    // Job Apis

    @GetMapping("/job/get")
    GenericJobResponse getJob(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz,
                              @RequestParam String jobId,
                              @RequestParam String companyId,
                              @RequestParam String teamId);

    // Shift Apis

    @PostMapping("/shift/list_worker_shifts")
    GenericShiftListResponse listWorkerShifts(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz,
                                              @RequestBody @Validated WorkerShiftListRequest request);
}
