package cn.eros.staffjoy.company.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * @author 周光兵
 * @date 2021/8/26 21:45
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Team {
    @Id
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @GeneratedValue(generator = "system-uuid")
    private String id;
    private String companyId;
    private String name;
    private boolean archived;
    private String timezone;
    private String dayWeekStarts;
    private String color;
}
