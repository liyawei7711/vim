package huaiye.com.vim.neety;

import java.io.File;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class FileTransferServer extends ChannelInitializer<Channel> {

    static FileTransferServer SINGLETON;
    static FileTransferClientHandler handler;

    public static FileTransferServer get() {
        if (SINGLETON == null) {
            SINGLETON = new FileTransferServer();
        }
        return SINGLETON;
    }

    private FileTransferServer() {
        try {
            bind(10086);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void bind(int port) throws Exception {

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .option(ChannelOption.SO_RCVBUF, 1024 * 20)
                    .option(ChannelOption.SO_SNDBUF, 1024 * 20)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(64, 1024 * 20 * 20, 65536 * 20))
                    .childHandler(this);

            ChannelFuture f = b.bind(port).sync();
            f.channel().closeFuture().addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    try {
                        bossGroup.shutdownGracefully();
                        workerGroup.shutdownGracefully();
                        f.channel().closeFuture();
                    } catch (Exception e) {

                    }
                }
            });
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * 发送文件
     *
     * @param file
     */
    public void sendFile(File file) {
        if (handler == null) {
            return;
        }
        handler.sendFileRequest(file);
    }

    public void close() {
//        f.channel().close();
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        handler = new FileTransferClientHandler();
        ch.pipeline().addLast(handler);
    }

    public void sendResponse(int result) {
        if (handler == null) {
            return;
        }
        handler.sendResultCode(0x01, result);
    }

    public boolean isConnected() {
        return handler != null;
    }
}
