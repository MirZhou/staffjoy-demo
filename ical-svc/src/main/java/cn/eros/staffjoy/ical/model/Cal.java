package cn.eros.staffjoy.ical.model;

import cn.eros.staffjoy.company.dto.ShiftDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author 周光兵
 * @date 2021/10/14 09:59
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cal {
    static final String CAL_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(CAL_DATE_PATTERN)
        .withZone(ZoneId.systemDefault());

    private String companyName;
    private List<ShiftDto> shiftList;

    public String getHeader() {
        return "BEGIN:VCALENDAR\r\n" +
            "METHOD:PUBLISH\r\n" +
            "VERSION:2.0\r\n" +
            "PRODID:-//Staffjoy//Staffjoy ICal Service//zh-CN\r\n";
    }

    public String getBody() {
        StringBuilder body = new StringBuilder();
        for (ShiftDto shiftDto : shiftList) {
            body.append("BEGIN:VEVENT\r\n");
            body.append("ORGANIZER;CN=Engineering:MAILTO:support@staffjoy.xyz\r\n");
            body.append("SUMMARY: Work at ").append(this.companyName).append("\r\n");
            body.append("UID:").append(shiftDto.getUserId()).append("\r\n");
            body.append("STATUS:CONFIRMED\r\n");
            body.append("DTSTART:").append(getCalDateFormat(shiftDto.getStart())).append("\r\n");
            body.append("DTEND:").append(getCalDateFormat(shiftDto.getStop())).append("\r\n");
            body.append("DTSTAMP:").append(getCalDateFormat(Instant.now())).append("\r\n");
            body.append("LAST-MODIFIED:").append(getCalDateFormat(Instant.now())).append("\r\n");
            body.append("LOCATION:  ").append(this.companyName).append("\r\n");
            body.append("END:VEVENT\r\n");
        }

        return body.toString();
    }

    public String getFooter() {
        return "END:VCALENDAR";
    }

    /**
     * Build concats an ical header/body/footer together
     *
     * @return information
     */
    public String build() {
        return this.getHeader() + this.getBody() + this.getFooter();
    }

    private String getCalDateFormat(Instant dt) {
        return DATE_TIME_FORMATTER.format(dt);
    }
}
