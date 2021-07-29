package cn.eros.staffjoy.faraday.core.interceptor;

import cn.eros.staffjoy.faraday.config.MappingProperties;
import cn.eros.staffjoy.faraday.core.http.ResponseData;

/**
 * @author 周光兵
 * @date 2021/7/28 22:57
 */
public interface PostForwardResponseInterceptor {
    void intercept(ResponseData data, MappingProperties mapping);
}
