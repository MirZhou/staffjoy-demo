package cn.eros.staffjoy.faraday.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

/**
 * @author 周光兵
 * @date 2021/7/27 13:34
 */
@ConfigurationProperties("faraday")
public class FaradayProperties {
    /**
     * Faraday servlet filter order.
     */
    private int filterOrder = HIGHEST_PRECEDENCE + 100;
    /**
     * Enable programmatic mapping or not,
     * false only in dev environment, in dev we use mapping via configuration file
     */
    private boolean enableProgrammaticMapping = true;
    /**
     * Properties responsible for collecting metrics during HTTP requests forwarding.
     */
    @NestedConfigurationProperty
    private MetricsProperties metrics = new MetricsProperties();
    /**
     * Properties responsible for tracing HTTP requests proxying processes;
     */
    @NestedConfigurationProperty
    private TracingProperties tracing = new TracingProperties();

    public int getFilterOrder() {
        return filterOrder;
    }

    public void setFilterOrder(int filterOrder) {
        this.filterOrder = filterOrder;
    }

    public boolean isEnableProgrammaticMapping() {
        return enableProgrammaticMapping;
    }

    public void setEnableProgrammaticMapping(boolean enableProgrammaticMapping) {
        this.enableProgrammaticMapping = enableProgrammaticMapping;
    }

    public MetricsProperties getMetrics() {
        return metrics;
    }

    public void setMetrics(MetricsProperties metrics) {
        this.metrics = metrics;
    }

    public TracingProperties getTracing() {
        return tracing;
    }

    public void setTracing(TracingProperties tracing) {
        this.tracing = tracing;
    }
}
