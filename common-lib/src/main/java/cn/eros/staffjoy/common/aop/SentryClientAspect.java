package cn.eros.staffjoy.common.aop;

import cn.eros.staffjoy.common.env.EnvConfig;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author 周光兵
 * @date 2021/7/22 13:32
 */
@Aspect
@Slf4j
public class SentryClientAspect {
    @Autowired
    private EnvConfig envConfig;

    @Around("execution(* io.sentry.SentryClient.send*(..))")
    public void around(ProceedingJoinPoint joinPoint) throws Throwable {
        if (this.envConfig.isDebug()) {
            log.debug("no sentry logging in debug mode");
            return;
        }

        joinPoint.proceed();
    }
}
