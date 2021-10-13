package cn.eros.staffjoy.company.repo;

import cn.eros.staffjoy.company.model.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * @author 周光兵
 * @date 2021/8/26 21:49
 */
@Repository
public interface ShiftRepository extends JpaRepository<Shift, String> {
    @Query(
        value = "select shift from Shift shift where shift.teamId = :teamId and shift.userId = :userId and shift.start >= :startTime and shift.start < :endTime order by  shift.start"
    )
    List<Shift> listWorkerShifts(@Param("teamId") String teamId,
                                 @Param("userId") String userId,
                                 @Param("startTime") Instant start,
                                 @Param("endTime") Instant end);

    @Query(
        value = "select shift from Shift shift where shift.teamId = :teamId and shift.jobId = :jobId and shift.start >= :startTime and shift.start < :endTime"
    )
    List<Shift> listShiftByJobId(@Param("teamId") String teamId,
                                 @Param("jobId") String jobId,
                                 @Param("startTime") Instant start,
                                 @Param("endTime") Instant end);

    @Query(
        value = "select shift from Shift shift where shift.teamId = :teamId and shift.userId = :userId and shift.jobId = :jobId and shift.start >= :startTime and shift.start < :endTime"
    )
    List<Shift> listShiftByUserIdAndJobId(@Param("teamId") String teamId,
                                          @Param("userId") String userId,
                                          @Param("jobId") String jobId,
                                          @Param("startTime") Instant start,
                                          @Param("endTime") Instant end);

    @Query(
        value = "select shift from Shift shift where shift.teamId = :teamId and shift.start >= :startTime and shift.start < :endTime"
    )
    List<Shift> listShiftByTeamIdOnly(@Param("teamId") String teamId,
                                      @Param("startTime") Instant start,
                                      @Param("endTime") Instant end);
}
