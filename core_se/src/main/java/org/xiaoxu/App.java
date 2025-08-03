package org.xiaoxu;


import lombok.extern.slf4j.Slf4j;
import org.xiaoxu.enums.Week;
import org.xiaoxu.proxy.MyInvocation;
import org.xiaoxu.service.HelloService;
import org.xiaoxu.service.impl.HelloServiceImpl;

import java.lang.reflect.Proxy;

/**
 * Hello world!
 *
 */
@Slf4j
public class App 
{
    public static void main( String[] args )
    {
//        String value = "ZZzZZKrp194289102792071987280502$name";
//        String[] split = value.split("\\$");
//        System.out.println(split[0]);
//        System.out.println(split[1]);
        HelloService service = new HelloServiceImpl();
        MyInvocation handler  = new MyInvocation(service);
        HelloService proxy = (HelloService) Proxy.newProxyInstance(
                service.getClass().getClassLoader(),
                service.getClass().getInterfaces(),
                handler);
        proxy.sayHello("xiaoxu");
    }

}
