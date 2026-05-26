package org.xiaoxu.se.foundation;

public class CallStackOrderMain {
    public static void main(String[] args) {
        System.out.println("1. main 调用 controller");
        controller();
        System.out.println("这行不会执行");
    }

    static void controller() {
        System.out.println("2. controller 调用 service");
        service();
        System.out.println("controller 这行也不会执行");
    }

    static void service() {
        System.out.println("3. service 调用 dao");
        dao();
        System.out.println("service 这行也不会执行");
    }

    static void dao() {
        System.out.println("4. dao 执行，准备抛异常");
        throw new RuntimeException("数据库炸了");
        // 抛出的瞬间，这之后的代码不执行
        // JVM 从 dao() 这个栈帧开始往上找 catch
    }

}
