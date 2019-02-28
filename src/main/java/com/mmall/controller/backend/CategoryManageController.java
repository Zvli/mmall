package com.mmall.controller.backend;

import com.mmall.common.Constants;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping(value = "manager/category")
public class CategoryManageController {

    @Autowired
    private IUserService userService;

    public ServerResponse addCategory(HttpSession session, String categoryName, @RequestParam(value = "parentId",defaultValue = "0") int parentId){

        User user = (User) session.getAttribute(Constants.CURRENT_USER);

        if(userService.checkAdminRole(user).isSuccess()){
            return null;
        }else {
            return ServerResponse.createErroeMessage("无权限操作,需要管理员权限");
        }
    }


}
