package com.sparta.logistics;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("CI 환경에 DB/Redis/RabbitMQ 인프라 미구성으로 컨텍스트 로드 불가 - 단위 테스트로 대체")
class LogisticsApplicationTests {

    @Test
    void contextLoads() {
    }

}
