package com.madf.io.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

public class Server {
    public static void main(String[] args) throws IOException {
        //还是ServerSocket通道，只不过是双向的，可以同时进行读写
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.socket().bind(new InetSocketAddress("127.0.0.1", 8888));
        ssc.configureBlocking(false);//设定为非阻塞模型

        System.out.println("Server started, listening on: " + ssc.getLocalAddress());
        Selector selector = Selector.open();//打开一个Selector
        //注册Selector对何事件关注（OP_ACCEPT）：监测是否有客户端需要连接
        ssc.register(selector, SelectionKey.OP_ACCEPT);
        while (true) {
            selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();//所有事件存放的集合
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();//先移除该Key，再进行处理
                handle(key);
            }
        }
    }

    private static void handle(SelectionKey key) {
        if (key.isAcceptable()) {
            try {
                ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                SocketChannel sc = ssc.accept();
                sc.configureBlocking(false);
                //new Client
                String hostIp = ((InetSocketAddress) sc.getRemoteAddress()).getHostString();
                System.out.println("Client " + hostIp + " trying to connect");
                sc.register(key.selector(), SelectionKey.OP_READ);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
            }
        } else if (key.isReadable()) {
            SocketChannel sc = null;
            try {
                sc = (SocketChannel) key.channel();
                ByteBuffer buffer = ByteBuffer.allocate(512);
                buffer.clear();
                int len = sc.read(buffer);

                System.out.println("Server ready print msg from Client...");
                if (len != -1) {
                    System.out.println(new String(buffer.array(), 0, len));
                }

                System.out.println("Server ready to write msg to Client....");
                ByteBuffer bufferTpWrite = ByteBuffer.wrap("HelloClient, I'm Server".getBytes());
                sc.write(bufferTpWrite);
                System.out.println("Server write msg to Client success");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (sc != null) {
                    try {
                        sc.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
