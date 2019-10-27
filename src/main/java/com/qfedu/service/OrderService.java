package com.qfedu.service;

import java.util.Date;

/**
 * @author Administrator
 * @version 1.0
 * @date 2019/10/18 21:52
 */
public interface OrderService {
    int decrItemStock(int itemId);

    int createOrder(String userName, int promoId, Date date);

    int createOrderAndDecrStock(int itemId, String username, int promoId);
}

