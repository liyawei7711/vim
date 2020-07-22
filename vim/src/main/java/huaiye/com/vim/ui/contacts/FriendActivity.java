package huaiye.com.vim.ui.contacts;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.github.promeg.pinyinhelper.Pinyin;
import com.huaiye.sdk.logger.Logger;
import com.ttyy.commonanno.anno.BindLayout;
import com.ttyy.commonanno.anno.BindView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import huaiye.com.vim.R;
import huaiye.com.vim.VIMApp;
import huaiye.com.vim.bus.MessageEvent;
import huaiye.com.vim.common.AppBaseActivity;
import huaiye.com.vim.common.AppUtils;
import huaiye.com.vim.common.SP;
import huaiye.com.vim.common.helper.ChatContactsGroupUserListHelper;
import huaiye.com.vim.common.recycle.LiteBaseAdapter;
import huaiye.com.vim.common.recycle.SafeLinearLayoutManager;
import huaiye.com.vim.common.rx.RxUtils;
import huaiye.com.vim.common.views.FastRetrievalBar;
import huaiye.com.vim.dao.AppDatas;
import huaiye.com.vim.dao.auth.AppAuth;
import huaiye.com.vim.dao.msgs.User;
import huaiye.com.vim.dao.msgs.VimMessageListMessages;
import huaiye.com.vim.models.ModelApis;
import huaiye.com.vim.models.ModelCallback;
import huaiye.com.vim.models.contacts.ContactsApi;
import huaiye.com.vim.models.contacts.bean.ContactsBean;
import huaiye.com.vim.models.contacts.bean.ContactsGroupChatListBean;
import huaiye.com.vim.models.contacts.bean.ContactsGroupUserListBean;
import huaiye.com.vim.models.contacts.bean.CreateGroupContactData;
import huaiye.com.vim.models.contacts.bean.DomainInfoList;
import huaiye.com.vim.models.contacts.bean.GroupInfo;
import huaiye.com.vim.ui.contacts.sharedata.ChoosedContactsNew;
import huaiye.com.vim.ui.home.adapter.ContactsViewHolder;
import huaiye.com.vim.ui.home.adapter.GroupContactsItemAdapter;
import huaiye.com.vim.ui.meet.ChatGroupActivityNew;
import ttyy.com.jinnetwork.core.work.HTTPRequest;
import ttyy.com.jinnetwork.core.work.HTTPResponse;

@BindLayout(R.layout.activity_friend)
public class FriendActivity extends AppBaseActivity {

    public static final String TAG = "FragmentContacts";

    @BindView(R.id.refresh_view)
    SwipeRefreshLayout refresh_view;
    @BindView(R.id.contacts_retrieval_bar)
    FastRetrievalBar contacts_retrieval_bar;
    /*@BindView(R.id.rct_view_layout)
    RelativeLayout rct_view_layout;*/
    @BindView(R.id.rct_view)
    RecyclerView rct_view;
    @BindView(R.id.iv_empty_view)
    View iv_empty_view;
    @BindView(R.id.tv_title)
    View tv_title;
    @BindView(R.id.tv_letter_high_fidelity_item)
    TextView tv_letter_high_fidelity_item;

    LiteBaseAdapter<User> adapter;
    GroupContactsItemAdapter mGroupitemAdapter;

    private ArrayList<User> mCustomContacts = new ArrayList<>();
    private ArrayList<User> mAllContacts = new ArrayList<>();

    private ArrayList<GroupInfo> mlstGroupInfo = new ArrayList<>();

    private ArrayList<User> mOnlineContacts = new ArrayList<>();

    private boolean isFreadList = true;
    private int requestCount = 0;
    private int currentRequestTime = 0;
    private int totalRequest = 0;

    private void initData() {
        requestContacts();
        requestGroupContacts();
    }

