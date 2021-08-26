package cn.eros.staffjoy.company.dto;

import cn.eros.staffjoy.common.api.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 周光兵
 * @date 2021/8/25 21:14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenericDirectoryResponse extends BaseResponse {
    private DirectoryEntryDto directoryEntry;
}
