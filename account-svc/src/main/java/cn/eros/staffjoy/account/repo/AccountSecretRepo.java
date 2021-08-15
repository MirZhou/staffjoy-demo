package cn.eros.staffjoy.account.repo;

import cn.eros.staffjoy.account.model.AccountSecret;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>create time：2021-08-15 11:19
 *
 * @author Eros
 */
@Repository
public interface AccountSecretRepo extends JpaRepository<AccountSecret, String> {
    AccountSecret findAccountSecretByEmail(String email);

    @Modifying(clearAutomatically = true)
    @Query("update AccountSecret accountSecret set accountSecret.passwordHash = :passwordHash where accountSecret.id = :id")
    @Transactional
    int updatePasswordHashById(@Param("passwordHash") String passwordHash, @Param("id") String id);
}
