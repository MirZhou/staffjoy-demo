package cn.eros.staffjoy.whoami.dto;

import cn.eros.staffjoy.company.dto.AdminOfList;
import cn.eros.staffjoy.company.dto.WorkerOfList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 周光兵
 * @date 2021/10/15 16:11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IAmDto {
    private boolean support;
    private String userId;
    private WorkerOfList workerOfList;
    private AdminOfList adminOfList;
}