    private void requestContacts() {
        mAllContacts.clear();
        if (null != VIMApp.getInstance().mDomainInfoList && VIMApp.getInstance().mDomainInfoList.size() > 0) {
            totalRequest++;
            for (DomainInfoList.DomainInfo domainInfo : VIMApp.getInstance().mDomainInfoList) {
                ModelApis.Contacts().requestAllContacts(domainInfo.strDomainCode, new ModelCallback<ContactsBean>() {
                    @Override
                    public void onSuccess(ContactsBean contactsBean) {
                        if (null != contactsBean && null != contactsBean.userList && contactsBean.userList.size() > 0) {
                            for (User user : contactsBean.userList) {
                                if (user.strUserID.equals(AppAuth.get().getUserID())) {
                                    contactsBean.userList.remove(user);
                                    break;
                                }
                            }
                            mAllContacts.addAll(contactsBean.userList);
                        }
                        doCallBack();
                    }

                    @Override
                    public void onFinish(HTTPResponse httpResponse) {
                        doCallBack();
                    }
                });
            }
        }
    }

    private void doCallBack() {
        totalRequest--;
        if (totalRequest == 0) {
            refresh_view.setRefreshing(false);
            if (isFreadList) {
                updateContacts(true);
            }
        }
    }

    private void getUserInfos(ArrayList<User> userList) {
        new RxUtils<>().doOnThreadObMain(new RxUtils.IThreadAndMainDeal<Map<String, List<String>>>() {
            @Override
            public Map<String, List<String>> doOnThread() {
                Map<String, List<String>> groups = groupBystrDomainCode(userList);
                return groups;
            }

            @Override
            public void doOnMain(Map<String, List<String>> data) {
                for (Map.Entry<String, List<String>> entry : data.entrySet()) {
                    String mapKey = entry.getKey();
                    List<String> mapValue = entry.getValue();
                    ContactsApi.get().requestUserInfoList(mapKey, mapValue, new ModelCallback<ContactsBean>() {
                        @Override
                        public void onSuccess(ContactsBean contactsBean) {

                            if (null != contactsBean && null != contactsBean.userList && contactsBean.userList.size() > 0) {
                                AppDatas.MsgDB().getFriendListDao().insertAll(contactsBean.userList);
                                refreshUserData(contactsBean.userList);
                            }
                        }
                    });
                }
            }
        });


    }

    private void refreshUserData(List<User> users) {
        new RxUtils<List<User>>().doOnThreadObMain(new RxUtils.IThreadAndMainDeal<List<User>>() {
            @Override
            public List<User> doOnThread() {
                if (null != mAllContacts && mAllContacts.size() > 0 && null != users && users.size() > 0) {
                    for (User userAll : mAllContacts) {
                        for (User user : users) {
                            if (userAll.getDomainCode().equals(user.getDomainCode()) && userAll.strUserID.equals(user.strUserID)) {
                                mAllContacts.set(mAllContacts.indexOf(userAll), user);
                                continue;
                            }
                        }
                    }

                }
                return mAllContacts;
            }

            @Override
            public void doOnMain(List<User> data) {

                if (null != adapter) {
                    adapter.notifyDataSetChanged();
                    if (isFreadList) {
                        updateContacts(false);
                    }
                }
            }
        });

    }

    private void refreshCurrentUserData(User user) {
        new RxUtils<List<User>>().doOnThreadObMain(new RxUtils.IThreadAndMainDeal<List<User>>() {
            @Override
            public List<User> doOnThread() {
                if (null != mAllContacts && mAllContacts.size() > 0 && null != user) {
                    for (User userAll : mAllContacts) {
                        if (userAll.getDomainCode().equals(user.getDomainCode()) && userAll.strUserID.equals(user.strUserID)) {
                            mAllContacts.set(mAllContacts.indexOf(userAll), user);
                            continue;
                        }
                    }

                }
                return mAllContacts;
            }

            @Override
            public void doOnMain(List<User> data) {

                if (null != adapter) {
                    adapter.notifyDataSetChanged();
                    if (isFreadList) {
                        updateContacts(false);
                    }
                }
            }
        });

    }

