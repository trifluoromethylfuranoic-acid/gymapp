package com.epam.lenda.gymapp.aspect;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {
    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object logControllerInvocation(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            Object result = joinPoint.proceed();
            log.trace("Invoking controller method {} with arguments {}, response: {}", joinPoint.getSignature(),
                    Arrays.toString(joinPoint.getArgs()), result);
            return result;
        } catch (Throwable e) {
            log.trace("Invoking controller method {} with arguments {}, exception: {}", joinPoint.getSignature(),
                    Arrays.toString(joinPoint.getArgs()), e.toString());
            throw e;
        }
    }
}
