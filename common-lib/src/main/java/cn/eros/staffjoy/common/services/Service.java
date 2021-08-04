package cn.eros.staffjoy.common.services;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Service is an app on Staffjoy that runs on a subdomain
 *
 * @author 周光兵
 * @date 2021/8/4 22:11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Service {
    /**
     * Public, Authenticated, or Admin
     */
    private int security;
    /**
     * If true, service is suppressed in stage and prod
     */
    private boolean restrictDev;
    /**
     * Backend service to query
     */
    private String backendDomain;
    /**
     * If true, injects a header for HTML responses telling the browser not to cache HTML
     */
    private boolean noCacheHtml;
}
