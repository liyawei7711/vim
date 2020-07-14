package huaiye.com.vim.ui.contacts;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.promeg.pinyinhelper.Pinyin;
import com.huaiye.sdk.HYClient;
import com.huaiye.sdk.sdpmsgs.social.SendUserBean;
import com.ttyy.commonanno.anno.BindLayout;
import com.ttyy.commonanno.anno.BindView;
import com.ttyy.commonanno.anno.OnClick;
import com.ttyy.commonanno.anno.route.BindExtra;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import huaiye.com.vim.R;
import huaiye.com.vim.VIMApp;
import huaiye.com.vim.bus.EventUserClick;
import huaiye.com.vim.bus.EventUserSelected;
import huaiye.com.vim.bus.EventUserSelectedComplete;
import huaiye.com.vim.bus.MessageEvent;
import huaiye.com.vim.common.AppBaseActivity;
import huaiye.com.vim.common.AppUtils;
import huaiye.com.vim.common.recycle.LiteBaseAdapter;
import huaiye.com.vim.common.recycle.SafeLinearLayoutManager;
import huaiye.com.vim.common.rx.RxUtils;
import huaiye.com.vim.dao.AppDatas;
import huaiye.com.vim.dao.auth.AppAuth;
import huaiye.com.vim.dao.msgs.ChangyongLianXiRenBean;
import huaiye.com.vim.dao.msgs.User;
import huaiye.com.vim.models.ModelApis;
import huaiye.com.vim.models.ModelCallback;
import huaiye.com.vim.models.contacts.bean.ContactsBean;
import huaiye.com.vim.models.contacts.bean.CreateGroupContactData;
import huaiye.com.vim.models.contacts.bean.CustomResponse;
import huaiye.com.vim.models.contacts.bean.DeptData;
import huaiye.com.vim.models.contacts.bean.DomainInfoList;
import huaiye.com.vim.models.contacts.bean.SelectedModeBean;
import huaiye.com.vim.ui.contacts.sharedata.ChoosedContactsNew;
import huaiye.com.vim.ui.contacts.viewholder.UserViewOrgHolder;
import huaiye.com.vim.ui.home.FragmentContacts;
import huaiye.com.vim.ui.home.adapter.ContactsDeptViewOrgHolder;
import huaiye.com.vim.ui.home.adapter.ContactsViewHolder;
import huaiye.com.vim.ui.meet.ChatGroupActivityNew;
import ttyy.com.jinnetwork.core.work.HTTPResponse;
import ttyy.com.recyclerexts.base.EXTRecyclerAdapter;
import ttyy.com.recyclerexts.base.EXTViewHolder;

import static huaiye.com.vim.common.AppUtils.nEncryptIMEnable;
import static huaiye.com.vim.ui.contacts.sharedata.ChoosedContactsNew.atData;
import static huaiye.com.vim.ui.contacts.sharedata.ChoosedContactsNew.selectedDept;
import static huaiye.com.vim.ui.contacts.sharedata.ChoosedContactsNew.userDeptMap;

/**
 * author: admin
 * date: 2018/01/15
 * version: 0
 * mail: secret
 * desc: ContactsChoiceActivity
 */
@BindLayout(R.layout.activity_contacts_root_org)
public class ContactsAddOrDelActivityNewOrg extends AppBaseActivity {
    @BindView(R.id.refresh_view)
    SwipeRefreshLayout refresh_view;
    @BindView(R.id.et_key)
    TextView et_key;

    @BindView(R.id.ll_search)
    View ll_search;

    @BindView(R.id.rct_view_suozai)
    RecyclerView rct_view_suozai;
    @BindView(R.id.rct_view)
    RecyclerView rct_view;
    @BindView(R.id.tv_title)
    View tv_title;

    @BindView(R.id.tv_choose_confirm)
    TextView tv_choose_confirm;

