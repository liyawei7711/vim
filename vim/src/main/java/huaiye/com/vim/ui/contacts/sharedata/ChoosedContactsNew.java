package huaiye.com.vim.ui.contacts.sharedata;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import huaiye.com.vim.common.constant.SPConstant;
import huaiye.com.vim.dao.AppDatas;
import huaiye.com.vim.dao.auth.AppAuth;
import huaiye.com.vim.dao.msgs.User;
import huaiye.com.vim.models.contacts.bean.DeptData;
import huaiye.com.vim.models.contacts.bean.GroupInfo;
import huaiye.com.vim.models.contacts.bean.SelectedModeBean;

import static huaiye.com.vim.models.contacts.bean.SelectedModeBean.type_dept;
import static huaiye.com.vim.models.contacts.bean.SelectedModeBean.type_group;
import static huaiye.com.vim.models.contacts.bean.SelectedModeBean.type_user;

/**
 * Created by ywt on 2019/2/27.
 */

public class ChoosedContactsNew {
    private ArrayList<SelectedModeBean> mShowData = new ArrayList<>();
    private ArrayList<User> mContacts = new ArrayList<>();
    private ArrayList<GroupInfo> mGroups = new ArrayList<>();
    private ArrayList<DeptData> mDepts = new ArrayList<>();

    private User mSelf;

    public static ArrayList<DeptData> atData = new ArrayList<>();//所在部门
    public static Map<String, String> userDeptMap = new HashMap<>();//人员对应的部门
    public static Map<String, String> userGroupMap = new HashMap<>();//人员对应的部门
    public static ArrayList<String> selectedDept = new ArrayList<>();

    public User getSelf() {
        if (mSelf != null) {
        } else {
            create();
        }
        return mSelf;
    }

    private ChoosedContactsNew() {
        create();
    }

    private void create() {
        mSelf = new User();
        mSelf.strUserID = String.valueOf(AppDatas.Auth().getUserID());
        mSelf.strLoginName = AppDatas.Auth().getUserLoginName();
        mSelf.strUserName = AppDatas.Auth().getUserName();
        mSelf.strDomainCode = AppDatas.Auth().getDomainCode();
        mSelf.deviceType = 1;
        mSelf.nStatus = 1;
        mSelf.nJoinStatus = 2;
        mSelf.strHeadUrl = AppDatas.Auth().getHeadUrl(AppDatas.Auth().getUserID() + SPConstant.STR_HEAD_URL);
    }

    static class Holder {
        static final ChoosedContactsNew SINGLETON = new ChoosedContactsNew();
    }

    public static ChoosedContactsNew get() {
        return ChoosedContactsNew.Holder.SINGLETON;
    }

    public void setContacts(ArrayList<User> list) {
        if (list == null) {
            mContacts.clear();
            return;
        }
        for (User item : list) {
            if (item.strUserID.equals(mSelf.strUserID)) {
                list.remove(item);
                break;
            }
        }
        mContacts.add(mSelf);//自己始终在列表头
        mContacts.addAll(list);
    }

    public void addSelf() {
        if (!mContacts.contains(mSelf)) {
            mSelf.strUserID = String.valueOf(AppDatas.Auth().getUserID());
            mSelf.strLoginName = AppDatas.Auth().getUserLoginName();
            mSelf.strUserName = AppDatas.Auth().getUserName();
            mSelf.strDomainCode = AppDatas.Auth().getDomainCode();
            mSelf.deviceType = 1;
            mSelf.nStatus = 1;
            mSelf.nJoinStatus = 2;
            if (TextUtils.isEmpty(mSelf.strHeadUrl)) {
                mSelf.strHeadUrl = AppDatas.Auth().getHeadUrl(AppDatas.Auth().getUserID() + SPConstant.STR_HEAD_URL);
            }
            mContacts.add(0, mSelf);//如果自己不存在就加到列表头
        }
    }

    /**
     * 用户add
     *
     * @param user
     * @return
     */
    public boolean add(User user, boolean needShow) {
        if (user == null) {
            return false;
        }
        if (!mContacts.contains(mSelf)) {
            if (TextUtils.isEmpty(mSelf.strHeadUrl)) {
                mSelf.strHeadUrl = AppDatas.Auth().getHeadUrl(AppDatas.Auth().getUserID() + SPConstant.STR_HEAD_URL);
            }
            mContacts.add(0, mSelf);//如果自己不存在就加到列表头
        }
        for (User item : mContacts) {
            if (item.strUserID.equals(user.strUserID)) {
                return true;
            }
        }
        mContacts.add(user);

        if (!needShow) {
            return true;
        }

        boolean has = false;
        for (SelectedModeBean temp : mShowData) {
            if (temp.isUser() && temp.strId.equals(user.strUserID)) {
                has = true;
                break;
            }
        }
        if (!has) {
            mShowData.add(new SelectedModeBean(user.strUserName, type_user, user.strUserID));
        }
        return true;
    }

    /**
     * 用户remove
     *
     * @param user
     * @return
     */
    public boolean remove(User user) {
        if (user == null || mSelf.strUserID.equals(user.strUserID)) {
            //自己不能被删除
            return false;
        }
        for (User item : mContacts) {
            if (item.strUserID.equals(user.strUserID)) {
                mContacts.remove(item);
                break;
            }
        }

        for (SelectedModeBean temp : mShowData) {
            if (temp.isUser() && temp.strId.equals(user.strUserID)) {
                mShowData.remove(temp);
                break;
            }
        }

        return false;
    }

