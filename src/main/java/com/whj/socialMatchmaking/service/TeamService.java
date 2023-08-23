package com.whj.socialMatchmaking.service;

import com.whj.socialMatchmaking.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.whj.socialMatchmaking.model.domain.User;
import com.whj.socialMatchmaking.model.dto.TeamQueryRequest;
import com.whj.socialMatchmaking.model.request.TeamDeleteRequest;
import com.whj.socialMatchmaking.model.request.TeamJoinRequest;
import com.whj.socialMatchmaking.model.request.TeamQuitRequest;
import com.whj.socialMatchmaking.model.request.TeamUpdateRequest;
import com.whj.socialMatchmaking.model.vo.TeamUserVO;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


/**
* @author Baldwin
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2023-07-24 15:43:51
*/
public interface TeamService extends IService<Team> {
    long addTeam(Team team, User loginUser);

    List<TeamUserVO> listTeams(TeamQueryRequest teamQuery, boolean isAdmin);

    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);

    boolean joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, User loginUser);

    @Transactional(rollbackFor = Exception.class)
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User currentUser);

    @Transactional(rollbackFor = Exception.class)
    boolean deleteTeam(TeamDeleteRequest teamDeleteRequest, User currentUser);
}
