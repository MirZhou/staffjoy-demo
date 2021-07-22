package cn.eros.staffjoy.common.env;

import lombok.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 周光兵
 * @date 2021/7/21 21:51
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
        map = new HashMap<>(16);

        EnvConfig envConfig = EnvConfig.builder()
            .name(EnvConstant.ENV_DEV)
            .debug(true)
            .externalApex("staffjoy-v2.local")
            .internalApex(EnvConstant.ENV_DEV)
            .scheme("http")
            .build();

        map.put(EnvConstant.ENV_DEV, envConfig);

        envConfig = EnvConfig.builder()
            .name(EnvConstant.ENV_TEST)
            .debug(true)
            .externalApex("staffjoy-v2.local")
            .internalApex(EnvConstant.ENV_TEST)
            .scheme("http")
            .build();

        map.put(EnvConstant.ENV_TEST, envConfig);

        // for aliyun k8s demo, enable debug and use http and staffjoy-uat.local
        // in real world, disable debug and use http and staffjoy-uat.xyz in UAT environment
        envConfig = EnvConfig.builder()
            .name(EnvConstant.ENV_UAT)
            .debug(true)
            .externalApex("staffjoy-v2.local")
            .internalApex(EnvConstant.ENV_UAT)
            .scheme("http")
            .build();

        map.put(EnvConstant.ENV_UAT, envConfig);

        envConfig = EnvConfig.builder()
            .name(EnvConstant.ENV_PROD)
            .debug(false)
            .externalApex("/")
            .internalApex(EnvConstant.ENV_PROD)
            .scheme("http")
            .build();

        map.put(EnvConstant.ENV_PROD, envConfig);
    }

    public static EnvConfig getEnvConfig(String env) {
        EnvConfig envConfig = map.get(env);

        if (envConfig == null) {
            envConfig = map.get(EnvConstant.ENV_DEV);
        }

        return envConfig;
    }
}
