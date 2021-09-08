package cn.eros.staffjoy.company.repo;

import cn.eros.staffjoy.company.model.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author 周光兵
 * @date 2021/8/26 21:49
 */
@Repository
public interface ShiftRepository extends JpaRepository<Shift, String> {
}
