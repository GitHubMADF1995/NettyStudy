package com.madf.io.bio;

import java.io.IOException;
import java.net.Socket;

public class Client {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("127.0.0.1", 8888);
        socket.getOutputStream().write("HelloServer, I'm client!!!".getBytes());
        socket.getOutputStream().flush();
//        socket.getOutputStream().close();
        System.out.println("client write over, waiting for msg back from server....");
        byte[] bytes = new byte[1024];
        int len = socket.getInputStream().read(bytes);
        System.out.println("client print msg from server:" + new String(bytes, 0, len));
        socket.close();
    }
}
