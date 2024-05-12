package cn.eros.staffjoy.faraday.core.mappings;

import cn.eros.staffjoy.faraday.config.FaradayProperties;
import cn.eros.staffjoy.faraday.config.MappingProperties;
import cn.eros.staffjoy.faraday.core.http.HttpClientProvider;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import org.springframework.boot.autoconfigure.web.ServerProperties;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * 路由映射表
 *
 * @author Eros
 * @since 2024/5/12
 */
public abstract class MappingsProvider {
    private static final ILogger log = SLoggerFactory.getLogger(MappingsProvider.class);

    protected final ServerProperties serverProperties;
    protected final FaradayProperties faradayProperties;
    protected final MappingsValidator mappingsValidator;
    protected final HttpClientProvider httpClientProvider;
    protected List<MappingProperties> mappings;

    public MappingsProvider(ServerProperties serverProperties,
                            FaradayProperties faradayProperties,
                            MappingsValidator mappingsValidator,
                            HttpClientProvider httpClientProvider) {
        this.serverProperties = serverProperties;
        this.faradayProperties = faradayProperties;
        this.mappingsValidator = mappingsValidator;
        this.httpClientProvider = httpClientProvider;
    }

    public MappingProperties resolveMapping(String originHost, HttpServletRequest request) {
        if (shouldUpdateMappings(request)) {
            this.updateMappings();
        }

        List<MappingProperties> resolvedMappings = this.mappings.stream()
                .filter(mapping -> originHost.equalsIgnoreCase(mapping.getHost()))
                .collect(Collectors.toList());

        if (isEmpty(resolvedMappings)) {
            log.warn("No mapping found for host {}", originHost);
            return null;
        }

        return resolvedMappings.get(0);
    }

    @PostConstruct
    protected synchronized void updateMappings() {
        List<MappingProperties> newMappings = this.retrieveMappings();
        mappingsValidator.validateMappings(newMappings);
        mappings = newMappings;
        this.httpClientProvider.updateHttpClient(mappings);
        log.info("Destination mappings updated", mappings);
    }

    protected abstract boolean shouldUpdateMappings(HttpServletRequest request);

    protected abstract List<MappingProperties> retrieveMappings();
}
