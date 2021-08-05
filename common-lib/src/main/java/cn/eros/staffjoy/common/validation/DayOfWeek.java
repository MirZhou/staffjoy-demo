package cn.eros.staffjoy.common.validation;

import javax.validation.Constraint;
import java.lang.annotation.*;

/**
 * @author 周光兵
 * @date 2021/8/5 23:06
 */
@Documented
@Constraint(validatedBy = DayOfWeekValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DayOfWeek {
    String message() default "Unknown day of week";

    Class[] groups() default {};

    Class[] payload() default {};
}
