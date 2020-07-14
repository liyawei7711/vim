package huaiye.com.vim.ui.contacts;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ttyy.commonanno.anno.BindLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import huaiye.com.vim.R;
import huaiye.com.vim.VIMApp;
import huaiye.com.vim.common.AppBaseActivity;
import huaiye.com.vim.common.AppUtils;
import huaiye.com.vim.common.SP;
import huaiye.com.vim.common.helper.ChatContactsGroupUserListHelper;
import huaiye.com.vim.common.recycle.LiteBaseAdapter;
import huaiye.com.vim.common.recycle.SafeLinearLayoutManager;
import huaiye.com.vim.common.rx.RxUtils;
import huaiye.com.vim.dao.AppDatas;
import huaiye.com.vim.dao.msgs.VimMessageBean;
import huaiye.com.vim.dao.msgs.VimMessageListMessages;
import huaiye.com.vim.models.ModelApis;
import huaiye.com.vim.models.ModelCallback;
import huaiye.com.vim.models.contacts.bean.ContactsGroupChatListBean;
import huaiye.com.vim.models.contacts.bean.ContactsGroupUserListBean;
import huaiye.com.vim.models.contacts.bean.CreateGroupContactData;
import huaiye.com.vim.models.contacts.bean.DomainInfoList;
import huaiye.com.vim.models.contacts.bean.GroupInfo;
import huaiye.com.vim.ui.contacts.viewholder.GroupInfoViewHolder;
import huaiye.com.vim.ui.meet.ChatGroupActivityNew;
import ttyy.com.jinnetwork.core.work.HTTPRequest;
import ttyy.com.jinnetwork.core.work.HTTPResponse;


/**
 * Created by ywt on 2019/3/21.
 */
@BindLayout(R.layout.activity_group_list)
public class GroupListActivity extends AppBaseActivity {

    @BindView(R.id.et_key)
    EditText et_key;
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

    ArrayList<GroupInfo> lstGroupInfo = new ArrayList<>();
    LiteBaseAdapter<GroupInfo> myCreateAdapter;
    ArrayList<GroupInfo> mCreateGroupInfo = new ArrayList<>();
    LiteBaseAdapter<GroupInfo> myJonieAdapter;
    ArrayList<GroupInfo> mJonieGroupInfo = new ArrayList<>();
    int requestCount = 0;
    int currentRequestTime = 0;
    boolean isFreadList = false;
    boolean isRef = false;
    private boolean create;//当前是不是创建的模式

    public static Map<String, String> mapGroupName = new HashMap<>();

    @Override
    protected void initActionBar() {
        initNavigateView();
    }

    private void initNavigateView() {
        getNavigate().setVisibility(View.VISIBLE);
        getNavigate().setTitlText("我的群组")
                .setRightText("创建群聊")
                .setRightClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(GroupListActivity.this, ContactsAddOrDelActivityNewOrg.class);
                        intent.putExtra("titleName", AppUtils.getResourceString(R.string.user_detail_add_user_title));
                        intent.putExtra("isSelectUser", true);
                        intent.putExtra("isCreateGroup", true);
                        intent.putExtra("isAddMore", false);
                        GroupListActivity.this.startActivity(intent);
                    }
                })
                .setLeftClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
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
                GroupInfoViewHolder.class,
                R.layout.item_contacts_person,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GroupInfo groupInfo = (GroupInfo) v.getTag();
                        Intent intent = new Intent(GroupListActivity.this, ChatGroupActivityNew.class);
                        CreateGroupContactData contactsBean = new CreateGroupContactData();
                        contactsBean.strGroupID = groupInfo.strGroupID;
                        contactsBean.strGroupDomainCode = groupInfo.strGroupDomainCode;
                        intent.putExtra("mContactsBean", contactsBean);
                        startActivity(intent);
                    }
                }, "false");
        myJonieAdapter = new LiteBaseAdapter<>(this,
                mJonieGroupInfo,
                GroupInfoViewHolder.class,
                R.layout.item_contacts_person,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GroupInfo groupInfo = (GroupInfo) v.getTag();
                        Intent intent = new Intent(GroupListActivity.this, ChatGroupActivityNew.class);
                        CreateGroupContactData contactsBean = new CreateGroupContactData();
                        contactsBean.strGroupID = groupInfo.strGroupID;
                        contactsBean.strGroupDomainCode = groupInfo.strGroupDomainCode;
                        intent.putExtra("mContactsBean", contactsBean);
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

        et_key.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    Editable s = et_key.getText();
                    if (s != null && s.length() > 0) {
                        showData(s.toString());
                    } else {
                        showData("");
                    }
                    return true;
                }
                return false;
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(VimMessageBean obj) {
        requestGroupContacts();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

}
