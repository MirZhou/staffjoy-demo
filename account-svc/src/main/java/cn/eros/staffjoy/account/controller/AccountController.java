package cn.eros.staffjoy.account.controller;

import cn.eros.staffjoy.account.dto.*;
import cn.eros.staffjoy.account.service.AccountService;
import cn.eros.staffjoy.common.api.BaseResponse;
import cn.eros.staffjoy.common.auth.AuthConstant;
import cn.eros.staffjoy.common.auth.AuthContext;
import cn.eros.staffjoy.common.auth.Authorize;
import cn.eros.staffjoy.common.auth.PermissionDeniedException;
import cn.eros.staffjoy.common.env.EnvConfig;
import cn.eros.staffjoy.common.env.EnvConstant;
import cn.eros.staffjoy.common.error.ServiceException;
import cn.eros.staffjoy.common.validation.PhoneNumber;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

/**
 * @author 周光兵
 * @date 2021/7/29 22:27
 */
@RestController
@RequestMapping("/v1/account")
@Validated
public class AccountController {
    private static final ILogger LOGGER = SLoggerFactory.getLogger(AccountController.class);

    @Autowired
    private AccountService accountService;

    @Autowired
    private EnvConfig envConfig;

    @PostMapping("/create")
    @Authorize({
            AuthConstant.AUTHORIZATION_SUPPORT_USER,
            AuthConstant.AUTHORIZATION_WWW_SERVICE,
            AuthConstant.AUTHORIZATION_COMPANY_SERVICE
    })
    public GenericAccountResponse createAccount(@RequestBody @Valid CreateAccountRequest request) {
        AccountDto accountDto = this.accountService.create(request.getName(), request.getEmail(), request.getPhoneNumber());

        return new GenericAccountResponse(accountDto);
    }

    @PostMapping(path = "/track_event")
    public BaseResponse trackEvent(@RequestBody @Valid TrackEventRequest request) {
        this.accountService.trackEvent(request.getUserid(), request.getEvent());

        return BaseResponse.builder()
                .message("event tracked")
                .build();
    }

    @PostMapping("/sync_user")
    public BaseResponse syncUser(@RequestBody @Valid SyncUserRequest request) {
        this.accountService.syncUser(request.getUserid());

        return BaseResponse.builder()
                .message("user synced")
                .build();
    }

