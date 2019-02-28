package com.mmall.controller.backend;


import com.mmall.common.Constants;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/manager/user")
public class UserManageController {

    @Autowired
    private IUserService userService;


    /**
     * 管理员登录
     * @param username
     * @param password
     * @param session
     * @return
     */
    @RequestMapping(value = "login.do",method = RequestMethod.POST)
    public ServerResponse<User> login(String username, String password, HttpSession session){

        ServerResponse<User> response=userService.login(username,password);
        if(response.isSuccess()){
            User user=response.getData();
            //判断是否是管理员登录
            if(user.getRole()== Constants.Role.ROLE_ADMIN){
                session.setAttribute(Constants.CURRENT_USER,user);
                return response;
            }else {
                return ServerResponse.createErroeMessage("不是管理员账号，无法登录");
            }
        }
        return response;

    }
}
