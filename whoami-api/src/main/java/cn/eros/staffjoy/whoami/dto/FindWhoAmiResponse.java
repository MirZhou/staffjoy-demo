package cn.eros.staffjoy.whoami.dto;

import cn.eros.staffjoy.common.api.BaseResponse;
import lombok.*;

/**
 * @author 周光兵
 * @date 2021/10/15 16:12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class FindWhoAmiResponse extends BaseResponse {
    private IAmDto iAm;
}
