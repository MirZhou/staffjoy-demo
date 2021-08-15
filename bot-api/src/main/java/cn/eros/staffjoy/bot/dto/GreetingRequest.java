package cn.eros.staffjoy.bot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * <p>create time：2021-08-15 10:11
 *
 * @author Eros
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GreetingRequest {
    @NotBlank
    private String userId;
}
