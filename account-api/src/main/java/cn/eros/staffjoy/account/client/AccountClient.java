package cn.eros.staffjoy.account.client;

import cn.eros.staffjoy.account.AccountConstant;
import cn.eros.staffjoy.account.dto.*;
import cn.eros.staffjoy.common.api.BaseResponse;
import cn.eros.staffjoy.common.auth.AuthConstant;
import cn.eros.staffjoy.common.validation.PhoneNumber;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

/**
 * @author 周光兵
 * @date 2021/7/20 23:41
 */
@FeignClient(name = AccountConstant.SERVICE_NAME, path = "/v1/account", url = "${staffjoy.account-service-endpoint")
public interface AccountClient {
    @PostMapping(path = "/create")
    GenericAccountResponse createAccount(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String auth,
                                         @RequestBody @Valid CreateAccountRequest request);

    @PostMapping(path = "/track_event")
    BaseResponse trackEvent(@RequestBody @Valid TrackEventRequest request);

    @PostMapping(path = "/sync_user")
    BaseResponse syncUser(@RequestBody @Valid SyncUserRequest request);

    @GetMapping(path = "/list")
    ListAccountResponse listAccounts(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String auth,
                                     @RequestParam int offset,
                                     @RequestParam @Min(0) int limit);

    @PostMapping(path = "/get_or_create")
    GenericAccountResponse getOrCreateAccount(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String auth,
                                              @RequestBody @Valid GetOrCreateRequest request);

    @GetMapping(path = "/get")
    GenericAccountResponse getAccount(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String auth,
                                      @RequestParam @NotBlank String userid);

    @PutMapping(path = "/update")
    GenericAccountResponse updateAccount(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String auth,
                                         @RequestBody @Valid AccountDto accountDto);

    @GetMapping(path = "/get_account_by_phonenumber")
    GenericAccountResponse getAccountByPhoneNumber(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String auth,
                                                   @RequestParam @PhoneNumber String phoneNumber);

    @PutMapping(path = "/update_password")
    BaseResponse updatePassword(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String auth,
                                @RequestBody @Valid UpdatePasswordRequest request);

    @PostMapping(path = "/verify_password")
    GenericAccountResponse verifyPassword(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String auth,
                                @RequestBody @Valid VerifyPasswordRequest request);

    @PostMapping(path = "/request_password_reset")
    BaseResponse requestPasswordReset(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String auth,
                                      @RequestBody @Valid PasswordResetRequest request);

    @PostMapping(path = "/request_email_change")
    BaseResponse requestEmailChange(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String auth,
                                    @RequestBody @Valid EmailChangeRequest request);

    @PostMapping(path = "/change_email")
    BaseResponse changeEmail(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String auth,
                             @RequestBody @Valid EmailConfirmationRequest request);
}
