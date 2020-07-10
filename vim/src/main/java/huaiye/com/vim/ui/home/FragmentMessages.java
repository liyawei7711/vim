package huaiye.com.vim.ui.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;

import com.huaiye.sdk.HYClient;
import com.huaiye.sdk.logger.Logger;
import com.huaiye.sdk.sdpmsgs.social.SendUserBean;
import com.ttyy.commonanno.anno.BindLayout;
import com.ttyy.commonanno.anno.BindView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import huaiye.com.vim.R;
import huaiye.com.vim.bus.MessageEvent;
import huaiye.com.vim.bus.ReafBean;
import huaiye.com.vim.bus.RefMessageList;
import huaiye.com.vim.common.AppBaseActivity;
import huaiye.com.vim.common.AppBaseFragment;
import huaiye.com.vim.common.AppUtils;
import huaiye.com.vim.common.SP;
import huaiye.com.vim.common.dialog.LogicDialog;
import huaiye.com.vim.common.helper.ChatContactsGroupUserListHelper;
import huaiye.com.vim.common.recycle.LiteBaseAdapter;
import huaiye.com.vim.common.recycle.RecycleTouchUtils;
import huaiye.com.vim.common.recycle.SafeLinearLayoutManager;
import huaiye.com.vim.common.rx.RxUtils;
import huaiye.com.vim.dao.AppDatas;
import huaiye.com.vim.dao.auth.AppAuth;
import huaiye.com.vim.dao.msgs.BroadcastManage;
import huaiye.com.vim.dao.msgs.User;
import huaiye.com.vim.dao.msgs.VimMessageBean;
import huaiye.com.vim.dao.msgs.VimMessageListBean;
import huaiye.com.vim.dao.msgs.VimMessageListMessages;
import huaiye.com.vim.models.ModelApis;
import huaiye.com.vim.models.ModelCallback;
import huaiye.com.vim.models.contacts.bean.ContactsBean;
import huaiye.com.vim.models.contacts.bean.ContactsGroupUserListBean;
import huaiye.com.vim.models.contacts.bean.CreateGroupContactData;
import huaiye.com.vim.push.MessageNotify;
import huaiye.com.vim.push.MessageReceiver;
import huaiye.com.vim.ui.auth.SettingAddressSafeActivity;
import huaiye.com.vim.ui.chat.holder.ChatListViewHolder;
import huaiye.com.vim.ui.meet.ChatGroupActivityNew;
import huaiye.com.vim.ui.meet.ChatSingleActivity;
import ttyy.com.jinnetwork.core.work.HTTPResponse;

import static android.support.v7.widget.helper.ItemTouchHelper.Callback.makeMovementFlags;
import static huaiye.com.vim.common.AppBaseActivity.showToast;
import static huaiye.com.vim.common.AppUtils.nEncryptIMEnable;

/**
 * author: admin
 * date: 2017/12/28
 * version: 0
 * mail: secret
 * desc: FragmentMessages
 */
@BindLayout(R.layout.fragment_messages)
public class FragmentMessages extends AppBaseFragment implements MessageNotify {

    @BindView(R.id.message_list)
    RecyclerView message_list;
    @BindView(R.id.ll_empty)
    View ll_empty;
    @BindView(R.id.fl_search)
    View fl_search;


    ArrayList<VimMessageListBean> datas = new ArrayList<>();
    Map<String, VimMessageListBean> maps = new HashMap<>();
    public static Map<String, String> mapGroupName = new HashMap<>();
    LiteBaseAdapter<VimMessageListBean> adapter;
    LiteBaseAdapter<VimMessageListBean> adapterSearch;
    private boolean isSOS;

    @SuppressLint("ResourceAsColor")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dimissChatMoreStylePopupWindow();
        EventBus.getDefault().register(this);
        MessageReceiver.get().subscribe(this);

