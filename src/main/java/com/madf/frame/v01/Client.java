package com.madf.frame.v01;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ReferenceCountUtil;

/**
 * 客户端
 */
public class Client {

    private Channel channel = null;//用该channel进行传输

    public void connect() {
        //线程池
        EventLoopGroup group = new NioEventLoopGroup(1);

        Bootstrap b = new Bootstrap();

        try {
            ChannelFuture f = b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ClientChannelInitializer())
                    .connect("localhost", 8888);

            f.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (!future.isSuccess()) {
                        System.out.println("client not connected....");
                    } else {
                        System.out.println("client connected....");
                        channel = future.channel();
                    }
                }
            });

            f.sync();
            f.channel().closeFuture().sync();
            System.out.println("客户端已经退出....");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    public void send(String msg) {
        ByteBuf buf = Unpooled.copiedBuffer(msg.getBytes());
        channel.writeAndFlush(buf);
    }

    /**
     * 实现客户端优雅的关闭
     */
    public void closeClient() {
        this.send("_bye_");
    }
}

class ClientChannelInitializer extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //channel第一次连上server可用，写出一个字符串
        ByteBuf buf = Unpooled.copiedBuffer("hello, Server".getBytes());
        ctx.writeAndFlush(buf);
    }

    /**
     * 客户端接收到来自服务端的消息后，将消息显示到客户端界面上
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf buf = null;
        try {
            buf = (ByteBuf) msg;
            byte[] bytes = new byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(), bytes);
            String msgAccepted = new String(bytes);
            ClientFrame.INSTANCE.updateText(msgAccepted);//更新客户端的显示
        } finally {
            //释放对它的引用
            if (buf != null) {
                ReferenceCountUtil.release(buf);
            }
        }
    }
}
