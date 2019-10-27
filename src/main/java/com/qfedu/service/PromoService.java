package com.qfedu.service;

import com.qfedu.pojo.ItemPromoDetail;
import com.qfedu.pojo.Promo;

import java.util.List;

/**
 * @author Administrator
 * @version 1.0
 * @date 2019/10/14 19:37
 */
public interface PromoService {

    List<Promo> getAllGoods();

    ItemPromoDetail getItemByitemId(int itemId);



}
