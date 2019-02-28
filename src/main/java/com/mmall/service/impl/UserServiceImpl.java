package com.mmall.service.impl;

import com.mmall.common.Constants;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;



@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {

        int resultCount = userMapper.checkUserName(username);
        if (resultCount == 0) {
            return ServerResponse.createErroeMessage("用户名不存在");
        }

        // 密码登录MD5
        String md5Password = MD5Util.MD5EncodeUtf8(password);


        User user = userMapper.selectLogin(username, md5Password);
        if (user == null) {
            return ServerResponse.createErroeMessage("密码错误");
        }

        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登录成功", user);
    }

    @Override
    public ServerResponse<String> register(User user) {

        System.out.println("question:" + user.getQuestion().toString());
        System.out.println("你好");

        ServerResponse validResponse = this.checkValid(user.getUsername(), Constants.USERNAME);
        if (!validResponse.isSuccess()) {
            return validResponse;
        }

        validResponse = this.checkValid(user.getEmail(), Constants.EMAIL);
        if (!validResponse.isSuccess()) {
            return validResponse;
        }

        user.setRole(Constants.Role.ROLE_CUSTOMER);

        //MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));

        int resultCount = userMapper.insert(user);
        if (resultCount == 0) {
            return ServerResponse.createErroeMessage("注册失败");
        }
        return ServerResponse.createBySuccess("注册成功");

    }

    @Override
    public ServerResponse<String> checkValid(String param, String type) {
        if (StringUtils.isNotBlank(type)) {
            //开始校验
            //int resultCount;
            if (Constants.EMAIL.equals(type)) {
                int resultCount = userMapper.checkEmail(param);
                if (resultCount > 0) {
                    return ServerResponse.createErroeMessage("该邮箱已注册");
                }
            }
            if (Constants.USERNAME.equals(type)) {
                int resultCount = userMapper.checkUserName(param);
                if (resultCount > 0) {
                    return ServerResponse.createErroeMessage("用户名已存在");
                }
            }
        } else {
            return ServerResponse.createErroeMessage("参数错误");
        }
        return ServerResponse.createBySuccessMessage("校验成功");
    }


    public ServerResponse<String> selectQuestion(String username) {
        ServerResponse validResponse = this.checkValid(username, Constants.USERNAME);
        if (validResponse.isSuccess()) {
            //checkValid()方法是根据用户名校验用户是否存在，如果不存在返回true
            return ServerResponse.createErroeMessage("用户不存在");
        }
        String question = userMapper.selectQuestionByUsername(username);
        if (StringUtils.isNoneBlank(username)) {
            return ServerResponse.createBySuccess(question);
        }

        return ServerResponse.createErroeMessage("密保问题为空");
    }


    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        int resultCount = userMapper.checkAnswer(username, question, answer);
        if (resultCount > 0) {
            //说明问题和答案是这个用户的，并且是正确的
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX + username, forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createErroeMessage("密保问题回答错误");
    }

    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken) {
        if (StringUtils.isBlank(forgetToken)) {
            return ServerResponse.createErroeMessage("参数错误，token需传递");
        }

        ServerResponse validResponse = this.checkValid(username, Constants.USERNAME);
        if (validResponse.isSuccess()) {
            return ServerResponse.createErroeMessage("用户名不存在");
        }

        String token = TokenCache.getValue(TokenCache.TOKEN_PREFIX + username);
        if (StringUtils.isBlank(token)) {
            return ServerResponse.createErroeMessage("token无效或者过期");
        }

        if (StringUtils.equals(token, forgetToken)) {
            String md5Password = MD5Util.MD5EncodeUtf8(passwordNew);
            int rowCount = userMapper.updatePasswordByUsername(username, md5Password);
            if (rowCount == 1) {
                return ServerResponse.createBySuccessMessage("修改密码成功");
            }
        } else {
            return ServerResponse.createErroeMessage("token不匹配，请重新获取");
        }

        return ServerResponse.createErroeMessage("修改密码失败");

    }

    @Override
    public ServerResponse<String> resetPassword(String passwordOld, String passwordNew, User user) {
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld), user.getId());
        if (resultCount == 0) {
            return ServerResponse.createErroeMessage("旧密码错误，请重新输入");
        }
        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if (updateCount > 0) {
            return ServerResponse.createBySuccessMessage("密码更新成功");
        }
        return ServerResponse.createErroeMessage("密码更新失败");
    }

    @Override
    public ServerResponse<User> updateInformation(User user) {
        //判断email是否已被其他用户占用
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(), user.getId());
        if (resultCount > 0) {
            return ServerResponse.createErroeMessage("当前Email已被占用，请重新输入");
        }

        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());
        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if (updateCount > 0) {
            return ServerResponse.createBySuccess("用户信息更新成功", updateUser);
        }
        return ServerResponse.createErroeMessage("用户信息更新失败");
    }

    @Override
    public ServerResponse<User> getInformation(Integer userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            return ServerResponse.createErroeMessage("找不到当前用户");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }

    @Override
    public ServerResponse checkAdminRole(User user) {
        if (user != null && user.getRole().intValue() == Constants.Role.ROLE_ADMIN) {
            return ServerResponse.createBySuccess();
        }

        return ServerResponse.createError();
    }
}
