package com.qfedu.service.impl;

import com.qfedu.dao.PromoMapper;
import com.qfedu.pojo.ItemPromoDetail;
import com.qfedu.pojo.Promo;
import com.qfedu.service.LocalCacheService;
import com.qfedu.service.PromoService;
import com.qfedu.utils.JedisClient;
import com.qfedu.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Administrator
 * @version 1.0
 * @date 2019/10/14 19:36
 */
@Service
public class PromoServiceImpl implements PromoService {
    @Autowired
    PromoMapper promoMapper;
    @Autowired
    LocalCacheService localCacheService;
    @Autowired
    JedisClient jedisClient;

    @Override
    public List<Promo> getAllGoods() {
        return promoMapper.getAllGoods();
    }

    @Override
    public ItemPromoDetail getItemByitemId(int itemId) {
        // 因为活动还未开始的时候，用户会一直刷新详情页面，所以加一个缓存
        // Guava Cache 做一级缓存，用redis做二级缓存
        // 商品详情和库存分别存放
        ItemPromoDetail promoDetail = null;

        Object object = localCacheService.getFromCommonCache("ITEM_BYID" + itemId);

        if (object == null) {
            if (jedisClient.exists("ITEM_BYID" + itemId)) {
                //guava没有从redis中获取
                String jsonObj = jedisClient.get("ITEMID:" + itemId);
                promoDetail = JsonUtils.jsonToPojo(jsonObj, ItemPromoDetail.class);
            } else {
                //redis中也没有
                promoDetail = promoMapper.getItemByitemId(itemId);

                //将库存也存放入redis中
                int stockNum = promoDetail.getStock();
                jedisClient.set("ITEM_STOCK:"+itemId,stockNum+"");

                //获取商品是，根据库存可以设置大闸的数量
                jedisClient.set("ITEM_DAZHA:"+itemId,(stockNum*5)+"");

                String json = JsonUtils.objectToJson(promoDetail);
                jedisClient.set("ITEMID:"+itemId,json);

                localCacheService.setCommonCache("ITEMID:"+itemId,promoDetail);
            }
        } else {
            promoDetail = (ItemPromoDetail) object;
        }

        return promoDetail;
    }


}

