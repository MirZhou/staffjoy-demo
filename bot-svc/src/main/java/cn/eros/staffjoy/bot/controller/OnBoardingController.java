package cn.eros.staffjoy.bot.controller;

import cn.eros.staffjoy.bot.dto.OnboardWorkerRequest;
import cn.eros.staffjoy.bot.service.OnBoardingService;
import cn.eros.staffjoy.common.api.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 周光兵
 * @date 2021/8/19 20:41
 */
@RestController
@RequestMapping("/v1")
public class OnBoardingController {
    @Autowired
    private OnBoardingService onBoardingService;

    @PostMapping("/onboard_worker")
    public BaseResponse onboardWorker(@RequestBody @Validated OnboardWorkerRequest request) {
        this.onBoardingService.onboardWorker(request);

        return BaseResponse.builder()
            .message("onboarded worker")
            .build();
    }
}
