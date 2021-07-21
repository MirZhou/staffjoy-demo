package cn.eros.staffjoy.account.client;

import cn.eros.staffjoy.account.AccountConstant;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author 周光兵
 * @date 2021/7/20 23:41
 */
@FeignClient(name = AccountConstant.SERVICE_NAME, path = "/v1/account", url = "${staffjoy.account-service-endpoint")
public interface AccountClient {
}
