package cn.eros.staffjoy.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author 周光兵
 * @date 2021/7/21 00:21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountList {
    private List<AccountDto> accounts;
    private int limit;
    private int offset;
}
