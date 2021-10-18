package cn.eros.staffjoy.whoami.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * @author 周光兵
 * @date 2021/10/15 16:07
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntercomSettingsDto {
    private String appId;
    private String userId;
    private String userHash;
    private String name;
    private String email;
    private Instant createdAt;
}
