package cn.eros.staffjoy.common.validation;

import javax.validation.Constraint;

import com.auth0.jwt.interfaces.Payload;

import java.lang.annotation.*;

/**
 * @author 周光兵
 * @date 2021/8/5 23:11
 */
@Documented
@Constraint(validatedBy = TimezoneValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Timezone {
    String message() default "Invalid timezone";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
