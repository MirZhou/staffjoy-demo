package cn.eros.staffjoy.bot.controller;

import cn.eros.staffjoy.bot.dto.GreetingRequest;
import cn.eros.staffjoy.bot.service.GreetingService;
import cn.eros.staffjoy.common.api.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Eros
 * @date 2021-08-18 13:43
 */
@RestController
@RequestMapping(value = "/v1")
@Validated
public class GreetingController {
    @Autowired
    private GreetingService greetingService;

    @PostMapping("/sms_greeting")
    public BaseResponse sendSmsGreeting(@RequestBody @Validated GreetingRequest request) {
        this.greetingService.greeting(request.getUserId());

        return BaseResponse.builder()
                .message("greeting sent")
                .build();
    }
}
