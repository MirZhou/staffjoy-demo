package cn.eros.staffjoy.mail.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * <p>create time：2021-08-14 21:29
 *
 * @author Eros
 */
@Data
@Builder
public class EmailRequest {
    @NotBlank(message = "Please provide an email")
    private String to;
    @NotBlank(message = "Please provide a subject")
    private String subject;
    @NotBlank(message = "Please provide a verify body")
    @JsonProperty("html_body")
    private String htmlBody;
    private String name;
}
