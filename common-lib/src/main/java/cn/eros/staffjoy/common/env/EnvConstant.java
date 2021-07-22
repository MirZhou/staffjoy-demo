package cn.eros.staffjoy.common.env;

import lombok.Builder;
import lombok.Data;

/**
 * @author 周光兵
 * @date 2021/7/21 21:51
 */
@Data
@Builder
public class EnvConstant {
    public static final String ENV_DEV = "dev";
    public static final String ENV_TEST = "test";
    /**
     * similar to staging
     */
    public static final String ENV_UAT = "uat";
    public static final String ENV_PROD = "prod";
}
