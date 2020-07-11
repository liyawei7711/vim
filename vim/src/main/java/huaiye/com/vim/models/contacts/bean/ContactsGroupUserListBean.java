package huaiye.com.vim.models.contacts.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

import huaiye.com.vim.dao.msgs.User;

public class ContactsGroupUserListBean implements Serializable {
    public int nResultCode;
    public String strResultDescribe;
    public String strGroupDomainCode;
    public String strGroupID;
    public String strGroupName;
    public String strAnnouncement;
    public String strCreateTime;
    public String strCreaterDomainCode;
    public String strCreaterID;
    public int nBeinviteMode;
    public int nInviteMode;
    public int nTeamMemberLimit;
    public String strHeadUrl;

    public ArrayList<User> lstGroupUser;

}
