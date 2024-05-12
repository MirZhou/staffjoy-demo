package cn.eros.staffjoy.faraday.core.mappings;

import cn.eros.staffjoy.faraday.config.MappingProperties;
import cn.eros.staffjoy.faraday.exceptions.FaradayException;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.removeEnd;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * @author Eros
 * @since 2024/5/12
 */
public class MappingsValidator {
    public void validateMappings(List<MappingProperties> mappings) {
        if (isEmpty(mappings)) {
            return;
        }

        mappings.forEach(this::correctMapping);
    }

    private void correctMapping(MappingProperties mapping) {
        this.validateName(mapping);
        this.validateHost(mapping);
        this.validateDestinations(mapping);
        this.validateTimeout(mapping);
    }

    private void validateName(MappingProperties mapping) {
        if (isBlank(mapping.getName())) {
            throw new FaradayException("Empty name for mapping " + mapping);
        }
    }

    private void validateHost(MappingProperties mapping) {
        if (isBlank(mapping.getHost())) {
            throw new FaradayException("No source host for mapping " + mapping);
        }
    }

    protected void validateDestinations(MappingProperties mapping) {
        if (isEmpty(mapping.getDestinations())) {
            throw new FaradayException("No destination hosts for mapping" + mapping);
        }
        List<String> correctedHosts = new ArrayList<>(mapping.getDestinations().size());
        mapping.getDestinations().forEach(destination -> {
            if (isBlank(destination)) {
                throw new FaradayException("Empty destination for mapping " + mapping);
            }
            if (!destination.matches(".+://.+")) {
                destination = "http://" + destination;
            }
            destination = removeEnd(destination, "/");
            correctedHosts.add(destination);
        });
        mapping.setDestinations(correctedHosts);
    }

    protected void validateTimeout(MappingProperties mapping) {
        int connectTimeout = mapping.getTimeout().getConnect();
        if (connectTimeout < 0) {
            throw new FaradayException("Invalid connect timeout value: " + connectTimeout);
        }
        int readTimeout = mapping.getTimeout().getRead();
        if (readTimeout < 0) {
            throw new FaradayException("Invalid read timeout value: " + readTimeout);
        }
    }
}
