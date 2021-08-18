package cn.eros.staffjoy.bot.service;

import cn.eros.staffjoy.account.dto.AccountDto;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Eros
 * @date 2021-08-18 13:46
 */
@Service
public class GreetingService {
    private static final ILogger LOGGER = SLoggerFactory.getLogger(GreetingService.class);

    @Autowired
    private HelperService helperService;

    public void greeting(String userId) {
        AccountDto account = this.helperService.getAccountById(userId);

        DispatchPreference dispatchPreference = this.helperService.getPreferredDispatch(account);

        switch (dispatchPreference) {
            case DISPATCH_SMS:
                helperService.smsGreetingAsync(account.getPhoneNumber());
                break;
            case DISPATCH_EMAIL:
                this.helperService.mailGreetingAsync(account);
                break;
            default:
                LOGGER.info("Unable to send greeting to user %s - no comm method found", userId);
                break;
        }
    }
}
