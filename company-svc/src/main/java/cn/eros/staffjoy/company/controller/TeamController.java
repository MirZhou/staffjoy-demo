package cn.eros.staffjoy.company.controller;

import cn.eros.staffjoy.common.api.ResultCode;
import cn.eros.staffjoy.common.auth.AuthConstant;
import cn.eros.staffjoy.common.auth.AuthContext;
import cn.eros.staffjoy.common.auth.Authorize;
import cn.eros.staffjoy.company.dto.*;
import cn.eros.staffjoy.company.service.PermissionService;
import cn.eros.staffjoy.company.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author 周光兵
 * @date 2021/9/15 22:05
 */
@RestController
@RequestMapping("/v1/company/team")
@Validated
public class TeamController {
    @Autowired
    private TeamService teamService;

    @Autowired
    private PermissionService permissionService;

    @PostMapping("/create")
    @Authorize({
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
            AuthConstant.AUTHORIZATION_SUPPORT_USER,
            AuthConstant.AUTHORIZATION_WWW_SERVICE
    })
    public GenericTeamResponse createTeam(@RequestBody @Validated CreateTeamRequest request) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            this.permissionService.checkPermissionCompanyAdmin(request.getCompanyId());
        }

        return new GenericTeamResponse(this.teamService.createTeam(request));
    }

    @GetMapping("/list")
    @Authorize({
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
            AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public ListTeamResponse teamList(@RequestParam String companyId) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            this.permissionService.checkPermissionCompanyAdmin(companyId);
        }

        return ListTeamResponse.builder()
                .teamList(this.teamService.listTeams(companyId))
                .build();
    }

    @GetMapping("/get")
    @Authorize({
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
            AuthConstant.AUTHORIZATION_SUPPORT_USER,
            AuthConstant.AUTHORIZATION_ACCOUNT_SERVICE,
            AuthConstant.AUTHORIZATION_BOT_SERVICE,
            AuthConstant.AUTHORIZATION_WWW_SERVICE,
            AuthConstant.AUTHORIZATION_ICAL_SERVICE,
            AuthConstant.AUTHORIZATION_WHOAMI_SERVICE
    })
    public GenericTeamResponse getTeam(@RequestParam String companyId,
            @RequestParam String teamId) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            this.permissionService.checkPermissionCompanyAdmin(companyId);
        }

        return GenericTeamResponse.builder()
                .team(this.teamService.getTeamWithCompanyIdValidation(companyId, teamId))
                .build();
    }

    @PutMapping("/update")
    @Authorize({
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
            AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public GenericTeamResponse updateTeam(@RequestBody @Validated TeamDto teamDto) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            this.permissionService.checkPermissionCompanyAdmin(teamDto.getCompanyId());
        }

        return GenericTeamResponse.builder()
                .team(this.teamService.updateTeam(teamDto))
                .build();
    }

    @GetMapping("/get_worker_team_info")
    @Authorize({
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
            AuthConstant.AUTHORIZATION_SUPPORT_USER,
            AuthConstant.AUTHORIZATION_ICAL_SERVICE
    })
    public GenericWorkerResponse getWorkerTeamInfo(@RequestParam(required = false) String companyId,
            @RequestParam String userId) {
        GenericWorkerResponse response = new GenericWorkerResponse();

        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())
                && !userId.equals(AuthContext.getUserId())) {
            if (StringUtils.isEmpty(companyId)) {
                response.setCode(ResultCode.PARAM_MISS);
                response.setMessage("missing companyId");
                return response;
            }

            this.permissionService.checkPermissionCompanyAdmin(companyId);
        }

        response.setWorkerDto(this.teamService.getWorkerTeamInfo(userId));

        return response;
    }
}
