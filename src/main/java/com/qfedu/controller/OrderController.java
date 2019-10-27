package com.qfedu.controller;

import com.alibaba.druid.util.StringUtils;
import com.qfedu.expection.BusinessException;
import com.qfedu.expection.EmBusinessError;
import com.qfedu.mq.SendMessageUtils;
import com.qfedu.pojo.ResultEntry;
import com.qfedu.service.OrderService;
import com.qfedu.utils.JedisClient;
import com.qfedu.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator
 * @version 1.0
 * @date 2019/10/18 21:51
 */
@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    OrderService orderService;
    @Autowired
    JedisClient jedisClient;

    @RequestMapping("/createOrder")
    @Transactional  //添加事务
    public ResultEntry<String> creatOrder(String username, Integer promoId, Integer itemId, String userToken, String promoToken) throws BusinessException {
        //先判断活动令牌合法是否存在，是否正确
        if (!jedisClient.exists(userToken)) {
            throw new BusinessException(EmBusinessError.PROMO_TOKEN_ERROR);
        }
        String s = jedisClient.get(userToken);
        if (!StringUtils.equals(s, promoToken)) {
            throw new BusinessException(EmBusinessError.PROMO_TOKEN_ERROR);
        }
        System.out.println(s);
        System.out.println(promoToken);
        //活动令牌存在，也不一定能看是否是重复订单秒杀
        if (jedisClient.exists("ORDER_" + itemId + username + promoId)) {
            throw new BusinessException(EmBusinessError.KILLED_AGAIN_ERROR);
        } else {
            //秒杀成功，redis库存先减一，然后通过消息中间件给数据库响应，数据库也进行操作
            jedisClient.decr("ITEM_STOCK:" + itemId);

            try {
                Map<String, Object> map = new HashMap<>();
                map.put("itemId", itemId);
                map.put("username", username);
                map.put("promoId", promoId);
                SendMessageUtils.sendMsg(JsonUtils.objectToJson(map));
            } catch (Exception e) {
                throw new BusinessException(EmBusinessError.UNKNOWN_ERROR);
            }
        }

        return new ResultEntry<>(true,"下单成功，请立即支付");
    }


}
