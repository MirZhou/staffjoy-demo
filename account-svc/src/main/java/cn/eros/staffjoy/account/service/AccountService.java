package cn.eros.staffjoy.account.service;

import cn.eros.staffjoy.account.AccountConstant;
import cn.eros.staffjoy.account.dto.AccountDto;
import cn.eros.staffjoy.account.dto.AccountList;
import cn.eros.staffjoy.account.model.Account;
import cn.eros.staffjoy.account.model.AccountSecret;
import cn.eros.staffjoy.account.props.AppProps;
import cn.eros.staffjoy.account.repo.AccountRepo;
import cn.eros.staffjoy.account.repo.AccountSecretRepo;
import cn.eros.staffjoy.account.service.helper.ServiceHelper;
import cn.eros.staffjoy.common.api.BaseResponse;
import cn.eros.staffjoy.common.api.ResultCode;
import cn.eros.staffjoy.common.auditlog.LogEntry;
import cn.eros.staffjoy.common.auth.AuthConstant;
import cn.eros.staffjoy.common.auth.AuthContext;
import cn.eros.staffjoy.common.crypto.Sign;
import cn.eros.staffjoy.common.env.EnvConfig;
import cn.eros.staffjoy.common.error.ServiceException;
import cn.eros.staffjoy.common.utils.Helper;
import cn.eros.staffjoy.mail.client.MailClient;
import cn.eros.staffjoy.mail.dto.EmailRequest;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author 周光兵
 * @date 2021/7/29 22:34
 */
@Service
@RequiredArgsConstructor
public class AccountService {
    private static final ILogger LOGGER = SLoggerFactory.getLogger(AccountService.class);

    private final AccountRepo accountRepo;
    private final AccountSecretRepo accountSecretRepo;

    private final ServiceHelper serviceHelper;

    private final AppProps appProps;

    private final EnvConfig envConfig;

    private final MailClient mailClient;

    private final PasswordEncoder passwordEncoder;

    private final ModelMapper modelMapper;

    @PersistenceContext
    private EntityManager entityManager;

    public AccountDto getOrCreate(String name, String email, String phoneNumber) {
        Account existingAccount = null;

        if (StringUtils.hasText(email)) {
            existingAccount = this.accountRepo.findAccountByEmail(email);
        }

        if (existingAccount == null && StringUtils.hasText(phoneNumber)) {
            existingAccount = this.accountRepo.findAccountByPhoneNumber(phoneNumber);
        }

        if (existingAccount != null) {
            return this.convertToDto(existingAccount);
        }

        return this.create(name, email, phoneNumber);
    }

    public AccountDto getAccountByPhoneNumber(String phoneNumber) {
        Account account = this.accountRepo.findAccountByPhoneNumber(phoneNumber);

        if (account == null) {
            throw new ServiceException(ResultCode.NOT_FOUND, "User with specified phone number not found.");
        }

        return this.convertToDto(account);
    }

    public AccountDto create(String name, String email, String phoneNumber) {
        if (StringUtils.hasText(email)) {
            // Check to see if account exists
            Account foundAccount = this.accountRepo.findAccountByEmail(email);

            if (foundAccount != null) {
                throw new ServiceException("A user with that email already exists. Try a password set");
            }
        }

        if (StringUtils.hasText(phoneNumber)) {
            Account foundAccount = this.accountRepo.findAccountByPhoneNumber(phoneNumber);

            if (foundAccount != null) {
                throw new ServiceException("A user with that phone number already exists. Try a password set");
            }
        }

        if (name == null) {
            name = "";
        }

        if (email == null) {
            email = "";
        }

        if (phoneNumber == null) {
            phoneNumber = "";
        }

        Account account = Account.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        account.setMemberSince(Instant.now());

        try {
            this.accountRepo.save(account);
        } catch (Exception e) {
            String errMsg = "Cloud not create user account";
            throw new ServiceException(errMsg, e);
        }

        return null;
    }

    public AccountList list(int offset, int limit) {
        if (limit <= 0) {
            limit = 10;
        }

        Pageable pageRequest = PageRequest.of(offset, limit);
        Page<Account> accountPage = accountRepo.findAll(pageRequest);

        List<AccountDto> accountDtoList = accountPage.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return AccountList.builder()
                .limit(limit)
                .offset(offset)
                .accounts(accountDtoList)
                .build();
    }

    public AccountDto get(String userid) {
        Optional<Account> account = this.accountRepo.findById(userid);

        if (!account.isPresent()) {
            throw new ServiceException(String.format("User with id %s not found", userid));
        }

        return this.convertToDto(account.get());
    }

