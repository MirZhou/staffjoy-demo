package cn.eros.staffjoy.faraday.core.http;

import java.net.URI;

/**
 * @author 周光兵
 * @date 2021/7/27 22:22
 */
public class ForwardDestination {
    protected final URI uri;
    protected final String mappingName;
    protected final String mappingMetricsName;

    public ForwardDestination(URI uri, String mappingName, String mappingMetricsName) {
        this.uri = uri;
        this.mappingName = mappingName;
        this.mappingMetricsName = mappingMetricsName;
    }

    public URI getUri() {
        return uri;
    }

    public String getMappingName() {
        return mappingName;
    }

    public String getMappingMetricsName() {
        return mappingMetricsName;
    }
}
