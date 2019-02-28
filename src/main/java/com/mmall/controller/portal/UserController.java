package com.mmall.controller.portal;

import com.mmall.common.Constants;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/user/")
public class UserController {

    @Autowired
    private IUserService iUserService;


    /**
     * 登录
     *
     * @param username
     * @param password
     * @param session
     * @return
     */
    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    public ServerResponse<User> login(String username, String password, HttpSession session) {

        ServerResponse<User> response = iUserService.login(username, password);
        if (response.isSuccess()) {
            session.setAttribute(Constants.CURRENT_USER, response.getData());
        }
        return response;
    }

    /**
     * 登出
     *
     * @param session
     * @return
     */
    @RequestMapping(value = "logout.do", method = RequestMethod.GET)
    public ServerResponse<String> logout(HttpSession session) {
        session.removeAttribute(Constants.CURRENT_USER);
        return ServerResponse.createBySuccess();
    }


    /**
     * 注册
     *
     * @param user
     * @return
     */
    @RequestMapping(value = "register.do", method = RequestMethod.POST)
    public ServerResponse<String> register(User user) {
        return iUserService.register(user);
    }


    @RequestMapping(value = "validate.do", method = RequestMethod.GET)
    public ServerResponse<String> checkValid(String param, String type) {
        return iUserService.checkValid(param, type);
    }

    @RequestMapping(value = "queryUserInfo.do", method = RequestMethod.GET)
    public ServerResponse<User> getUserInfo(HttpSession session) {
        User user = (User) session.getAttribute(Constants.CURRENT_USER);
        if (user != null) {
            return ServerResponse.createBySuccess(user);
        }

        return ServerResponse.createErroeMessage("用户未登录，无法获取用户信息");
    }

    @RequestMapping(value = "forgetResetPassword.do", method = RequestMethod.POST)
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken) {


        return iUserService.forgetResetPassword(username, passwordNew, forgetToken);
    }

    @RequestMapping(value = "queryQuestion.do", method = RequestMethod.GET)
    public ServerResponse<String> queryQuestion(String username) {
        return iUserService.selectQuestion(username);
    }

    @RequestMapping(value = "checkAnswer.do", method = RequestMethod.POST)
    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        System.out.println(answer);
        return iUserService.checkAnswer(username, question, answer);
    }

    @RequestMapping(value = "resetPassword.do", method = RequestMethod.POST)
    public ServerResponse<String> resetPassword(HttpSession session, String passwordOld, String passwordNew) {
        User user = (User) session.getAttribute(Constants.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createErroeMessage("用户未登录，请重新登录");
        }
        return iUserService.resetPassword(passwordOld, passwordNew, user);
    }

    @RequestMapping(value = "updateInformation.do",method = RequestMethod.POST)
    public ServerResponse<User> updateInformation(HttpSession session,User user){
        User currentUser = (User) session.getAttribute(Constants.CURRENT_USER);
        if(currentUser==null){
            return ServerResponse.createErroeMessage("用户未登录");
        }
        user.setId(currentUser.getId());
        user.setUsername(currentUser.getUsername());
        ServerResponse<User> response=iUserService.updateInformation(user);
        if(response.isSuccess()){
            //response.getData().setUsername(currentUser.getUsername());
            session.setAttribute(Constants.CURRENT_USER,response.getData());
        }
        return response;
    }

    @RequestMapping(value = "getInformation.do",method = RequestMethod.POST)
    public ServerResponse<User> getInformation(HttpSession session){
        User currentUser = (User) session.getAttribute(Constants.CURRENT_USER);
        if(currentUser==null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),"未登录,需要强制登录status=10");
        }
        return iUserService.getInformation(currentUser.getId());

    }


}
