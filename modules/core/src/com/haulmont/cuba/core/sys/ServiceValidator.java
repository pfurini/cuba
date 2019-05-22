/*
 * Copyright (c) 2008-2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.haulmont.cuba.core.sys;

import com.haulmont.cuba.core.global.BeanValidation;
import com.haulmont.cuba.core.global.validation.MethodParametersValidationException;
import com.haulmont.cuba.core.global.validation.MethodResultValidationException;
import com.haulmont.cuba.core.global.validation.ServiceMethodConstraintViolation;
import com.haulmont.cuba.core.global.validation.groups.ServiceParametersChecks;
import com.haulmont.cuba.core.global.validation.groups.ServiceResultChecks;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Nullable;
import javax.validation.ConstraintViolation;
import javax.validation.executable.ExecutableValidator;
import javax.validation.groups.Default;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;

public class ServiceValidator {
    private static final Logger log = LoggerFactory.getLogger(ServiceValidator.class);

    private BeanValidation beanValidation;

    public void setBeanValidation(BeanValidation beanValidation) {
        this.beanValidation = beanValidation;
    }

    private Object validate(ProceedingJoinPoint ctx) throws Throwable {
        ValidateServiceMethodContext validatedContext = getValidateServiceMethodContext(ctx);
        validateMethodParameters(ctx, validatedContext);

        Object res = ctx.proceed();

        validateMethodResult(ctx, validatedContext, res);
        return res;
    }

    @Nullable
    protected ValidateServiceMethodContext getValidateServiceMethodContext(ProceedingJoinPoint ctx) {
        ValidateServiceMethodContext validatedContext = null;
        if (ctx instanceof MethodInvocationProceedingJoinPoint) {
            MethodInvocationProceedingJoinPoint methodInvocationCtx = (MethodInvocationProceedingJoinPoint) ctx;

            Method method = ((MethodSignature) ctx.getSignature()).getMethod();

            Validated validated = getValidated(method, ctx.getSignature().getDeclaringType());
            if (validated != null) {
                Object[] args = methodInvocationCtx.getArgs();
                ExecutableValidator validator = beanValidation.getValidator().forExecutables();

                validatedContext = new ValidateServiceMethodContext(validator, ctx.getThis(),
                        method, args, validated.value());
            }
        }
        return validatedContext;
    }

    protected void validateMethodParameters(ProceedingJoinPoint ctx,
                                            @Nullable ValidateServiceMethodContext validatedContext) {
        if (validatedContext != null) {
            log.trace("Validating service call params: {}", ctx.getSignature());

            ExecutableValidator validator = validatedContext.getValidator();

            Class[] constraintGroups = validatedContext.getGroups();
            if (constraintGroups.length == 0) {
                constraintGroups = new Class[]{Default.class, ServiceParametersChecks.class};
            }

            Set<ConstraintViolation<Object>> violations = validator.validateParameters(
                    validatedContext.getTarget(),
                    validatedContext.getMethod(),
                    validatedContext.getArgs(),
                    constraintGroups);

            if (!violations.isEmpty()) {
                Class serviceInterface = ctx.getSignature().getDeclaringType();
                Set<ConstraintViolation<Object>> resultViolations = violations.stream()
                        .map(violation -> new ServiceMethodConstraintViolation(serviceInterface, violation))
                        .collect(Collectors.toSet());

                throw new MethodParametersValidationException("Service method parameters validation failed", resultViolations);
            }
        }
    }

    protected void validateMethodResult(ProceedingJoinPoint ctx, ValidateServiceMethodContext validatedContext,
                                        Object methodResult) {
        if (validatedContext != null) {
            ExecutableValidator validator = validatedContext.getValidator();

            log.trace("Validating service call result: {}", ctx.getSignature());

            Class[] constraintGroups = validatedContext.getGroups();
            if (constraintGroups.length == 0) {
                constraintGroups = new Class[]{Default.class, ServiceResultChecks.class};
            }

            Set<ConstraintViolation<Object>> violations = validator.validateReturnValue(
                    validatedContext.getTarget(),
                    validatedContext.getMethod(),
                    methodResult,
                    constraintGroups);

            if (!violations.isEmpty()) {
                Class serviceInterface = ctx.getSignature().getDeclaringType();
                Set<ConstraintViolation<Object>> paramsViolations = violations.stream()
                        .map(violation -> new ServiceMethodConstraintViolation(serviceInterface, violation))
                        .collect(Collectors.toSet());

                throw new MethodResultValidationException("Service method result validation failed", paramsViolations);
            }
        }
    }

    protected Validated getValidated(Method method, Class targetClass) {
        Validated validatedAnn = AnnotationUtils.findAnnotation(method, Validated.class);
        if (validatedAnn == null) {
            validatedAnn = AnnotationUtils.findAnnotation(targetClass, Validated.class);
        }
        return validatedAnn;
    }

    protected static class ValidateServiceMethodContext {
        protected ExecutableValidator validator;
        protected Object target;
        protected Method method;
        protected Object[] args;
        protected Class[] groups;

        public ValidateServiceMethodContext() {
        }

        public ValidateServiceMethodContext(ExecutableValidator validator, Object target, Method method, Object[] args,  Class[] groups) {
            this.validator = validator;
            this.target = target;
            this.method = method;
            this.args = args;
            this.groups = groups;
        }

        public ExecutableValidator getValidator() {
            return validator;
        }

        public void setValidator(ExecutableValidator validator) {
            this.validator = validator;
        }

        public Object getTarget() {
            return target;
        }

        public void setTarget(Object target) {
            this.target = target;
        }

        public Method getMethod() {
            return method;
        }

        public void setMethod(Method method) {
            this.method = method;
        }

        public Class[] getGroups() {
            return groups;
        }

        public void setGroups(Class[] groups) {
            this.groups = groups;
        }

        public Object[] getArgs() {
            return args;
        }

        public void setArgs(Object[] args) {
            this.args = args;
        }
    }
}
