package com.madf.frame.v02;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.nio.charset.StandardCharsets;

/**
 * 服务端
 */
public class Server {
    //存放多个客户端的channel
    public static ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 在打开服务端窗口时，启动服务端
     */
    public void serverStart() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(2);

        try {
            ServerBootstrap b = new ServerBootstrap();
            ChannelFuture f = b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new TankMsgDecoder())
                                    .addLast(new ServerChildHandler());
                        }
                    })
                    .bind(8888)
                    .sync();

            ServerFrame.INSTANCE.updateServerMsg("Server started...");
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public void closeServer() {
        ByteBuf buf = Unpooled.copiedBuffer("服务端即将关闭！！！".getBytes(StandardCharsets.UTF_8));
        clients.writeAndFlush(buf);
    }
}

class ServerChildHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //客户端连上服务端时将channel加入clients
        Server.clients.add(ctx.channel());
    }

    /**
     * 服务端接收客户端发送的消息，将消息显示到服务端界面上
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            TankMsg tm = (TankMsg) msg;
            System.out.println(tm);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ReferenceCountUtil.release(msg);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        //删除出现异常的客户端channel，并关闭连接
        Server.clients.remove(ctx.channel());
        ctx.close();
    }
}