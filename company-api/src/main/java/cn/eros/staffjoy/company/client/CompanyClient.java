package cn.eros.staffjoy.company.client;

import cn.eros.staffjoy.common.api.BaseResponse;
import cn.eros.staffjoy.common.auth.AuthConstant;
import cn.eros.staffjoy.common.validation.Group1;
import cn.eros.staffjoy.common.validation.Group2;
import cn.eros.staffjoy.company.CompanyConstant;
import cn.eros.staffjoy.company.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author 周光兵
 * @date 2021/8/6 08:34
 */
@FeignClient(name = CompanyConstant.SERVICE_NAME, path = "/v1/company", url = "${staffjoy.company-service-endpoint")
public interface CompanyClient {
    // Company Apis

    @PostMapping("/create")
    GenericCompanyResponse createCompany(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz,
                                         @RequestBody @Validated({Group2.class}) CompanyDto companyDto);

    @GetMapping("/list")
    ListCompanyResponse listCompanies(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz,
                                      @RequestParam int offset,
                                      @RequestParam int limit);

    @GetMapping("/get")
    GenericCompanyResponse getCompany(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz,
                                      @RequestParam("company_id") String companyId);

    @PutMapping("/update")
    GenericCompanyResponse updateCompany(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz,
                                         @Validated({Group1.class}) CompanyDto companyDto);

    // Admin Apis

    @GetMapping("/admin/list")
    ListAdminResponse listAdmins(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz,
                                 @RequestParam String companyId);

    @GetMapping("/admin/get")
    GenericDirectoryResponse getAdmin(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz,
                                      @RequestParam String companyId,
                                      @RequestParam String userId);

    @PostMapping("/admin/create")
    GenericDirectoryResponse createAdmin(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz,
                                         @RequestBody @Validated DirectoryEntryRequest request);

    @DeleteMapping("/admin/delete")
    BaseResponse deleteAdmin(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz,
                             @RequestBody @Valid DirectoryEntryRequest request);

    @GetMapping("/admin/admin_of")
    GetAdminOfResponse getAdminOf(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam String userid);

    // Directory Apis

    @PostMapping("/directory/create")
    GenericDirectoryResponse createDirectory(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz,
                                             @RequestBody @Validated NewDirectoryEntry request);

    @GetMapping("/directory/list")
    ListDirectoryResponse listDirectories(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz,
                                          @RequestParam String companyId,
                                          @RequestParam int offset,
                                          @RequestParam int limit);

    @GetMapping("/directory/get")
    GenericDirectoryResponse getDirectoryEntry(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz,
                                               @RequestParam String companyId,
                                               @RequestParam String userId);

    @PutMapping("/directory/update")
    GenericDirectoryResponse updateDirectoryEntry(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz,
                                                  @RequestBody @Validated DirectoryEntryDto request);

    @GetMapping("/directory/get_associations")
    GetAssociationResponse getAssociations(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz,
                                           @RequestParam String companyId,
                                           @RequestParam int offset,
                                           @RequestParam int limit);

    // WorkerDto Apis

    @PostMapping("/worker/create")
    GenericDirectoryResponse createWorker(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz,
                                          @RequestBody @Validated WorkerDto workerDto);

    @GetMapping("/worker/list")
    ListWorkerResponse listWorkers(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz,
                                   @RequestParam String companyId,
                                   @RequestParam String teamId);

    @GetMapping("/worker/get")
    GenericDirectoryResponse getWorker(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz,
                                       @RequestParam String companyId,
                                       @RequestParam String teamId,
                                       @RequestParam String userId);

    @DeleteMapping("/worker/delete")
    BaseResponse deleteWorker(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz,
                              @RequestBody @Validated WorkerDto workerDto);

    @GetMapping("/worker/get_worker_of")
    GetWorkerOfResponse getWorkerOf(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam String userid);

    // Team Apis

    @PostMapping("/team/create")
    GenericTeamResponse createTeam(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz,
                                   @RequestBody @Validated CreateTeamRequest request);

    @GetMapping("/team/list")
    ListTeamResponse teamList(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz,
                              @RequestParam String companyId);

    @GetMapping("/team/get")
    GenericTeamResponse getTeam(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz,
                                @RequestParam String companyId,
                                @RequestParam String teamId);

    @PutMapping("/team/update")
    GenericTeamResponse updateTeam(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz,
                                   @RequestBody @Validated TeamDto teamDto);

    @GetMapping("/team/get_worker_team_info")
    GenericWorkerResponse getWorkerTeamInfo(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz,
                                            @RequestParam(required = false) String companyId,
                                            @RequestParam String userId);

    // Job Apis

    @PostMapping("/job/create")
    GenericJobResponse createJob(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz,
                                 @RequestBody @Validated CreateJobRequest request);

    @GetMapping("/job/list")
    ListJobResponse listJob(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz,
                            @RequestParam String companyId,
                            @RequestParam String teamId);

    @GetMapping("/job/get")
    GenericJobResponse getJob(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz,
                              @RequestParam String jobId,
                              @RequestParam String companyId,
                              @RequestParam String teamId);

    @PutMapping("/job/update")
    GenericJobResponse updateJob(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz,
                                 @RequestBody @Validated JobDto jobDto);

    // Shift Apis

    @PostMapping("/shift/create")
    GenericShiftResponse createShift(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz,
                                     @RequestBody @Validated CreateShiftRequest request);

    @PostMapping("/shift/list_worker_shifts")
    GenericShiftListResponse listWorkerShifts(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz,
                                              @RequestBody @Validated WorkerShiftListRequest request);

    @PostMapping("/shift/list")
    GenericShiftListResponse listShifts(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz,
                                        @RequestBody @Validated ShiftListRequest request);

    @PostMapping("/shift/bulk_publish")
    GenericShiftListResponse bulkPublishShifts(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz,
                                               @RequestBody @Validated BulkPublishShiftsRequest request);

    @GetMapping("/shift/get")
    GenericShiftResponse getShift(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz,
                                  @RequestParam String shiftId,
                                  @RequestParam String teamId,
                                  @RequestParam String companyId);

    @PutMapping("/shift/update")
    GenericShiftResponse updateShift(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz,
                                     @RequestBody @Validated ShiftDto shiftDto);

    @DeleteMapping("/shift/delete")
    BaseResponse deleteShift(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz,
                             @RequestParam String shiftId,
                             @RequestParam String teamId,
                             @RequestParam String companyId);
}
