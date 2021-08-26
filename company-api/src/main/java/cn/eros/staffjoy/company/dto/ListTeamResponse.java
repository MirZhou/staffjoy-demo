package cn.eros.staffjoy.company.dto;

import cn.eros.staffjoy.common.api.BaseResponse;
import lombok.*;

/**
 * @author 周光兵
 * @date 2021/8/26 13:15
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListTeamResponse extends BaseResponse {
    private TeamList teamList;
}
