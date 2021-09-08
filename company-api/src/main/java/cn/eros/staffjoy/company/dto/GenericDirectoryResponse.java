package cn.eros.staffjoy.company.dto;

import cn.eros.staffjoy.common.api.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @author 周光兵
 * @date 2021/8/25 21:14
 */
@EqualsAndHashCode(callSuper=false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class GenericDirectoryResponse extends BaseResponse {
    private DirectoryEntryDto directoryEntry;
}
