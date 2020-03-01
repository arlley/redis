package com.arlley.redis.aspectj;


import com.arlley.redis.domain.User;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.Objects;

@Component
@Aspect
public class Cache {

    @Resource
    private RedisTemplate redisTemplate;

    @Pointcut("execution(* com.arlley.redis.jdbc.*.get* (..))")
    private void pointCut(){

    }


    @Around("com.arlley.redis.aspectj.Cache.pointCut()")
    public Object cache(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = joinPoint.getTarget().getClass().getDeclaredMethod(signature.getName(), ((MethodSignature) joinPoint.getSignature()).getMethod().getParameterTypes());
        com.arlley.redis.Cache cache = method.getAnnotation(com.arlley.redis.Cache.class);
        if(cache == null){
            return joinPoint.proceed();
        }
        String keyEl = cache.key();


        ExpressionParser parser = new SpelExpressionParser();
        Expression expression = parser.parseExpression(keyEl);
        EvaluationContext context = new StandardEvaluationContext(); // 参数
        // 添加参数
        Object[] args = joinPoint.getArgs();
        DefaultParameterNameDiscoverer discover = new DefaultParameterNameDiscoverer();
        String[] parameterNames = discover.getParameterNames(method);
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i].toString());
        }
        // 解析
        String key = expression.getValue(context).toString();
        Object user =  redisTemplate.opsForHash().get(cache.pre(), key);
        if(Objects.isNull(user)){
            user = joinPoint.proceed();
            System.out.println("从数据库获取");
            redisTemplate.opsForHash().put(cache.pre(), key, user);
        }else{
            System.out.println("从缓存中获取");
        }

        return user;
    }

}
