package com.enjoy.annotation;

import java.lang.annotation.*;

@Target(ElementType.FIELD)  //在属性上使用
@Retention(RetentionPolicy.RUNTIME)//在运行时可以通过反射获取载体
@Documented //java doc
public @interface EnjoyAutowired {

    String value() default "";
}
