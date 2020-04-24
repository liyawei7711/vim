package huaiye.com.vim.neety;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.RandomAccessFile;

import huaiye.com.vim.bus.TransMsgBean;
import huaiye.com.vim.common.AppUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

public class FileTransferClientHandler extends ChannelInboundHandlerAdapter {
    public static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 正在接收的文件大小标记
     */
    private volatile int byteRead = -1;
    /**
     * 文件当前接收位置
     */
    private volatile long start = 0;
    /**
     * 文件名称
     */
    private volatile String fileName;
    /**
     * 正在接收的文件
     */
    private File file_dir;
    /**
     * 正在发送的文件
     */
    File file;
    RandomAccessFile randomAccessFile = null;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!channels.contains(ctx.channel())) {
            channels.add(ctx.channel());
        }
        ByteBuf buf = (ByteBuf) msg;
        if (byteRead != -1) {
            readFile(buf, byteRead);
        } else {
            int type1 = buf.readShort();
            int type = buf.readByte();
            int str3 = buf.readInt();

            switch (type) {
                case 0x01:
                    renZhengShenFen(buf);
                    break;
                case 0x02:
                    renFileName(buf, str3);
                    break;
                case 0x03:
                    if (str3 > 1) {
                        readFile(buf, str3);
                    } else {
                        EventBus.getDefault().post(new TransMsgBean(3, "received"));
                    }
                    break;
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
    }

    private void readFile(ByteBuf buf, int len) throws Exception {
        if (byteRead == -1) {
            byteRead = len;
        }
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);

        this.file_dir = new File(AppUtils.ctx.getExternalFilesDir(null) + File.separator + "Vim/chat/linshi/");
        if (!file_dir.exists()) {
            file_dir.mkdirs();
        }
        file_dir = new File(file_dir, fileName);
        if (start == 0) { //只有在文件开始传的时候才进入 这样就减少了对象创建 和可能出现的一些错误
            //根据 MD5 和 文件类型 来确定是否存在这样的文件 如果存在就 秒传
            if (file_dir.exists()) {
                file_dir.delete();
            }
            file_dir.createNewFile();
            randomAccessFile = new RandomAccessFile(file_dir, "rw");
        }

        randomAccessFile.seek(start);
        randomAccessFile.write(bytes);

        start = start + bytes.length;
        if (start < byteRead) {
            //进度条
        } else {
            //完成
            randomAccessFile.close();
            EventBus.getDefault().post(new TransMsgBean(3, file_dir));
            start = 0;
            byteRead = -1;
            randomAccessFile = null;
            file_dir = null;

            sendResultCode(0x03, 0x00);
        }
    }

    private void renZhengShenFen(ByteBuf buf) {
        try {
            String devId = new String(buf.readBytes(20).array());
            String pwd = new String(buf.readBytes(buf.readableBytes()).array());
            EventBus.getDefault().post(new TransMsgBean(1, devId, pwd));
        } catch (Exception e) {
            sendResultCode(0x01, 0x01);
        }
    }

    private void renFileName(ByteBuf buf, int len) {
        if (len == 1) {
            int str4 = buf.readByte();
            if (0 == str4) {
                sendFile();
            }
        } else {
            fileName = new String(buf.readBytes(buf.readableBytes()).array());
            sendResultCode(0x02, 0x00);
        }
    }

    private void sendFile() {
        if (file == null) {
            return;
        }
        int byteRead;

        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
            randomAccessFile.seek(0);
            byte[] bytes = new byte[(int) file.length()];
            if ((byteRead = randomAccessFile.read(bytes)) != -1) {
                ByteBuf byteBuf = Unpooled.buffer(byteRead + 7);
                byteBuf.writeShort(0xAABB);
                byteBuf.writeByte(0x03);
                byteBuf.writeInt(byteRead);
                byteBuf.writeBytes(bytes);
                channels.writeAndFlush(byteBuf);
                file = null;
            } else {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void sendFileRequest(File file) {
        this.file = file;

        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeShort(0xAABB);
        byteBuf.writeByte(0x02);
        byteBuf.writeInt(file.getName().getBytes().length);
        byteBuf.writeBytes(file.getName().getBytes());
        channels.writeAndFlush(byteBuf);
    }

    public void sendResultCode(int type, int result) {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeShort(0xAABB);
        byteBuf.writeByte(type);
        byteBuf.writeInt(1);
        byteBuf.writeByte(result);

        channels.writeAndFlush(byteBuf);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("cccccccccccc " + cause.toString());
//        EventBus.getDefault().post(new MessageBean(cause.getMessage()));
        cause.printStackTrace();
        ctx.close();
    }

    public void close() {
        channels.close();
    }

}
