package com.madf.io.bio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 客户端连上后会建立一个通道，该通道是单向的，不能同时进行读写
 */
public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket();
        ss.bind(new InetSocketAddress("127.0.0.1", 8888));
        System.out.println("SocketServer build success...");
        while (true) {
            Socket socket = ss.accept();//阻塞方法，只有client连上了，才能往下执行
            System.out.println("client connected!");
            new Thread(() -> {
                handle(socket);
            }).start();
        }
    }

    public static void handle(Socket socket) {
        try {
            byte[] bytes = new byte[1024];
            int len = socket.getInputStream().read(bytes);//read也是阻塞方法
            System.out.println("server msg from client print: " + new String(bytes, 0, len));
            System.out.println("Server ready to write msg from client to client");
            socket.getOutputStream().write(bytes, 0, len);//write也是阻塞方法
            socket.getOutputStream().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
