package cn.eros.staffjoy.common.auditlog;

import com.github.structlog4j.IToLog;
import lombok.Builder;
import lombok.Data;

/**
 * <p>create time：2021-08-15 09:42
 *
 * @author Eros
 */
@Data
@Builder
public class LogEntry implements IToLog {
    private String currentUserId;
    private String companyId;
    private String teamId;
    private String authorization;
    private String targetType;
    private String targetId;
    private String originalContents;
    private String updatedContents;

    @Override
    public Object[] toLog() {
        return new Object[]{
                "auditlog", "true",
                "currentUserId", currentUserId,
                "companyId", companyId,
                "teamId", teamId,
                "authorization", authorization,
                "targetType", targetType,
                "targetId", targetId,
                "originalContents", originalContents,
                "updatedContents", updatedContents
        };
    }
}
