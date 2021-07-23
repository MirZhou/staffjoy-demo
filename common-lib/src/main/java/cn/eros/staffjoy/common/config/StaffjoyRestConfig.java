package cn.eros.staffjoy.common.config;

import cn.eros.staffjoy.common.aop.SentryClientAspect;
import cn.eros.staffjoy.common.error.GlobalExceptionTranslator;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author 周光兵
 * @date 2021/7/23 13:28
 */
@Configuration
@Import(value = {StaffjoyConfig.class, SentryClientAspect.class, GlobalExceptionTranslator.class})
public class StaffjoyRestConfig {
}
