package cn.eros.staffjoy.company.dto;

import cn.eros.staffjoy.common.api.BaseResponse;
import lombok.*;

/**
 * @author 周光兵
 * @date 2021/8/5 23:21
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetWorkerOfResponse extends BaseResponse {
    private WorkerOfList workerOfList;
}
