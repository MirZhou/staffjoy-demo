package cn.eros.staffjoy.faraday.config;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 周光兵
 * @date 2021/7/27 14:01
 */
public class MappingProperties {
    /**
     * name of the mapping
     */
    private String name;
    /**
     * Path for mapping incoming HTTP requests URIs.
     */
    private String host;
    /**
     * List of destination hosts where HTTP requests will be forwarded.
     */
    private List<String> destinations = new ArrayList<>();
    /**
     * Properties responsible for timeout while forwarding HTTP requests.
     */
    private TimeoutProperties timeout = new TimeoutProperties();

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
            return "TimeoutProperties{" +
                "connect=" + connect +
                ", read=" + read +
                '}';
        }
    }
}
