package cn.eros.staffjoy.faraday.core.http;

import cn.eros.staffjoy.faraday.core.trace.IncomingRequest;

import static cn.eros.staffjoy.faraday.core.utils.BodyConverter.convertBodyToString;

/**
 * @author 周光兵
 * @date 2021/7/28 13:13
 */
public class ForwardRequest extends IncomingRequest {
    protected String mappingName;
    protected byte[] body;

    public String getMappingName() {
        return mappingName;
    }

    public void setMappingName(String mappingName) {
        this.mappingName = mappingName;
    }

    public String getBodyAsString() {
        return convertBodyToString(body);
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}
