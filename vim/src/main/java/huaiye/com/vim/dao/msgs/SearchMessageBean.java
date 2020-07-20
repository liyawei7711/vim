package huaiye.com.vim.dao.msgs;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * author: admin
 * date: 2018/05/28
 * version: 0
 * mail: secret
 * desc: vimMessageBean
 */

public class SearchMessageBean implements Serializable {
    public VimMessageListBean listBean;
    public ArrayList<ChatMessageBase> chatMessageBases = new ArrayList<>();

    public SearchMessageBean(VimMessageListBean listBean) {
        this.listBean = listBean;
        if(this.chatMessageBases == null) {
            this.chatMessageBases = new ArrayList<>();
        }
    }

    public SearchMessageBean(VimMessageListBean listBean, ArrayList<ChatMessageBase> chatMessageBases) {
        this.listBean = listBean;
        this.chatMessageBases = chatMessageBases;
        if(this.chatMessageBases == null) {
            this.chatMessageBases = new ArrayList<>();
        }
    }

}
