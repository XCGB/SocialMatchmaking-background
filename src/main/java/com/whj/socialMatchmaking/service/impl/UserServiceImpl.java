package com.whj.socialMatchmaking.service.impl;

import cn.hutool.core.lang.Pair;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.whj.socialMatchmaking.common.ErrorCode;
import com.whj.socialMatchmaking.constant.UserConstant;
import com.whj.socialMatchmaking.exception.BusinessException;
import com.whj.socialMatchmaking.model.domain.User;
import com.whj.socialMatchmaking.service.UserService;
import com.whj.socialMatchmaking.mapper.UserMapper;
import com.whj.socialMatchmaking.utils.AlgorithmUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.whj.socialMatchmaking.constant.UserConstant.ADMIN_ROLE;
import static com.whj.socialMatchmaking.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author Baldwin
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createTime: 2023-07-13 19:38:48
*/
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    // 盐值,混淆密码
    private static final String SALT = "Baldwin";

    /**
     * 校验注册用户参数合法性
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     */
    private void validateUserRegisterParams(String userAccount, String userPassword, String checkPassword) {
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() > 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户名过长");
        }
        if (userAccount.length() < 3) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账户小于3");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码或校验密码小于八位");
        }
    }

    /**
     * 校验账户是否包含特殊字符
     *
     * @param userAccount 用户账户
     */
    private void validateUserAccountSpecialCharacters(String userAccount) {
        String volidPattern = "[`~!@#$%^&*()+=|{}:;\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？']";
        Matcher matcher = Pattern.compile(volidPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户不能包含特殊字符");
        }
    }

    /**
     * 校验账户是否重复
     *
     * @param userAccount 用户账户
     */
    private void validateUserAccountDuplicate(String userAccount) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户重复");
        }
    }

    /**
     * 校验密码是否一致
     *
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     */
    private void validatePasswordMatch(String userPassword, String checkPassword) {
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码和校验密码不匹配");
        }
    }

    /**
     * 密码加密
     *
     * @param userPassword 用户密码
     * @return {@link String}
     */
    private String encryptPassword(String userPassword) {
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

    /**
     * 创建用户
     *
     * @param userAccount     用户账号
     * @param encryptPassword 加密后的密码
     * @return {@link User}
     */
    private User createUser(String userAccount, String encryptPassword) {
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        return user;
    }

    /**
     * 用户注册
     * @param userAccount   用户账号
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 用户ID
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        validateUserRegisterParams(userAccount, userPassword, checkPassword);
        validateUserAccountSpecialCharacters(userAccount);
        validateUserAccountDuplicate(userAccount);
        validatePasswordMatch(userPassword, checkPassword);

        String encryptPassword = encryptPassword(userPassword);
        User user = createUser(userAccount, encryptPassword);
        boolean result = this.save(user);
        if (!result) {
            throw new BusinessException(ErrorCode.INSERT_ERROR, "无法插入数据");
        }

        return user.getId();
    }

    /**
     * 用户登录
     *
     * @param userAccount   用户账号
     * @param userPassword  用户密码
     * @param request       HTTP请求
     * @return {@link User}
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 3) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户长度小于3");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度小于8");
        }

        validateUserAccountSpecialCharacters(userAccount);

        // 2. 查询账户
        // 密码加密
        String encryptPassword = encryptPassword(userPassword);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        queryWrapper.eq("user_password", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户登录失败,账户或密码错误");
        }

        // 3. 用户脱敏
        User safetyUser = getSafetyUser(user);

        // 4. 记录用户登录状态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }

    /**
     * 用户脱敏
     *
     * @param originUser 原用户信息
     * @return {@link User}
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setProfile(originUser.getProfile());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setTags(originUser.getTags());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setGmtCreate(originUser.getGmtCreate());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserPassword(null);
        return safetyUser;
    }

    /**
     * 用户登出
     *
     * @param request HTTP请求
     * @return {@link Integer}
     */
    @Override
    public Integer userLogout(HttpServletRequest request) {
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }


    /**
     * 根据标签搜索用户（通过内存计算）
     *
     * @param tagList 用户标签列表
     * @return {@link List}<{@link User}>
     */
    @Override
    public List<User> searchUsersByTags(List<String> tagList){
        if (CollectionUtils.isEmpty(tagList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 1. 查询所有用户
//        long startTime = System.currentTimeMillis();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        // 2. 在内存中查询
        Gson gson = new Gson();
        userList = userList.stream().filter(user -> {
            String tagStr = user.getTags();
            // 判空（通过Optional 链式调用）减少圈复杂度
            tagStr = Optional.ofNullable(tagStr).orElse("");

            Set<String> tagNameSet = gson.fromJson(tagStr,new TypeToken<Set<String>>(){}.getType());
            // 判空（通过Optional 链式调用）减少圈复杂度
            tagNameSet = Optional.ofNullable(tagNameSet).orElse(new HashSet<>());

            for (String tagName : tagList) {
                if (!tagNameSet.contains(tagName)){
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
//        log.info("memory query time = " + (System.currentTimeMillis() - startTime));
        return userList;
    }

    /**
     * 获取当前用户
     *
     * @param request HTTP请求
     * @return {@link User}
     */
    @Override
    public User getCurrentUser(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return user;
    }

    /**
     * 是否为管理员
     * @param request HTTP请求
     * @return true/false
     */
    public boolean isAdmin(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        return user != null && user.getUserRole() == UserConstant.ADMIN_ROLE;
    }

    /**
     * 更新用户信息
     *
     * @param user      需要更新的用户
     * @param loginUser 当前用户
     * @return {@link Boolean}
     */
    @Override
    public Boolean updateUser(User user, User loginUser) {
        Long userId = user.getId();
        if (userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2. 校验权限
        // 2.1 管理员可以更新任意信息
        // 2.2 用户只能更新自己的信息
        if (!isAdmin(loginUser) && !userId.equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User oldUser = this.getById(user.getId());
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        // 3. 触发更新
        int i = this.baseMapper.updateById(user);
        return i > 0;
    }

    /**
     * 匹配相似用户
     *
     * @param num         匹配个数
     * @param currentUser 当前用户
     * @return {@link List}<{@link User}>
     */
    @Override
    public List<User> matchUsers(long num, User currentUser) {
        // 过滤非空 (3.46s)
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id","tags");
        queryWrapper.isNotNull("tags");
        List<User> userList = this.list(queryWrapper);
//        List<User> userList = this.list(); // 不添加过滤（14.95s - 13.15s)
        // 获取当前用户的标签列表
        String tags = currentUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
        // 用户列表的下标 => 相似度
//        SortedMap<Integer, Long> indexDistanceMap = new TreeMap<>();
        List<Pair<User,Long>> list = new ArrayList<>();
        for (User user : userList) {
            String userTags = user.getTags();
            // 无标签
            if (StringUtils.isBlank(userTags) || user.getId().equals(currentUser.getId())) {
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            // 计算分数
            long distance = AlgorithmUtils.minDistance(tagList, userTagList);
            list.add(new Pair<>(user, distance));
        }

        List<Pair<User,Long>> topUserPairList = list.stream()
                .sorted((a, b) -> (int) (a.getValue() - b.getValue()))
                .limit(num)
                .collect(Collectors.toList());
        List<Long> userIdList = topUserPairList.stream().map(pair -> pair.getKey().getId()).collect(Collectors.toList());
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", userIdList);
        Map<Long,List<User>> usersIdListMap = this.list(userQueryWrapper)
                .stream()
                .map(this::getSafetyUser)
                .collect(Collectors.groupingBy(User::getId));
        List<User> finalUserList = new ArrayList<>();
        for (Long userId : userIdList)  {
            finalUserList.add(usersIdListMap.get(userId).get(0));
        }
        return finalUserList;
    }

    /**
     * 根据标签搜索用户（通过SQL查询）
     *
     * @param tagList 用户标签列表
     * @return {@link List}<{@link User}>
     */
    @Deprecated
    public List<User> searchUsersByTagsBySQL(List<String> tagList){
        if (CollectionUtils.isEmpty(tagList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 方法一 使用SQL查询（简单）
        userMapper.selectCount(null);
//        long startTime = System.currentTimeMillis();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        for (String tagName : tagList) {
            queryWrapper = queryWrapper.like("tags",tagName);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
//        log.info("sql query time = " + (System.currentTimeMillis() - startTime));
        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }

    /**
     * 是否为管理员
     *
     * @param loginUser 登录的用户
     * @return true/false
     */
    @Override
    public boolean isAdmin(User loginUser) {
        return loginUser.getUserRole() == ADMIN_ROLE;
    }

//    @Override
//    public Long ImportUser(UserInfo userInfo) {
//        String username = userInfo.getName();
//        String uuid = UUID.randomUUID().toString();
//        String shortUUID = uuid.substring(0, 15);
//
//        String userAccount = "hj-user-" + shortUUID;
//        String userPassword = "12345678";
//        Integer gender = userInfo.getGender();
//        String encryptPassword = encryptPassword(userPassword);
//
//        // 插入数据
//        User user = new User();
//        user.setUsername(username);
//        user.setUserAccount(userAccount);
//        user.setUserPassword(encryptPassword);
//        user.setGender(gender);
//
//        boolean result = this.save(user);
//        if (!result) {
//            throw new BusinessException(ErrorCode.INSERT_ERROR, "无法插入数据");
//        }
//        return user.getId();
//    }

}








