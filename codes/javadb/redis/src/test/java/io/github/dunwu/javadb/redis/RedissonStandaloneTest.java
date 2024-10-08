package io.github.dunwu.javadb.redis;

import cn.hutool.core.thread.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @since 2018/6/19
 */
@Slf4j
public class RedissonStandaloneTest {

    private static RedissonClient redissonClient;

    static {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:redisson-standalone.xml");
        redissonClient = (RedissonClient) applicationContext.getBean("standalone");
    }

    @Test
    @DisplayName("测试连接")
    public void testRedissonConnect() {
        // 首先获取redis中的key-value对象，key不存在没关系
        RBucket<String> keyObject = redissonClient.getBucket("key");
        // 如果key存在，就设置key的值为新值value
        // 如果key不存在，就设置key的值为value
        keyObject.set("value");
        String value = keyObject.get();
        System.out.println("value=" + value);
    }

    @Test
    @DisplayName("分布式锁测试")
    public void testLock() {
        // 两个线程任务都是不断再尝试获取或，直到成功获取锁后才推出任务
        // 第一个线程获取到锁后，第二个线程需要等待 5 秒超时后才能获取到锁
        CountDownLatch latch = new CountDownLatch(2);
        ExecutorService executorService = ThreadUtil.newFixedExecutor(2, "获取锁", true);
        executorService.submit(new Task(latch));
        executorService.submit(new Task(latch));

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    // 输出：
    // 17:59:25.896 [获取锁1] [INFO ] i.g.d.j.redis.RedissonStandaloneTest.run -
    //     获取分布式锁成功
    // 17:59:26.888 [获取锁0] [WARN ] i.g.d.j.redis.RedissonStandaloneTest.run -
    //     获取分布式锁失败
    // 17:59:27.889 [获取锁0] [WARN ] i.g.d.j.redis.RedissonStandaloneTest.run -
    //     获取分布式锁失败
    // 17:59:28.891 [获取锁0] [WARN ] i.g.d.j.redis.RedissonStandaloneTest.run -
    //     获取分布式锁失败
    // 17:59:29.892 [获取锁0] [WARN ] i.g.d.j.redis.RedissonStandaloneTest.run -
    //     获取分布式锁失败
    // 17:59:30.895 [获取锁0] [WARN ] i.g.d.j.redis.RedissonStandaloneTest.run -
    //     获取分布式锁失败
    // 17:59:30.896 [获取锁0] [INFO ] i.g.d.j.redis.RedissonStandaloneTest.run -
    //     获取分布式锁成功

    static class Task implements Runnable {

        private CountDownLatch latch;

        public Task(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void run() {
            while (true) {
                RLock lock = redissonClient.getLock("test_lock");
                try {
                    boolean isLock = lock.tryLock(1, 5, TimeUnit.SECONDS);
                    if (isLock) {
                        log.info("获取分布式锁成功");
                        break;
                    } else {
                        log.warn("获取分布式锁失败");
                    }
                } catch (Exception e) {
                    log.error("获取分布式锁异常", e);
                }
            }
            latch.countDown();
        }

    }

}
