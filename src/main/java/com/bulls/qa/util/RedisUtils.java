package com.bulls.qa.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.params.SetParams;

import javax.annotation.PreDestroy;
import java.util.Map;

//@Component
public class RedisUtils {

    private static final Logger logger = LoggerFactory.getLogger(RedisUtils.class);
    private JedisPoolConfig config;
    JedisPool jedisPool;
    private Jedis jedis;

    public RedisUtils(@Value("${qiho.redis.ip}") String ip, @Value("${qiho.redis.port}") int port,
                      @Value("${qiho.redis.auth}") String auth, @Value("${qiho.redis.index}") int index) {
        config = new JedisPoolConfig();
        config.setMaxTotal(3);
        config.setMaxIdle(3);
        jedisPool = new JedisPool(config, ip, port);
        jedis = jedisPool.getResource();
        //jedis = new Jedis(ip, port);
        jedis.auth(auth);
        jedis.select(index);
    }

    public boolean setValue(String key, String value) {
        boolean result = false;
        if ("OK".equals(jedis.set(key, value))) {
            result = true;
        }
        return result;
    }

    public boolean setValue(String key, String value, int seconds) {
        boolean result = false;
        if ("OK".equals(jedis.set(key, value, SetParams.setParams().ex(seconds)))) {
            result = true;
        }
        return result;
    }

    public boolean hsetValue(String key, Map<String, String> hash) {
        boolean result = false;
        Long res = jedis.hset(key, hash);
        Long except = Long.valueOf(hash.keySet().size());
        if (except == res) {
            result = true;
        } else if (res != 0 && res < except) {
            result = true;
        }
        logger.info("预期:{}，实际成功:{}", except, res);
        return result;
    }

    public boolean hsetValue(String key, String field, String value) {
        boolean result = false;
        if (jedis.hset(key, field, value) >= 0) {
            result = true;
        }
        return result;
    }

    public String getValue(String key) {
        return jedis.get(key);
    }

    public Map<String, String> hgetAll(String key) {
        return jedis.hgetAll(key);
    }

    public String hgetValue(String key, String field) {
        return jedis.hget(key, field);
    }


    public boolean delKey(String key) {
        boolean result = false;
        if (!jedis.exists(key)) {
            logger.info("key:{}，不存在", key);
            return true;
        }
        if (jedis.del(key) > 0) {
            result = true;
        }
        return result;
    }


    public boolean hdelField(String key, String field) {
        boolean result = false;
        if (!jedis.hexists(key, field)) {
            logger.info("key:{},field:{} 不存在", key, field);
            return true;
        }
        if (jedis.hdel(key, field) > 0) {
            result = true;
        }
        return result;
    }

    public void closeConnect() {
        //归还连接池资源，不是真正关闭，但是要执行，否则多了可能会占满连接
        jedis.close();
    }

    @PreDestroy
    public void poolClose() {
        if (this.jedisPool != null) {
            this.jedisPool.close();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        String ip = "47.111.157.152";//自动化环境:autotest-config.duibatest.com.cn
        int port = 6379;
        String auth = "duiba123";
        int index = 0;
        RedisUtils redisUtils = new RedisUtils(ip, port, auth, index);
//        String key = "ljtest";
//        String value = "测试";
        //String result=redisUtils.getValue("ORDER_PRIMITIVE_AUTH_EMAIL");
        String result = redisUtils.getValue("SMS_18513107708");
        System.out.println(result);

//        System.out.println(redisUtils.setValue(key, value));
//        System.out.println(redisUtils.getValue(key));
//        System.out.println(redisUtils.delKey(key));
//        System.out.println(redisUtils.getValue(key));
//        Map<String, String> hash = new HashMap<>();
//        hash.put("1", "test1");
//        hash.put("2", "test2");
//        System.out.println(redisUtils.hsetValue(key, hash));
//        System.out.println(redisUtils.hgetAll(key));
//        System.out.println(redisUtils.hsetValue(key, "2", "啊啊啊啊啊啊"));
//        System.out.println(redisUtils.hgetValue(key, "2"));
//        System.out.println(redisUtils.hdelField(key, "2"));
//        System.out.println(redisUtils.delKey(key));
//        System.out.println(redisUtils.hgetAll(key));

        redisUtils.closeConnect();
    }
}
