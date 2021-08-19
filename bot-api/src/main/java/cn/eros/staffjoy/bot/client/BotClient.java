package cn.eros.staffjoy.bot.client;

import cn.eros.staffjoy.bot.BotConstant;
import cn.eros.staffjoy.bot.dto.*;
import cn.eros.staffjoy.common.api.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
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

    @PostMapping("/onboard_worker")
    BaseResponse onboardWorker(@RequestBody @Validated OnboardWorkerRequest request);

    @PostMapping("/alert_new_shift")
    BaseResponse alertNewShift(@RequestBody @Validated AlertNewShiftRequest request);

    @PostMapping("/alert_new_shifts")
    BaseResponse alertNewShifts(@RequestBody @Validated AlertNewShiftsRequest request);

    @PostMapping("/alert_removed_shift")
    BaseResponse alertRemovedShift(@RequestBody @Validated AlertRemovedShiftRequest request);

    @PostMapping("/alert_removed_shifts")
    BaseResponse alertRemovedShifts(@RequestBody @Validated AlertRemovedShiftsRequest request);

    @PostMapping("/alert_changed_shift")
    BaseResponse alertChangedShift(@RequestBody @Validated AlertChangedShiftRequest request);
}
