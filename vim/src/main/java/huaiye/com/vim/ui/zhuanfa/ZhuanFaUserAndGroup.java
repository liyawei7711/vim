package huaiye.com.vim.ui.zhuanfa;

import android.content.Context;
import android.view.View;

import java.util.ArrayList;

import huaiye.com.vim.R;
import huaiye.com.vim.common.recycle.LiteBaseAdapter;
import huaiye.com.vim.dao.msgs.User;
import huaiye.com.vim.models.contacts.bean.GroupInfo;
import huaiye.com.vim.ui.contacts.viewholder.UserViewHolder;
import huaiye.com.vim.ui.zhuanfa.holder.ZhuanFaShareViewHolder;

public class ZhuanFaUserAndGroup {
    private ArrayList<User> users = new ArrayList<>();
    private ArrayList<GroupInfo> groupInfos = new ArrayList<>();

    private ArrayList<ZhuanFaShareBean> allData = new ArrayList<>();
    LiteBaseAdapter<ZhuanFaShareBean> adapter;

    public LiteBaseAdapter getAdapter(Context context) {
        adapter = new LiteBaseAdapter<>(context,
                allData,
                ZhuanFaShareViewHolder.class,
                R.layout.zhuanfa_share_choose_item,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ZhuanFaShareBean bean = (ZhuanFaShareBean) v.getTag();
                        removeData(bean);
                        adapter.notifyItemRemoved(allData.indexOf(bean));
                    }
                }, "false");
        return adapter;
    }
    public ArrayList<GroupInfo> getGroupInfos() {
        return groupInfos;
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    static class Holder {
        static final ZhuanFaUserAndGroup SINGLETON = new ZhuanFaUserAndGroup();
    }

    public static ZhuanFaUserAndGroup get() {
        return ZhuanFaUserAndGroup.Holder.SINGLETON;
    }

    private ZhuanFaUserAndGroup() {

    }

    public void add(User user) {
        users.add(user);
        allData.add(new ZhuanFaShareBean(user.strUserName, user.strUserID, user.getDomainCode(), user.strHeadUrl, false));
        if(adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
    public void remove(User user) {
        users.remove(user);
        for(ZhuanFaShareBean show: allData) {
            if(user.strUserID.equals(show.id)) {
                allData.remove(show);
                break;
            }
        }
        if(adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    public void add(GroupInfo groupInfo) {
        groupInfos.add(groupInfo);
        allData.add(new ZhuanFaShareBean(groupInfo.strGroupName, groupInfo.strGroupID, groupInfo.strGroupDomainCode, groupInfo.strHeadUrl, true));
        if(adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
    public void remove(GroupInfo groupInfo) {
        groupInfos.remove(groupInfo);
        for(ZhuanFaShareBean show: allData) {
            if(groupInfo.strGroupID.equals(show.id)) {
                allData.remove(show);
                break;
            }
        }
        if(adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
    public void removeData(ZhuanFaShareBean bean) {

    }

    public void clearUser() {
        ArrayList<ZhuanFaShareBean> datas = new ArrayList<>();
        for(User temp : users) {
            for(ZhuanFaShareBean show: allData) {
                if(temp.strUserID.equals(show.id)) {
                    datas.add(show);
                    break;
                }
            }
        }
        for(ZhuanFaShareBean temp : datas) {
            allData.remove(temp);
        }
        if(adapter != null) {
            adapter.notifyDataSetChanged();
        }
        users.clear();
    }

    public void clearGroup() {
        ArrayList<ZhuanFaShareBean> datas = new ArrayList<>();
        for(GroupInfo temp : groupInfos) {
            for(ZhuanFaShareBean show: allData) {
                if(temp.strGroupID.equals(show.id)) {
                    datas.add(show);
                    break;
                }
            }
        }
        for(ZhuanFaShareBean temp : datas) {
            allData.remove(temp);
        }
        if(adapter != null) {
            adapter.notifyDataSetChanged();
        }
        groupInfos.clear();
    }

    public void clearAll() {
        users.clear();
        groupInfos.clear();
        allData.clear();
    }
}
