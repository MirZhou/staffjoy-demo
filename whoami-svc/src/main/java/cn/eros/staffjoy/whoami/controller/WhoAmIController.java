package cn.eros.staffjoy.whoami.controller;

import cn.eros.staffjoy.common.auth.AuthConstant;
import cn.eros.staffjoy.common.auth.AuthContext;
import cn.eros.staffjoy.common.auth.Authorize;
import cn.eros.staffjoy.whoami.dto.FindWhoAmIResponse;
import cn.eros.staffjoy.whoami.dto.GetIntercomSettingResponse;
import cn.eros.staffjoy.whoami.dto.IAmDto;
import cn.eros.staffjoy.whoami.dto.IntercomSettingsDto;
import cn.eros.staffjoy.whoami.service.WhoAmIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 周光兵
 * @date 2021/10/18 22:45
 */
@RestController
@RequestMapping("/v1")
public class WhoAmIController {
    @Autowired
    private WhoAmIService whoAmIService;

    @GetMapping
    @Authorize({
        AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
        AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public FindWhoAmIResponse findWhoAmI() {
        String userId = AuthContext.getUserId();
        IAmDto iAmDto = this.whoAmIService.findWhoIAm(userId);

        String authz = AuthContext.getAuthz();
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(authz)) {
            iAmDto.setSupport(true);
        }

        return new FindWhoAmIResponse(iAmDto);
    }

    @GetMapping("/intercom")
    @Authorize({
        AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
        AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public GetIntercomSettingResponse getIntercomSettings() {
        String userId = AuthContext.getUserId();
        IntercomSettingsDto intercomSettingsDto = this.whoAmIService.findIntercomSettings(userId);

        return new GetIntercomSettingResponse(intercomSettingsDto);
    }
}
