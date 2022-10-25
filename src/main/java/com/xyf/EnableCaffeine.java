package com.xyf;


import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

//作用在类上
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(CaffeineFactory.class)
public @interface EnableCaffeine {
    String[] basePackages() default "";
}
