package huaiye.com.vim.models.contacts.bean;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.io.Serializable;

@Entity(tableName = "tb_group_list", primaryKeys ={"strGroupID","strGroupDomainCode"})
public class GroupInfo implements Serializable {
    @NonNull
    @ColumnInfo
    public String strGroupDomainCode;
    @NonNull
    @ColumnInfo
    public String strGroupID;
    @ColumnInfo
    public String strGroupName;
    @ColumnInfo
    public String strHeadUrl;
    @ColumnInfo
    public int nMsgTop;
    @ColumnInfo
    public int nNoDisturb;
    @Ignore
    public boolean isSelected;

    public GroupInfo(){

    }

}