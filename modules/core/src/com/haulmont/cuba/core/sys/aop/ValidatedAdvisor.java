package com.haulmont.cuba.core.sys.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.validation.annotation.Validated;

public class ValidatedAdvisor {
    private final MethodInterceptor interceptor;
    private final StaticMethodMatcherPointcut pointcut = new TimingAnnotationOnClassOrInheritedInterfacePointcut();

    public ValidatedAdvisor (TimerContext timerContext) {
        super();
        this.interceptor = (MethodInvocation invocation) -> timerContext.runThrowable(invocation.getMethod().getName(),
                invocation::proceed);
    }

    @Override
    public Pointcut getPointcut() {
        return this.pointcut;
    }

    @Override
    public Advice getAdvice() {
        return this.interceptor;
    }

    private final class TimingAnnotationOnClassOrInheritedInterfacePointcut extends StaticMethodMatcherPointcut {
        @Override
        public boolean matches(Method method, Class<?> targetClass) {
            if (AnnotationUtils.findAnnotation(method, Validated.class) != null) {
                return true;
            }
            return AnnotationUtils.findAnnotation(targetClass, Validated.class) != null;
        }
    }
}
}