    private Map<String, List<String>> groupBystrDomainCode(List<User> userList) {
        Map<String, List<String>> groupBy = new HashMap<>();
        for (User nUser : userList) {
            if (groupBy.containsKey(nUser.getDomainCode())) {
                groupBy.get(nUser.getDomainCode()).add(nUser.strUserID);
            } else {
                List<String> users = new ArrayList<>();
                users.add(nUser.strUserID);
                groupBy.put(nUser.getDomainCode(), users);
            }
        }
        return groupBy;
    }

    private void requestGroupContacts() {
        Log.i(this.getClass().getName(), "requestGroupContacts");
        /* -1表示不分页，即获取所有联系人 */
        if (null != VIMApp.getInstance().mDomainInfoList && VIMApp.getInstance().mDomainInfoList.size() > 0) {
            refresh_view.setRefreshing(true);
            requestCount = VIMApp.getInstance().mDomainInfoList.size();
            currentRequestTime = 0;
            for (DomainInfoList.DomainInfo domainInfo : VIMApp.getInstance().mDomainInfoList) {
                ModelApis.Contacts().requestGroupBuddyContacts(-1, 0, 0, null, domainInfo.strDomainCode, new ModelCallback<ContactsGroupChatListBean>() {
                    @Override
                    public void onSuccess(final ContactsGroupChatListBean contactsBean) {
                        if (currentRequestTime == 0) {
                            mlstGroupInfo.clear();
                        }
                        ++currentRequestTime;
                        mlstGroupInfo.addAll(contactsBean.lstGroupInfo);
                        updateMsgTopNoDisturb(contactsBean.lstGroupInfo);
                        if (!isFreadList) {
                            updateGroupContacts();
                        }
                    }

                    @Override
                    public void onFailure(HTTPResponse httpResponse) {
                        super.onFailure(httpResponse);
                        ++currentRequestTime;
                    }

                    @Override
                    public void onCancel(HTTPRequest httpRequest) {
                        super.onCancel(httpRequest);
                        ++currentRequestTime;
                    }

                    @Override
                    public void onFinish(HTTPResponse httpResponse) {
                        super.onFinish(httpResponse);
                        if (requestCount == currentRequestTime) {
                            refresh_view.setRefreshing(false);
                            if (null != mlstGroupInfo && mlstGroupInfo.size() > 0) {
                                AppDatas.MsgDB().getGroupListDao().insertAll(mlstGroupInfo);
                                Logger.debug(TAG, AppDatas.MsgDB().getGroupListDao().getGroupList().size() + "");
                            }
                        }
                    }
                });
            }
        } else {
            refresh_view.setRefreshing(false);
            VIMApp.getInstance().getDomainCodeList();
        }

    }

    private void updateMsgTopNoDisturb(ArrayList<GroupInfo> lstGroupInfo) {
        if (null != lstGroupInfo && lstGroupInfo.size() > 0) {
            new RxUtils<>().doOnThreadObMain(new RxUtils.IThreadAndMainDeal() {
                @Override
                public Object doOnThread() {
                    for (GroupInfo groupInfo : lstGroupInfo) {
                        VimMessageListMessages.get().updateNoDisturb(groupInfo.strGroupDomainCode + groupInfo.strGroupID, groupInfo.nNoDisturb);
                        SP.putInt(groupInfo.strGroupDomainCode + groupInfo.strGroupID + AppUtils.SP_SETTING_NODISTURB, groupInfo.nNoDisturb);
                        if (groupInfo.nMsgTop == SP.getInteger(groupInfo.strGroupDomainCode + groupInfo.strGroupID + AppUtils.SP_SETTING_MSG_TOP, 0)) {
                            continue;
                        }
                        VimMessageListMessages.get().updateMsgTop(groupInfo.strGroupDomainCode + groupInfo.strGroupID, groupInfo.nMsgTop);
                        SP.putInt(groupInfo.strGroupDomainCode + groupInfo.strGroupID + AppUtils.SP_SETTING_MSG_TOP, groupInfo.nMsgTop);
                        SP.putLong(groupInfo.strGroupDomainCode + groupInfo.strGroupID + AppUtils.SP_SETTING_MSG_TOP_TIME, System.currentTimeMillis());
                    }
                    return "";
                }

                @Override
                public void doOnMain(Object data) {

                }
            });
        }
    }

