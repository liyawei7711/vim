package huaiye.com.vim.ui.contacts;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ttyy.commonanno.anno.BindLayout;
import com.ttyy.commonanno.anno.BindView;
import com.ttyy.commonanno.anno.OnClick;
import com.ttyy.commonanno.anno.route.BindExtra;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import huaiye.com.vim.R;
import huaiye.com.vim.VIMApp;
import huaiye.com.vim.bus.EventUserClick;
import huaiye.com.vim.bus.EventUserSelectedComplete;
import huaiye.com.vim.common.AppBaseActivity;
import huaiye.com.vim.common.AppUtils;
import huaiye.com.vim.common.SP;
import huaiye.com.vim.common.helper.ChatContactsGroupUserListHelper;
import huaiye.com.vim.common.recycle.LiteBaseAdapter;
import huaiye.com.vim.common.recycle.SafeLinearLayoutManager;
import huaiye.com.vim.common.rx.RxUtils;
import huaiye.com.vim.dao.AppDatas;
import huaiye.com.vim.dao.auth.AppAuth;
import huaiye.com.vim.dao.msgs.User;
import huaiye.com.vim.dao.msgs.VimMessageListMessages;
import huaiye.com.vim.models.ModelApis;
import huaiye.com.vim.models.ModelCallback;
import huaiye.com.vim.models.contacts.bean.ContactsGroupChatListBean;
import huaiye.com.vim.models.contacts.bean.ContactsGroupUserListBean;
import huaiye.com.vim.models.contacts.bean.CreateGroupContactData;
import huaiye.com.vim.models.contacts.bean.DomainInfoList;
import huaiye.com.vim.models.contacts.bean.GroupInfo;
import huaiye.com.vim.models.contacts.bean.SelectedModeBean;
import huaiye.com.vim.ui.contacts.sharedata.ChoosedContactsNew;
import huaiye.com.vim.ui.contacts.viewholder.GroupInfoViewHolder;
import huaiye.com.vim.ui.contacts.viewholder.GroupInfoViewOrgHolder;
import ttyy.com.jinnetwork.core.work.HTTPRequest;
import ttyy.com.jinnetwork.core.work.HTTPResponse;
import ttyy.com.recyclerexts.base.EXTRecyclerAdapter;
import ttyy.com.recyclerexts.base.EXTViewHolder;

import static huaiye.com.vim.ui.contacts.sharedata.ChoosedContactsNew.userGroupMap;

/**
 * Created by ywt on 2019/3/21.
 */
@BindLayout(R.layout.activity_group_list_org)
public class GroupListOrgActivity extends AppBaseActivity {

    @BindView(R.id.et_key)
    TextView et_key;
    @BindView(R.id.close)
    ImageView close;

    @BindView(R.id.tv_my_create)
    TextView tv_my_create;
    @BindView(R.id.tv_my_jonie)
    TextView tv_my_jonie;
    @BindView(R.id.view_my_create)
    View view_my_create;
    @BindView(R.id.view_my_jonie)
    View view_my_jonie;

    @BindView(R.id.rct_view_create)
    RecyclerView rct_view_create;
    @BindView(R.id.rct_view_jonie)
    RecyclerView rct_view_jonie;
    @BindView(R.id.iv_empty_view)
    View iv_empty_view;
    @BindView(R.id.refresh_view)
    SwipeRefreshLayout refresh_view;
    @BindView(R.id.ll_choosed_persons)
    LinearLayout llChoosedPersons;
    @BindView(R.id.rct_choosed)
    RecyclerView rct_choosed;
    @BindView(R.id.tv_choose_confirm)
    TextView tv_choose_confirm;

