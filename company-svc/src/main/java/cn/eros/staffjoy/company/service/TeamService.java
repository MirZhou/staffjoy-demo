package cn.eros.staffjoy.company.service;

import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.eros.staffjoy.common.api.ResultCode;
import cn.eros.staffjoy.company.dto.TeamDto;
import cn.eros.staffjoy.company.model.Team;
import cn.eros.staffjoy.company.repo.TeamRepository;

import cn.eros.staffjoy.common.error.ServiceException;

/**
 * 
 * @author 周光兵
 * @date 2021/09/08 13:31
 */
@Service
public class TeamService {
    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private ModelMapper modelMapper;

    public TeamDto getTeam(String teamId) {
        Optional<Team> team = this.teamRepository.findById(teamId);

        if (!team.isPresent()) {
            throw new ServiceException(ResultCode.NOT_FOUND, "team with specified id not found");
        }

        return this.converToDto(team.get());
    }

    private TeamDto converToDto(Team team) {
        return this.modelMapper.map(team, TeamDto.class);
    }
}
