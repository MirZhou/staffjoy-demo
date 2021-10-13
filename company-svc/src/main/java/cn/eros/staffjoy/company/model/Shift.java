package cn.eros.staffjoy.company.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.Instant;

/**
 * @author 周光兵
 * @date 2021/8/26 21:44
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Shift {
    @Id
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @GeneratedValue(generator = "system-uuid")
    private String id;
    private String teamId;
    private Instant start;
    private Instant stop;
    private String userId;
    private String jobId;
    private boolean published;
}
