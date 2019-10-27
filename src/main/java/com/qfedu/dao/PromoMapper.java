package com.qfedu.dao;

import com.qfedu.pojo.ItemPromoDetail;
import com.qfedu.pojo.Promo;

import java.util.List;

/**
 * @author Administrator
 * @version 1.0
 * @date 2019/10/14 19:46
 */
public interface PromoMapper {

    List<Promo> getAllGoods();

    ItemPromoDetail getItemByitemId(int itemId);

    int updateStoreByItemId(int id);

}
