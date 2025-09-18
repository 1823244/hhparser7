package com.ens.hhparser5.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Сервис для отслеживания количества вызовов методов с использованием Spring Boot Actuator
 */
@Service
public class MethodCallMetricsService {
    
    private final Logger logger = LoggerFactory.getLogger(MethodCallMetricsService.class);
    private final MeterRegistry meterRegistry;
    private final Map<String, AtomicInteger> methodCallCounts = new ConcurrentHashMap<>();
    
    public MethodCallMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
    
    /**
     * Регистрирует вызов метода и увеличивает счетчик
     * @param methodName имя метода
     */
    public void recordMethodCall(String methodName) {
        // Увеличиваем счетчик в Actuator
        Counter counter = meterRegistry.counter("method.calls", "method", methodName);
        counter.increment();
        
        // Также сохраняем в локальной карте для быстрого доступа
        methodCallCounts.computeIfAbsent(methodName, k -> new AtomicInteger(0)).incrementAndGet();
    }
    
    /**
     * Получает количество вызовов метода
     * @param methodName имя метода
     * @return количество вызовов
     */
    public int getCallCount(String methodName) {
        return methodCallCounts.getOrDefault(methodName, new AtomicInteger(0)).get();
    }
    
    /**
     * Получает все счетчики вызовов методов
     * @return карта с именами методов и количеством вызовов
     */
    public Map<String, Integer> getAllCallCounts() {
        Map<String, Integer> result = new HashMap<>();
        methodCallCounts.forEach((key, value) -> result.put(key, value.get()));
        return result;
    }
    
    /**
     * Выводит статистику вызовов методов в лог
     */
    public void logMethodCallStatistics() {
        logger.info("=== Methods call statistics ===");
        methodCallCounts.forEach((method, count) -> 
            logger.info("Method {} has been called {} times", method, count.get()));
        logger.info("=================================");
    }
}
