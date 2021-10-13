package cn.eros.staffjoy.company.repo;

import cn.eros.staffjoy.company.model.Worker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

/**
 * @author 周光兵
 * @date 2021/8/26 21:49
 */
@Repository
public interface WorkerRepository extends JpaRepository<Worker, String> {
    Worker findByTeamIdAndUserId(String teamId, String userId);

    List<Worker> findByUserId(String userId);

    List<Worker> findByTeamId(String teamId);

    @Modifying(clearAutomatically = true)
    @Query("delete from Worker worker where worker.teamId = :teamId and worker.userId = :userId")
    @Transactional
    int deleteWorker(@Param("teamId") String teamId, @Param("userId") String userId);
}
