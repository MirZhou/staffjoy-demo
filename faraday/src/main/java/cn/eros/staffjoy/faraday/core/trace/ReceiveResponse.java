package cn.eros.staffjoy.faraday.core.trace;

import org.springframework.http.HttpStatus;

import static cn.eros.staffjoy.faraday.core.utils.BodyConverter.convertBodyToString;

/**
 * @author 周光兵
 * @date 2021/7/28 22:47
 */
public class ReceiveResponse extends HttpEntity {
    protected HttpStatus status;
    protected byte[] body;

    public HttpStatus getStatus() {
        return status;
    }

    protected void setStatus(HttpStatus status) {
        this.status = status;
    }

    public String getBodyAsString() {
        return convertBodyToString(body);
    }

    public byte[] getBody() {
        return body;
    }

    protected void setBody(byte[] body) {
        this.body = body;
    }
}
