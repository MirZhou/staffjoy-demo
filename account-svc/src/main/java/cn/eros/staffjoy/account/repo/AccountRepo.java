package cn.eros.staffjoy.account.repo;

import cn.eros.staffjoy.account.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author 周光兵
 * @date 2021/7/29 22:41
 */
@Repository
public interface AccountRepo extends JpaRepository<Account, String> {
    Account findAccountByEmail(String email);

    Account findAccountByPhoneNumber(String phoneNumber);

    @Modifying(clearAutomatically = true)
    @Query("update Account account set account.email = :email, account.confirmedAndActive = true where account.id = id")
    @Transactional
    int updateEmailAndActivateById(@Param("email") String email, @Param("id") String id);
}