    private ArrayList<User> getCustomContacts(ArrayList<User> data) {
        if (data == null || data.size() <= 0) {
            return new ArrayList<>();
        }
        for (User item : data) {
            String upPinYin = "";
            item.isSelected = false;
            for (User temp : ChoosedContactsNew.get().getContacts()) {
                if (temp.strUserName.equals(item.strUserName)) {
                    item.isSelected = true;
                    break;
                }
            }
            if (TextUtils.isEmpty(item.strUserNamePinYin)) {
                item.strUserNamePinYin = Pinyin.toPinyin(item.strUserName, "_");
                if (TextUtils.isEmpty(item.strUserNamePinYin)) {
                    item.strUserNamePinYin = "#";
                }
                upPinYin = item.strUserNamePinYin.toUpperCase();
            } else {
                upPinYin = item.strUserNamePinYin.toUpperCase();
            }
            String a = "#";
            item.pinyin = String.valueOf(TextUtils.isEmpty(upPinYin) ? a.charAt(0) : upPinYin.charAt(0));
        }

        return data;
    }

    public void updateContacts(boolean isNeedRefreshUserInfo) {
        new RxUtils<List<User>>().doOnThreadObMain(new RxUtils.IThreadAndMainDeal() {
            @Override
            public Object doOnThread() {
                mCustomContacts.clear();
                mCustomContacts.addAll(getCustomContacts(mAllContacts));
                return mCustomContacts;
            }

            @Override
            public void doOnMain(Object data) {
                rct_view.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                if (isNeedRefreshUserInfo) {
                    getUserInfos(mAllContacts);
                }
            }
        });


    }

    public void updateGroupContacts() {

        rct_view.setAdapter(mGroupitemAdapter);
        mGroupitemAdapter.setDatas(mlstGroupInfo);
        mGroupitemAdapter.notifyDataSetChanged();
    }

    private void switchOnline() {
        /*切换是否显示全部联系人*/
        AppDatas.Constants().switchShowAllContacts();

        updateContacts(false);
    }

    private ArrayList<User> getOnlineContacts() {
        mOnlineContacts.clear();
        for (User item : mAllContacts) {
            if (item.nStatus > 0) {
                mOnlineContacts.add(item);
            }
        }
        return mOnlineContacts;
    }

