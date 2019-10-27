package com.qfedu.mq;

import com.qfedu.expection.BusinessException;
import com.qfedu.expection.EmBusinessError;
import com.qfedu.service.OrderService;
import com.qfedu.utils.JedisClient;
import com.qfedu.utils.JsonUtils;
import com.rabbitmq.client.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.*;

@Component
public class GetMessageUtils {
    private final static String QUEUE_NAME = "fanout_exchange_queue_kc_miao";
    private final static String EXCHANGE_NAME = "fanout_exchange_kc_miao";
    private ExecutorService executorService;
    @Autowired
    OrderService orderService;
    @Autowired
    JedisClient jedisClient;

    @PostConstruct
    public void init() throws Exception {
        executorService = Executors.newFixedThreadPool(20);
        // 获取到连接
        Connection connection = ConnectionUtil.getConnection();
        // 获取通道
        Channel channel = connection.createChannel();
        // 声明队列
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);

        // 绑定队列到交换机
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");

        // 定义队列的消费者
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            // 获取消息，并且处理，这个方法类似事件监听，如果有消息的时候，会被自动调用
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
                // body 即消息体
                String msg = new String(body);
                System.out.println(" [消费者1] received : " + msg + "!");
                Map<String, Object> obj = JsonUtils.jsonToPojo(msg, Map.class);
                // msg转换为  itemId
                // 根据itemId，访问数据库，更改库存，并且下单保存数据
                int itemId = (int) obj.get("itemId");
                String username = (String) obj.get("username");
                int promoId = (int) obj.get("promoId");
                //隐患......
                try {
                    xiaDanJianCk(itemId, username, promoId);
                } catch (BusinessException e) {
                    e.printStackTrace();
                }
            }
        };
        // 监听队列，自动返回完成
        channel.basicConsume(QUEUE_NAME, true, consumer);
    }

    public void xiaDanJianCk(int itemId, String username, int promoId) throws BusinessException {
        //如果拿到令牌的人很多，通过令牌拦截流量的方式就很弱，我们可以使用如下方式
        //拥塞窗口为20的等待队列，用来队列化泄洪
        Future<Object> future = executorService.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                //中间件得到消息，操作数据库，更改库存
                System.out.println("这个有什么问题嘞！");

                // 如果库存减了，而订单没有生成，理论讲是要回滚的
                int result = orderService.decrItemStock(itemId);

                if (result == 1) {
                    //生成订单
                    Date date = new Date();
                    int insertResult = orderService.createOrder(username, promoId, date);
                    if (insertResult == 0) {
                        throw new BusinessException(EmBusinessError.KILLED_AGAIN_ERROR);
                    }

                    jedisClient.set("ORDER_" + itemId + username + promoId, "ORDER_" + itemId + username + promoId);
                    //如果处理的比较好的话，过期时间是活动结束时间
                    jedisClient.expire("ORDER_" + itemId + username + promoId, 3600 * 24 * 3);

                } else {
                    throw new BusinessException(EmBusinessError.ORADER_ERROR);
                }
                return null;
            }
        });

        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new BusinessException(EmBusinessError.UNKNOWN_ERROR);
        }
    }

    public void  xiaDanJianCkPROCEDURE(int itemId,String username,int promoId) throws BusinessException{
        //如果拿到令牌的人很多，通过令牌拦截流量的方式就很弱，我们可以使用如下方式
        //拥塞窗口为20的等待队列，用来队列化泄洪
        Future<Object> future = executorService.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {

                //中间件得到消息，操作数据库，更改库存
                System.out.println("这个有什么问题嘞！");

                // 如果库存减了，而订单没有生成，理论讲是要回滚的
                int result = orderService.createOrderAndDecrStock(itemId,username,promoId);

                if(result == 1){

                    jedisClient.set("ORDER:"+itemId+username+promoId,"ORDER_"+itemId+username+promoId);
                    //如果处理的比较好的话，过期时间是活动结束时间
                    jedisClient.expire("ORDER:"+itemId+username+promoId,3600*24*3);

                }else if(result == -1){
                    throw new BusinessException(EmBusinessError.KILLED_AGAIN_ERROR);
                }else if(result == -2){
                    throw new BusinessException(EmBusinessError.UNKNOWN_ERROR);
                }else if(result == -3){
                    throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);
                }
                return null;
            }
        });

        try {
            future.get();
        }catch (InterruptedException e) {
            throw new BusinessException(EmBusinessError.UNKNOWN_ERROR);
        } catch (ExecutionException e) {
            throw new BusinessException(EmBusinessError.UNKNOWN_ERROR);
        }
    }


}
