package huaiye.com.vim.models.contacts.bean;

import java.io.Serializable;
import java.util.ArrayList;

import huaiye.com.vim.dao.msgs.User;


/**
 * author: zhangzhen
 * date: 2019/07/23
 * version: 0
 * mail: secret
 * desc: CreateGroupContactData
 */

public class CreateGroupContactData implements Serializable {

    public int nResultCode;
    public String strResultDescribe;
    public String strGroupDomainCode;
    public String strGroupID;
    public String sessionName;
    public ArrayList<User> userList;
}
