package cn.eros.staffjoy.company.dto;

import cn.eros.staffjoy.common.api.BaseResponse;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * @author 周光兵
 * @date 2021/8/26 13:18
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class GenericWorkerResponse extends BaseResponse {
    private WorkerDto workerDto;
}