    @BindView(R.id.ll_choosed_persons)
    LinearLayout llChoosedPersons;
    @BindView(R.id.rct_choosed)
    RecyclerView rct_choosed;

    LiteBaseAdapter<User> adapter;
    LiteBaseAdapter<DeptData> adapterAt;
    EXTRecyclerAdapter<SelectedModeBean> mChoosedAdapter;

    private ArrayList<User> mCustomContacts = new ArrayList<>();
    private ArrayList<User> mAllContacts = new ArrayList<>();//常用联系人

    @BindExtra
    ArrayList<User> mUserList;
    @BindExtra
    String titleName;
    @BindExtra
    boolean isSelectUser;
    @BindExtra
    boolean needAddSelf;
    @BindExtra
    boolean isCreateGroup;
    @BindExtra
    boolean isCreateVideoPish;
    @BindExtra
    boolean isAddMore;
    @BindExtra
    boolean isJinJiMore;
    @BindExtra
    String strGroupDomainCode;
    @BindExtra
    String strGroupID;
    @BindExtra
    String strGroupName;
    @BindExtra
    int max;
    @BindExtra
    boolean isZhuanFa;

    long currentTime;

    @Override
    protected void initActionBar() {
        EventBus.getDefault().register(this);
        if (max == -1) {
            max = 1000;
        }

        if (TextUtils.isEmpty(titleName)) {
            titleName = "联系人";
        }

        if (isSelectUser) {
            getNavigate().setTitlText(titleName)
                    .setLeftClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onBackPressed();
                        }
                    })
                    .setRightClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onEvent(new EventUserSelectedComplete());
                        }
                    });

            getNavigate().getRightTextView().setPadding(AppUtils.dp2px(this, 8f), AppUtils.dp2px(this, 4f), AppUtils.dp2px(this, 8f), AppUtils.dp2px(this, 4f));
            if (isJinJiMore || isAddMore || isCreateGroup || isCreateVideoPish) {
                getNavigate().getRightTextView().setBackgroundResource(R.drawable.shape_choosed_confirm);
                getNavigate().setRightText("确定");
            } else {
                getNavigate().getRightTextView().setBackgroundResource(R.drawable.shape_choosed_delete);
                getNavigate().setRightText(AppUtils.getString(R.string.user_detail_del_perple));
            }
        } else {
            getNavigate().setTitlText(titleName)
                    .setLeftClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onBackPressed();
                        }
                    });
        }

    }

    private Object getSendUsers(ArrayList<User> contacts) {
        ArrayList<SendUserBean> nSendUserBeans = new ArrayList<>();
        for (User user : contacts) {
            if (!user.strUserID.equals(AppDatas.Auth().getUserID())) {
                SendUserBean sendUserBean = new SendUserBean(user.strUserID, user.getDomainCode(), user.strUserName);
                nSendUserBeans.add(sendUserBean);
            }

        }
        return nSendUserBeans;
    }

    /**
     * 踢人
     */
    private void kickoutGroupUser() {
        ModelApis.Contacts().requestKickoutGroupUser(strGroupDomainCode, strGroupID, getKickoutPeple(), new ModelCallback<CustomResponse>() {
            @Override
            public void onSuccess(final CustomResponse contactsBean) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        kickoutGroupUserSuccess();
                    }
                });
            }

            @Override
            public void onFailure(HTTPResponse httpResponse) {
                super.onFailure(httpResponse);
                showToast("加群失败");
            }
        });
    }

    private void kickoutGroupUserSuccess() {
        StringBuilder str = new StringBuilder();
        for (User temp : getKickoutPeple()) {
            str.append(temp.strUserName + ",");
        }
        MessageEvent event = new MessageEvent(AppUtils.EVENT_KICKOUT_PEOPLE_TO_SUCCESS);
        event.setGroupDomain(strGroupDomainCode);
        event.setGroupId(strGroupID);
        event.msgContent = str.toString().substring(0, str.length() - 1);
        EventBus.getDefault().post(event);
        finish();
    }

    private void createGroupChat() {

        ModelApis.Contacts().requestCreateGroupChat(getGroupName(), ChoosedContactsNew.get().getContacts(), new ModelCallback<CreateGroupContactData>() {
            @Override
            public void onSuccess(final CreateGroupContactData contactsBean) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        go2ChatGroup(contactsBean);
                    }
                });
            }

            @Override
            public void onFailure(HTTPResponse httpResponse) {
                super.onFailure(httpResponse);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showToast("群创建失败");
                    }
                });
            }
        });
    }

    private void addJinJiLianXiRen() {
        ArrayList<User> mContacts = ChoosedContactsNew.get().getContacts();
        Intent intent = new Intent();
        intent.putExtra("users", mContacts);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void addPeople2Group() {
        ModelApis.Contacts().requestInviteUserJoinGroupChat(strGroupDomainCode, strGroupID, strGroupName, getAddPeple(), new ModelCallback<CustomResponse>() {
            @Override
            public void onSuccess(final CustomResponse contactsBean) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addPeople2GroupSuccess();

                    }
                });
            }

            @Override
            public void onFailure(HTTPResponse httpResponse) {
                super.onFailure(httpResponse);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showToast("加群失败");
                    }
                });
            }
        });
    }

    private void addPeople2GroupSuccess() {
        StringBuilder str = new StringBuilder();
        for (User temp : getAddPeple()) {
            str.append(temp.strUserName + ",");
        }
        MessageEvent event = new MessageEvent(AppUtils.EVENT_ADD_PEOPLE_TO_GROUP_SUCCESS);
        event.setGroupDomain(strGroupDomainCode);
        event.setGroupId(strGroupID);
        event.msgContent = str.toString().substring(0, str.length() - 1);
        EventBus.getDefault().post(event);
        showToast("加群成功");
        finish();
    }

    private ArrayList<User> getAddPeple() {
        ArrayList<User> users = new ArrayList<User>();
        if (null != ChoosedContactsNew.get().getContacts() && null != ChoosedContactsNew.get().getContacts()) {
            for (User item : ChoosedContactsNew.get().getContacts()) {
                if (item.nJoinStatus != 2) {
                    users.add(item);
                }
            }
        }
        return users;
    }

    private ArrayList<User> getKickoutPeple() {
        ArrayList<User> users = new ArrayList<User>();
        if (null != ChoosedContactsNew.get().getContacts() && null != ChoosedContactsNew.get().getContacts()) {
            for (User item : ChoosedContactsNew.get().getContacts()) {
                if (!item.strUserID.equals(AppDatas.Auth().getUserID())) {
                    users.add(item);
                }
            }
        }
        return users;
    }

    private String getGroupName() {
        StringBuilder stringGoupName = new StringBuilder();
        if (null != ChoosedContactsNew.get().getContacts() && ChoosedContactsNew.get().getContacts().size() > 0) {
//            for (User item : ChoosedContactsNew.get().getContacts()) {
//                if (ChoosedContactsNew.get().getContacts().indexOf(item) < 6) {
//                    stringGoupName.append(item.strUserName + "、");
//                }
//            }
//            if (null != stringGoupName && stringGoupName.indexOf("、") >= 0) {
//                stringGoupName.deleteCharAt(stringGoupName.lastIndexOf("、"));
//            }
//        }
//        if (TextUtils.isEmpty(stringGoupName)) {
            stringGoupName.append("群聊(" + ChoosedContactsNew.get().getContactsSize() + ")");
//        }
        }
        return stringGoupName.toString();
    }

    private void go2ChatGroup(CreateGroupContactData contactsBean) {
        EventBus.getDefault().post(new MessageEvent(AppUtils.EVENT_CREATE_GROUP_SUCCESS));
        Intent intent = new Intent(this, ChatGroupActivityNew.class);
        intent.putExtra("mContactsBean", contactsBean);
//        intent.putParcelableArrayListExtra("mMessageUsersDate", getMessageUsersDate());
        startActivity(intent);
        finish();
    }

    @Override
    public void doInitDelay() {
        initData();
        changeShowSelected();
        tv_choose_confirm.setVisibility(View.GONE);
        refresh_view.setColorSchemeColors(ContextCompat.getColor(this, R.color.blue),
                ContextCompat.getColor(this, R.color.colorPrimary));
        refresh_view.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestOnLine(false);
            }
        });
        rct_view.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                int[] location2 = new int[2];
                rct_view.getLocationInWindow(location2);

            }
        });
        refresh_view.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isJinJiMore || isAddMore || isCreateGroup || isCreateVideoPish) {
                    requestDatas();
                } else {
                    refresh_view.setRefreshing(false);
                }
            }
        });
        if (!isJinJiMore && !isAddMore && !isCreateGroup && !isCreateVideoPish) {
            refresh_view.setEnabled(false);
        }

        rct_view.setLayoutManager(new SafeLinearLayoutManager(this));

        adapter = new LiteBaseAdapter<>(this,
                mCustomContacts,
                UserViewOrgHolder.class,
                R.layout.letter_item_layout_org,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        User user = (User) v.getTag();

                        if (isJinJiMore || isAddMore || isCreateGroup || isCreateVideoPish) {
                            if (user.nJoinStatus != 2) {
                                handleChoice(user);
                            }
                        } else {
                            handleChoice(user);
                        }
                        changeShowSelected();
                    }
                }, "false");
        UserViewOrgHolder.mIsChoice = isSelectUser;
        rct_view.setAdapter(adapter);

        rct_view_suozai.setLayoutManager(new SafeLinearLayoutManager(this));
        adapterAt = new LiteBaseAdapter<>(this,
                atData,
                ContactsDeptViewOrgHolder.class,
                R.layout.item_contacts_person_chat_org,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DeptData deptData = (DeptData) v.getTag();

                        for (DeptData temp : FragmentContacts.allDeptDatas) {
                            if (deptData.strDepID.equals(temp.strDepID)) {
                                deptData.nDepType = temp.nDepType;
                                break;
                            }
                        }
                        deptData.strDomainCode = AppAuth.get().getDomainCode();
                        if (v.getId() == R.id.tv_next) {
                            DomainInfoList.DomainInfo domain = null;
                            if (null != VIMApp.getInstance().mDomainInfoList && VIMApp.getInstance().mDomainInfoList.size() > 0) {
                                for (DomainInfoList.DomainInfo temp : VIMApp.getInstance().mDomainInfoList) {
                                    if (temp.strDomainCode.equals(AppAuth.get().getDomainCode())) {
                                        domain = temp;
                                        break;
                                    }
                                }
                            }
                            deptData.strDomainCode = domain == null ? "" : domain.strDomainCode;
                            ArrayList<String> titleName = new ArrayList<>();
                            titleName.add(TextUtils.isEmpty(deptData.strName) ? deptData.strDepName : deptData.strName);
                            Intent intent = new Intent(ContactsAddOrDelActivityNewOrg.this, DeptDeepListOrgActivity.class);
                            intent.putExtra("domainName", (domain == null ? "" : domain.strDomainName));
                            intent.putExtra("titleName", titleName);
                            intent.putExtra("deptData", deptData);
                            intent.putExtra("max", max);
                            intent.putExtra("map", FragmentContacts.map);
                            startActivity(intent);
                        } else {
                            handleChoice(deptData);
                        }
                    }
                }, "false");
        ContactsDeptViewOrgHolder.mIsChoice = isSelectUser;
        rct_view_suozai.setAdapter(adapterAt);

        if (isJinJiMore || isAddMore || isCreateGroup || isCreateVideoPish) {
            requestDatas();
        } else {
            new RxUtils<>().doOnThreadObMain(new RxUtils.IThreadAndMainDeal() {
                @Override
                public Object doOnThread() {
                    try {
                        if (null != mUserList && mUserList.size() > 0) {
                            for (User user : mUserList) {
                                if (TextUtils.isEmpty(user.strHeadUrl)) {
                                    user.strHeadUrl = AppDatas.MsgDB().getFriendListDao().getFriendHeadPic(user.strUserID, user.getDomainCode());
                                }
                            }
                            mAllContacts.clear();
                            mAllContacts.addAll(mUserList);
                        }
                    } catch (Exception e) {

                    }

                    return "";
                }

                @Override
                public void doOnMain(Object data) {
                    updateContacts();

                }
            });

        }

        rct_choosed.setLayoutManager(new SafeLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mChoosedAdapter = new EXTRecyclerAdapter<SelectedModeBean>(R.layout.item_contacts_person_choosed) {
            @Override
            public void onBindViewHolder(EXTViewHolder extViewHolder, int i, SelectedModeBean contactData) {
                extViewHolder.setText(R.id.tv_contact_name, contactData.strName);
            }
        };
        mChoosedAdapter.setDatas(ChoosedContactsNew.get().getSelectedMode());
        mChoosedAdapter.setOnItemClickListener(new EXTRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClicked(View view, int i) {
                if (mChoosedAdapter.getDatas().get(i).strId.equals(AppDatas.Auth().getUserID())) {
                    return;
                }
                boolean isDel = false;
                for (User item : mAllContacts) {
                    if (mChoosedAdapter.getDatas().get(i).strId.equals(item.strUserID)) {
                        handleChoice(item);
                        isDel = true;
                        break;
                    }
                }
                if (!isDel) {
                    ChoosedContactsNew.get().remove(mChoosedAdapter.getDatas().get(i));
                    mChoosedAdapter.notifyDataSetChanged();
                }
                changeShowSelected();
            }
        });
        rct_choosed.setAdapter(mChoosedAdapter);
        changeShowSelected();

    }

    private void changeShowSelected() {
        if (!isSelectUser) {
            llChoosedPersons.setVisibility(View.GONE);
            return;
        }
        if (ChoosedContactsNew.get().getContactsSize() == 0 &&
                ChoosedContactsNew.get().getGroups().isEmpty() &&
                ChoosedContactsNew.get().getDepts().isEmpty()) {
            llChoosedPersons.setVisibility(View.GONE);
            getNavigate().setRightText("确定(0)");
        } else {
            llChoosedPersons.setVisibility(View.VISIBLE);
            getNavigate().setRightText("确定(" + ChoosedContactsNew.get().getSelectedModeSize() + ")");
        }
        getNavigate().getRightTextView().setBackgroundResource(R.drawable.shape_choosed_confirm);
    }

    private void initData() {
        ChoosedContactsNew.get().clear();
        try {
            if (isJinJiMore || isAddMore || isCreateGroup || isCreateVideoPish) {
                if (null != mUserList) {
                    for (int i = 0; i < mUserList.size(); i++) {
                        if (mUserList.get(i) != null) {
                            mUserList.get(i).nJoinStatus = 2;
                        }
                    }
                    ChoosedContactsNew.get().getContacts().addAll(mUserList);
                }
            } else {
                if (null != mUserList) {
                    for (int i = 0; i < mUserList.size(); i++) {
                        if (mUserList.get(i) != null) {
                            mUserList.get(i).nJoinStatus = 0;
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    /**
     * 获取数据
     */
    void requestDatas() {
        requestSelfDept();
        requestChangYong();
        FragmentContacts.requestDeptAll();
    }

    private void requestSelfDept() {
        ModelApis.Contacts().requestBuddyContacts(new ModelCallback<ContactsBean>() {
            @Override
            public void onSuccess(final ContactsBean contactsBean) {
                if (null != contactsBean && null != contactsBean.userList && contactsBean.userList.size() > 0) {
                    for (User temp : contactsBean.userList) {
                        if (temp.strUserID.equals(AppAuth.get().getUserID())) {
                            atData.clear();
                            if (temp.getUserDept() != null) {
                                atData.addAll(temp.getUserDept());
                                for (DeptData dep : atData) {
                                    if (selectedDept.contains(dep.strDepID)) {
                                        dep.isSelected = true;
                                    }
                                }
                            } else {
                            }
                            Collections.sort(atData, new Comparator<DeptData>() {
                                @Override
                                public int compare(DeptData o1, DeptData o2) {
                                    return o1.nDepType - o2.nDepType;
                                }
                            });

                            adapterAt.notifyDataSetChanged();
                            break;
                        }
                    }
                }
            }

            @Override
            public void onFinish(HTTPResponse httpResponse) {
                super.onFinish(httpResponse);
                refresh_view.setRefreshing(false);
            }
        });
    }

    private void requestChangYong() {
        mAllContacts.clear();
        for (ChangyongLianXiRenBean temp : AppDatas.MsgDB().getChangYongLianXiRen().queryAll(AppAuth.get().getUserID(), AppAuth.get().getDomainCode())) {
            mAllContacts.add(ChangyongLianXiRenBean.converToUser(temp));
            for (User user : mAllContacts) {
                if (ChoosedContactsNew.get().isContain(user)) {
                    user.isSelected = true;
                }
            }
        }
        Collections.sort(mAllContacts, new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                if (TextUtils.isEmpty(o1.saveTime)) {
                    o1.saveTime = "0";
                }
                if (TextUtils.isEmpty(o2.saveTime)) {
                    o2.saveTime = "0";
                }
                long t1 = Long.parseLong(o1.saveTime);
                long t2 = Long.parseLong(o2.saveTime);
                return (int) (t2 - t1);
            }
        });
        showData(et_key.getText().toString());
    }

    private void showData(String str) {
        mCustomContacts.clear();
        for (User temp : mAllContacts) {
            if (TextUtils.isEmpty(str)) {
                mCustomContacts.add(temp);
            } else if (temp.strUserName.contains(str) || temp.strLoginName.contains(str)) {
                mCustomContacts.add(temp);
            }
        }
        adapter.notifyDataSetChanged();

        refresh_view.setRefreshing(false);
    }

    protected void updateContacts() {
        mCustomContacts.clear();
        mCustomContacts.addAll(getCustomContacts(mAllContacts));
        if (mCustomContacts != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            setResult(RESULT_OK, data);
            finish();
        }
    }

    @OnClick(R.id.tv_choose_confirm)
    void onChoosedConfirmClicked() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    @OnClick({R.id.tv_group})
    public void onClick(View view) {
        Intent intent = new Intent(this, GroupListOrgActivity.class);
        intent.putExtra("max", max);
        startActivity(intent);
    }

    @OnClick({R.id.ll_search, R.id.et_key})
    public void onClickSearch(View view) {
        Intent intent = new Intent(this, SearchDeptUserListOrgActivity.class);
        intent.putExtra("max", max);
        startActivity(intent);
    }

    @OnClick({R.id.tv_dept_title})
    public void onClickTitle(View view) {
        Intent intent = new Intent(this, DeptListOrgActivity.class);
        intent.putExtra("max", max);
        startActivity(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventUserClick bean) {
        for (User item : mAllContacts) {
            if (bean.user.strId.equals(item.strUserID)) {
                handleChoice(item);
                break;
            }
        }
        changeShowSelected();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventUserSelected bean) {
        mChoosedAdapter.notifyDataSetChanged();
        changeShowSelected();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventUserSelectedComplete bean) {
        if (System.currentTimeMillis() - currentTime < 1500) {
            return;
        }
        currentTime = System.currentTimeMillis();
        if (isCreateVideoPish) {
            if (HYClient.getSdkOptions().encrypt().isEncryptBind() && nEncryptIMEnable) {
                if (ChoosedContactsNew.get().getContacts().size() != 2) {
                    showToast("加密分享只能为一人");
                    currentTime = 0;
                    return;
                }
            }

            MessageEvent nMessageEvent = new MessageEvent(AppUtils.EVENT_RPUSH_VIDEO);
            nMessageEvent.obj1 = getSendUsers(ChoosedContactsNew.get().getContacts());
            EventBus.getDefault().post(nMessageEvent);
            finish();
        } else if (isCreateGroup) {//单聊拉人建群
            createGroupChat();
        } else if (isAddMore) {//多聊增加人员
            addPeople2Group();
        } else if (isJinJiMore) {//紧急联系人
            addJinJiLianXiRen();
        } else {//踢人
            kickoutGroupUser();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        updateContacts();
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
                upPinYin = item.strUserNamePinYin.toUpperCase();
            } else {
                upPinYin = item.strUserNamePinYin.toUpperCase();
            }
            String a = "#";
            item.pinyin = String.valueOf(TextUtils.isEmpty(upPinYin) ? a.charAt(0) : upPinYin.charAt(0));
        }

        return data;
    }

    private void handleChoice(User user) {
        if (user == null) {
            return;
        }
        if (ChoosedContactsNew.get().isContain(user)) {
            user.isSelected = false;
            ChoosedContactsNew.get().remove(user);
        } else {
            if (ChoosedContactsNew.get().getContacts().size() >= max + 1) {
                showToast("最多选" + max + "人，已达人数上限");
                return;
            }
            user.isSelected = true;
            ChoosedContactsNew.get().add(user, true);
        }
        changeShowSelected();
        mChoosedAdapter.notifyDataSetChanged();
        adapter.notifyDataSetChanged();
    }

    private void handleChoice(final DeptData deptData) {
        ModelApis.Contacts().requestContactsOnly(deptData.strDomainCode, deptData.strDepID, 1, new ModelCallback<ContactsBean>() {
            @Override
            public void onSuccess(final ContactsBean contactsBean) {
                deptData.isSelected = !deptData.isSelected;

                if (deptData.isSelected) {
                    ChoosedContactsNew.get().add(deptData);
                    if (!selectedDept.contains(deptData.strDepID)) {
                        selectedDept.add(deptData.strDepID);
                    }
                } else {
                    ChoosedContactsNew.get().remove(deptData);
                    if (selectedDept.contains(deptData.strDepID)) {
                        selectedDept.remove(deptData.strDepID);
                    }
                }
                if (null != contactsBean && null != contactsBean.userList && contactsBean.userList.size() > 0) {
                    for (User user : contactsBean.userList) {
                        if (!user.strUserID.equals(AppAuth.get().getUserID())) {

                            if (deptData.isSelected) {
                                if (!ChoosedContactsNew.get().isContain(user)) {
                                    if (ChoosedContactsNew.get().getContacts().size() >= max + 1) {
                                        showToast("最多选" + max + "人，已达人数上限");
                                        return;
                                    }
                                    userDeptMap.put(user.strUserID, deptData.strDepID);
                                    user.isSelected = true;
                                    ChoosedContactsNew.get().add(user, false);
                                }
                            } else {
                                if (ChoosedContactsNew.get().isContain(user)) {
                                    user.isSelected = false;
                                    ChoosedContactsNew.get().remove(user);

                                    if (userDeptMap.containsKey(user.strUserID)) {
                                        userDeptMap.remove(user.strUserID);
                                    }
                                }
                            }
                        }
                    }
                    mChoosedAdapter.notifyDataSetChanged();
                    adapterAt.notifyDataSetChanged();
                    changeShowSelected();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ChoosedContactsNew.get().clear();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ContactsViewHolder.mIsChoice = isSelectUser;

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        if (mChoosedAdapter != null) {
            mChoosedAdapter.notifyDataSetChanged();
        }

        changeShowSelected();
    }
}
