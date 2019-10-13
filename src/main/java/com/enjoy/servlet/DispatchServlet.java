package com.enjoy.servlet;

import com.enjoy.annotation.*;
import com.enjoy.controller.OrderController;

import javax.print.DocFlavor;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DispatchServlet extends HttpServlet {
    public List<String> classNames = new ArrayList();
    Map<String,Object> beans = new HashMap<String,Object>();//容器
    Map<String,Object> handlerMap = new HashMap<String,Object>();//绑定url关系
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //super.doPost(req, resp);
        //http://127.0.0.1/enjoymvc/jams/query
        String uri =  req.getRequestURI();//    /enjoymvc/jams/query
        String contentPath = req.getContextPath(); //enjoymvc
        String path = uri.replace(contentPath,"");  //->  /jams/query
        //根据url 查到方法
        Method method = (Method) handlerMap.get(path);
        //执行方法
        OrderController instance =(OrderController) beans.get("/"+path.split("/")[1]);
        Object[] args = handle(req,resp,method);
        try {
            method.invoke(instance,args);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }


    private Object[] handle(HttpServletRequest request,HttpServletResponse response,Method method){
        //查到当前方法参数类型
        Class<?>[] paramClazzs = method.getParameterTypes();
        Object[] args = new Object[paramClazzs.length];
        int args_i = 0;
        int index = 0;

        for(Class<?> paramClazz : paramClazzs){
            if(ServletRequest.class.isAssignableFrom(paramClazz)){
                args[args_i++] = request;
            }
            if(ServletResponse.class.isAssignableFrom(paramClazz)){
                args[args_i++] = response;
            }
            Annotation[] annotations = method.getParameterAnnotations()[index];
            if(annotations.length>0){
                for(Annotation annotation:annotations){
                    if(EnjoyReqestParam.class.isAssignableFrom(annotation.getClass())){
                        EnjoyReqestParam rp = (EnjoyReqestParam)annotation;
                        args[args_i++] = request.getParameter(rp.value());
                    }
                }
            }
        }
        return args;







    }

    @Override
    public void init() throws ServletException {
        doScanPackage("com.enjoy"); //扫描
        doInstance();//通过反射实例化bean
        doAutowried();//处理依赖
        doUrlMapping();//url路径映射
    }
    void doUrlMapping(){
        for(Map.Entry<String,Object> entry:beans.entrySet()) {
            Object instance = entry.getValue();
            Class<?> clazz = instance.getClass();
            //判断控制类
            if (clazz.isAnnotationPresent(EnjoyController.class)) {
                EnjoyRequestMapping enjoyRequestMapping = clazz.getAnnotation(EnjoyRequestMapping.class);
                String classPath = enjoyRequestMapping.value();
                Method[] methods = clazz.getMethods();
                for(Method eachMethod:methods){
                    if(eachMethod.isAnnotationPresent(EnjoyRequestMapping.class)){
                        EnjoyRequestMapping methodMapping = eachMethod.getAnnotation(EnjoyRequestMapping.class);
                        String methodPath = methodMapping.value();
                        String fullPath = classPath + methodPath;
                        handlerMap.put(fullPath,eachMethod);
                    }
                }
            }
        }
    }
    void doAutowried(){
        for(Map.Entry<String,Object> entry:beans.entrySet()){
            Object instance = entry.getValue();
            Class<?> clazz = instance.getClass();
            //判断控制类
            if(clazz.isAnnotationPresent(EnjoyController.class)){
                //查控制类中的所有成员变量，看哪些成理变量需要注入
                Field[] fields = clazz.getDeclaredFields();
                for(Field field : fields){
                    if(field.isAnnotationPresent(EnjoyAutowired.class)){
                        EnjoyAutowired ea = field.getAnnotation(EnjoyAutowired.class);
                        String key = ea.value();
                        Object ins = beans.get(key);
                        //拿到autowired注解的实例对象 射入到controller类中
                        field.setAccessible(true);//autowired私有变量，设置权限
                        try {
                            field.set(instance,ins);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }else{
                        continue;
                    }
                }

            }

        }

    }
    void doInstance(){
        for(String className:classNames){
            String cn  = className.replace(".class","");
            //com.enjoy.OrderController
            //把全量路径名，得到class对象
            try {
                Class<?> clazz = Class.forName(cn);
                //判断这个类是否需要实例化，看clazz是否定义了需要实例化的对象注解
                if(clazz.isAnnotationPresent(EnjoyController.class)){
                    //controler map.put(key,obj1)
                    Object obj1 =  clazz.newInstance();
                    EnjoyRequestMapping enjoyRequestMapping = clazz.getAnnotation(EnjoyRequestMapping.class);
                    String key1 = enjoyRequestMapping.value();
                    beans.put(key1,obj1);
                }else if(clazz.isAnnotationPresent(EnjoyService.class)){
                    //servcie map.put(key,obj1)
                    Object obj1 =  clazz.newInstance();
                    EnjoyService enjoyService = clazz.getAnnotation(EnjoyService.class);
                    String key1 = enjoyService.value();
                    beans.put(key1,obj1);

                }else{
                    continue;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }

        }

    }


    void doScanPackage(String basePackage){
        URL url =this.getClass().getClassLoader().getResource("/"+basePackage.replaceAll("\\.","/"));
        String fileStr = url.getFile();
        File file = new File(fileStr);
        String[] filesStr = file.list();
        for(String path:filesStr){
            File filePath = new File(fileStr+path);
            if(filePath.isDirectory()){
                //文件夹,再调用自己
                doScanPackage(basePackage+"."+path);
            }else{
                //.class 结束
                classNames.add(basePackage + "."+filePath.getName());
            }
        }
    }
}
