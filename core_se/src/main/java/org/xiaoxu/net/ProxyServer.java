package org.xiaoxu.net;

import java.io.*;
import java.net.*;

public class ProxyServer {

    public static void main(String[] args) throws Exception {

        ServerSocket serverSocket = new ServerSocket(8080);

        System.out.println("代理服务器启动，监听 8080");

        while (true) {

            // ① 等待浏览器连接
            Socket clientSocket = serverSocket.accept();

            System.out.println(
                    "浏览器连接进来了："
                    + clientSocket.getRemoteSocketAddress()
            );

            // ② Nginx 自己作为客户端
            //    去连接真正的后端
            Socket backendSocket =
                    new Socket("127.0.0.1", 9000);

            System.out.println("已经连接后端 9000");

            // ③ 浏览器 → Nginx
            InputStream clientInput =
                    clientSocket.getInputStream();

            // ④ Nginx → 后端
            OutputStream backendOutput =
                    backendSocket.getOutputStream();

            // ⑤ 把浏览器发来的数据
            //    原封不动写给后端
            byte[] buffer = new byte[8192];

            int length;

            while ((length = clientInput.read(buffer)) != -1) {

                backendOutput.write(buffer, 0, length);
                backendOutput.flush();

                break;
            }

            // ⑥ 后端 → Nginx
            InputStream backendInput =
                    backendSocket.getInputStream();

            // ⑦ Nginx → 浏览器
            OutputStream clientOutput =
                    clientSocket.getOutputStream();

            // ⑧ 把后端返回的数据
            //    原封不动写回浏览器
            while ((length = backendInput.read(buffer)) != -1) {

                clientOutput.write(buffer, 0, length);
                clientOutput.flush();
            }

            backendSocket.close();
            clientSocket.close();
        }
    }
}