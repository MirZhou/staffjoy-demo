package cn.eros.staffjoy.company.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 周光兵
 * @date 2021/8/26 13:28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobList {
    @Builder.Default
    private List<JobDto> jobs = new ArrayList<>();
}
