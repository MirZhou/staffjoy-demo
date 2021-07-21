package cn.eros.staffjoy.account.dto;

import cn.eros.staffjoy.common.api.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author 周光兵
 * @date 2021/7/21 00:45
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
public class ListAccountResponse extends BaseResponse {
    private AccountList accountList;
}
