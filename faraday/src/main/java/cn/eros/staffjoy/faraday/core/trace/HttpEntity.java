package cn.eros.staffjoy.faraday.core.trace;

import org.springframework.http.HttpHeaders;

/**
 * @author 周光兵
 * @date 2021/7/27 13:51
 */
public class HttpEntity {
    protected HttpHeaders headers;

    public HttpHeaders getHeaders() {
        return headers;
    }

    public void setHeaders(HttpHeaders headers) {
        this.headers = headers;
    }
}
