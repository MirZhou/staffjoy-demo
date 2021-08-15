package cn.eros.staffjoy.account.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * <p>create time：2021-08-15 11:21
 *
 * @author Eros
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "account")
public class AccountSecret {
    @Id
    private String id;
    private String email;
    private boolean confirmAndActive;
    private String passwordHash;
}
