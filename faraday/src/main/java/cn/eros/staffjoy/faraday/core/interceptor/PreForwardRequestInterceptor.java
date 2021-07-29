package cn.eros.staffjoy.faraday.core.interceptor;

import cn.eros.staffjoy.faraday.config.MappingProperties;
import cn.eros.staffjoy.faraday.core.http.RequestData;

/**
 * @author 周光兵
 * @date 2021/7/27 22:13
 */
public interface PreForwardRequestInterceptor {
    void intercept(RequestData data, MappingProperties mappings);
}
