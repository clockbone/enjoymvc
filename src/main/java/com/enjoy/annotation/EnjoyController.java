package com.enjoy.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)  //只能用在类上
@Retention(RetentionPolicy.RUNTIME)//在运行时可以通过反射获取载体
@Documented //java doc
public @interface EnjoyController {

    String value() default "";
}
