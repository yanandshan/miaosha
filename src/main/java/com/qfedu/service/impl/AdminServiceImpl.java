package com.qfedu.service.impl;

import com.qfedu.dao.AdminMapper;
import com.qfedu.pojo.Admin;
import com.qfedu.service.AdminService;

import com.qfedu.utils.JedisClient;
import com.qfedu.utils.UUIDUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Administrator
 * @version 1.0
 * @date 2019/10/14 19:36
 */
@Service
public class AdminServiceImpl implements AdminService {
    @Autowired
    AdminMapper adminMapper;
    @Autowired
    JedisClient jedisClient;

    @Override
    public String login(Admin admin) {
        int result = adminMapper.login(admin);
        if (result > 0) {
            String token = UUIDUtils.getUUID();

            jedisClient.set(admin.getUsername(),token);
            jedisClient.expire(admin.getUsername(), 1800);
            return token;
        } else {
            return null;
        }
    }
}

