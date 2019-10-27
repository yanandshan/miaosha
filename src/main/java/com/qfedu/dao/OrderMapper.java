package com.qfedu.dao;

import java.util.Map;

/**
 * @author Administrator
 * @version 1.0
 * @date 2019/10/18 21:51
 */
public interface OrderMapper {

    int createOrder(Map<String, Object> map);

    void createOrderAndStock(Map<String, Object> map);
}