    public AccountDto update(AccountDto newAccountDto) {
        Account newAccount = this.convertToModel(newAccountDto);

        Optional<Account> existingAccount = this.accountRepo.findById(newAccount.getId());
        if (!existingAccount.isPresent()) {
            throw new ServiceException(ResultCode.NOT_FOUND, String.format("User with id %s not found", newAccount.getId()));
        }

        entityManager.detach(existingAccount.get());

        if (!this.serviceHelper.isAlmostSameInstant(newAccount.getMemberSince(), existingAccount.get().getMemberSince())) {
            throw new ServiceException(ResultCode.REQ_REJECT, "You cannot modify the member_since date");
        }

        if (StringUtils.hasText(newAccount.getEmail()) && !existingAccount.get().getEmail().equals(newAccount.getEmail())) {
            Account account = this.accountRepo.findAccountByEmail(newAccount.getEmail());

            if (account != null) {
                throw new ServiceException(ResultCode.REQ_REJECT, "A user with that email already exists. Try a password reset");
            }
        }

        if (StringUtils.hasText(newAccount.getPhoneNumber()) && !existingAccount.get().getPhoneNumber().equals(newAccount.getPhoneNumber())) {
            Account account = this.accountRepo.findAccountByPhoneNumber(newAccount.getPhoneNumber());

            if (account != null) {
                throw new ServiceException(ResultCode.REQ_REJECT, "A user with that phone number already exists. Try a password reset");
            }
        }

        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            if (!existingAccount.get().isConfirmedAndActive() && newAccount.isConfirmedAndActive()) {
                throw new ServiceException(ResultCode.REQ_REJECT, "You cannot activate this account");
            }

            if (existingAccount.get().isSupport() != newAccount.isSupport()) {
                throw new ServiceException(ResultCode.REQ_REJECT, "You cannot change the support parameter");
            }

            if (!existingAccount.get().getPhotoUrl().equals(newAccount.getPhotoUrl())) {
                throw new ServiceException(ResultCode.REQ_REJECT, "You cannot change the photo though this endpoint (see docs)");
            }

            // User can request email change - not do it
            if (!existingAccount.get().getEmail().equals(newAccount.getEmail())) {
                this.requestEmailChange(newAccount.getId(), newAccount.getEmail());
            }
        }

        newAccount.setPhotoUrl(Helper.generateGravatarUrl(newAccount.getEmail()));

        try {
            this.accountRepo.save(newAccount);
        } catch (Exception ex) {
            String errMsg = "Could not update the user account";
            this.serviceHelper.handleException(LOGGER, ex, errMsg);
            throw new ServiceException(errMsg, ex);
        }

        this.serviceHelper.syncUserAsync(newAccount.getId());

        LogEntry auditlog = LogEntry.builder()
                .authorization(AuthContext.getAuthz())
                .currentUserId(AuthContext.getUserId())
                .targetType("account")
                .targetId(newAccount.getId())
                .originalContents(existingAccount.get().toString())
                .updatedContents(newAccount.toString())
                .build();

        LOGGER.info("updated account", auditlog);

        // If account is being activated, or if phone number is changed by current user - send text
        if (newAccount.isConfirmedAndActive() &&
                StringUtils.hasText(newAccount.getPhoneNumber()) &&
                !newAccount.getPhoneNumber().equals(existingAccount.get().getPhoneNumber())) {
            this.serviceHelper.sendSmsGreeting(newAccount.getId());
        }

        this.trackEventWithAuthCheck("account_updated");

