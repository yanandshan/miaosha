package com.qfedu.controller;

import com.qfedu.expection.BusinessException;
import com.qfedu.expection.EmBusinessError;
import com.qfedu.pojo.Admin;
import com.qfedu.pojo.ResultEntry;
import com.qfedu.service.AdminService;
import com.qfedu.service.impl.AdminServiceImpl;
import com.qfedu.utils.Md5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Administrator
 * @version 1.0
 * @date 2019/10/14 19:07
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    AdminService adminService;

    @RequestMapping("/login")
    @ResponseBody
    public ResultEntry<String> login(String username, String password) throws BusinessException {
//        System.out.println(username + "," + password);

        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setPassword(Md5Utils.getMd5Str(password));

        String token = adminService.login(admin);

        if (token != null) {
            ResultEntry<String> resultEntry = new ResultEntry<String>(true, token);


            return resultEntry;
        } else {
            throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
        }


    }
}
