package com.sparta.logistics.application.common;

import com.sparta.logistics.common.code.ErrorResponseCode;
import com.sparta.logistics.common.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


/**
 * Redisson 분산락을 획득한 뒤 주어진 로직을 실행하는 공용 컴포넌트.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLockExecutor {
    private final RedissonClient redissonClient;

    /**
     * 지정된 키에 대해 분산락을 획득한 뒤 action을 실행한다.
     * 획득 대기 3초, 점유 최대 5초(자동 해제)로 데드락을 방지한다.
     *
     *  @param lockKey 락을 걸 대상 식별자. 같은 lockKey로 동시에 여러 요청이 들어오면, 그 중 하나만 락을 획득하고 나머지는 대기(또는 실패)한다.
     *  @param action  락을 획득한 뒤 실제로 실행할 로직. 결과값을 반환한다.
     */
    public <T> T executeWithLock(String lockKey, Callable<T> action) {
        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired = false;
        try {
            // 최대 3초 대기 후 락 획득 시도, 실패하면 즉시 예외
            acquired = lock.tryLock(3, 5, TimeUnit.SECONDS);
            if (!acquired) {
                log.warn("분산락 획득 실패: key={}", lockKey);
                throw new ApiException(ErrorResponseCode.INVALID_REQUEST); // 적절한 코드로 교체 가능
            }
            return action.call();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(ErrorResponseCode.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            // action 내부의 의미 있는 예외는 그대로 살려서 던짐
            if (e instanceof ApiException apiException) {
                throw apiException;
            }
            throw new ApiException(ErrorResponseCode.INTERNAL_SERVER_ERROR);
        } finally {
            // 내가 획득한 락만 해제 (안 그러면 남의 락을 풀려다 예외 발생)
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

}
