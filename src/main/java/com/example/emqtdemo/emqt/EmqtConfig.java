package com.example.emqtdemo.emqt;

import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.hawtdispatch.DispatchQueue;
import org.fusesource.mqtt.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yangxin
 * @date 2019/4/23
 */
@Configuration
public class EmqtConfig {

    @Value("${emqt.host}")
    private String host;

    @Value("${emqt.clientId}")
    private String clientId;

    @Value("${emqt.subcribe.topic}")
    private String topicName;

    private static Lock lock = new ReentrantLock();

    private static Map<Long, Integer> onlineMap = new ConcurrentHashMap<>();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Bean
    public MQTT mqtt() {
        try {
            logger.info("====连接到mqtt===");
            MQTT mqtt = new MQTT();
            mqtt.setHost(host);
            mqtt.setClientId(clientId);
            mqtt.setReconnectDelay(100);
            mqtt.setKeepAlive((short) 20);
            return mqtt;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    @Bean
    public CallbackConnection callbackConnection(MQTT mqtt) {
        try {
            CallbackConnection connection = mqtt.callbackConnection();

            connection.listener(new Listener() {
                @Override
                public void onConnected() {
                    logger.info("连接成功");
                }

                @Override
                public void onDisconnected() {
                    logger.info("断开连接");
                }

                @Override
                public void onPublish(UTF8Buffer topic, Buffer message, Runnable callback) {
                    try {
                        lock.lock();

                        logger.info("收到topic:" + topic.toString() + "消息为：" + message.utf8());
                        //表示监听成功
                        String topicName = topic.toString();
                        if (topicName.startsWith("/liveOnline")) {
                            Long liveId = findNum(topicName);
                            Integer integer = onlineMap.get(liveId);
                            if (integer == null) {
                                integer = 0;
                            }
                            onlineMap.put(liveId, ++integer);
                        }

                    }finally {
                        callback.run();
                        lock.unlock();
                    }

                }

                @Override
                public void onFailure(Throwable throwable) {
                    logger.error(throwable.getMessage(), throwable);
                }
            });

            connection.connect(new Callback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    //连接成功后会默认订阅主题($client/mengsu)
                    logger.info(clientId + "连接成功");
                }

                @Override
                public void onFailure(Throwable throwable) {
                    logger.error(throwable.getMessage(), throwable);
                }
            });

            //新建一个主题
            Topic[] topic = new Topic[]{new Topic(topicName, QoS.EXACTLY_ONCE),new Topic("/liveOnline/#",QoS.EXACTLY_ONCE)};

            connection.subscribe(topic, new Callback<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    logger.info(clientId + " topic订阅成功");
                }

                @Override
                public void onFailure(Throwable throwable) {
                    logger.info(clientId + " topic订阅 失败");
                    logger.error(throwable.getMessage(), throwable);
                }
            });

           /* connection.publish("/live/1", "这是服务器自己发出来的消息".getBytes(), QoS.AT_LEAST_ONCE, true,new Callback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    System.out.println("发送成功");

                }

                @Override
                public void onFailure(Throwable throwable) {
                    throwable.printStackTrace();
                }
            });*/


            DispatchQueue dispatchQueue = connection.getDispatchQueue();
            dispatchQueue.execute(new Runnable() {
                public void run() {
                    //在这里进行相应的订阅、发布、停止连接等等操作
                    System.out.println("在这里进行相应的订阅、发布、停止连接等等操作");
                }
            });

            return connection;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    private static Long findNum(String str) {
        String regEx="[^0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        String result = m.replaceAll("").trim();
        return Long.valueOf(result);
    }

    public int getOnlineCount(Long liveId){
        try {
            lock.lock();
            Integer integer = onlineMap.get(liveId);
            return integer == null ? 0 : integer;
        }finally {
            lock.unlock();
        }
    }

}