        getNavigate().hideLeftIcon()
                .hideRightIcon()
                .showTopSearch()
                .showTopAdd()
                .setReserveStatusbarPlace()
                .setTitlText(AppUtils.getString(R.string.app_name))
                .setTitleLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        startActivity(new Intent(getContext(), SettingAddressSafeActivity.class));
                        return false;
                    }
                })
                .setTopSearchClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isSOS) {
                            return;
                        }
                        Log.d(this.getClass().getName(), "onClick");
                        Intent intent = new Intent(getContext(), SearchChatActivity.class);
                        startActivity(intent);
                    }
                }).setTopAddClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSOS) {
                    return;
                }
                showChatMoreStylePopupWindow(v);
            }
        });

        new RecycleTouchUtils().initTouch(new RecycleTouchUtils.ITouchEvent() {

            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                return makeMovementFlags(0, ItemTouchHelper.LEFT);
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                final LogicDialog logicDialog = new LogicDialog(getContext());
                logicDialog.setCancelable(false);
                logicDialog.setCanceledOnTouchOutside(false);
                logicDialog.setMessageText("是否删除这条消息?");
                logicDialog.setConfirmClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try{
                            int i = viewHolder.getAdapterPosition();
                            VimMessageListBean data = datas.get(i);

                            datas.remove(i);
                            adapter.notifyDataSetChanged();
                            showEmpty();

                            VimMessageListMessages.get().del(data.sessionID);
                            if (data.groupType == 1 || data.groupType == 2) {
                                SP.putInt(data.sessionID + AppUtils.SP_SETTING_NODISTURB, 0);
                                AppDatas.MsgDB()
                                        .chatGroupMsgDao()
                                        .deleteBySessionID(data.sessionID, AppAuth.get().getUserID());
                                AppDatas.MsgDB()
                                        .chatGroupMsgDao()
                                        .deleteGroup(data.sessionID, AppAuth.get().getUserID());
                            } else {
                                AppDatas.MsgDB()
                                        .chatSingleMsgDao()
                                        .deleteBySessionID(data.sessionID);
                            }

                            if (data.sessionID.equals("0")) {
                                BroadcastManage.get().delAll();
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        File file = new File(AppUtils.audiovideoPath);
                                        if (file.exists()) {
                                            File[] files = file.listFiles();
                                            for (File f : files) {
                                                AppUtils.delFile(f);
                                            }
                                            file.delete();
                                        }
                                    }
                                }).start();
                            }
                        }catch (Exception e){

                        }

                    }
                }).setCancelClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        adapter.notifyDataSetChanged();
                    }
                }).show();
            }

        }).attachToRecyclerView(message_list);

        adapter = new LiteBaseAdapter<>(getContext(),
                datas,
                ChatListViewHolder.class,
                R.layout.item_chat_list_view,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dealAdapterItemClick(v);
                    }
                }, "false");

        adapterSearch = new LiteBaseAdapter<>(getContext(),
                datas,
                ChatListViewHolder.class,
                R.layout.item_chat_list_view,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dealAdapterItemClick(v);
                    }
                }, "false");
        message_list.setLayoutManager(new SafeLinearLayoutManager(getContext()));
        message_list.setAdapter(adapter);
    }

    private void dealAdapterItemClick(View v) {
        if (!HYClient.getSdkOptions().encrypt().isEncryptBind() && nEncryptIMEnable) {
            AppBaseActivity.showToast("加密模块未初始化完成，请稍后");
            return;
        }
        VimMessageListBean bean = (VimMessageListBean) v.getTag();
        bean.isRead = 1;
        VimMessageListMessages.get().isRead(bean);
        adapter.notifyItemChanged(datas.indexOf(bean));

        Intent intent;
        if (bean.groupType == 2) {
            requestUser(bean);
        } else if (bean.groupType == 1) {
            intent = new Intent(getActivity(), ChatGroupActivityNew.class);
            CreateGroupContactData contactsBean = new CreateGroupContactData();
            contactsBean.strGroupDomainCode = bean.groupDomainCode;
            contactsBean.strGroupID = bean.groupID;
            contactsBean.sessionName = bean.sessionName;
            intent.putExtra("mContactsBean", contactsBean);
            startActivity(intent);
        } else {
            intent = new Intent(getActivity(), ChatSingleActivity.class);
            intent.putExtra("mOtherUserName", bean.sessionName);
            User nUser = new User();
            if (!bean.sessionUserList.get(0).strUserID.equals(AppAuth.get().getUserID())) {
                intent.putExtra("mOtherUserId", bean.sessionUserList.get(0).strUserID);
                nUser.strUserName = bean.sessionUserList.get(0).strUserName;
                nUser.strUserID = bean.sessionUserList.get(0).strUserID;
                nUser.strUserDomainCode = bean.sessionUserList.get(0).strUserDomainCode;
                nUser.strDomainCode = bean.sessionUserList.get(0).strUserDomainCode;
            } else {
                intent.putExtra("mOtherUserId", bean.sessionUserList.get(1).strUserID);
                nUser.strUserName = bean.sessionUserList.get(1).strUserName;
                nUser.strUserID = bean.sessionUserList.get(1).strUserID;
                nUser.strUserDomainCode = bean.sessionUserList.get(1).strUserDomainCode;
                nUser.strDomainCode = bean.sessionUserList.get(1).strUserDomainCode;
            }

            intent.putExtra("nUser", nUser);
            intent.putExtra("sessionUserList", bean.sessionUserList);
            intent.putExtra("mOtherUserDomainCode", nUser.strUserDomainCode);
            startActivity(intent);
        }
    }

    private void requestUser(VimMessageListBean bean) {
        ((AppBaseActivity)getActivity()).mZeusLoadView.loadingText("正在加载").setLoading();
        ModelApis.Contacts().requestContacts(bean.groupDomainCode, bean.groupID, new ModelCallback<ContactsBean>() {
            @Override
            public void onSuccess(final ContactsBean contactsBean) {
                if (null != contactsBean && null != contactsBean.userList && contactsBean.userList.size() > 0) {
                    Intent intent = new Intent(getActivity(), ChatGroupActivityNew.class);
                    CreateGroupContactData groupContactData = new CreateGroupContactData();
                    groupContactData.strGroupDomainCode = bean.groupDomainCode;
                    groupContactData.strGroupID = bean.groupID;
                    groupContactData.sessionName = bean.sessionName;
                    groupContactData.userList = contactsBean.userList;
                    intent.putExtra("mContactsBean", groupContactData);
                    startActivity(intent);
                    ((AppBaseActivity)getActivity()).mZeusLoadView.dismiss();
                } else {
                    ((AppBaseActivity)getActivity()).mZeusLoadView.dismiss();
                    showToast("获取部门联系人失败");
                }
            }

            @Override
            public void onFailure(HTTPResponse httpResponse) {
                super.onFailure(httpResponse);
                ((AppBaseActivity)getActivity()).mZeusLoadView.dismiss();
                showToast("获取部门联系人失败");
            }
        });
    }

    public void refMessage() {
        if (isSOS) {
            showEmpty();
            return;
        }
        maps.clear();
        for (VimMessageListBean temp : datas) {
            maps.put(temp.sessionID, temp);
        }
        new RxUtils<>().doOnThreadObMain(new RxUtils.IThreadAndMainDeal<List<VimMessageListBean>>() {
            @Override
            public List<VimMessageListBean> doOnThread() {
                List<VimMessageListBean> allBean = VimMessageListMessages.get().getMessages();
                for (VimMessageListBean vimMessageListBean : allBean) {
                    if (vimMessageListBean.groupType == 1 || vimMessageListBean.groupType == 2) {
                        vimMessageListBean.strHeadUrl = AppDatas.MsgDB().getGroupListDao().getGroupHeadPic(vimMessageListBean.groupID, vimMessageListBean.groupDomainCode);
                    } else {
                        ArrayList<SendUserBean> messageUsers = vimMessageListBean.sessionUserList;
                        if (messageUsers != null && messageUsers.size() > 0) {
                            if (messageUsers.size() == 2) {
                                Logger.err("receive single chat list not 2 is " + messageUsers.size());
                                SendUserBean friend = null;
                                for (SendUserBean sendUserBean : messageUsers) {
                                    if (!sendUserBean.strUserID.equals(AppDatas.Auth().getUserID())) {
                                        friend = sendUserBean;
                                        break;
                                    }
                                }
                                vimMessageListBean.strHeadUrl = AppDatas.MsgDB().getFriendListDao().getFriendHeadPic(friend.strUserID, friend.strUserDomainCode);
                            }
                        }
                    }
                }

                mapGroupName.clear();
                for(VimMessageListBean temp : allBean) {
                    if(temp.groupType == 1) {
                        if(TextUtils.isEmpty(temp.sessionName)) {
                            ModelApis.Contacts().requestqueryGroupChatInfo(temp.groupDomainCode, temp.groupID,
                                    new ModelCallback<ContactsGroupUserListBean>() {
                                        @Override
                                        public void onSuccess(final ContactsGroupUserListBean contactsBean) {
                                            if(contactsBean != null) {
                                                ChatContactsGroupUserListHelper.getInstance().cacheContactsGroupDetail(contactsBean.strGroupID + "", contactsBean);
                                                mapGroupName.put(contactsBean.strGroupID, "群组("+contactsBean.lstGroupUser.size()+")");
                                            } else {
                                                mapGroupName.put(temp.groupID, "群组(0)");
                                            }

                                            if(adapter != null) {
                                                adapter.notifyDataSetChanged();
                                            }
                                        }

                                        @Override
                                        public void onFailure(HTTPResponse httpResponse) {
                                            super.onFailure(httpResponse);
                                            mapGroupName.put(temp.groupID, "群组(0)");

                                            if(adapter != null) {
                                                adapter.notifyDataSetChanged();
                                            }
                                        }
                                    });
                        }
                    }
                }
                return allBean;
            }

            @Override
            public void doOnMain(List<VimMessageListBean> data) {
                for (VimMessageListBean temp : data) {
                    if (maps.containsKey(temp.sessionID)) {
                        VimMessageListBean tempLin = maps.get(temp.sessionID);
                        if (tempLin.mStrEncrypt.equals(temp.msgTxt)) {
                            temp.isUnEncrypt = tempLin.isUnEncrypt;
                            temp.msgTxt = tempLin.msgTxt;
                        }
                    }
                }
                datas.clear();
                datas.addAll(data);
                adapter.notifyDataSetChanged();
                refNum();
                showEmpty();
            }
        });

    }

    /**
     * 个人头像变更
     *
     * @param user
     */
    private void refreshCurrentUserData(User user) {
        new RxUtils<ArrayList<VimMessageListBean>>().doOnThreadObMain(new RxUtils.IThreadAndMainDeal<ArrayList<VimMessageListBean>>() {
            @Override
            public ArrayList<VimMessageListBean> doOnThread() {
                if (null != datas && datas.size() > 0 && null != user) {
                    for (VimMessageListBean vimMessageListBean : datas) {
                        if (vimMessageListBean.groupType != 1) {
                            ArrayList<SendUserBean> messageUsers = vimMessageListBean.sessionUserList;
                            if (messageUsers != null && messageUsers.size() > 0) {
                                if (messageUsers.size() == 2) {
                                    Logger.err("receive single chat list not 2 is " + messageUsers.size());
                                    SendUserBean friend = null;
                                    for (SendUserBean sendUserBean : messageUsers) {
                                        if (!sendUserBean.strUserID.equals(AppDatas.Auth().getUserID())) {
                                            friend = sendUserBean;
                                            break;
                                        }
                                    }
                                    if (friend.strUserID.equals(user.strUserID) && friend.strUserDomainCode.equals(user.strUserDomainCode)) {
                                        vimMessageListBean.strHeadUrl = AppDatas.MsgDB().getFriendListDao().getFriendHeadPic(friend.strUserID, friend.strUserDomainCode);
                                        break;
                                    }
                                }


                            }
                        }
                    }

                }
                return datas;
            }

            @Override
            public void doOnMain(ArrayList<VimMessageListBean> data) {

                if (null != adapter) {
                    adapter.notifyDataSetChanged();
                }
            }
        });

    }

    public void refNum() {
        int count = 0;
        for (VimMessageListBean temp : datas) {
            if (temp.isRead == 0) {
                count++;
            }
        }
        ((MainActivity) getActivity()).changeRedCircle(count);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            refMessage();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ReafBean obj) {
        //接到群组的人员变化后刷新界面
        refMessage();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(VimMessageBean obj) {
        //接到群组的人员变化后刷新界面
        refMessage();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RefMessageList obj) {
        refMessage();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ContactsGroupUserListBean groupUserListBean) {
        for (VimMessageListBean temp : datas) {
            if (temp.sessionID.equals(groupUserListBean.strGroupDomainCode + groupUserListBean.strGroupID)) {
                refMessage();
                break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final MessageEvent messageEvent) {
        if (null == messageEvent) {
            return;
        }

        switch (messageEvent.what) {
            case AppUtils.EVENT_MESSAGE_MODIFY_HEAD_PIC:
                User user = (User) messageEvent.obj1;
                refreshCurrentUserData(user);
                break;
            default:
                break;
        }
    }

    private void showEmpty() {
        VimMessageListMessages.get().getMessagesUnReadNum();
        if (datas.size() > 0) {
            message_list.setVisibility(View.VISIBLE);
            ll_empty.setVisibility(View.GONE);
        } else {
            message_list.setVisibility(View.GONE);
            ll_empty.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        MessageReceiver.get().unSubscribe(this);
    }

    public void isRead() {
        AppDatas.Messages().isReadAll();
        ((MainActivity) getActivity()).resetMessageNumbers();
    }

    public void setSos(boolean isSOS) {
        this.isSOS = isSOS;
    }
}
