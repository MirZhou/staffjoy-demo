package cn.eros.staffjoy.bot.controller;

import cn.eros.staffjoy.bot.dto.*;
import cn.eros.staffjoy.bot.service.AlertService;
import cn.eros.staffjoy.common.api.BaseResponse;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author 周光兵
 * @date 2021/8/19 22:18
 */
@RestController
@RequestMapping("/v1")
@Validated
public class AlertController {
    private static final ILogger LOGGER = SLoggerFactory.getLogger(AlertController.class);

    @Autowired
    private AlertService alertService;

    @PostMapping("/alert_new_shift")
    public BaseResponse alertNewShift(@RequestBody @Valid AlertNewShiftRequest request) {
        LOGGER.warn("dsklajfd;sakfjdsafdsal");

        this.alertService.alertNewShift(request);

        return BaseResponse.builder()
            .message("new shift alerted")
            .build();
    }

    @PostMapping("/alert_new_shifts")
    public BaseResponse alertNewShifts(@RequestBody @Validated AlertNewShiftsRequest request) {
        this.alertService.alertNewShifts(request);

        return BaseResponse.builder()
            .message("new shifts alerted")
            .build();
    }

    @PostMapping("/alert_removed_shift")
    public BaseResponse alertRemovedShift(@RequestBody @Validated AlertRemovedShiftRequest request) {
        this.alertService.alertRemovedShift(request);

        return BaseResponse.builder()
            .message("removed shift alerted")
            .build();
    }

    @PostMapping("/alert_removed_shifts")
    public BaseResponse alertRemovedShifts(@RequestBody @Validated AlertRemovedShiftsRequest request) {
        this.alertService.alertRemovedShifts(request);

        return BaseResponse.builder()
            .message("removed shifts alerted")
            .build();
    }

    @PostMapping("/alert_changed_shift")
    public BaseResponse alertChangedShift(@RequestBody @Validated AlertChangedShiftRequest request) {
        this.alertService.alertChangedShift(request);

        return BaseResponse.builder()
            .message("changed shifts alerted")
            .build();
    }
}
