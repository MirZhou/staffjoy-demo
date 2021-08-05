package cn.eros.staffjoy.common.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;

/**
 * @author 周光兵
 * @date 2021/8/5 23:08
 */
public class DayOfWeekValidator implements ConstraintValidator<DayOfWeek, String> {
    private final List<String> daysOfWeek =
        Arrays.asList("sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            // can be null
            return true;
        }

        return daysOfWeek.contains(value.trim().toLowerCase());
    }
}
