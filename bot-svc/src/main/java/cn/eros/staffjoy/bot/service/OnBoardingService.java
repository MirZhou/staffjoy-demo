package cn.eros.staffjoy.bot.service;

import cn.eros.staffjoy.account.dto.AccountDto;
import cn.eros.staffjoy.bot.dto.OnboardWorkerRequest;
import cn.eros.staffjoy.company.dto.CompanyDto;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author 周光兵
 * @date 2021/8/19 20:51
 */
@Service
public class OnBoardingService {
    private static final ILogger LOGGER = SLoggerFactory.getLogger(OnBoardingService.class);

    @Autowired
    private HelperService helperService;

    public void onboardWorker(OnboardWorkerRequest request) {
        AccountDto account = this.helperService.getAccountById(request.getUserId());
        CompanyDto company = this.helperService.getCompanyById(request.getCompanyId());

        DispatchPreference dispatchPreference = this.helperService.getPreferredDispatch(account);

        switch (dispatchPreference) {
            case DISPATCH_SMS:
                this.helperService.smsOnboardAsync(account, company);
                break;
            case DISPATCH_EMAIL:
                this.helperService.mailOnBoardAsync(account, company);
                break;
            default:
                LOGGER.info("Unable to onboard user %s - no comm method found", request.getUserId());
        }
    }
}
