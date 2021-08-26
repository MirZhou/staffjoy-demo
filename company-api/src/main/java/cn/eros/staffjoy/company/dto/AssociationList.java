package cn.eros.staffjoy.company.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 周光兵
 * @date 2021/8/25 22:18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssociationList {
    private List<Association> accounts = new ArrayList<>();
    private int limit;
    private int offset;
}
