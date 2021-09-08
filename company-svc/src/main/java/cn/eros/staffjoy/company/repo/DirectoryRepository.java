package cn.eros.staffjoy.company.repo;

import cn.eros.staffjoy.company.model.Directory;
import feign.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

/**
 * @author 周光兵
 * @date 2021/8/26 21:49
 */
@Repository
public interface DirectoryRepository extends JpaRepository<Directory, String> {
    @Modifying(clearAutomatically = true)
    @Query("update Directory set internalId = :internalId where companyId = :companyId and userId = :userId")
    @Transactional(rollbackOn = Exception.class)
    int updateInternalIdByCompanyIdAndUserId(@Param("internalId") String internalId,
                                             @Param("companyId") String companyId,
                                             @Param("userId") String userId);

    Directory findByCompanyIdAndUserId(String companyId, String userId);

    Page<Directory> findByCompanyId(String companyId, Pageable pageable);
}
