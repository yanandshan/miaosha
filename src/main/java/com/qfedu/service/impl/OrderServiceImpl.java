package com.qfedu.service.impl;

import com.qfedu.dao.ItemMapper;
import com.qfedu.dao.OrderMapper;
import com.qfedu.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator
 * @version 1.0
 * @date 2019/10/18 21:52
 */
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    ItemMapper itemMapper;
   @Override
    public int decrItemStock(int itemId) {
        return itemMapper.decrItemStock(itemId);
    }

    @Override
    public int createOrder(String username, int promoId, Date date) {
        Map<String, Object> map = new HashMap<>();
        map.put("username",username);
        map.put("promoId",promoId+"");
        map.put("createTime",date);
        return orderMapper.createOrder(map);
    }

    @Override
    public int createOrderAndDecrStock(int itemId, String username, int promoId) {

        Map<String,Object> map =new HashMap<String,Object>();
        map.put("promoId",promoId);
        map.put("phone",username);
        map.put("itemId",itemId);
        map.put("result",0);
        orderMapper.createOrderAndStock(map);

        // 1 代表 订单成功，-1 重复秒杀， -2 未知错误 -3 代表库存不足
        int result = (int) map.get("result");
        return result;

    }
}