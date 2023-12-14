package com.bulls.qa.util;

import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RocketMqUtils {

    private static final Logger logger = LoggerFactory.getLogger(RocketMqUtils.class);

    private String mqAddress;

    public RocketMqUtils(String mqAddress) {
        this.mqAddress = mqAddress;
    }

    public SendResult sendMessage(String topic, String tags, String keys, String body) {
        DefaultMQProducer producer = new DefaultMQProducer(topic);
        producer.setNamesrvAddr(this.mqAddress);

        try {
            producer.start();

            Message msg = new Message(topic, tags, keys, body.getBytes());

            SendResult result = producer.send(msg);

            logger.info("mqServer_address:" + producer.getNamesrvAddr() + ";  id:" + result.getMsgId()
                    + ";  result:" + result.getSendStatus());
            return result;
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            producer.shutdown();

        }
        return null;
    }


    public static void main(String args[]) {
        String mqAdress = "10.172.58.137:9876";//测试环境
        RocketMqUtils rocketMqProducer = new RocketMqUtils(mqAdress);
        String topic = "smsResultNoticeTopicTest";
        String body = "{\"dbSmsId\":\"348\",\"code\":\"0\",\"desc\":\"成功\"}";
        rocketMqProducer.sendMessage(topic, "1", "1", body);
    }

}
