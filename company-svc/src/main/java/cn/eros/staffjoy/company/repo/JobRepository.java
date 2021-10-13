package cn.eros.staffjoy.company.repo;

import cn.eros.staffjoy.company.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author 周光兵
 * @date 2021/8/26 21:49
 */
@Repository
public interface JobRepository extends JpaRepository<Job, String> {
    List<Job> findByTeamId(String teamId);
}
