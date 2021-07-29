package cn.eros.staffjoy.faraday.core.http;

import cn.eros.staffjoy.faraday.config.FaradayProperties;
import cn.eros.staffjoy.faraday.config.MappingProperties;
import cn.eros.staffjoy.faraday.core.balancer.LoadBalancer;
import cn.eros.staffjoy.faraday.core.interceptor.PostForwardResponseInterceptor;
import cn.eros.staffjoy.faraday.core.trace.ProxyingTraceInterceptor;
import cn.eros.staffjoy.faraday.exceptions.FaradayException;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import static java.lang.System.nanoTime;
import static java.time.Duration.ofNanos;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.ResponseEntity.status;

/**
 * @author 周光兵
 * @date 2021/7/27 22:18
 */
public class RequestForwarder {
    private static final ILogger log = SLoggerFactory.getLogger(RequestForwarder.class);

    protected final FaradayProperties faradayProperties;
    protected final HttpClientProvider httpClientProvider;
    protected final LoadBalancer loadBalancer;
    protected final MeterRegistry meterRegistry;
    protected final ProxyingTraceInterceptor traceInterceptor;
    protected final PostForwardResponseInterceptor postForwardResponseInterceptor;

    public RequestForwarder(FaradayProperties faradayProperties,
                            HttpClientProvider httpClientProvider,
                            LoadBalancer loadBalancer,
                            MeterRegistry meterRegistry,
                            ProxyingTraceInterceptor traceInterceptor,
                            PostForwardResponseInterceptor postForwardResponseInterceptor) {
        this.faradayProperties = faradayProperties;
        this.httpClientProvider = httpClientProvider;
        this.loadBalancer = loadBalancer;
        this.meterRegistry = meterRegistry;
        this.traceInterceptor = traceInterceptor;
        this.postForwardResponseInterceptor = postForwardResponseInterceptor;
    }

    public ResponseEntity<byte[]> forwardHttpRequest(RequestData data, String traceId, MappingProperties mapping) {
        ForwardDestination destination = resolveForwardDestination(data.getUri(), mapping);

        prepareForwardedRequestHeaders(data, destination);

        this.traceInterceptor.onForwardStart(traceId, destination.getMappingName(),
            data.getMethod(), data.getHost(), destination.getUri().toString(),
            data.getBody(), data.getHeaders());

        RequestEntity<byte[]> request = new RequestEntity<>(data.getBody(), data.getHeaders(), data.getMethod(), destination.getUri());
        ResponseData response = this.sendRequest(traceId, request, mapping, destination.getMappingMetricsName(), data);

        log.debug(String.format("Forwarded: %s %s %s -> %s %d", data.getMethod(), data.getHost(), data.getUri(), destination.getUri(), response.getStatus().value()));

        traceInterceptor.onForwardComplete(traceId, response.getStatus(), response.getBody(), response.getHeaders());

        this.postForwardResponseInterceptor.intercept(response, mapping);

        this.prepareForwardedResponseHeaders(response);

        return status(response.getStatus())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    protected void prepareForwardedRequestHeaders(RequestData data, ForwardDestination destination) {
        HttpHeaders headers = data.getHeaders();

        headers.remove(TE);
    }

    protected void prepareForwardedResponseHeaders(ResponseData response) {
        HttpHeaders headers = response.getHeaders();
        headers.remove(TRANSFER_ENCODING);
        headers.remove(CONNECTION);
        headers.remove("Public-Key-Pins");
        headers.remove(SERVER);
        headers.remove("Strict-Transport-Security");
    }

    protected ForwardDestination resolveForwardDestination(String originUrl, MappingProperties mapping) {
        return new ForwardDestination(createDestinationUrl(originUrl, mapping), mapping.getName(), resolveMetricsName(mapping));
    }

    protected URI createDestinationUrl(String uri, MappingProperties mapping) {
        String host = loadBalancer.chooseDestination(mapping.getDestinations());

        try {
            return new URI(host + uri);
        } catch (URISyntaxException e) {
            throw new FaradayException("Error creating destination URL from HTTP request URI: " + uri + " using mapping " + mapping, e);
        }
    }

    protected ResponseData sendRequest(String traceId, RequestEntity<byte[]> request, MappingProperties mapping, String mappingMetricsName, RequestData requestData) {
        ResponseEntity<byte[]> response;
        long startingTime = nanoTime();

        try {
            response = httpClientProvider.getHttpClient(mapping.getName()).exchange(request, byte[].class);
            recordLatency(mappingMetricsName, startingTime);
        } catch (HttpStatusCodeException e) {
            recordLatency(mappingMetricsName, startingTime);

            response = status(e.getStatusCode())
                .headers(e.getResponseHeaders())
                .body(e.getResponseBodyAsByteArray());
        } catch (Exception e) {
            recordLatency(mappingMetricsName, startingTime);

            traceInterceptor.onForwardFailed(traceId, e);

            throw e;
        }

        UnmodifiableRequestData data = new UnmodifiableRequestData(requestData);

        return new ResponseData(response.getStatusCode(), response.getHeaders(), response.getBody(), data);
    }

    protected void recordLatency(String metricsName, long startingTime) {
        Optional.ofNullable(this.meterRegistry).ifPresent(meterRegistry -> meterRegistry.timer(metricsName).record(ofNanos(nanoTime() - startingTime)));
    }

    protected String resolveMetricsName(MappingProperties mapping) {
        return this.faradayProperties.getMetrics().getNamesPrefix() + "." + mapping.getName();
    }
}
