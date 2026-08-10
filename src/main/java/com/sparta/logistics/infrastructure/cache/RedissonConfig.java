package com.sparta.logistics.infrastructure.cache;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Redisson 클라이언트를 Spring Bean으로 등록하는 설정 클래스.
 * 이 Bean(RedissonClient)을 통해 분산락(RLock) 등 Redisson이 제공하는 기능을 사용할 수 있다.
 */
@Configuration
public class RedissonConfig {

    // application.yml의 spring.data.redis.host 값을 그대로 주입받음 (Redis 캐싱에서 쓰던 것과 동일한 값)
    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private String port;

    /**
     * RedissonClient를 Spring Bean으로 등록한다.
     * destroyMethod = "shutdown" : 애플리케이션이 종료될 때 Redisson 내부 연결(스레드풀, 커넥션 등)을 자동으로 정리(shutdown)하도록 지정.
     * 이걸 안 해주면 애플리케이션 종료 시 리소스가 안 닫혀서 스레드가 계속 떠있는 등의 문제가 생길 수 있음.
     */
    @Bean(destroyMethod = "shutdown")
    public RedissonClient  redissonConfig() {

        // Redisson 자체 설정 객체
        Config config = new Config();

        // useSingleServer() : Redis가 클러스터가 아니라 단일 서버(standalone)로 구성되어 있다는 뜻
        // setAddress() : 접속할 Redis 서버 주소를 "redis://호스트:포트" 형식으로 지정
        config.useSingleServer()
                .setAddress("redis://" + host + ":" + port);

        // 위 설정(config)을 바탕으로 실제 RedissonClient 인스턴스를 생성해서 반환.
        // 이 반환값이 Spring 컨테이너에 Bean으로 등록되고,
        // 이후 다른 클래스(예: DistributedLockExecutor)에서 생성자 주입으로 가져다 쓸 수 있게 됨.
        return Redisson.create(config);
    }
}
