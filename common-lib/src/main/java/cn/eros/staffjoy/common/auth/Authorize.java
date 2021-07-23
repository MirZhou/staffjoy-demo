package cn.eros.staffjoy.common.auth;

import java.lang.annotation.*;

/**
 * @author 周光兵
 * @date 2021/7/23 13:40
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Authorize {
    String[] value();
}
