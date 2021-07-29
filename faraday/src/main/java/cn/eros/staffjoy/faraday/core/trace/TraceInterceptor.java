package cn.eros.staffjoy.faraday.core.trace;

/**
 * @author 周光兵
 * @date 2021/7/27 13:39
 */
public interface TraceInterceptor {
    void onRequestReceived(String traceId, IncomingRequest request);

    void onNoMappingFound(String traceId, IncomingRequest request);

    void onForwardStart(String traceId, IncomingRequest request);

    void onForwardError(String traceId, Throwable error);

    void onForwardComplete(String traceId, ReceiveResponse response);
}