    ArrayList<GroupInfo> lstGroupInfo = new ArrayList<>();
    LiteBaseAdapter<GroupInfo> myCreateAdapter;
    ArrayList<GroupInfo> mCreateGroupInfo = new ArrayList<>();
    LiteBaseAdapter<GroupInfo> myJonieAdapter;
    ArrayList<GroupInfo> mJonieGroupInfo = new ArrayList<>();
    EXTRecyclerAdapter<SelectedModeBean> mChoosedAdapter;

    @BindExtra
    int max;
    @BindExtra
    boolean isZhuanFa;

    int requestCount = 0;
    int currentRequestTime = 0;
    boolean isFreadList = false;
    boolean isRef = false;
    private boolean create;//当前是不是创建的模式

    public static Map<String, String> mapGroupName = new HashMap<>();
    ArrayList<String> selectedMap = new ArrayList<>();

    @Override
    protected void initActionBar() {
        initNavigateView();
    }

    private void initNavigateView() {
        EventBus.getDefault().register(this);
        getNavigate().setVisibility(View.VISIBLE);
        getNavigate().setTitlText("我的群组")
                .setLeftClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void doInitDelay() {
        initView();
    }

    private void initView() {
        tv_my_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeSelected(true);
            }
        });
        tv_my_jonie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeSelected(false);
            }
        });
        myCreateAdapter = new LiteBaseAdapter<>(this,
                mCreateGroupInfo,
                GroupInfoViewOrgHolder.class,
                R.layout.item_contacts_person_org,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GroupInfo groupInfo = (GroupInfo) v.getTag();
                        if (v.getId() == R.id.tv_next) {
                            Intent intent = new Intent(GroupListOrgActivity.this, GroupUserListOrgActivity.class);
                            intent.putExtra("max", max);
                            intent.putExtra("groupInfo", groupInfo);
                            intent.putExtra("isZhuanFa", isZhuanFa);
                            startActivity(intent);
                        } else {
                            handleChoice(groupInfo);
                        }
                    }
                }, "false");
        GroupInfoViewOrgHolder.mIsChoice = true;

        myJonieAdapter = new LiteBaseAdapter<>(this,
                mJonieGroupInfo,
                GroupInfoViewHolder.class,
                R.layout.item_contacts_person,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GroupInfo groupInfo = (GroupInfo) v.getTag();
                        Intent intent = new Intent(GroupListOrgActivity.this, GroupUserListOrgActivity.class);
                        CreateGroupContactData contactsBean = new CreateGroupContactData();
                        contactsBean.strGroupID = groupInfo.strGroupID;
                        contactsBean.strGroupDomainCode = groupInfo.strGroupDomainCode;
                        intent.putExtra("mContactsBean", contactsBean);
                        intent.putExtra("isZhuanFa", isZhuanFa);
                        startActivity(intent);
                    }
                }, "false");
        rct_view_create.setAdapter(myCreateAdapter);
        rct_view_create.setLayoutManager(new SafeLinearLayoutManager(this));
        rct_view_jonie.setAdapter(myJonieAdapter);
        rct_view_jonie.setLayoutManager(new SafeLinearLayoutManager(this));

        refresh_view.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isFreadList) {
                } else {
                    requestGroupContacts();
                }
            }
        });

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

                for(GroupInfo temp : mCreateGroupInfo) {
                    if(temp.strGroupID.equals(mChoosedAdapter.getDatas().get(i).strId)) {
                        temp.isSelected = false;
                        break;
                    }
                }
                myCreateAdapter.notifyDataSetChanged();

                SelectedModeBean bean = mChoosedAdapter.getDatas().get(i);
                ChoosedContactsNew.get().remove(bean);
                mChoosedAdapter.notifyDataSetChanged();
                EventBus.getDefault().post(new EventUserClick(bean));
            }
        });
        rct_choosed.setAdapter(mChoosedAdapter);

        changeShowSelected();
    }

    private void handleChoice(GroupInfo groupInfo) {
        ModelApis.Contacts().requestqueryGroupChatInfo(groupInfo.strGroupDomainCode, groupInfo.strGroupID, new ModelCallback<ContactsGroupUserListBean>() {
            @Override
            public void onSuccess(final ContactsGroupUserListBean contactsBean) {
                groupInfo.isSelected = !groupInfo.isSelected;
                if (groupInfo.isSelected) {
                    ChoosedContactsNew.get().add(groupInfo);
                    if (!selectedMap.contains(groupInfo.strGroupID)) {
                        selectedMap.add(groupInfo.strGroupID);
                    }
                } else {
                    ChoosedContactsNew.get().remove(groupInfo);
                    if (selectedMap.contains(groupInfo.strGroupID)) {
                        selectedMap.remove(groupInfo.strGroupID);
                    }
                }
                if (!isZhuanFa) {
                    if (contactsBean != null && contactsBean.lstGroupUser != null && contactsBean.lstGroupUser.size() > 0) {
                        for (User user : contactsBean.lstGroupUser) {
                            if (!user.strUserID.equals(AppAuth.get().getUserID())) {

                                if (groupInfo.isSelected) {
                                    if (!ChoosedContactsNew.get().isContain(user)) {
                                        if (ChoosedContactsNew.get().getContacts().size() >= max + 1) {
                                            showToast("最多选" + max + "人，已达人数上限");
                                            return;
                                        }
                                        userGroupMap.put(user.strUserID, groupInfo.strGroupID);
                                        user.isSelected = true;
                                        ChoosedContactsNew.get().add(user, false);
                                    }
                                } else {
                                    if (ChoosedContactsNew.get().isContain(user)) {
                                        user.isSelected = false;
                                        ChoosedContactsNew.get().remove(user);

                                        if (userGroupMap.containsKey(user.strUserID)) {
                                            userGroupMap.remove(user.strUserID);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                mChoosedAdapter.notifyDataSetChanged();
                myCreateAdapter.notifyDataSetChanged();

                changeShowSelected();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        requestGroupContacts();
    }

    private void requestGroupContacts() {
        if (isRef) {
            return;
        }
        isRef = true;

        lstGroupInfo.clear();
        if (null != VIMApp.getInstance().mDomainInfoList && VIMApp.getInstance().mDomainInfoList.size() > 0) {
            refresh_view.setRefreshing(true);
            requestCount = VIMApp.getInstance().mDomainInfoList.size();
            currentRequestTime = 0;
            for (DomainInfoList.DomainInfo domainInfo : VIMApp.getInstance().mDomainInfoList) {
                ModelApis.Contacts().requestGroupBuddyContacts(-1, 0, 0, null, domainInfo.strDomainCode, new ModelCallback<ContactsGroupChatListBean>() {
                    @Override
                    public void onSuccess(final ContactsGroupChatListBean contactsBean) {
                        if (currentRequestTime == 0) {
                            lstGroupInfo.clear();
                        }
                        ++currentRequestTime;
                        lstGroupInfo.addAll(contactsBean.lstGroupInfo);
                        updateMsgTopNoDisturb(contactsBean.lstGroupInfo);

                        showData(et_key.getText().toString());
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
                        isRef = false;
                        if (requestCount == currentRequestTime) {
                            refresh_view.setRefreshing(false);
                            if (null != mJonieGroupInfo && mJonieGroupInfo.size() > 0) {
                                AppDatas.MsgDB().getGroupListDao().insertAll(mJonieGroupInfo);
                            }
                            if (null != mCreateGroupInfo && mCreateGroupInfo.size() > 0) {
                                AppDatas.MsgDB().getGroupListDao().insertAll(mCreateGroupInfo);
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

    private void showData(String str) {
        mCreateGroupInfo.clear();
        mJonieGroupInfo.clear();
        for (GroupInfo temp : lstGroupInfo) {
            if (selectedMap.contains(temp.strGroupID)) {
                temp.isSelected = true;
            }
            if (TextUtils.isEmpty(str)) {
                mCreateGroupInfo.add(temp);
                mJonieGroupInfo.add(temp);
            } else {
                if (temp.strGroupName.contains(str)) {
                    mCreateGroupInfo.add(temp);
                    mJonieGroupInfo.add(temp);
                }
            }
        }

        mapGroupName.clear();
        for (GroupInfo temp : mCreateGroupInfo) {
            if (selectedMap.contains(temp.strGroupID)) {
                temp.isSelected = true;
            }
            if (TextUtils.isEmpty(temp.strGroupName)) {
                ModelApis.Contacts().requestqueryGroupChatInfo(temp.strGroupDomainCode, temp.strGroupID,
                        new ModelCallback<ContactsGroupUserListBean>() {
                            @Override
                            public void onSuccess(final ContactsGroupUserListBean contactsBean) {
                                if (contactsBean != null) {
                                    ChatContactsGroupUserListHelper.getInstance().cacheContactsGroupDetail(contactsBean.strGroupID + "", contactsBean);
                                    mapGroupName.put(contactsBean.strGroupID, "群组(" + contactsBean.lstGroupUser.size() + ")");
                                } else {
                                    mapGroupName.put(temp.strGroupID, "群组(0)");
                                }

                                temp.strGroupName = mapGroupName.get(contactsBean.strGroupID);

                                if (myCreateAdapter != null) {
                                    myCreateAdapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onFailure(HTTPResponse httpResponse) {
                                super.onFailure(httpResponse);
                                mapGroupName.put(temp.strGroupID, "群组(0)");
                                temp.strGroupName = mapGroupName.get(temp.strGroupID);
                                if (myCreateAdapter != null) {
                                    myCreateAdapter.notifyDataSetChanged();
                                }
                            }
                        });
            }
        }

        if (!isFreadList) {
            myCreateAdapter.notifyDataSetChanged();
            myJonieAdapter.notifyDataSetChanged();
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

    private void changeSelected(boolean create) {
        this.create = create;
        if (create) {
            tv_my_create.setTextColor(Color.parseColor("#22a5ff"));
            tv_my_jonie.setTextColor(Color.parseColor("#333333"));
            view_my_create.setVisibility(View.VISIBLE);
            view_my_jonie.setVisibility(View.GONE);
            rct_view_create.setVisibility(View.VISIBLE);
            rct_view_jonie.setVisibility(View.GONE);
        } else {
            tv_my_create.setTextColor(Color.parseColor("#333333"));
            tv_my_jonie.setTextColor(Color.parseColor("#22a5ff"));
            view_my_create.setVisibility(View.GONE);
            view_my_jonie.setVisibility(View.VISIBLE);
            rct_view_create.setVisibility(View.GONE);
            rct_view_jonie.setVisibility(View.VISIBLE);
        }
    }

    private void changeShowSelected() {
        if (ChoosedContactsNew.get().getContactsSize() == 0 &&
                ChoosedContactsNew.get().getGroups().isEmpty() &&
                ChoosedContactsNew.get().getDepts().isEmpty()) {
            llChoosedPersons.setVisibility(View.GONE);
            tv_choose_confirm.setText("确定(0)");
        } else {
            llChoosedPersons.setVisibility(View.VISIBLE);
            tv_choose_confirm.setText("确定(" + ChoosedContactsNew.get().getSelectedModeSize() + ")");
        }
    }

    @OnClick({R.id.ll_search, R.id.et_key})
    public void onClickSearch(View view) {
        Intent intent = new Intent(this, SearchDeptUserListOrgActivity.class);
        intent.putExtra("max", max);
        intent.putExtra("isZhuanFa", isZhuanFa);
        startActivity(intent);
    }

    @OnClick(R.id.tv_choose_confirm)
    public void onClick(View view) {
        EventBus.getDefault().post(new EventUserSelectedComplete());
        finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventUserSelectedComplete bean) {
        finish();
    }

}
