package huaiye.com.vim.ui.zhuanfa;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import com.huaiye.sdk.logger.Logger;
import com.huaiye.sdk.sdpmsgs.social.SendUserBean;
import com.ttyy.commonanno.anno.BindLayout;
import com.ttyy.commonanno.anno.route.BindExtra;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import huaiye.com.vim.R;
import huaiye.com.vim.VIMApp;
import huaiye.com.vim.bus.CloseZhuanFa;
import huaiye.com.vim.common.AppBaseActivity;
import huaiye.com.vim.common.AppUtils;
import huaiye.com.vim.common.SP;
import huaiye.com.vim.common.recycle.SafeLinearLayoutManager;
import huaiye.com.vim.common.rx.RxUtils;
import huaiye.com.vim.dao.AppDatas;
import huaiye.com.vim.dao.msgs.ChatMessageBase;
import huaiye.com.vim.dao.msgs.UserInfo;
import huaiye.com.vim.dao.msgs.VimMessageBean;
import huaiye.com.vim.dao.msgs.VimMessageListMessages;
import huaiye.com.vim.models.ModelApis;
import huaiye.com.vim.models.ModelCallback;
import huaiye.com.vim.models.contacts.bean.ContactsGroupChatListBean;
import huaiye.com.vim.models.contacts.bean.DomainInfoList;
import huaiye.com.vim.models.contacts.bean.GroupInfo;
import huaiye.com.vim.ui.home.adapter.GroupContactsItemAdapter;
import ttyy.com.jinnetwork.core.work.HTTPRequest;
import ttyy.com.jinnetwork.core.work.HTTPResponse;


/**
 * Created by ywt on 2019/3/21.
 */
@BindLayout(R.layout.activity_group_list)
public class ZhuanFaGroupListActivity extends AppBaseActivity {

    @BindView(R.id.fl_root)
    View fl_root;
    @BindView(R.id.rct_view_create)
    RecyclerView rct_view_create;
    @BindView(R.id.iv_empty_view)
    View iv_empty_view;
    @BindView(R.id.refresh_view)
    SwipeRefreshLayout refresh_view;

    GroupContactsItemAdapter mGroupitemAdapter;
    private ArrayList<GroupInfo> mlstGroupInfo = new ArrayList<>();
    private int requestCount = 0;
    private int currentRequestTime = 0;
    private boolean isFreadList = false;

    @BindExtra
    String strUserDomainCode;
    @BindExtra
    String strUserID;
    @BindExtra
    ChatMessageBase data;
    @BindExtra
    ArrayList<UserInfo> users;
    @BindExtra
    boolean isGroup;
    @BindExtra
    String strGroupID;
    @BindExtra
    String strGroupDomain;
    @BindExtra
    ArrayList<SendUserBean> mMessageUsersDate;

    ZhuanFaGroupPopupWindowDuoFa zhuanFaGroupPopupWindow;

    @Override
    protected void initActionBar() {
        initNavigateView();
    }

    private void initNavigateView() {
        getNavigate().setVisibility(View.VISIBLE);
        getNavigate().setTitlText("群组")
                .setRightText("确定")
                .setLeftClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                })
                .setRightClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ArrayList<GroupInfo> groupInfos = new ArrayList<>();
                        for (GroupInfo temp : mlstGroupInfo) {
                            if (temp.isSelected) {
                                groupInfos.add(temp);
                            }
                        }
                        if (groupInfos.isEmpty()) {
                            showToast("请选择发送对象");
                            return;
                        }
                        zhuanFaGroupPopupWindow.setSendUser(groupInfos);
                        zhuanFaGroupPopupWindow.showAtLocation(fl_root, Gravity.CENTER, 0, 0);
                        zhuanFaGroupPopupWindow.showData(ZhuanFaGroupListActivity.this.data);
                    }
                });
    }

    @Override
    public void doInitDelay() {
        initView();
    }

    private void initView() {
        zhuanFaGroupPopupWindow = new ZhuanFaGroupPopupWindowDuoFa(this, users, strUserID, strUserDomainCode, isGroup, strGroupID, strGroupDomain);
        mGroupitemAdapter = new GroupContactsItemAdapter(this, mlstGroupInfo, true, null);
        mGroupitemAdapter.setOnItemClickLinstener(new GroupContactsItemAdapter.OnItemClickLinstener() {
            @Override
            public void onClick(int position, GroupInfo groupInfo) {
                groupInfo.isSelected = !groupInfo.isSelected;
                mGroupitemAdapter.notifyItemChanged(mlstGroupInfo.indexOf(groupInfo));
            }
        });
        rct_view_create.setAdapter(mGroupitemAdapter);
        rct_view_create.setLayoutManager(new SafeLinearLayoutManager(this));

        refresh_view.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isFreadList) {
                } else {
                    requestGroupContacts();
                }
            }
        });

        requestGroupContacts();
    }

    public void updateGroupContacts() {

        rct_view_create.setAdapter(mGroupitemAdapter);
        mGroupitemAdapter.setDatas(mlstGroupInfo);
        mGroupitemAdapter.notifyDataSetChanged();
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

                        for (GroupInfo temp : contactsBean.lstGroupInfo) {
//                            if (!temp.strGroupID.equals(data.groupID)) {
                            mlstGroupInfo.add(temp);
//                            }
                        }

                        updateMsgTopNoDisturb(mlstGroupInfo);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(VimMessageBean obj) {
        requestGroupContacts();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final CloseZhuanFa messageEvent) {
        finish();
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
