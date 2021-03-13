package com.madf.io.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class Server {
    public static void main(String[] args) throws Exception {
        final AsynchronousServerSocketChannel serverChannel = AsynchronousServerSocketChannel
                .open()
                .bind(new InetSocketAddress(8888));

        //此处的accept方法是非阻塞方法，一调用就会进行处理
        serverChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
            //CompletionHandler运用了Observer观察者模式，连接上后交给CompletionHandler处理（钩子函数）
            @Override
            public void completed(AsynchronousSocketChannel client, Object attachment) {
                //如果不写此行代码下一个客户端将会连接不进来
                serverChannel.accept(null, this);
                try {
                    System.out.println("RemoteAddress is: " + client.getRemoteAddress());
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    client.read(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
                        @Override
                        public void completed(Integer result, ByteBuffer attachment) {
                            attachment.flip();
                            System.out.println(new String(attachment.array(), 0, result));
                            client.write(ByteBuffer.wrap("HelloClient, I'm Server!!!".getBytes()));
                        }

                        @Override
                        public void failed(Throwable exc, ByteBuffer attachment) {
                            exc.printStackTrace();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                exc.printStackTrace();
            }
        });

        while (true) {
            Thread.sleep(1000);
        }
    }
}
