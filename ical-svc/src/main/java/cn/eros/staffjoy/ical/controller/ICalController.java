package cn.eros.staffjoy.ical.controller;

import cn.eros.staffjoy.ical.model.Cal;
import cn.eros.staffjoy.ical.service.ICalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.nio.charset.StandardCharsets;

/**
 * @author 周光兵
 * @date 2021/10/14 09:56
 */
@Controller
public class ICalController {
    @Autowired
    private ICalService iCalService;

    @GetMapping("/{user_id}.ics")
    public @ResponseBody
    HttpEntity<byte[]> getCalByUserId(@PathVariable(value = "user_id") String userId) {
        Cal cal = iCalService.getCalByUserId(userId);

        byte[] calBytes = cal.build().getBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "calendar", StandardCharsets.UTF_8));
        headers.set("Content-Disposition", "attachment; filename=" + userId + ".ics");
        headers.setContentLength(calBytes.length);

        return new HttpEntity<>(calBytes, headers);
    }
}
