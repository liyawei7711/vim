package huaiye.com.vim.dao.msgs;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.util.ArrayList;

import huaiye.com.vim.dao.auth.AppAuth;
import huaiye.com.vim.models.contacts.bean.DeptData;

/**
 * 紧急联系人
 * Created by LENOVO on 2019/3/28.
 */

@Entity(tableName = "tb_user_changyong_lianxi")
public class ChangyongLianXiRenBean implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public long id;
    @ColumnInfo
    public String ownerUserId;
    @ColumnInfo
    public String ownerUserDomain;

    @NonNull
    @ColumnInfo
    public String strUserID;
    /**
     * 登录名
     */
    @ColumnInfo
    public String strLoginName;
    /**
     * 用户名
     */
    @ColumnInfo
    public String strUserName;
    /**
     * 用户名拼音
     */
    @ColumnInfo
    public String strUserNamePinYin;
    /**
     * 用户角色编号
     */
    @ColumnInfo
    public int nRoleID;
    /**
     * 角色类型 1:超级管理员 2：自定义
     */
    @ColumnInfo
    public int nRoleType;
    /**
     * 性别（0：未知 1：男 2：女）
     */
    @ColumnInfo
    public int nSex;
    /**
     * 手机号码
     */
    @ColumnInfo
    public String strMobilePhone;
    /**
     * 优先级，越小越高
     */
    @ColumnInfo
    public int nPriority;
    /**
     * 备注
     */
    @ColumnInfo
    public String strRemark;
    @ColumnInfo
    public String strLastLoginTime;
    /**
     * 用户状态 -1:未登录 0:离线 1：空闲，2采集中，3：对讲中 4：会议中
     */
    @ColumnInfo
    public int nStatus;
    /**
     * 最新经度
     */
    @ColumnInfo
    public double dLongitude;
    /**
     * 最新纬度
     */
    @ColumnInfo
    public double dLatitude;
    /**
     * 最新高度
     */
    @ColumnInfo
    public double dHeight;
    /**
     * 当前速度
     */
    @ColumnInfo
    public double dSpeed;

    /**
     * 业务域code，与请求中的一致，请求中为空则为本域code
     */
    @NonNull
    @ColumnInfo
    public String strDomainCode;


    @Ignore
    public String strUserDomainCode;


    @ColumnInfo
    public String strCollectTime;

    @ColumnInfo
    public String strHeadUrl;

    /**
     * (可选)用户登录sie的token
     */
    @ColumnInfo
    public String strUserTokenID;
    /**
     * 频道域（注以下四个字段，nState=3有效）
     */
    @ColumnInfo
    public String strTrunkChannelDomainCode;
    /**
     * 频道ID
     */
    @ColumnInfo
    public int nTrunkChannelID;
    /**
     * 频道名
     */
    @ColumnInfo
    public String strTrunkChannelName;
    /**
     * 是否正在发言
     * 0：否 状态为对讲中
     * 1：是 状态为发言
     */
    @ColumnInfo
    public int nSpeaking;
    /**
     * 登陆终端类型 1：android 2：ios 3：PC 4：web
     */
    @ColumnInfo
    public int nDevType;
    /**
     * 用户所属部门(0表示没有部门)
     */
    @ColumnInfo
    public int nDepID;
    /**
     * 部门名称
     */
    @ColumnInfo
    public String strDepName;
    @ColumnInfo
    public String strRoleName;
    /**
     * 终端设备也当成人处理
     */
    @ColumnInfo
    public int deviceType;
    @ColumnInfo
    public int extend1;
    @ColumnInfo
    public int extend2;
    @ColumnInfo
    public int extend3;
    @ColumnInfo
    public String strPostName;
    @ColumnInfo
    public String nPostID;
    @ColumnInfo
    public String strDept;
    @ColumnInfo
    public String saveTime;

    @Ignore
    public ArrayList<DeptData> lstDepartment;

    public void setLstDepartment(ArrayList<DeptData> lstDepartment) {
        this.lstDepartment = lstDepartment;
        this.strDept = new Gson().toJson(lstDepartment);
    }

    public ArrayList<DeptData> getUserDept() {
        if (TextUtils.isEmpty(strDept)) {
            if(lstDepartment == null) {
                return new ArrayList<>();
            } else {
                this.strDept = new Gson().toJson(lstDepartment);
                return lstDepartment;
            }
        }
        if(lstDepartment == null) {
            lstDepartment = new Gson().fromJson(strDept, new TypeToken<ArrayList<DeptData>>(){}.getType());
        }
        return lstDepartment;
    }

    public static User converToUser(ChangyongLianXiRenBean bean) {
        User user1 = new User();
        user1.strUserID = bean.strUserID;
        user1.strLoginName = bean.strLoginName;
        user1.strUserName = bean.strUserName;
        user1.strUserNamePinYin = bean.strUserNamePinYin;
        user1.nRoleID = bean.nRoleID;
        user1.nRoleType = bean.nRoleType;
        user1.nRoleType = bean.nRoleType;
        user1.nSex = bean.nSex;
        user1.strMobilePhone = bean.strMobilePhone;
        user1.nPriority = bean.nPriority;
        user1.strRemark = bean.strRemark;
        user1.strLastLoginTime = bean.strLastLoginTime;
        user1.nStatus = bean.nStatus;
        user1.dLongitude = bean.dLongitude;
        user1.dLatitude = bean.dLatitude;
        user1.dHeight = bean.dHeight;
        user1.dSpeed = bean.dSpeed;
        user1.strDomainCode = bean.strDomainCode;
        user1.strUserDomainCode = bean.strUserDomainCode;
        user1.strCollectTime = bean.strCollectTime;
        user1.strHeadUrl = bean.strHeadUrl;
        user1.strUserTokenID = bean.strUserTokenID;
        user1.strTrunkChannelDomainCode = bean.strTrunkChannelDomainCode;
        user1.nTrunkChannelID = bean.nTrunkChannelID;
        user1.strTrunkChannelName = bean.strTrunkChannelName;
        user1.nSpeaking = bean.nSpeaking;
        user1.nDevType = bean.nDevType;
        user1.nDepID = bean.nDepID;
        user1.strDepName = bean.strDepName;
        user1.strRoleName = bean.strRoleName;
        user1.deviceType = bean.deviceType;
        user1.strDept = bean.strDept;
        return user1;
    }
    public static ChangyongLianXiRenBean converToChangyongLianXiRen(User bean) {
        ChangyongLianXiRenBean user1 = new ChangyongLianXiRenBean();
        user1.ownerUserId = AppAuth.get().getUserID();
        user1.ownerUserDomain = AppAuth.get().getDomainCode();
        user1.strUserID = bean.strUserID;
        user1.strLoginName = bean.strLoginName;
        user1.strUserName = bean.strUserName;
        user1.strUserNamePinYin = bean.strUserNamePinYin;
        user1.nRoleID = bean.nRoleID;
        user1.nRoleType = bean.nRoleType;
        user1.nSex = bean.nSex;
        user1.strMobilePhone = bean.strMobilePhone;
        user1.nPriority = bean.nPriority;
        user1.strRemark = bean.strRemark;
        user1.strLastLoginTime = bean.strLastLoginTime;
        user1.nStatus = bean.nStatus;
        user1.dLongitude = bean.dLongitude;
        user1.dLatitude = bean.dLatitude;
        user1.dHeight = bean.dHeight;
        user1.dSpeed = bean.dSpeed;
        user1.strDomainCode = bean.strDomainCode;
        user1.strUserDomainCode = bean.strUserDomainCode;
        user1.strCollectTime = bean.strCollectTime;
        user1.strHeadUrl = bean.strHeadUrl;
        user1.strUserTokenID = bean.strUserTokenID;
        user1.strTrunkChannelDomainCode = bean.strTrunkChannelDomainCode;
        user1.nTrunkChannelID = bean.nTrunkChannelID;
        user1.strTrunkChannelName = bean.strTrunkChannelName;
        user1.nSpeaking = bean.nSpeaking;
        user1.nDevType = bean.nDevType;
        user1.strDepName = bean.strDepName;
        user1.strRoleName = bean.strRoleName;
        user1.deviceType = bean.deviceType;
        user1.nPostID = bean.nPostID;
        user1.strPostName = bean.strPostName;
        user1.strDept = bean.getStrDept();
        user1.saveTime = System.currentTimeMillis()+"";
        return user1;
    }
    public static ChangyongLianXiRenBean converToChangyongLianXiRen(String strToUserName, String strToUserID, String strToUserDomainCode) {
        ChangyongLianXiRenBean user1 = new ChangyongLianXiRenBean();
        user1.ownerUserId = AppAuth.get().getUserID();
        user1.ownerUserDomain = AppAuth.get().getDomainCode();
        user1.strUserID = strToUserID;
        user1.strUserName = strToUserName;
        user1.strDomainCode = strToUserDomainCode;
        user1.saveTime = System.currentTimeMillis()+"";
        return user1;
    }

}
