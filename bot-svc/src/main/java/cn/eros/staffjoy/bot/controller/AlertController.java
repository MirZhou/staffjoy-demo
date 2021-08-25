package cn.eros.staffjoy.bot.controller;

import cn.eros.staffjoy.bot.dto.*;
import cn.eros.staffjoy.bot.service.AlertService;
import cn.eros.staffjoy.common.api.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 周光兵
 * @date 2021/8/19 22:18
 */
@RestController
@RequestMapping("/v1")
public class AlertController {
    @Autowired
    private AlertService alertService;

    @PostMapping("/alert_new_shift")
    public BaseResponse alertNewShift(@RequestBody AlertNewShiftRequest request) {
        this.alertService.alertNewShift(request);

        return BaseResponse.builder()
            .message("new shift alerted")
            .build();
    }

    @PostMapping("/alert_new_shifts")
    public BaseResponse alertNewShifts(@RequestBody AlertNewShiftsRequest request) {
        this.alertService.alertNewShifts(request);

        return BaseResponse.builder()
            .message("new shifts alerted")
            .build();
    }

    @PostMapping("/alert_removed_shift")
    public BaseResponse alertRemovedShift(@RequestBody AlertRemovedShiftRequest request) {
        this.alertService.alertRemovedShift(request);

        return BaseResponse.builder()
            .message("removed shift alerted")
            .build();
    }

    @PostMapping("/alert_removed_shifts")
    public BaseResponse alertRemovedShifts(@RequestBody AlertRemovedShiftsRequest request) {
        this.alertService.alertRemovedShifts(request);

        return BaseResponse.builder()
            .message("removed shifts alerted")
            .build();
    }

    @PostMapping("/alert_changed_shift")
    public BaseResponse alertChangedShift(@RequestBody AlertChangedShiftRequest request) {
        this.alertService.alertChangedShift(request);

        return BaseResponse.builder()
            .message("changed shifts alerted")
            .build();
    }
}
