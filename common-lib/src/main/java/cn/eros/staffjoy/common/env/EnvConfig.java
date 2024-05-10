package cn.eros.staffjoy.common.env;

import lombok.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Eros
 * @since 2024/5/10
 */
@Data
@Builder
public class EnvConfig {
    private String name;
    private boolean debug;
    private String externalApex;
    private String internalApex;
    private String scheme;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private static Map<String, EnvConfig> map;

    static {
        map = new HashMap<>(4);

        map.put(EnvConstant.ENV_DEV, EnvConfig.builder()
                .name(EnvConstant.ENV_DEV)
                .debug(true)
                .externalApex("staffjoy-v2.local")
                .internalApex(EnvConstant.ENV_DEV)
                .scheme("http")
                .build());

        map.put(EnvConstant.ENV_TEST, EnvConfig.builder()
                .name(EnvConstant.ENV_TEST)
                .debug(true)
                .externalApex("staffjoy-v2.local")
                .internalApex(EnvConstant.ENV_TEST)
                .scheme("http")
                .build());

        map.put(EnvConstant.ENV_UAT, EnvConfig.builder()
                .name(EnvConstant.ENV_UAT)
                .debug(false)
                .externalApex("staffjoy-v2.local")
                .internalApex(EnvConstant.ENV_UAT)
                .scheme("https")
                .build());

        map.put(EnvConstant.ENV_PROD, EnvConfig.builder()
                .name(EnvConstant.ENV_PROD)
                .debug(false)
                .externalApex("staffjoy.com")
                .internalApex(EnvConstant.ENV_PROD)
                .scheme("https")
                .build());
    }

    public static EnvConfig getEnvConfig(String name) {
        EnvConfig envConfig = map.get(name);

        if (envConfig == null) {
            envConfig = map.get(EnvConstant.ENV_DEV);
        }

        return envConfig;
    }
}
