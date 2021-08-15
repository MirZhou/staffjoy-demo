package cn.eros.staffjoy.bot.client;

import cn.eros.staffjoy.bot.BotConstant;
import cn.eros.staffjoy.bot.dto.GreetingRequest;
import cn.eros.staffjoy.common.api.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

/**
 * <p>create time：2021-08-15 10:14
 *
 * @author Eros
 */
@FeignClient(name = BotConstant.SERVICE_NAME, path = "/v1", url = "${staffjoy.bot-service-endpoint}")
public interface BotClient {
    @PostMapping("/sms_greeting")
    BaseResponse sendSmsGreeting(@RequestBody @Valid GreetingRequest request);
}
