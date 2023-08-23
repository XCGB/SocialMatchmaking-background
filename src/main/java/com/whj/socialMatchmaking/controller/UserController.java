package com.whj.socialMatchmaking.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.whj.socialMatchmaking.common.BaseResponse;
import com.whj.socialMatchmaking.common.ErrorCode;
import com.whj.socialMatchmaking.common.ResultUtils;
import com.whj.socialMatchmaking.exception.BusinessException;
import com.whj.socialMatchmaking.model.domain.User;
import com.whj.socialMatchmaking.model.request.*;
import com.whj.socialMatchmaking.service.UserService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.whj.socialMatchmaking.constant.UserConstant.*;

/**
 * @author: Baldwin
 * @createTime: 2023-07-17 13:53
 * @description: 用户控制器
 */
@Slf4j
@RestController
@RequestMapping("/user")
@Tag(name = "user-controller", description = "用户控制器")
//@Api(tags = "用户管理相关接口")
@CrossOrigin(origins = "http://127.0.0.1:5173", allowCredentials = "true")
public class UserController {
    /**
     * 用户服务
     */
    @Resource
    private UserService userService;

    /**
     * redis操作字符串模板类
     */
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 用户登录
     *
     * @param userLoginRequest 用户登录请求封装类
     * @param request          http请求
     * @return {@link BaseResponse}<{@link User}>
     */
    @PostMapping("/login")
    @ApiOperation("用户登录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userAccount", defaultValue = "whj"),
            @ApiImplicitParam(name = "userPassword", defaultValue = "12345678"),
    })
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    /**
     * 用户登出
     *
     * @param request http请求
     * @return {@link BaseResponse}<{@link Integer}>
     */
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Integer result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前用户登录态
     *
     * @param request http请求
     * @return {@link BaseResponse}<{@link User}>
     */
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentObj = (User) userObj;
        if (currentObj == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long userId = currentObj.getId();
        // todo 校验用户是否合法
        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册请求封装类
     * @return {@link BaseResponse}<{@link Long}>
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        Long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 更新用户信息
     *
     * @param user 更新用户信息
     * @param request http请求
     * @return {@link BaseResponse}<{@link Boolean}>
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody User user, HttpServletRequest request){
        // 1. 校验参数是否为空
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getCurrentUser(request);
        Boolean result = userService.updateUser(user, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 根据标签搜索用户
     *
     * @param tagNameList 用户标签列表
     * @return {@link BaseResponse}<{@link List}<{@link User}>>
     */
    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList){
        if (CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException((ErrorCode.PARAMS_ERROR));
        }
        List<User> userList = userService.searchUsersByTags(tagNameList);
        return ResultUtils.success(userList);
    }

    /**
     * 推荐用户（分页）
     *
     * @param pageSize 每页人数
     * @param pageNum 页数
     * @param request http请求
     * @return {@link BaseResponse}<{@link Page}<{@link User}>>
     */
    @GetMapping("recommend")
    public BaseResponse<Page<User>> recommendUsers(long pageSize, long pageNum, HttpServletRequest request){
        User currentUser = userService.getCurrentUser(request);
        String redisKey = String.format(REDIS_KEY_PREFIX, currentUser.getId());
        String value = stringRedisTemplate.opsForValue().get(redisKey);
        Page<User> userPage = JSONUtil.toBean(value, Page.class);
        if (value != null){
            return ResultUtils.success(userPage);
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        Page<User> userList = userService.page(new Page<>(pageNum, pageSize), queryWrapper);
        try {
            stringRedisTemplate.opsForValue().set(redisKey, JSONUtil.toJsonStr(userList),3600, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("redis set key error",e);
        }
        return ResultUtils.success(userList);
    }

    /**
     * 获取匹配用户
     *
     * @param num 用户个数
     * @param request http请求
     * @return {@link BaseResponse}<{@link List}<{@link User}>>
     */
    @GetMapping("/match")
    public BaseResponse<List<User>> matchUsers(long num, HttpServletRequest request) {
        if (num <= 0 || num > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User currentUser = userService.getCurrentUser(request);
        return ResultUtils.success(userService.matchUsers(num, currentUser));

    }

//    /**
//     * 管理员根据ID更新用户信息
//     * @param id 用户ID
//     * @param updateUser 更新用户请求
//     * @param request http请求
//     * @return {@link BaseResponse}<{@link Boolean}>
//     */
//    @PutMapping("/update/{id}")
//    public BaseResponse<Boolean> updateUserById(@PathVariable Long id, @RequestBody UserUpdateRequest updateUser, HttpServletRequest request) {
//        // 1. 获取用户鉴权信息
//        if (!isAdmin(request)) {
//            throw new BusinessException(ErrorCode.NO_AUTH);
//        }
//        Boolean result = userService.updateUserById(id, updateUser);
//        return ResultUtils.success(result);
//    }
//    /**
//     * 查询用户信息
//     *
//     * @param username 用户昵称
//     * @return {@link BaseResponse}<{@link List}<{@link User}>>
//     */
//    @GetMapping("/search")
//    public BaseResponse<List<User>> searchUsers(@RequestParam(required = false) String username,
//                                                HttpServletRequest request) {
//        // 1. 获取用户鉴权信息
//        if (!isAdmin(request)) {
//            throw new BusinessException(ErrorCode.NO_AUTH);
//        }
//
//        // 2. 查询用户信息
//        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//        if (StringUtils.isNotBlank(username)) {
//            // like是 %column%
//            queryWrapper.like("username", username);
//        }
//
//        // 3. 返回脱敏后的用户信息
//        List<User> userList = userService.list(queryWrapper);
//        List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
//
//        return ResultUtils.success(list);
//    }
//
//    /**
//     * 获取用户鉴权
//     *
//     * @param request http请求
//     * @return {@link Boolean}
//     */
//    private boolean isAdmin(HttpServletRequest request) {
//        Object userRole = request.getSession().getAttribute(USER_LOGIN_STATE);
//        User user = (User) userRole;
//        if (user == null) {
//            throw new BusinessException(ErrorCode.NOT_LOGIN);
//        } else if ( user.getUserRole() != ADMIN_ROLE) {
//            throw new BusinessException(ErrorCode.NO_AUTH);
//        }
//        return true;
//    }
}

