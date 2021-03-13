package com.madf.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GlobalEventExecutor;

public class HelloNetty {

    //通道组，服务端接收到某个客户端消息时，将消息转发给其他每个客户端
    public static ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public static void main(String[] args) {
        new NettyServer(8888).serverStart();
    }
}

class NettyServer {
    int port = 8888;
    public NettyServer(int port) {
        this.port = port;
    }

    public void serverStart() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();//负责连接
        EventLoopGroup workerGroup = new NioEventLoopGroup();//负责处理
        ServerBootstrap b = new ServerBootstrap();

        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)//建立连接通道的类型
                //childHandler每一个客户端连接后，给其一个监听器进行处理
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        //在该通道上添加一个对此通道进行处理的自己的处理器，又是一个监听器
                        ch.pipeline().addLast(new Handler());
                    }
                });

        try {
            ChannelFuture f = b.bind(port).sync();
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}

class Handler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        super.channelActive(ctx);
        HelloNetty.clients.add(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        super.channelRead(ctx, msg);
        System.out.println("Server: channel read");
        ByteBuf buf = (ByteBuf) msg;//ByteBuf是直接访问操作系统的内容，因此它不在JVM垃圾回收机制之内
        System.out.println(buf.toString(CharsetUtil.UTF_8));
        System.out.println("ByteBuf refCount: " + buf.refCnt());//ByteBuf引用的个数
//        ctx.writeAndFlush(msg);//writeAndFlush自动释放掉ByteBuf所占用操作系统的内存
        HelloNetty.clients.writeAndFlush(msg);//给多个客户端转发消息
        ctx.close();

//        ByteBuf buf = null;
//        System.out.println("Server: channel read");
//        try {
//            buf = (ByteBuf) msg;
//            System.out.println(buf.toString(CharsetUtil.UTF_8));
//            System.out.println("ByteBuf refCount: " + buf.refCnt());//ByteBuf引用的个数
//        } finally {
//            if (buf != null) ReferenceCountUtil.release(buf);
//            System.out.println("ByteBuf refCount: " + buf.refCnt());
//        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
        ctx.close();
    }
}