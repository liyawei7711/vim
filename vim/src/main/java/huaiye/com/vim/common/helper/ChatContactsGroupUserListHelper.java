package huaiye.com.vim.common.helper;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

import huaiye.com.vim.models.contacts.bean.ContactsGroupUserListBean;

/**
 * 缓存群信息(包含群成员)
 */

public class ChatContactsGroupUserListHelper {

    private static ChatContactsGroupUserListHelper INSTANCE = new ChatContactsGroupUserListHelper();


    private Map<String, ContactsGroupUserListBean> chatCache = new HashMap<String, ContactsGroupUserListBean>();

    /**
     * ChatLocalPathHelper ,单例模式
     */
    public static ChatContactsGroupUserListHelper getInstance() {
        return INSTANCE;
    }

    private ChatContactsGroupUserListHelper() {

    }

    public void cacheContactsGroupDetail(String key, ContactsGroupUserListBean value) {
        if (chatCache.containsKey(key)) {
            chatCache.remove(key);
        }
        chatCache.put(key, value);
    }

    public ContactsGroupUserListBean getContactsGroupDetail(String key) {
        if (!TextUtils.isEmpty(key) && null != chatCache && chatCache.containsKey(key)) {
            return chatCache.get(key);
        } else {
            return null;
        }
    }

    public void destoryContactsGroupDetail() {
        chatCache.clear();
        chatCache = null;
    }

}
