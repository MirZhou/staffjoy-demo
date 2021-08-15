package cn.eros.staffjoy.company.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 周光兵
 * @date 2021/8/5 23:22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerOfList {
    private String userid;
    @Builder.Default
    private List<TeamDto> teams = new ArrayList<>();
}
