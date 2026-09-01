package org.xiaoxu.net;

import java.io.*;
import java.net.*;

public class BackendServer {

    public static void main(String[] args) throws Exception {

        ServerSocket serverSocket = new ServerSocket(9000);

        System.out.println("后端服务启动，监听 9000");

        while (true) {

            Socket socket = serverSocket.accept();

            System.out.println("收到连接：" + socket.getRemoteSocketAddress());

            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(socket.getInputStream())
                    );

            // 读取 HTTP 请求
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);

                if (line.isEmpty()) {
                    break;
                }
            }

            // 返回 HTTP 响应
            OutputStream outputStream = socket.getOutputStream();

            String response =
                    "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "Content-Length: 11\r\n" +
                    "\r\n" +
                    "Hello World";

            outputStream.write(response.getBytes());
            outputStream.flush();

            socket.close();
        }
    }
}