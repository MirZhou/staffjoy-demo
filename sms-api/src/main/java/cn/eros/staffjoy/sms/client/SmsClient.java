package cn.eros.staffjoy.sms.client;

import cn.eros.staffjoy.common.api.BaseResponse;
import cn.eros.staffjoy.common.auth.AuthConstant;
import cn.eros.staffjoy.sms.SmsConstant;
import cn.eros.staffjoy.sms.dto.SmsRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import javax.validation.Valid;

/**
 * @author 周光兵
 * @date 2021/8/18 22:52
 */
@FeignClient(name = SmsConstant.SERVICE_NAME, path = "/v1", url = "${staffjoy.sms-service-endpoint}")
public interface SmsClient {
    @PostMapping("/queue_send")
    BaseResponse send(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Valid SmsRequest request);
}
