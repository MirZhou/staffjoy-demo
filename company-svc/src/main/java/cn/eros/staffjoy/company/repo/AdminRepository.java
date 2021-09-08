package cn.eros.staffjoy.company.repo;

import cn.eros.staffjoy.company.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author 周光兵
 * @date 2021/8/26 21:49
 */
@Repository
public interface AdminRepository extends JpaRepository<Admin, String> {
    int deleteByCompanyIdAndUserId(String companyId, String userId);

    Admin findByCompanyIdAndUserId(String companyId, String userId);

    List<Admin> findByCompanyId(String companyId);

    List<Admin> findByUserId(String userId);
}
