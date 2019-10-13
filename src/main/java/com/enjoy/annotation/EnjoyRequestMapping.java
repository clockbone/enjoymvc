package com.enjoy.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE,ElementType.METHOD})  //在类和方法上加
@Retention(RetentionPolicy.RUNTIME)//在运行时可以通过反射获取载体
@Documented //java doc
public @interface EnjoyRequestMapping {

    String value() default "";
}
