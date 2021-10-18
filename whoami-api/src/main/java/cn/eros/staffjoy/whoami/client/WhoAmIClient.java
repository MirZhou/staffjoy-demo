package cn.eros.staffjoy.whoami.client;

import cn.eros.staffjoy.common.auth.AuthConstant;
import cn.eros.staffjoy.whoami.WhoAmIConstant;
import cn.eros.staffjoy.whoami.dto.FindWhoAmIResponse;
import cn.eros.staffjoy.whoami.dto.GetIntercomSettingResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * @author 周光兵
 * @date 2021/10/15 16:16
 */
@FeignClient(name = WhoAmIConstant.SERVICE_NAME, path = "/v1", url = "${staffjoy.whoami-service-endpoint}")
public interface WhoAmIClient {
    @GetMapping
    FindWhoAmIResponse findWhoAmI(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz);

    @GetMapping("/intercom")
    GetIntercomSettingResponse getIntercomSettings(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz);
}
