package cn.eros.staffjoy.sms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @author 周光兵
 * @date 2021/8/18 22:51
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SmsRequest {
    @NotBlank(message = "Please provide a phone number")
    private String to;
    @NotBlank(message = "Please provide a template code")
    private String templateCode;
    private String templateParam;
}
