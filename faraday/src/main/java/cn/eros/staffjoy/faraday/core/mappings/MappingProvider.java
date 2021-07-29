package cn.eros.staffjoy.faraday.core.mappings;

import cn.eros.staffjoy.faraday.config.MappingProperties;
import cn.eros.staffjoy.faraday.core.http.HttpClientProvider;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * @author 周光兵
 * @date 2021/7/27 13:57
 */
public abstract class MappingProvider {
    private static final ILogger log = SLoggerFactory.getLogger(MappingProvider.class);

    protected final MappingsValidator mappingsValidator;
    protected final HttpClientProvider httpClientProvider;
    protected List<MappingProperties> mappings;

    protected MappingProvider(MappingsValidator mappingsValidator, HttpClientProvider httpClientProvider) {
        this.mappingsValidator = mappingsValidator;
        this.httpClientProvider = httpClientProvider;
    }

    public MappingProperties resolveMapping(String originHost, HttpServletRequest request) {
        if (shouldUpdateMapping(request)) {
            updateMappings();
        }

        List<MappingProperties> resolveMappings = mappings.stream()
            .filter(mapping -> originHost.equalsIgnoreCase(mapping.getHost()))
            .collect(Collectors.toList());

        if (isEmpty(resolveMappings)) {
            return null;
        }

        return resolveMappings.get(0);
    }

    protected synchronized void updateMappings() {
        List<MappingProperties> newMappings = retrieveMappings();
        mappingsValidator.validate(newMappings);

        mappings = newMappings;
        httpClientProvider.updateHttpClients(mappings);

        log.info("Destination mappings updated", mappings);
    }

    protected abstract boolean shouldUpdateMapping(HttpServletRequest request);

    protected abstract List<MappingProperties> retrieveMappings();
}