    public boolean remove(SelectedModeBean bean) {
        if (bean == null || mSelf.strUserID.equals(bean.strId)) {
            //自己不能被删除
            return false;
        }

        if (bean.isUser()) {
            for (User item : mContacts) {
                if (item.strUserID.equals(bean.strId)) {
                    mContacts.remove(item);
                    break;
                }
            }
        } else if (bean.isGroup()) {
            for (GroupInfo item : mGroups) {
                if (item.strGroupID.equals(bean.strId)) {
                    mGroups.remove(item);
                    break;
                }
            }
        } else {
            for (DeptData item : mDepts) {
                if (item.strDepID.equals(bean.strId)) {
                    mDepts.remove(item);
                    break;
                }
            }
        }


        mShowData.remove(bean);

        return false;
    }

    /**
     * 用户contain
     *
     * @param user
     * @return
     */
    public boolean isContain(User user) {
        for (User item : mContacts) {
            if (item.strUserID.equals(user.strUserID)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 群组add
     *
     * @param groupInfo
     * @return
     */
    public boolean add(GroupInfo groupInfo) {
        if (groupInfo == null) {
            return false;
        }

        for (GroupInfo item : mGroups) {
            if (item.strGroupID.equals(groupInfo.strGroupID)) {
                return true;
            }
        }
        mGroups.add(groupInfo);

        boolean has = false;
        for (SelectedModeBean temp : mShowData) {
            if (temp.isGroup() && temp.strId.equals(groupInfo.strGroupID)) {
                has = true;
                break;
            }
        }
        if (!has) {
            mShowData.add(new SelectedModeBean(groupInfo.strGroupName, type_group, groupInfo.strGroupID));
        }

        return true;
    }

    /**
     * 群组remove
     *
     * @param groupInfo
     * @return
     */
    public boolean remove(GroupInfo groupInfo) {
        if (groupInfo == null) {
            return false;
        }
        for (GroupInfo item : mGroups) {
            if (item.strGroupID.equals(groupInfo.strGroupID)) {
                mGroups.remove(groupInfo);
                break;
            }
        }

        for (SelectedModeBean temp : mShowData) {
            if (temp.isGroup() && temp.strId.equals(groupInfo.strGroupID)) {
                mShowData.remove(temp);
                break;
            }
        }

        return false;
    }

    /**
     * 部门add
     *
     * @param deptData
     * @return
     */
    public boolean add(DeptData deptData) {
        if (deptData == null) {
            return false;
        }

        for (DeptData item : mDepts) {
            if (item.strDepID.equals(deptData.strDepID)) {
                return true;
            }
        }
        mDepts.add(deptData);

        boolean has = false;
        for (SelectedModeBean temp : mShowData) {
            if (temp.isDept() && temp.strId.equals(deptData.strDepID)) {
                has = true;
                break;
            }
        }
        if (!has) {
            mShowData.add(new SelectedModeBean(deptData.getName(), type_dept, deptData.strDepID));
        }

        return true;
    }

    /**
     * 群组remove
     *
     * @param deptData
     * @return
     */
    public boolean remove(DeptData deptData) {
        if (deptData == null) {
            //自己不能被删除
            return false;
        }
        for (DeptData item : mDepts) {
            if (item.strDepID.equals(deptData.strDepID)) {
                mDepts.remove(item);
                break;
            }
        }


        for (SelectedModeBean temp : mShowData) {
            if (temp.isDept() && temp.strId.equals(deptData.strDepID)) {
                mShowData.remove(temp);
                break;
            }
        }

        return false;
    }

    /**
     * 部门contain
     *
     * @param deptData
     * @return
     */
    public boolean isContain(DeptData deptData) {
        for (DeptData item : mDepts) {
            if (item.strDepID.equals(deptData.strDepID)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 用户contain
     *
     * @param userId
     * @return
     */
    public boolean isContain(String userId) {
        for (User item : mContacts) {
            if (item.strUserID.equals(userId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 群组contain
     *
     * @param groupInfo
     * @return
     */
    public boolean isContain(GroupInfo groupInfo) {
        for (GroupInfo item : mGroups) {
            if (item.strGroupID.equals(groupInfo.strGroupID)) {
                return true;
            }
        }
        return false;
    }

    public int getContactsSize() {
        if (mContacts.contains(mSelf)) {
            return mContacts.size() - 1;
        } else {
            return mContacts.size();
        }
    }
    public int getShowTotalSize() {
        int total;
        if (mContacts.contains(mSelf)) {
            total = mContacts.size() - 1;
        } else {
            total = mContacts.size();
        }

        total += mGroups.size();
        total += mDepts.size();

        return total;
    }

    public ArrayList<User> getContacts() {
        return mContacts;
    }

    public ArrayList<GroupInfo> getGroups() {
        return mGroups;
    }

    public ArrayList<DeptData> getDepts() {
        return mDepts;
    }

    public ArrayList<SelectedModeBean> getSelectedMode() {
        return mShowData;
    }

    public int getSelectedModeSize() {
        boolean has = false;
        for (SelectedModeBean bean : mShowData) {
            if (bean.strId.equals(AppAuth.get().getUserID())) {
                has = true;
                break;
            }
        }
        if (has) {
            return mShowData.size() - 1;
        } else {
            return mShowData.size();
        }
    }

    public void clear() {
        mShowData.clear();
        mContacts.clear();
        mGroups.clear();
        mDepts.clear();

        atData.clear();
        userDeptMap.clear();
        userGroupMap.clear();
        selectedDept.clear();

    }

}
