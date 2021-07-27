package cn.eros.staffjoy.faraday.core.balancer;

import java.util.List;

/**
 * @author 周光兵
 * @date 2021/7/27 22:28
 */
public interface LoadBalancer {
    String chooseDestination(List<String> destinations);
}
