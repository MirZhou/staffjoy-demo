package cn.eros.staffjoy.faraday.core.http;

import cn.eros.staffjoy.faraday.config.MappingProperties;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

/**
 * Http Client 映射表
 *
 * @author Eros
 * @since 2024/5/12
 */
public class HttpClientProvider {
    protected Map<String, RestTemplate> httpClients = new HashMap<>(16);

    public void updateHttpClient(List<MappingProperties> mappings) {
        httpClients = mappings.stream()
                .collect(toMap(MappingProperties::getName, this::createRestTemplate));
    }

    private RestTemplate createRestTemplate(MappingProperties mapping) {
        CloseableHttpClient client = this.createHttpClient(mapping).build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(client);
        requestFactory.setConnectTimeout(mapping.getTimeout().getConnect());
        requestFactory.setReadTimeout(mapping.getTimeout().getRead());

        return new RestTemplate(requestFactory);
    }

    private HttpClientBuilder createHttpClient(MappingProperties mapping) {
        return HttpClientBuilder.create()
                .useSystemProperties()
                .disableRedirectHandling()
                .disableCookieManagement();
    }
}
