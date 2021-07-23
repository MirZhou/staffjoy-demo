package cn.eros.staffjoy.common.async;

import org.springframework.core.task.TaskDecorator;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * 装饰器：为异步任务添加上下文
 *
 * @author 周光兵
 * @date 2021/7/23 13:23
 */
public class ContextCopyingDecorator implements TaskDecorator {
    @Override
    public Runnable decorate(Runnable runnable) {
        RequestAttributes context = RequestContextHolder.currentRequestAttributes();

        return () -> {
            try {
                RequestContextHolder.setRequestAttributes(context);

                runnable.run();
            } finally {
                RequestContextHolder.resetRequestAttributes();
            }
        };
    }
}
