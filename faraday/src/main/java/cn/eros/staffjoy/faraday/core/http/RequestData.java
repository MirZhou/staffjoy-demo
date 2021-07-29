package cn.eros.staffjoy.faraday.core.http;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import javax.servlet.http.HttpServletRequest;

import static cn.eros.staffjoy.faraday.core.utils.BodyConverter.convertStringToBody;

/**
 * @author 周光兵
 * @date 2021/7/27 21:56
 */
public class RequestData extends UnmodifiableRequestData {
    private boolean needRedirect;
    private String redirectUrl;

    public RequestData(HttpMethod method, String uri, String host, HttpHeaders headers, byte[] body, HttpServletRequest originRequest) {
        super(method, uri, host, headers, body, originRequest);
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public void setBody(String body) {
        this.body = convertStringToBody(body);
    }

    public boolean isNeedRedirect() {
        return needRedirect;
    }

    public void setNeedRedirect(boolean needRedirect) {
        this.needRedirect = needRedirect;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
}
