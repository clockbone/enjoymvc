package com.enjoy.controller;

import com.enjoy.annotation.EnjoyAutowired;
import com.enjoy.annotation.EnjoyController;
import com.enjoy.annotation.EnjoyReqestParam;
import com.enjoy.annotation.EnjoyRequestMapping;
import com.enjoy.service.OrderService;

@EnjoyController
@EnjoyRequestMapping("get")
public class OrderController {

    @EnjoyAutowired
    OrderService orderService;

    @EnjoyRequestMapping("queryOrder")
    public void queryOrder(@EnjoyReqestParam("p1")String p1){
        System.out.println("p1"+p1);

    }
}
