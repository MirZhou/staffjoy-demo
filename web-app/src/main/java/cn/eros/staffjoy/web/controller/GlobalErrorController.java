package cn.eros.staffjoy.web.controller;

import cn.eros.staffjoy.common.config.StaffjoyProps;
import cn.eros.staffjoy.common.env.EnvConfig;
import cn.eros.staffjoy.web.view.Constant;
import cn.eros.staffjoy.web.view.error.ErrorPage;
import cn.eros.staffjoy.web.view.error.ErrorPageFactory;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import io.sentry.SentryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * @author 周光兵
 * @date 2021/7/21 13:50
 */
@Controller
@SuppressWarnings(value = "Duplicates")
public class GlobalErrorController implements ErrorController {
    static final ILogger logger = SLoggerFactory.getLogger(GlobalErrorController.class);

    @Autowired
    private ErrorPageFactory errorPageFactory;
    @Autowired
    private SentryClient sentryClient;
    @Autowired
    private StaffjoyProps staffjoyProps;
    @Autowired
    private EnvConfig envConfig;

    @Override
    public String getErrorPath() {
        return "/error";
    }

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        ErrorPage errorPage;

        if (statusCode != null && (Integer) statusCode == HttpStatus.NOT_FOUND.value()) {
            errorPage = this.errorPageFactory.buildNotFoundPage();
        } else {
            errorPage = this.errorPageFactory.buildInternalServerErrorPage();
        }

        if (exception != null) {
            if (this.envConfig.isDebug()) {
                logger.error("Global error handling", exception);
            } else {
                this.sentryClient.sendException((Exception) exception);

                UUID uuid = this.sentryClient.getContext().getLastEventId();

                errorPage.setSentryErrorId(uuid.toString());
                errorPage.setSentryPublicDsn(this.staffjoyProps.getSentryDsn());

                logger.warn("Reported error to sentry", "id", uuid.toString(), "error", exception);
            }
        }

        model.addAttribute(Constant.ATTRIBUTE_NAME_PAGE, errorPage);

        return "error";
    }
}
