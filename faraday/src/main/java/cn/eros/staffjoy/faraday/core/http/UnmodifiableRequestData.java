package cn.eros.staffjoy.faraday.core.http;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import javax.servlet.http.HttpServletRequest;

import static cn.eros.staffjoy.faraday.core.utils.BodyConverter.convertBodyToString;

/**
 * @author 周光兵
 * @date 2021/7/27 21:57
 */
public class UnmodifiableRequestData {
    protected HttpMethod method;
    protected String uri;
    protected String host;
    protected HttpHeaders headers;
    protected byte[] body;
    protected HttpServletRequest originRequest;

    public UnmodifiableRequestData(RequestData requestData) {
        this(
            requestData.getMethod(),
            requestData.getUri(),
            requestData.getHost(),
            requestData.getHeaders(),
            requestData.getBody(),
            requestData.getOriginRequest()
        );
    }

    public UnmodifiableRequestData(HttpMethod method, String uri, String host, HttpHeaders headers, byte[] body, HttpServletRequest originRequest) {
        this.method = method;
        this.uri = uri;
        this.host = host;
        this.headers = headers;
        this.body = body;
        this.originRequest = originRequest;
    }

    public String getBodyAsString() {
        return convertBodyToString(body);
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public String getHost() {
        return host;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        return body;
    }

    public HttpServletRequest getOriginRequest() {
        return originRequest;
    }
}