    @Override
    protected void initActionBar() {
        EventBus.getDefault().register(this);
        getNavigate().setVisibility(View.VISIBLE);
        getNavigate().setTitlText("我的好友")
                .setTitlClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View V) {
                        isFreadList = true;
                        contacts_retrieval_bar.setVisibility(View.VISIBLE);
                        if (null != mAllContacts && mAllContacts.size() > 0) {
                            updateContacts(true);
                        } else {
                            requestContacts();
                        }
                    }
                });

        refresh_view.setColorSchemeColors(ContextCompat.getColor(this, R.color.blue),
                ContextCompat.getColor(this, R.color.colorPrimary));
        rct_view.setLayoutManager(new SafeLinearLayoutManager(this));
        adapter = new LiteBaseAdapter<>(this,
                mCustomContacts,
                ContactsViewHolder.class,
                R.layout.item_contacts_person,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        User user = (User) v.getTag();
                        Intent intent = new Intent(FriendActivity.this, ContactDetailNewActivity.class);
                        intent.putExtra("nUser", user);
                        startActivity(intent);
                    }
                }, "false");

        mGroupitemAdapter = new GroupContactsItemAdapter(this, mlstGroupInfo, false, null);
        mGroupitemAdapter.setOnItemClickLinstener(new GroupContactsItemAdapter.OnItemClickLinstener() {
            @Override
            public void onClick(int position, GroupInfo user) {
                Intent intent = new Intent(FriendActivity.this, ChatGroupActivityNew.class);
                CreateGroupContactData contactsBean = new CreateGroupContactData();
                contactsBean.strGroupID = mlstGroupInfo.get(position).strGroupID;
                contactsBean.strGroupDomainCode = mlstGroupInfo.get(position).strGroupDomainCode;
                intent.putExtra("mContactsBean", contactsBean);
                startActivity(intent);
            }
        });

        refresh_view.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isFreadList) {
                    requestContacts();
                } else {
                    requestGroupContacts();
                }
            }
        });

        contacts_retrieval_bar.setTextView(tv_letter_high_fidelity_item);
        contacts_retrieval_bar.setOnTouchingLetterChangedListener(new FastRetrievalBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                int position = -1;
                if (mCustomContacts.size() <= 0) {
                    return;
                }
                for (int i = 0; i < mCustomContacts.size(); i++) {
                    if (s.equals(String.valueOf(mCustomContacts.get(i).pinyin))) {
                        position = i;
                        break;
                    }
                }
                if (position == -1) {
                    return;
                }
                if (position == 0) {
                    rct_view.scrollToPosition(0);
                } else {
                    rct_view.scrollToPosition(position);
                }
            }

            @Override
            public void setSidePressed() {

            }

            @Override
            public void setSideUnPressed() {

            }
        });

        initData();
    }

    @Override
    public void doInitDelay() {

    }

    @Override
    public void onResume() {
        super.onResume();
       /* if (isFreadList) {
            requestContacts();
        } else {
            requestGroupContacts();
        }*/
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final MessageEvent messageEvent) {
        if (null == messageEvent) {
            return;
        }
        switch (messageEvent.what) {
            case AppUtils.EVENT_CREATE_GROUP_SUCCESS_ADDGROUP_TO_LIST:
            case AppUtils.EVENT_MESSAGE_MODIFY_GROUP:
                new RxUtils().doOnThreadObMain(new RxUtils.IThreadAndMainDeal() {
                    @Override
                    public Object doOnThread() {
                        if (null != mlstGroupInfo && !TextUtils.isEmpty(messageEvent.msgContent)) {
                            ContactsGroupUserListBean nContactsGroupUserListBean = ChatContactsGroupUserListHelper.getInstance().getContactsGroupDetail(messageEvent.msgContent);
                            boolean needAddGroup = true;
                            if (null != nContactsGroupUserListBean) {
                                for (GroupInfo nGroupInfo : mlstGroupInfo) {
                                    if (nGroupInfo.strGroupID.equals(messageEvent.msgContent)) {
                                        nGroupInfo.strHeadUrl = nContactsGroupUserListBean.strHeadUrl;
                                    }
                                    if (messageEvent.msgContent.equals(nGroupInfo.strGroupID)) {
                                        needAddGroup = false;
                                        break;
                                    }
                                }
                                if (needAddGroup) {
                                    GroupInfo nGroupInfo = new GroupInfo();
                                    nGroupInfo.strGroupDomainCode = nContactsGroupUserListBean.strGroupDomainCode;
                                    nGroupInfo.strGroupID = nContactsGroupUserListBean.strGroupID;
                                    nGroupInfo.strGroupName = nContactsGroupUserListBean.strGroupName;
                                    nGroupInfo.strHeadUrl = nContactsGroupUserListBean.strHeadUrl;
                                    mlstGroupInfo.add(nGroupInfo);
                                }
                            }

                        }
                        return mlstGroupInfo;
                    }

                    @Override
                    public void doOnMain(Object data) {
                        if (null != mGroupitemAdapter && !isFreadList) {
                            mGroupitemAdapter.notifyDataSetChanged();
                        }

                    }
                });

                break;
            case AppUtils.EVENT_MESSAGE_ADD_FRIEND:
            case AppUtils.EVENT_MESSAGE_DEL_FRIEND:
                requestContacts();
                break;
            case AppUtils.EVENT_DEL_GROUP_SUCCESS:
            case AppUtils.EVENT_LEAVE_GROUP_SUCCESS:
                requestGroupContacts();
                break;
            case AppUtils.EVENT_MESSAGE_MODIFY_HEAD_PIC:
                User user = (User) messageEvent.obj1;
                refreshCurrentUserData(user);
                break;
            default:
                break;
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

}
