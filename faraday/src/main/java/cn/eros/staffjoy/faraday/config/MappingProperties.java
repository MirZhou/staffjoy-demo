package cn.eros.staffjoy.faraday.config;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.builder.ToStringStyle.NO_CLASS_NAME_STYLE;

/**
 * @author Eros
 * @since 2024/5/12
 */
public class MappingProperties {
    private String name;
    private String host;
    private List<String> destinations = new ArrayList<>();
    private TimeoutProperties timeout = new TimeoutProperties();
    private Map<String, Object> customConfiguration = new HashMap<>(16);

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public List<String> getDestinations() {
        return destinations;
    }

    public void setDestinations(List<String> destinations) {
        this.destinations = destinations;
    }

    public TimeoutProperties getTimeout() {
        return timeout;
    }

    public void setTimeout(TimeoutProperties timeout) {
        this.timeout = timeout;
    }

    public Map<String, Object> getCustomConfiguration() {
        return customConfiguration;
    }

    public void setCustomConfiguration(Map<String, Object> customConfiguration) {
        this.customConfiguration = customConfiguration;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, NO_CLASS_NAME_STYLE)
                .append("name", name)
                .append("host", host)
                .append("destinations", destinations)
                .append("timeout", timeout)
                .append("customConfiguration", customConfiguration)
                .toString();
    }

    public static class TimeoutProperties {
        private int connect = 2000;
        private int read = 20000;

        public int getConnect() {
            return connect;
        }

        public void setConnect(int connect) {
            this.connect = connect;
        }

        public int getRead() {
            return read;
        }

        public void setRead(int read) {
            this.read = read;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, NO_CLASS_NAME_STYLE)
                    .append("connect", connect)
                    .append("read", read)
                    .toString();
        }
    }
}
