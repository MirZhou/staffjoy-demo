package cn.eros.staffjoy.mail.client;

import cn.eros.staffjoy.common.api.BaseResponse;
import cn.eros.staffjoy.mail.MailConstant;
import cn.eros.staffjoy.mail.dto.EmailRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

/**
 * <p>create time：2021-08-14 21:45
 *
 * @author Eros
 */
@FeignClient(name = MailConstant.SERVICE_NAME, path = "/v1", url = "${staffjoy.email-service-endpoint}")
public interface MailClient {
    @PostMapping("/send")
    BaseResponse send(@RequestBody @Valid EmailRequest request);
}
