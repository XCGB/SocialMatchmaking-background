package com.whj.socialMatchmaking.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.whj.socialMatchmaking.common.BaseResponse;
import com.whj.socialMatchmaking.common.ErrorCode;
import com.whj.socialMatchmaking.common.ResultUtils;
import com.whj.socialMatchmaking.exception.BusinessException;
import com.whj.socialMatchmaking.model.domain.Team;
import com.whj.socialMatchmaking.model.domain.User;
import com.whj.socialMatchmaking.model.domain.UserTeam;
import com.whj.socialMatchmaking.model.dto.TeamQueryRequest;
import com.whj.socialMatchmaking.model.request.*;
import com.whj.socialMatchmaking.model.vo.TeamUserVO;
import com.whj.socialMatchmaking.service.TeamService;
import com.whj.socialMatchmaking.service.UserService;
import com.whj.socialMatchmaking.service.UserTeamService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author: Baldwin
 * @createTime: 2023-07-24 15:59
 * @description: 队伍控制器
 */
@Slf4j
@RestController
@RequestMapping("/team")
@Tag(name = "team-controller", description = "队伍控制器")
@CrossOrigin(origins = "http://127.0.0.1:5173", allowCredentials = "true")
public class TeamController {
    /**
     * 用户服务
     */
    @Resource
    private UserService userService;

    /**
     * 队伍服务
     */
    @Resource
    private TeamService teamService;

    /**
     * 用户队伍服务
     */
    @Resource
    private UserTeamService userTeamService;

    /**
     * 加入团队
     *
     * @param teamAddRequest 团队添加请求
     * @param request        HTTP请求
     * @return {@link BaseResponse}<{@link Long}>
     */
    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request){
        if (teamAddRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User currentUser = userService.getCurrentUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest, team);
        long teamId = teamService.addTeam(team, currentUser);
        return ResultUtils.success(teamId);
    }

    /**
     * 更新团队
     *
     * @param teamUpdateRequest 团队更新请求
     * @param request           请求
     * @return {@link BaseResponse}<{@link Boolean}>
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request){
        if (teamUpdateRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User currentUser = userService.getCurrentUser(request);
        boolean result = teamService.updateTeam(teamUpdateRequest, currentUser);
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新数据失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 通过ID获取队伍
     *
     * @param id 队伍ID
     * @return {@link BaseResponse}<{@link Team}>
     */
    @GetMapping("/get")
    public BaseResponse<Team> getTeamById(long id){
        if (id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        if (team == null){
            throw new BusinessException(ErrorCode.NULL_ERROR, "查询数据为空");
        }
        return ResultUtils.success(team);
    }

    /**
     * 查询团队名单
     *
     * @param teamQuery 团队查询请求
     * @param request   HTTP请求
     * @return {@link BaseResponse}<{@link List}<{@link TeamUserVO}>>
     */
    @GetMapping("/list")
    public BaseResponse<List<TeamUserVO>> getTeamList(TeamQueryRequest teamQuery, HttpServletRequest request){
        if (teamQuery == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean isAdmin = userService.isAdmin(request);
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, isAdmin);
        // 判断当前用户是否已经加入队伍
        final List<Long> teamIdList = teamList.stream().map(TeamUserVO::getId).collect(Collectors.toList());
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        try {
            User currentUser = userService.getCurrentUser(request);
            userTeamQueryWrapper.eq("user_id",currentUser.getId());
            userTeamQueryWrapper.in("team_id",teamIdList);
            List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
            Set<Long> hasJoinTeamIdSet = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            teamList.forEach(team -> {
                boolean hasJoin = hasJoinTeamIdSet.contains(team.getId());
                team.setHasJoin(hasJoin);
            });
        } catch (Exception e) {}
        // 查询已加入队伍的人数
        QueryWrapper<UserTeam> teamJoinNumWrapper = new QueryWrapper<>();
        teamJoinNumWrapper.in("team_id", teamIdList);
        List<UserTeam> userTeamList = userTeamService.list(teamJoinNumWrapper);
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamList.forEach(team -> team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(),new ArrayList<>()).size()));
        return ResultUtils.success(teamList);
    }

//    @GetMapping("/list/page")
//    public BaseResponse<Page<Team>> getTeamListByPage(TeamQueryRequest teamQuery){
//        if (teamQuery == null){
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        Team team = new Team();
//        BeanUtils.copyProperties(teamQuery, team);
//        Page<Team> page = new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize());
//        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
//        Page<Team> resultPage = teamService.page(page,queryWrapper);
//        return ResultUtils.success(resultPage);
//    }

    /**
     * 加入团队
     *
     * @param teamJoinRequest 团队加入请求
     * @param request         HTTP请求
     * @return {@link BaseResponse}<{@link Boolean}>
     */
    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request){
        if (teamJoinRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User currentUser = userService.getCurrentUser(request);
        boolean result = teamService.joinTeam(teamJoinRequest, currentUser);
        return ResultUtils.success(result);
    }

    /**
     * 退出团队
     *
     * @param teamQuitRequest 团队辞职请求
     * @param request         HTTP请求
     * @return {@link BaseResponse}<{@link Boolean}>
     */
    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request){
        if (teamQuitRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User currentUser = userService.getCurrentUser(request);
        boolean result = teamService.quitTeam(teamQuitRequest, currentUser);
        return ResultUtils.success(result);
    }

    /**
     * 删除团队
     *
     * @param teamDeleteRequest 删除请求
     * @param request           HTTP请求
     * @return {@link BaseResponse}<{@link Boolean}>
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody TeamDeleteRequest teamDeleteRequest, HttpServletRequest request){
        if (teamDeleteRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User currentUser = userService.getCurrentUser(request);
        boolean result = teamService.deleteTeam(teamDeleteRequest, currentUser);
        return ResultUtils.success(result);
    }

    /**
     * 我创建团队名单
     *
     * @param teamQuery   团队查询
     * @param request     HTTP请求
     * @return {@link BaseResponse}<{@link List}<{@link TeamUserVO}>>
     */
    @GetMapping("/list/create")
    public BaseResponse<List<TeamUserVO>> listCreateTeam(TeamQueryRequest teamQuery, HttpServletRequest request){
        if (teamQuery == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User currentUser = userService.getCurrentUser(request);
        teamQuery.setUserId(currentUser.getId());
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, true);
        return ResultUtils.success(teamList);
    }

    /**
     * 我加入团队名单
     *
     * @param teamQuery   团队查询
     * @param request     请求
     * @return {@link BaseResponse}<{@link List}<{@link TeamUserVO}>>
     */
    @GetMapping("/list/join")
    public BaseResponse<List<TeamUserVO>> listJoinTeam(TeamQueryRequest teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User currentUser = userService.getCurrentUser(request);

        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",currentUser.getId());
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);

        Map<Long, List<UserTeam>> listMap = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        List<Long> idList = new ArrayList<>(listMap.keySet());
        teamQuery.setIdList(idList);

        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, true);
        return ResultUtils.success(teamList);
    }


}
