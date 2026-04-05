package com.smartcart.order.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Async Thread Pool Configuration.
 *
 * INTERVIEW QUESTION: "How do you manage many threads?"
 * ANSWER: "I use Spring's ThreadPoolTaskExecutor. Instead of creating threads manually,
 * I configure a pool with core size, max size, and queue capacity. Tasks submitted via
 * @Async are executed by pooled threads. This prevents thread explosion, reuses threads,
 * and provides backpressure via the queue. I can monitor pool metrics for tuning."
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);        // minimum threads always alive
        executor.setMaxPoolSize(10);        // max threads under load
        executor.setQueueCapacity(25);      // tasks waiting when all threads busy
        executor.setThreadNamePrefix("smartcart-async-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();

        log.info("Async thread pool initialized: core={}, max={}, queue={}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());

        return executor;
    }
}
