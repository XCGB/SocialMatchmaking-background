package com.whj.socialMatchmaking.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whj.socialMatchmaking.model.domain.UserTeam;
import com.whj.socialMatchmaking.service.UserTeamService;
import com.whj.socialMatchmaking.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author Baldwin
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2023-07-24 15:43:35
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




