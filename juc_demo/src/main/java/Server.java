import java.io.*;
import java.net.*;

/**
 *
 * @author xiaoxu
 * 服务器 端
 */
public class Server {
    public static void main(String[] args) {
        try {
            // 1. 创建一个服务器 socket，监听端口 8888
            ServerSocket serverSocket = new ServerSocket(8888);
            System.out.println("服务端启动，等待连接...");

            // 2. 等待客户端连接（阻塞）
            Socket socket = serverSocket.accept();
            System.out.println("客户端已连接：" + socket.getInetAddress());

            // 3. 获取输入流，接收客户端发送的数据
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String clientMsg = reader.readLine();
            System.out.println("客户端说：" + clientMsg);

            // 4. 获取输出流，发送数据给客户端
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            writer.println("你好，客户端！");

            // 5. 关闭资源
            writer.close();
            reader.close();
            socket.close();
            serverSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
