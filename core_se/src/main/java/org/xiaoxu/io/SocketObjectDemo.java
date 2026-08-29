package org.xiaoxu.io;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * L6 练习(通关考): Socket 上的流 —— 两台"机器"各持一对出入口管子对接。
 * 一个 main 里同时演服务端和客户端(服务端丢线程里跑), 现实中它们在两台机器上,
 * 但流的使用方式一模一样 —— 这正是"程序只认管子"的终极证明。
 *
 * 你问的"对象序列化上网络"就是本例的后半段:
 *   ObjectOutputStream 套在 socket.getOutputStream() 上, 对象直接飞过去。
 *
 * 经典坑(代码里已规避): ObjectInputStream 构造时会阻塞等对端发来流头,
 * 两端都必须"先建输出流、后建输入流", 否则互相等对方先开口, 直接死锁。
 */
public class SocketObjectDemo {

    public static void main(String[] args) throws Exception {
        // 端口传 0 = 让系统随机分配, 演示代码不用赌端口没被占
        try (ServerSocket server = new ServerSocket(0)) {
            int port = server.getLocalPort();

            Thread serverThread = new Thread(() -> runServer(server));
            serverThread.start();

            runClient(port);
            serverThread.join(); // 等服务端打完日志再退出 main
        }
    }

    static void runServer(ServerSocket server) {
        try (Socket socket = server.accept(); // 阻塞等一个客户端进来
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            // 收到什么原样回什么(echo), 对象则回"已签收"的描述
            for (int i = 0; i < 2; i++) {
                Object received = in.readObject();
                System.out.println("[服务端] 收到: " + received);
                if (received instanceof PaymentMsg msg) {
                    out.writeObject("服务端已签收订单 " + msg.orderId + ", 金额 " + msg.amount);
                } else {
                    out.writeObject("服务端回显: " + received);
                }
                out.flush();
            }
        } catch (Exception e) {
            System.err.println("[服务端] 异常: " + e.getMessage());
        }
    }

    static void runClient(int port) throws Exception {
        try (Socket socket = new Socket("127.0.0.1", port);
             // 注意顺序: 两端都是先 out 后 in, 规避 ObjectInputStream 流头死锁
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("hello, 我是一根管子上的字符串");
            out.flush();
            System.out.println("[客户端] " + in.readObject());

            PaymentMsg msg = new PaymentMsg(10001L, 199.99, "这单走 socket 序列化");
            out.writeObject(msg);
            out.flush();
            System.out.println("[客户端] " + in.readObject());
        }
    }

    /** 网络传输的消息体。真实生产里这个格式已换成 JSON/Protobuf, 但"可序列化"这个前提从没变过 */
    public static class PaymentMsg implements Serializable {
        private static final long serialVersionUID = 1L;

        long orderId;
        double amount;
        /** transient 演示: 就算主动序列化, 标了 transient 的字段也上不了网络 */
        transient String secret = "内部风控字段";

        String note;

        PaymentMsg(long orderId, double amount, String note) {
            this.orderId = orderId;
            this.amount = amount;
            this.note = note;
        }

        @Override
        public String toString() {
            return "PaymentMsg{orderId=" + orderId + ", amount=" + amount
                    + ", note='" + note + "', secret=" + (secret == null ? "null(没过网络)" : "本地还在") + "}";
        }
    }
}
