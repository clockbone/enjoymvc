package com.enjoy.annotation;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)  //用于传参
@Retention(RetentionPolicy.RUNTIME)//在运行时可以通过反射获取载体
@Documented //java doc
public @interface EnjoyReqestParam {

    String value() default "";
}
