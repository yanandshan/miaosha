package com.qfedu.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public class SendMessageUtils {
    private final static String EXCHANGE_NAME = "fanout_exchange_kc_miao";

    public static void sendMsg(String msg) throws Exception {
        // 获取到连接
        Connection connection = ConnectionUtil.getConnection();
        // 获取通道
        Channel channel = connection.createChannel();
        // 声明exchange，指定类型为fanout
        channel.exchangeDeclare(EXCHANGE_NAME, "fanout",true);

        // 发布消息到Exchange
        channel.basicPublish(EXCHANGE_NAME, "", null, msg.getBytes());
        System.out.println(" [生产者] Sent '" + msg + "'");

        channel.close();
        connection.close();
    }

    public static void main(String[] args) {
        try {
            sendMsg("hello laoyan");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
