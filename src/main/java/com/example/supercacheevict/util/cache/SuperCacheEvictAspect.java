package com.example.supercacheevict.util.cache;

import com.example.supercacheevict.util.redis.RedisService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.cache.CacheManager;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class SuperCacheEvictAspect {

    private final CacheManager cacheManager;
    private final RedisService redisService;
    private final ExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer paramNameDiscoverer = new DefaultParameterNameDiscoverer();

    public SuperCacheEvictAspect(CacheManager cacheManager, RedisService redisService) {
        this.cacheManager = cacheManager;
        this.redisService = redisService;
    }

    @AfterReturning(value = "@annotation(superEvict)", argNames = "joinPoint,superEvict")
    public void after(JoinPoint joinPoint, SuperCacheEvict superEvict) {
        String cacheName = superEvict.value();
        String keyPatternSpEL = superEvict.keyPattern();

        String evaluatedPattern = evaluateSpEL(joinPoint, keyPatternSpEL);

        var springCache = cacheManager.getCache(cacheName);
        if (springCache == null) {
            return;
        }

        if (springCache instanceof RedisCache redisCache) {
            String fullKeyPattern = redisCache.getCacheConfiguration().getKeyPrefixFor(cacheName).concat(evaluatedPattern);
            redisService.deleteByPattern(fullKeyPattern);
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
