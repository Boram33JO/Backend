package com.sparta.i_mu.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.host}")
    private String host;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host,port);
        config.setDatabase(0);
        return new LettuceConnectionFactory();
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactoryForDB1() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        config.setDatabase(1);
        return new LettuceConnectionFactory(config);
    }

    /**
     * refreshToken
     *
     * @return
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        // setKeySerializer, setValueSerializer 설정
        // redis-cli을 통해 직접 데이터를 조회 시 알아볼 수 없는 형태로 출력되는 것을 방지
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory());

        return redisTemplate;
    }

    /**
     * 노래 정보
     * @return
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate1() {
        // redisTemplate 를 받아와서 set, get, delete 를 사용
        RedisTemplate<String, Object> redisTemplate1 = new RedisTemplate<>();

        redisTemplate1.setKeySerializer(new StringRedisSerializer());
        redisTemplate1.setValueSerializer(new StringRedisSerializer());
        redisTemplate1.setConnectionFactory(redisConnectionFactoryForDB1());

        return redisTemplate1;
    }

}
