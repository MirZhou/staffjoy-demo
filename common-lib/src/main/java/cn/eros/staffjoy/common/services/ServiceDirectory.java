package cn.eros.staffjoy.common.services;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author 周光兵
 * @date 2021/8/4 22:17
 */
public class ServiceDirectory {
    private static final Map<String, Service> SERVICE_MAP;

    static {
        Map<String, Service> map = new TreeMap<>();

        Service service = Service.builder()
            .security(SecurityConstant.SEC_AUTHENTICATED)
            .restrictDev(false)
            .backendDomain("account-service")
            .build();
        map.put("account", service);

        service = Service.builder()
            .security(SecurityConstant.SEC_AUTHENTICATED)
            .restrictDev(false)
            .backendDomain("app-service")
            .noCacheHtml(true)
            .build();
        map.put("app", service);

        service = Service.builder()
            .security(SecurityConstant.SEC_AUTHENTICATED)
            .restrictDev(false)
            .backendDomain("company-service")
            .build();
        map.put("company", service);

        service = Service.builder()
            // Debug site for faraday proxy
            .security(SecurityConstant.SEC_PUBLIC)
            .restrictDev(true)
            .backendDomain("httpbin.org")
            .build();
        map.put("faraday", service);

        service = Service.builder()
            .security(SecurityConstant.SEC_PUBLIC)
            .restrictDev(false)
            .backendDomain("ical-service")
            .build();
        map.put("ical", service);

        service = Service.builder()
            .security(SecurityConstant.SEC_AUTHENTICATED)
            .restrictDev(false)
            .backendDomain("myaccount-service")
            .noCacheHtml(true)
            .build();
        map.put("myaccount", service);

        service = Service.builder()
            .security(SecurityConstant.SEC_AUTHENTICATED)
            .restrictDev(true)
            .backendDomain("superpowers-service")
            .build();
        map.put("superpowers", service);

        service = Service.builder()
            .security(SecurityConstant.SEC_AUTHENTICATED)
            .restrictDev(false)
            .backendDomain("whoami-service")
            .build();
        map.put("whoami-service", service);

        service = Service.builder()
            .security(SecurityConstant.SEC_PUBLIC)
            .restrictDev(false)
            .backendDomain("www-service")
            .build();
        map.put("www", service);

        SERVICE_MAP = Collections.unmodifiableMap(map);
    }

    public static Map<String, Service> getMapping() {
        return SERVICE_MAP;
    }
}
