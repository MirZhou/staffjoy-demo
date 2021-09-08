package cn.eros.staffjoy.company.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.eros.staffjoy.company.dto.TeamDto;
import cn.eros.staffjoy.company.dto.WorkerOfList;
import cn.eros.staffjoy.company.model.Worker;
import cn.eros.staffjoy.company.repo.WorkerRepository;

/**
 * 
 * @author 周光兵
 * @date 2021/09/08 13:24
 */
@Service
public class WorkerService {
    @Autowired
    private WorkerRepository workerRepository;
    
    @Autowired
    private TeamService teamService;

    public WorkerOfList getWorkerOf(String userId) {
        List<Worker> workerList = this.workerRepository.findByUserId(userId);

        WorkerOfList workerOfList = WorkerOfList.builder().userid(userId).build();

        for (Worker worker : workerList) {
            TeamDto teamDto = this.teamService.getTeam(worker.getTeamId());
            workerOfList.getTeams().add(teamDto);
        }

        return workerOfList;
    }
}
