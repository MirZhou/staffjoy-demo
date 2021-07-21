package cn.eros.staffjoy.account.dto;

import cn.eros.staffjoy.common.validation.PhoneNumber;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Email;

/**
 * @author 周光兵
 * @date 2021/7/21 00:44
 */
@Data
@NoArgsConstructor
@Builder
public class GetOrCreateRequest {
    private String name;
    @Email(message = "Invalid email")
    private String email;
    @PhoneNumber
    private String phoneNumber;

    @AssertTrue(message = "Empty request")
    private boolean isValidRequest() {
        return StringUtils.hasText(name) || StringUtils.hasText(email) || StringUtils.hasText(phoneNumber);
    }

}
