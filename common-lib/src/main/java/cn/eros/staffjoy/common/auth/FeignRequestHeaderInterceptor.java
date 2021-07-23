package cn.eros.staffjoy.common.auth;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.util.StringUtils;

/**
 * @author 周光兵
 * @date 2021/7/23 13:44
 */
public class FeignRequestHeaderInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        String userId = AuthContext.getUserId();

        if (!StringUtils.isEmpty(userId)) {
            requestTemplate.header(AuthConstant.CURRENT_USER_HEADER, userId);
        }
    }
}
