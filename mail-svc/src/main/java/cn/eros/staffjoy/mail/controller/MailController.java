package cn.eros.staffjoy.mail.controller;

import cn.eros.staffjoy.common.api.BaseResponse;
import cn.eros.staffjoy.mail.dto.EmailRequest;
import cn.eros.staffjoy.mail.service.MailSendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * <p>create time：2021-08-16 08:48
 *
 * @author Eros
 */
@RestController
@RequestMapping("/v1")
@Validated
public class MailController {
    @Autowired
    private MailSendService mailSendService;

    @PostMapping("/send")
    public BaseResponse send(@RequestBody @Valid EmailRequest request) {
        this.mailSendService.syncSendEmail(request);

        return new BaseResponse() {{
            setMessage("email has been sent async.");
        }};
    }
}
