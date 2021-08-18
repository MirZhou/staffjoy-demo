package cn.eros.staffjoy.bot.service;

import cn.eros.staffjoy.account.client.AccountClient;
import cn.eros.staffjoy.account.dto.AccountDto;
import cn.eros.staffjoy.bot.props.AppProps;
import cn.eros.staffjoy.common.env.EnvConfig;
import cn.eros.staffjoy.company.client.CompanyClient;
import cn.eros.staffjoy.mail.client.MailClient;
import io.sentry.SentryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Eros
 * @date 2021-08-18 13:47
 */
@Service
public class HelperService {

    @Autowired
    private EnvConfig envConfig;

    @Autowired
    private MailClient mailClient;
    @Autowired
    private AccountClient accountClient;
    @Autowired
    private CompanyClient companyClient;

    @Autowired
    private AppProps appProps;

    @Autowired
    private SentryClient sentryClient;

    private static final String[] standardGreetings = {
            "Hi %s!",
            "Hey %s -",
            "Hello %s.",
            "Hey. %s -!",
    };

    public AccountDto getAccountById(String userId) {
        return null;
    }

    public DispatchPreference getPreferredDispatch(AccountDto account) {
        return null;
    }
}
