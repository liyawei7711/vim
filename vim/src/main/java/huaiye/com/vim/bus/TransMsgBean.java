package huaiye.com.vim.bus;

import java.io.File;

/**
 * Created by Administrator on 2018\3\29 0029.
 */

public class TransMsgBean {
    public File file;
    public int type;
    public String devId;
    public String pwd;
    public String fanKui;

    public TransMsgBean(int type, File file) {
        this.type = type;
        this.file = file;
    }

    public TransMsgBean(int type, String devId, String pwd) {
        this.type = type;
        this.devId = devId;
        this.pwd = pwd;
    }

    public TransMsgBean(int type, String fanKui) {
        this.type = type;
        this.fanKui = fanKui;
    }
}