    @GetMapping("/list")
    @Authorize({
            AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public ListAccountResponse listAccounts(@RequestParam int offset,
                                            @RequestParam @Min(0) int limit) {
        AccountList accountList = this.accountService.list(offset, limit);

        return new ListAccountResponse(accountList);
    }

    @PostMapping("/get_or_create")
    @Authorize({
        AuthConstant.AUTHORIZATION_SUPPORT_USER,
        AuthConstant.AUTHORIZATION_WWW_SERVICE,
        AuthConstant.AUTHORIZATION_COMPANY_SERVICE
    })
    public GenericAccountResponse getOrCreate(@RequestBody @Valid GetOrCreateRequest request) {
        AccountDto accountDto = this.accountService.getOrCreate(request.getName(), request.getEmail(), request.getPhoneNumber());

        return new GenericAccountResponse(accountDto);
    }

    @GetMapping("/get")
    @Authorize({
            AuthConstant.AUTHORIZATION_WWW_SERVICE,
            AuthConstant.AUTHORIZATION_ACCOUNT_SERVICE,
            AuthConstant.AUTHORIZATION_COMPANY_SERVICE,
            AuthConstant.AUTHORIZATION_WHOAMI_SERVICE,
            AuthConstant.AUTHORIZATION_BOT_SERVICE,
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
            AuthConstant.AUTHORIZATION_SUPPORT_USER,
            AuthConstant.AUTHORIZATION_SUPERPOWERS_SERVICE
    })
    public GenericAccountResponse getAccount(@RequestParam @NotBlank String userid) {
        this.validateAuthenticatedUser(userid);
        this.validateEnv();

        AccountDto accountDto = this.accountService.get(userid);

        return new GenericAccountResponse(accountDto);
    }

    @PutMapping("/update")
    @Authorize({
            AuthConstant.AUTHORIZATION_WWW_SERVICE,
            AuthConstant.AUTHORIZATION_COMPANY_SERVICE,
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
            AuthConstant.AUTHORIZATION_SUPPORT_USER,
            AuthConstant.AUTHORIZATION_SUPERPOWERS_SERVICE
    })
    public GenericAccountResponse updateAccount(@RequestBody @Valid AccountDto newAccountDto) {
        this.validateAuthenticatedUser(newAccountDto.getId());
        this.validateEnv();

        AccountDto accountDto = this.accountService.update(newAccountDto);

        return new GenericAccountResponse(accountDto);
    }

    @GetMapping("/get_account_by_phonenumber")
    @Authorize({
            AuthConstant.AUTHORIZATION_SUPPORT_USER,
            AuthConstant.AUTHORIZATION_WWW_SERVICE,
            AuthConstant.AUTHORIZATION_COMPANY_SERVICE
    })
    public GenericAccountResponse getAccountByPhoneNumber(@RequestParam @PhoneNumber String phoneNumber) {
        AccountDto accountDto = this.accountService.getAccountByPhoneNumber(phoneNumber);

        return new GenericAccountResponse(accountDto);
    }

    @PutMapping("/update_password")
    @Authorize({
            AuthConstant.AUTHORIZATION_WWW_SERVICE,
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
            AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public BaseResponse updatePassword(@RequestBody @Valid UpdatePasswordRequest request) {
        this.validateAuthenticatedUser(request.getUserid());

        this.accountService.updatePassword(request.getUserid(), request.getPassword());

        return new BaseResponse() {
            {
                setMessage("password updated");
            }
        };
    }

    @PostMapping("/verify_password")
    @Authorize({
        AuthConstant.AUTHORIZATION_WWW_SERVICE,
        AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public GenericAccountResponse verifyPassword(@RequestBody @Valid VerifyPasswordRequest request) {
        AccountDto accountDto = this.accountService.verifyPassword(request.getEmail(), request.getPassword());

        return new GenericAccountResponse(accountDto);
    }

    @PostMapping("/request_password_reset")
    @Authorize({
            AuthConstant.AUTHORIZATION_WWW_SERVICE,
            AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public BaseResponse requestPasswordReset(@RequestBody @Valid PasswordResetRequest request) {
        this.accountService.requestPasswordReset(request.getEmail());

        return new BaseResponse() {{
            setMessage("password reset requested");
        }};
    }

    @PostMapping("/request_email_change")
    @Authorize({
            AuthConstant.AUTHORIZATION_AUTHENTICATED_USER,
            AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public BaseResponse requestEmailChange(@RequestBody @Valid EmailChangeRequest request) {
        this.validateAuthenticatedUser(request.getUserid());

        this.accountService.requestEmailChange(request.getUserid(), request.getEmail());

        return new BaseResponse() {{
            setMessage("email change requested");
        }};
    }

    @PostMapping("/change_email")
    @Authorize({
            AuthConstant.AUTHORIZATION_WWW_SERVICE,
            AuthConstant.AUTHORIZATION_SUPPORT_USER
    })
    public BaseResponse changeEmail(@RequestBody @Valid EmailConfirmationRequest request) {
        this.accountService.changeEmailAndActivateAccount(request.getUserid(), request.getEmail());

        return new BaseResponse() {{
            setMessage("email change requested");
        }};
    }

    private void validateAuthenticatedUser(String userId) {
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            String currentUserId = AuthContext.getUserId();

            if (StringUtils.isEmpty(currentUserId)) {
                throw new ServiceException("Failed to find current user id");
            }

            if (!userId.equals(currentUserId)) {
                throw new PermissionDeniedException("You do not have access to this service");
            }
        }
    }

    private void validateEnv() {
        if (AuthConstant.AUTHORIZATION_SUPERPOWERS_SERVICE.equals(AuthContext.getAuthz())) {
            if (!EnvConstant.ENV_DEV.equals(this.envConfig.getName())) {
                LOGGER.warn("Development service trying to connect outside development environment");
                throw new PermissionDeniedException("This service is not available outside development environments");
            }
        }
    }

}
