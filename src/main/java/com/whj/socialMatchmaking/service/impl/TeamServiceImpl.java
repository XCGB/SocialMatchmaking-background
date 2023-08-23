package com.whj.socialMatchmaking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whj.socialMatchmaking.common.ErrorCode;
import com.whj.socialMatchmaking.exception.BusinessException;
import com.whj.socialMatchmaking.mapper.TeamMapper;
import com.whj.socialMatchmaking.model.domain.Team ;
import com.whj.socialMatchmaking.model.domain.User;
import com.whj.socialMatchmaking.model.domain.UserTeam;
import com.whj.socialMatchmaking.model.dto.TeamQueryRequest;
import com.whj.socialMatchmaking.model.enums.TeamStatusEnum;
import com.whj.socialMatchmaking.model.request.TeamDeleteRequest;
import com.whj.socialMatchmaking.model.request.TeamJoinRequest;
import com.whj.socialMatchmaking.model.request.TeamQuitRequest;
import com.whj.socialMatchmaking.model.request.TeamUpdateRequest;
import com.whj.socialMatchmaking.model.vo.TeamUserVO;
import com.whj.socialMatchmaking.model.vo.UserVO;
import com.whj.socialMatchmaking.service.TeamService;
import com.whj.socialMatchmaking.service.UserService;
import com.whj.socialMatchmaking.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
* @author Baldwin
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2023-07-24 15:43:51
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redissonClient;

    /**
     * 插入队伍信息表到队伍表和关联表（需要保证原子性）
     *
     * @param team   队伍
     * @param userId 用户ID
     * @return long
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    long insertTeamData(Team team, Long userId){
        team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = team.getId();
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"关联表插入失败");
        }
        return teamId;
    }

    /**
     * 加入队伍
     *
     * @param team       队伍
     * @param loginUser  当前用户
     * @return long
     */
    @Override
    public long addTeam(Team team, User loginUser) {
        // 1. 请求参数是否为空？
        if (team == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2. 是否登录，未登录不允许创建
        if (loginUser == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 3. 校验信息
        // 3.1. 队伍人数 > 1 且 <= 20
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum < 1 || maxNum > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍人数不满足需求");
        }
        // 3.2. 队伍标题 <= 20
        String name = team.getName();
        if (StringUtils.isBlank(name) || name.length() > 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍标题不符合要求");
        }
        // 3.3. 描述 <= 512
        String description = team.getDescription();
        if (StringUtils.isNotBlank(description) && description.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍描述过长");
        }
        // 3.4. status 是否公开（int）不传默认为 0（公开）
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (statusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍状态不满足要求");
        }
        // 3.5. 如果 status 是加密状态，一定要有密码，且密码 <= 32
        String password = team.getPassword();
        if (TeamStatusEnum.SECRET.equals(statusEnum)) {
            if ((StringUtils.isBlank(password) || password.length() > 32)){
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"密码设置不正确");
            }
        }
        // 3.6. 超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if (new Date().after(expireTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"超时时间大于当前时间");
        }
        // 3.7. 校验用户最多创建 5 个队伍
        //TODO 用户可能同时创建100个队伍，要加锁
        final long userId = loginUser.getId();
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        long teamCount = this.count(queryWrapper);
        if (teamCount >= 5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户最多创建5个队伍");
        }
        // 4. 插入队伍信息到队伍表
        // 5. 插入用户  => 队伍关系到关系表
        return insertTeamData(team, userId);
    }

    /**
     * 查询队伍（公告|加密）
     *
     * @param teamQuery 查询队伍请求
     * @param isAdmin   是否是管理员
     * @return {@link List}<{@link TeamUserVO}>
     */
    @Override
    public List<TeamUserVO> listTeams(TeamQueryRequest teamQuery, boolean isAdmin){
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        if (teamQuery != null){
            Long id = teamQuery.getId();
            if (id != null && id > 0){
                queryWrapper.eq("id",id);
            }
            List<Long> idList = teamQuery.getIdList();
            if (CollectionUtils.isNotEmpty(idList)){
                queryWrapper.in("id",idList);
            }
            String searchText = teamQuery.getSearchText();
            if (StringUtils.isNotBlank(searchText)){
                queryWrapper.and(qw -> qw.like("name",searchText).or().like("description",searchText));
            }
            String name = teamQuery.getName();
            if (StringUtils.isNotBlank(name)){
                queryWrapper.like("name",name);
            }
            String description = teamQuery.getDescription();
            if (StringUtils.isNotBlank(description)){
                queryWrapper.like("description",description);
            }
            Integer maxNum = teamQuery.getMaxNum();
            if (maxNum != null && maxNum > 0) {
                queryWrapper.eq("max_num", maxNum);
            }
            Long userId = teamQuery.getUserId();
            if (userId != null && userId >0) {
                queryWrapper.eq("user_id", userId);
            }
            Integer status = teamQuery.getStatus();
            TeamStatusEnum enumByValue = TeamStatusEnum.getEnumByValue(status);
            if (enumByValue == null){
                enumByValue = TeamStatusEnum.PUBLIC;
            }
            if (!isAdmin && enumByValue.equals(TeamStatusEnum.PRIVATE)) {
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
            queryWrapper.eq("status", enumByValue.getValue());
        }
        // 不显示过期队伍
        queryWrapper.and(qw -> qw.gt("expire_time", new Date()).or().isNull("expire_time"));
        List<Team> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }
        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if (userId == null) {
                continue;
            }
            User user = userService.getById(userId);
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);
            if (user != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                teamUserVO.setCreateUser(userVO);
            }
            teamUserVOList.add(teamUserVO);
        }
        return teamUserVOList;
    }

    /**
     * 更新队伍
     *
     * @param teamUpdateRequest 更新队伍请求
     * @param loginUser         当前用户
     * @return boolean
     */
    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = teamUpdateRequest.getId();
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team oldTeam = this.getById(id);
        if (oldTeam == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        if (!oldTeam.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(teamUpdateRequest.getStatus());
        if (statusEnum.equals(TeamStatusEnum.SECRET)) {
            if (StringUtils.isBlank(teamUpdateRequest.getPassword())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密房间必须设置密码");
            }
        }

        // 比较新值和旧值，如果相同则不更新数据库
        if (isEqual(teamUpdateRequest, oldTeam)) {
            return true; // 不执行更新操作，直接返回成功
        }

        Team updateTeam = new Team();
        BeanUtils.copyProperties(teamUpdateRequest, updateTeam);
        return this.updateById(updateTeam);
    }

    /**
     * 加入队伍
     *
     * @param teamJoinRequest 加入队伍请求
     * @param loginUser       当前用户
     * @return boolean
     */
    @Override
    public boolean joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, User loginUser) {
        Long teamId = teamJoinRequest.getTeamId();
        Team team = getTeamById(teamId);

        Date expireTime = team.getExpireTime();
        if (expireTime != null && expireTime.before(new Date())) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍已过期");
        }

        Integer status = team.getStatus();
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if (TeamStatusEnum.PRIVATE.equals(teamStatusEnum)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "禁止加入私有队伍");
        }

        String password = teamJoinRequest.getPassword();
        if (TeamStatusEnum.SECRET.equals(teamStatusEnum)) {
            if (StringUtils.isBlank(password) || !password.equals(team.getPassword())) {
                throw new BusinessException(ErrorCode.NULL_ERROR, "密码错误");
            }
        }

        // 获取用户已加入队伍数量，不需要获取锁
        Long userId = loginUser.getId();
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        long userJoinCount = userTeamService.count(queryWrapper);
        if (userJoinCount > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户最多加入五个队伍");
        }

        // 不能重复加入已加入的队伍，不需要获取锁
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("team_id", teamId);
        long hasUserJoinTeam = userTeamService.count(queryWrapper);
        if (hasUserJoinTeam > 0) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "用户已加入该队伍");
        }

        // 获取队伍已加入人数，不需要获取锁
        long teamJoinNum = this.countTeamUserByTeamId(teamId);
        if (teamJoinNum >= team.getMaxNum()) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍已满");
        }

        // 获取锁，仅包含数据写入操作
        RLock lock = redissonClient.getLock("HJ:JoinTeam:doCache:lock");
        try {
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                System.out.println("getLock:" + Thread.currentThread().getName());
                // 加入队伍信息
                UserTeam userTeam = new UserTeam();
                userTeam.setUserId(userId);
                userTeam.setTeamId(teamId);
                userTeam.setJoinTime(new Date());
                return userTeamService.save(userTeam);
            }
        } catch (InterruptedException e) {
            log.error("joinTeamCache error", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return false;
    }

    /**
     * 退出队伍
     *
     * @param teamQuitRequest 退出队伍请求
     * @param currentUser     当前用户
     * @return boolean
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User currentUser) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamQuitRequest.getTeamId();
        Team team = getTeamById(teamId);
        long userId = currentUser.getId();
        UserTeam queryUserTeam = new UserTeam();
        queryUserTeam.setTeamId(teamId);
        queryUserTeam.setUserId(userId);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>(queryUserTeam);
        long count = userTeamService.count(queryWrapper);
        if (count == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未加入队伍");
        }
        // 获取队伍人数
        long teamJoinNum = this.countTeamUserByTeamId(teamId);
        // 仅剩一人、解散
        if (teamJoinNum == 1) {
            this.removeById(teamId);
        } else  {
            // 是否为队长
            if (team.getUserId() == userId) {
                // 权限转移
                // 查询已加入队伍的用户的加入时间(根据ID）
                QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                userTeamQueryWrapper.eq("team_id", teamId);
                userTeamQueryWrapper.last("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
                if (CollectionUtils.isEmpty(userTeamList) || userTeamList.size() <= 1) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                UserTeam nextTUerTeam = userTeamList.get(1);
                Long nextTUerTeamUserId = nextTUerTeam.getUserId();
                // 更新当前队长
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(nextTUerTeamUserId);
                boolean result = this.updateById(updateTeam);
                if (!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新队长失败");
                }
            }
        }
        // 移除关系
        return userTeamService.remove(queryWrapper);
    }

    /**
     * 解散队伍
     *
     * @param teamDeleteRequest 解散队伍请求
     * @param currentUser       当前用户
     * @return boolean
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(TeamDeleteRequest teamDeleteRequest, User currentUser) {
        Long id = teamDeleteRequest.getTeamId();
        Team team = getTeamById(id);
        long teamId = team.getId();
        if (!team.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无解散队伍权限");
        }
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("team_id", teamId);
        userTeamService.remove(userTeamQueryWrapper);
        this.removeById(teamId);

        return false;
    }

    /**
     * 获取队伍已加入人数
     *
     * @param teamId 队伍ID
     * @return long
     */
    private long countTeamUserByTeamId(long teamId) {
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("team_id", teamId);
        return userTeamService.count(queryWrapper);
    }

    /**
     * 根据ID得到队伍
     *
     * @param teamId 队伍ID
     * @return {@link Team}
     */
    private Team getTeamById(Long teamId) {
        if (teamId == null || teamId < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取队伍信息，不需要获取锁
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        }
        return team;
    }

    /**
     * 比较更新队伍是否和原队伍信息一致
     *
     * @param newTeam 新队伍
     * @param oldTeam 原队伍
     * @return boolean
     */
    private boolean isEqual(TeamUpdateRequest newTeam, Team oldTeam) {
        return Objects.equals(newTeam.getName(), oldTeam.getName()) &&
                Objects.equals(newTeam.getDescription(), oldTeam.getDescription()) &&
                Objects.equals(newTeam.getPassword(), oldTeam.getPassword()) &&
                Objects.equals(newTeam.getStatus(), oldTeam.getStatus()) &&
                Objects.equals(newTeam.getExpireTime(), oldTeam.getExpireTime());
    }

}




