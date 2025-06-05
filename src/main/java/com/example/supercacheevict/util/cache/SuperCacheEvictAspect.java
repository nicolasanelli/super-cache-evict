package com.example.supercacheevict.util.cache;

import com.example.supercacheevict.util.redis.RedisService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class SuperCacheEvictAspect {

    private final CacheManager cacheManager;
    private final RedisService redisService;
    private final MeterRegistry meterRegistry;
    private final Logger log = LoggerFactory.getLogger(SuperCacheEvictAspect.class);
    private final ExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer paramNameDiscoverer = new DefaultParameterNameDiscoverer();

    public SuperCacheEvictAspect(CacheManager cacheManager, RedisService redisService, MeterRegistry meterRegistry) {
        this.cacheManager = cacheManager;
        this.redisService = redisService;
        this.meterRegistry = meterRegistry;
    }

    @AfterReturning(value = "@annotation(superEvict)", argNames = "joinPoint,superEvict")
    public void after(JoinPoint joinPoint, SuperCacheEvict superEvict) {
        long start = System.nanoTime();

        String cacheName = superEvict.value();
        String keyPatternSpEL = superEvict.keyPattern();
        String evaluatedPattern = evaluateSpEL(joinPoint, keyPatternSpEL);

        log.info("[SuperCacheEvict] Starting eviction for cache '{}', pattern '{}'", cacheName, evaluatedPattern);

        var springCache = cacheManager.getCache(cacheName);
        if (springCache == null) {
            return;
        }

        if (springCache instanceof RedisCache redisCache) {
            String fullKeyPattern = redisCache.getCacheConfiguration().getKeyPrefixFor(cacheName).concat(evaluatedPattern);
            var count = redisService.deleteByPattern(fullKeyPattern);
            log.info("[SuperCacheEvict] {} key(s) removed from cache '{}'", count, cacheName);

            Counter.builder("super_cache_evict.count")
                    .description("Total number of keys removed from cache via @SuperCacheEvict")
                    .tags("cache", cacheName, "pattern", evaluatedPattern)
                    .register(meterRegistry)
                    .increment(count);

            long duration = System.nanoTime() - start;

            Timer.builder("super_cache_evict.duration")
                    .description("Execution duration of cache eviction via @SuperCacheEvict")
                    .tags("cache", cacheName)
                    .register(meterRegistry)
                    .record(duration, TimeUnit.NANOSECONDS);
        }
    }

    private String evaluateSpEL(JoinPoint joinPoint, String spel) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String[] paramNames = paramNameDiscoverer.getParameterNames(method);
        Object[] args = joinPoint.getArgs();

        EvaluationContext context = new StandardEvaluationContext();
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }

        return parser.parseExpression(spel).getValue(context, String.class);
    }
}
