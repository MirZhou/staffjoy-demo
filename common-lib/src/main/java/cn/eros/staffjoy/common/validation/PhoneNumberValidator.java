package cn.eros.staffjoy.common.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author 周光兵
 * @date 2021/7/21 00:17
 */
public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            // can be null
            return true;
        }

        return value.matches("[0-9]+")
            && (value.length() > 8) && (value.length() < 14);
    }
}
