package org.xiaoxu;

import java.io.*;
import java.net.*;

/**
 * 客户端
 */
public class Client {
    public static void main(String[] args) {
        try {
            // 1. 连接到服务端的 IP 和端口
            Socket socket = new Socket("127.0.0.1", 8888);
            System.out.println("已连接到服务端");

            // 2. 获取输出流，发送数据给服务端
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            writer.println("你好，服务端！");

            // 3. 获取输入流，接收服务端返回的数据
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String serverMsg = reader.readLine();
            System.out.println("服务端说：" + serverMsg);

            // 4. 关闭资源
            writer.close();
            reader.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
