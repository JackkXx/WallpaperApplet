package com.config;
//导入包
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;


//@Configuration
public class RedisConfig {
        //Redis链接工厂
        @Autowired
        private RedisConnectionFactory redisConnectionFactory;

        //缓存操作组件：RedisTemplate的自定义配置
        @Bean
        public RedisTemplate<String, Object> redisTemplate(){
            //创建需要用到的序列化对象
            StringRedisSerializer stringRedisSerializer=new StringRedisSerializer();
            GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer();

            //定义RedisTemplate实例
            RedisTemplate<String, Object> redisTemplate=new RedisTemplate<>();

            //设置Redis的链接工厂
            redisTemplate.setConnectionFactory(redisConnectionFactory);

            //指定大Key序列化策略为String序列化，Value为JDK自带的序列化策略
            redisTemplate.setKeySerializer(RedisSerializer.string());
            redisTemplate.setValueSerializer(jsonRedisSerializer);

            //指定hashKey序列化策略为String序列化-针对hash散列存储
            redisTemplate.setHashKeySerializer(RedisSerializer.string());
            redisTemplate.setHashValueSerializer(jsonRedisSerializer);
            return redisTemplate;
        }

    /*
        不实现序列化
     */
    @Bean
    public RedisTemplate<String,Object> redisTemplateTest(){
        RedisTemplate<String,Object> redisTemplate=new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }
}
