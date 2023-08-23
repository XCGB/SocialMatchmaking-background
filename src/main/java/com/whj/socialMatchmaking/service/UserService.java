package com.whj.socialMatchmaking.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whj.socialMatchmaking.model.domain.User;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author Baldwin
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2023-07-13 19:38:48
*/
public interface UserService extends IService<User> {
    long userRegister(String userAccount, String userPassword, String checkPassword);

    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    User getSafetyUser(User originUser);

    Integer userLogout(HttpServletRequest request);

    List<User> searchUsersByTags(List<String> tagList);

    User getCurrentUser(HttpServletRequest request);

    boolean isAdmin(HttpServletRequest request);

    boolean isAdmin(User loginUser);

    Boolean updateUser(User user, User loginUser);

    List<User> matchUsers(long num, User currentUser);

//    Long ImportUser(UserInfo userInfo);
}

