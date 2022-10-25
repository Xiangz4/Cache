package com.xyf;

import java.lang.annotation.*;

//作用在方法上，并且编译时能够进行获取
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface CaffeineAnnotation {
    String key() default "";
}