        return this.convertToDto(newAccount);
    }

    public void updatePassword(String userId, String password) {
        String pwdHash = this.passwordEncoder.encode(password);

        int affected = this.accountSecretRepo.updatePasswordHashById(pwdHash, userId);
        if (affected != 1) {
            throw new ServiceException(ResultCode.NOT_FOUND, "user with specified id not found");
        }

        LogEntry auditLog = LogEntry.builder()
                .authorization(AuthContext.getAuthz())
                .currentUserId(AuthContext.getUserId())
                .targetType("account")
                .targetId(userId)
                .build();

        LOGGER.info("updated password", auditLog);

        this.trackEventWithAuthCheck("password_updated");
    }

    public AccountDto verifyPassword(String email, String password) {
        AccountSecret accountSecret = this.accountSecretRepo.findAccountSecretByEmail(email);

        if (accountSecret == null) {
            throw new ServiceException(ResultCode.NOT_FOUND, "account with specified email not found");
        }

        if (!accountSecret.isConfirmAndActive()) {
            throw new ServiceException(ResultCode.REQ_REJECT, "This user has not confirmed their account");
        }

        if (StringUtils.isEmpty(accountSecret.getPasswordHash())) {
            throw new ServiceException(ResultCode.REQ_REJECT, "This user has not set up their password");
        }

        if (!passwordEncoder.matches(password, accountSecret.getPasswordHash())) {
            throw new ServiceException(ResultCode.UNAUTHORIZED, "Incorrect password");
        }

        Optional<Account> account = this.accountRepo.findById(accountSecret.getId());

        if (!account.isPresent()) {
            throw new ServiceException(String.format("User with id %s not found", accountSecret.getId()));
        }

        return this.convertToDto(account.get());
    }

    /**
     * RequestPasswordReset sends an email to a user with a password reset link
     *
     * @param email email address
     */
    public void requestPasswordReset(String email) {
        Account account = this.accountRepo.findAccountByEmail(email);

        if (account == null) {
            throw new ServiceException(ResultCode.NOT_FOUND, "No user with that email exists");
        }

        String subject = "Reset your Staffjoy password";
        // reset
        boolean activate = false;
        String template = AccountConstant.RESET_PASSWORD_TMPL;
        if (!account.isConfirmedAndActive()) {
            // Not actually active - make some tweaks for activate instead of password reset
            // activate
            activate = true;
            subject = "Activate your Staffjoy account";
            template = AccountConstant.ACTIVATE_ACCOUNT_TMPL;
        }

        // Send verification email
        this.sendEmail(account.getId(), email, account.getName(), subject, template, activate);
    }

    /**
     * sends an email to a user with a confirm email link
     *
     * @param userId user id
     * @param email  email address
     */
    public void requestEmailChange(String userId, String email) {
        Optional<Account> account = this.accountRepo.findById(userId);

        if (!account.isPresent()) {
            throw new ServiceException(ResultCode.NOT_FOUND, String.format("User with id %s not found", userId));
        }

        String subject = "Confirm You New Email Address";
        this.sendEmail(account.get().getId(), email, account.get().getName(), subject, AccountConstant.CONFIRM_EMAIL_TMPL, true);
    }

    /**
     * sets and account to active and updates it's email.
     * It's used after a user clicks a confirmation link in their email.
     *
     * @param userId user id
     * @param email  email address
     */
    public void changeEmailAndActivateAccount(String userId, String email) {
        int affected = this.accountRepo.updateEmailAndActivateById(email, userId);

        if (affected != 1) {
            throw new ServiceException(ResultCode.NOT_FOUND, "user with specified id not found");
        }

        this.serviceHelper.syncUserAsync(userId);

        LogEntry logEntry = LogEntry.builder()
                .authorization(AuthContext.getAuthz())
                .currentUserId(AuthContext.getUserId())
                .targetType("account")
                .targetId(userId)
                .updatedContents(email)
                .build();

        LOGGER.info("changed email", logEntry);

        this.trackEventWithAuthCheck("email_updated");
    }

    public void trackEvent(String userid, String eventName) {
        this.serviceHelper.trackEventAsync(userid, eventName);
    }

    public void syncUser(String userid) {
        this.serviceHelper.syncUserAsync(userid);
    }

    private void sendEmail(String userId, String email, String name, String subject, String template, boolean activateOrConfirm) {
        String token;
        try {
            token = Sign.generateEmailConfirmationToken(userId, email, appProps.getSigningSecret());
        } catch (Exception ex) {
            String errMsg = "Could not create token";
            this.serviceHelper.handleException(LOGGER, ex, errMsg);
            throw new ServiceException(errMsg, ex);
        }

        String pathFormat = "/activate/%s";

        if (!activateOrConfirm) {
            pathFormat = "/rest/%s";
        }

        String path = String.format(pathFormat, token);
        URI link;
        try {
            link = new URI("http", "www." + this.envConfig.getExternalApex(), path, null);
        } catch (URISyntaxException ex) {
            String errMsg = "Could not create activation url";

            if (!activateOrConfirm) {
                errMsg = "Could not create reset url";
            }

            this.serviceHelper.handleException(LOGGER, ex, errMsg);
            throw new ServiceException(errMsg, ex);
        }

        String htmlBody;
        if (activateOrConfirm) {
            // active or confirm
            htmlBody = String.format(template, name, link.toString(), link.toString(), link.toString());
        } else {
            // reset
            htmlBody = String.format(template, link.toString(), link.toString());
        }

        EmailRequest emailRequest = EmailRequest.builder()
                .to(email)
                .name(name)
                .subject(subject)
                .htmlBody(htmlBody)
                .build();

        BaseResponse baseResponse;
        try {
            baseResponse = mailClient.send(emailRequest);
        } catch (Exception ex) {
            String errMsg = "Unable to send email";
            this.serviceHelper.handleException(LOGGER, ex, errMsg);
            throw new ServiceException(errMsg, ex);
        }

        if (!baseResponse.isSuccess()) {
            this.serviceHelper.handleError(LOGGER, baseResponse.getMessage());
            throw new ServiceException(baseResponse.getMessage());
        }
    }

    private void trackEventWithAuthCheck(String eventName) {
        String userId = AuthContext.getUserId();

        if (StringUtils.isEmpty(userId)) {
            return;
        }

        this.trackEvent(userId, eventName);
    }

    private Account convertToModel(AccountDto accountDto) {
        return this.modelMapper.map(accountDto, Account.class);
    }

    private AccountDto convertToDto(Account account) {
        return this.modelMapper.map(account, AccountDto.class);
    }
}
