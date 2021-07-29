package cn.eros.staffjoy.faraday.core.trace;

import cn.eros.staffjoy.faraday.config.FaradayProperties;
import cn.eros.staffjoy.faraday.core.http.ForwardRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import static java.util.UUID.randomUUID;

/**
 * @author 周光兵
 * @date 2021/7/27 13:32
 */
public class ProxyingTraceInterceptor {
    protected final FaradayProperties faradayProperties;
    protected final TraceInterceptor traceInterceptor;

    public ProxyingTraceInterceptor(FaradayProperties faradayProperties, TraceInterceptor traceInterceptor) {
        this.faradayProperties = faradayProperties;
        this.traceInterceptor = traceInterceptor;
    }

    public String generateTraceId() {
        return this.faradayProperties.getTracing().isEnabled() ? randomUUID().toString() : null;
    }

    public void onRequestReceived(String traceId, HttpMethod httpMethod, String host, String uri, HttpHeaders headers) {
        this.runIfTracingIsEnabled(() -> {
            IncomingRequest request = getIncomingRequest(httpMethod, host, uri, headers);

            traceInterceptor.onRequestReceived(traceId, request);
        });
    }

    public void onNoMappingFound(String traceId, HttpMethod method, String host, String uri, HttpHeaders headers) {
        this.runIfTracingIsEnabled(() -> {
            IncomingRequest request = getIncomingRequest(method, host, uri, headers);
            traceInterceptor.onNoMappingFound(traceId, request);
        });
    }

    public void onForwardStart(String traceId, String mappingName, HttpMethod method, String host, String uri, byte[] body, HttpHeaders headers) {
        runIfTracingIsEnabled(() -> {
            ForwardRequest request = new ForwardRequest();
            request.setMappingName(mappingName);
            request.setMethod(method);
            request.setHost(host);
            request.setUri(uri);
            request.setBody(body);
            request.setHeaders(headers);

            traceInterceptor.onForwardStart(traceId, request);
        });
    }

    public void onForwardComplete(String traceId, HttpStatus status, byte[] body, HttpHeaders headers) {
        runIfTracingIsEnabled(() -> {
            ReceiveResponse response = new ReceiveResponse();
            response.setStatus(status);
            response.setBody(body);
            response.setHeaders(headers);

            traceInterceptor.onForwardComplete(traceId, response);
        });
    }

    public void onForwardFailed(String traceId, Throwable error) {
        runIfTracingIsEnabled(() -> {
            traceInterceptor.onForwardError(traceId, error);
        });
    }

    protected void runIfTracingIsEnabled(Runnable operation) {
        if (this.faradayProperties.getTracing().isEnabled()) {
            operation.run();
        }
    }

    private IncomingRequest getIncomingRequest(HttpMethod method, String host, String uri, HttpHeaders headers) {
        IncomingRequest request = new IncomingRequest();
        request.setHeaders(headers);
        request.setMethod(method);
        request.setHost(host);
        request.setUri(uri);

        return request;
    }
}
