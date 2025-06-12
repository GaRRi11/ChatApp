package com.gary.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RetryableOperation {
    int maxAttempts() default 3;
    Class<? extends Throwable>[] retryOn() default {Exception.class};
    long delay() default 1000; // milliseconds
}
