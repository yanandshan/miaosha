package com.qfedu.controller;

import com.alibaba.druid.util.StringUtils;
import com.qfedu.expection.BusinessException;
import com.qfedu.expection.EmBusinessError;
import com.qfedu.pojo.ItemPromoDetail;
import com.qfedu.pojo.Promo;
import com.qfedu.pojo.ResultEntry;
import com.qfedu.service.PromoService;
import com.qfedu.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @version 1.0
 * @date 2019/10/14 20:46
 */
@RestController
@RequestMapping("/promo")
public class PromoController {
    @Autowired
    PromoService promoService;
    @Autowired
    JedisClient jedisClient;

    @RequestMapping("/list")
    @ResponseBody
    public ResultEntry<List<Promo>> goodsList() {
        List<Promo> list = promoService.getAllGoods();
        return new ResultEntry<List<Promo>>(true, list);
    }

    @RequestMapping("/getItem")
    @ResponseBody
    public ResultEntry<ItemPromoDetail> getItem(int itemId, String username, String token) throws BusinessException {
        ItemPromoDetail promoDetail = null;
        if (jedisClient.exists(username)) {
            String token1 = jedisClient.get(username);

            if (!StringUtils.equals(token1, token)) {
                throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
            }
        } else {
            throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
        }
        promoDetail = promoService.getItemByitemId(itemId);
        return new ResultEntry<ItemPromoDetail>(true, promoDetail);

    }

    @RequestMapping("/createCode/{userToken}")
    public void createYzm(HttpServletResponse response, @PathVariable("userToken") String userToken){
        Map<String,Object> map = CodeUtil.generateCodeAndPic();
        try {
            ImageIO.write((RenderedImage) map.get("codePic"), "jpeg", response.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("验证码的值为："+map.get("code"));
        String yzmCode = map.get("code").toString();

        jedisClient.set("validateCode:"+userToken,yzmCode);
        jedisClient.expire("validateCode:"+userToken,60);

    }

    @RequestMapping("/createToken")
    @ResponseBody
    public ResultEntry<String> createToken(int itemId, int promoId, String userToken,String yzm) throws BusinessException {
        if (jedisClient.exists("validateCode:"+userToken)){
            //验证码是否存在
            final String s = jedisClient.get("validateCode:" + userToken);
            if (StringUtils.equals(s,yzm)){
                //验证码是否正确
                int dazha = Integer.valueOf(jedisClient.get("ITEM_DAZHA:" + itemId));
                if (dazha <= 0) {
                    //设置商品总数的5倍人数发放token令牌
                    throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);
                } else {
                    //生成一个token令牌给购买者，持令牌可下单成功
                    String token = UUIDUtils.getUUID() + itemId + promoId;

                    jedisClient.set(userToken, token);
                    jedisClient.expire(userToken, 180);
                    jedisClient.decr("ITEM_DAZHA:" + itemId);
                    return new ResultEntry<String>(true,token);
                }

            }else {
                throw new BusinessException(EmBusinessError.VALIDATE_CODE_ERROR);
            }
        }else {
            throw new BusinessException(EmBusinessError.VALIDATE_CODE_ERROR);
        }





    }


}
