package huaiye.com.vim.ui.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import com.huaiye.sdk.sdpmsgs.social.SendUserBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import huaiye.com.vim.R;
import huaiye.com.vim.common.dialog.ZeusLoadView;
import huaiye.com.vim.common.recycle.LiteBaseAdapter;
import huaiye.com.vim.common.recycle.SafeLinearLayoutManager;
import huaiye.com.vim.common.rx.RxUtils;
import huaiye.com.vim.dao.AppDatas;
import huaiye.com.vim.dao.auth.AppAuth;
import huaiye.com.vim.dao.msgs.ChatGroupMsgBean;
import huaiye.com.vim.dao.msgs.ChatSingleMsgBean;
import huaiye.com.vim.dao.msgs.SearchMessageBean;
import huaiye.com.vim.dao.msgs.User;
import huaiye.com.vim.dao.msgs.VimMessageListBean;
import huaiye.com.vim.dao.msgs.VimMessageListMessages;
import huaiye.com.vim.models.ModelApis;
import huaiye.com.vim.models.ModelCallback;
import huaiye.com.vim.models.contacts.bean.ContactsBean;
import huaiye.com.vim.models.contacts.bean.CreateGroupContactData;
import huaiye.com.vim.ui.chat.holder.ChatListViewNewHolder;
import huaiye.com.vim.ui.meet.ChatGroupActivityNew;
import huaiye.com.vim.ui.meet.ChatSingleActivity;
import ttyy.com.jinnetwork.core.work.HTTPResponse;

import static huaiye.com.vim.common.AppBaseActivity.showToast;

/**
 * Created by Administrator on 2018\3\14 0014.
 */

public class SearchChatActivity extends Activity {
    @BindView(R.id.et_key)
    EditText et_key;
    @BindView(R.id.et_search_cancel)
    TextView et_search_cancel;
    @BindView(R.id.close)
    ImageView close;
    @BindView(R.id.rct_view)
    RecyclerView rct_view;
    @BindView(R.id.iv_empty_view)
    View iv_empty_view;

    LiteBaseAdapter<SearchMessageBean> adapter;
    ArrayList<SearchMessageBean> datas = new ArrayList<>();//展示数据
    Map<String, SearchMessageBean> maps = new HashMap<>();
    public ZeusLoadView mZeusLoadView;

    boolean canShow = false;
    int totalNum = 0;

    @SuppressLint("ResourceAsColor")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_home_meetings_search);
        ButterKnife.bind(this);

        mZeusLoadView = new ZeusLoadView(this);
        mZeusLoadView.setCancelable(true);

        initListener();
        initData();
    }

    private void initData() {
        adapter = new LiteBaseAdapter<>(this,
                datas,
                ChatListViewNewHolder.class,
                R.layout.item_chat_list_new_view,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dealAdapterItemClick(v);
                    }
                }, "false");
        rct_view.setLayoutManager(new SafeLinearLayoutManager(this));
        rct_view.setAdapter(adapter);
    }

    private void dealAdapterItemClick(View v) {
        SearchMessageBean bean = (SearchMessageBean) v.getTag();
        bean.listBean.isRead = 1;
        VimMessageListMessages.get().isRead(bean.listBean);
        adapter.notifyDataSetChanged();

        Intent intent;
        if (bean.listBean.groupType == 2) {
            requestUser(bean.listBean);
        } else if (bean.listBean.groupType == 1) {
            intent = new Intent(this, ChatGroupActivityNew.class);
            CreateGroupContactData contactsBean = new CreateGroupContactData();
            contactsBean.strGroupDomainCode = bean.listBean.groupDomainCode;
            contactsBean.strGroupID = bean.listBean.groupID;
            contactsBean.sessionName = bean.listBean.sessionName;
            intent.putExtra("mContactsBean", contactsBean);
            startActivity(intent);
        } else {
            intent = new Intent(this, ChatSingleActivity.class);
            intent.putExtra("mOtherUserName", bean.listBean.sessionName);
            User nUser = new User();
            if (!bean.listBean.sessionUserList.get(0).strUserID.equals(AppAuth.get().getUserID())) {
                intent.putExtra("mOtherUserId", bean.listBean.sessionUserList.get(0).strUserID);
                nUser.strUserName = bean.listBean.sessionUserList.get(0).strUserName;
                nUser.strUserID = bean.listBean.sessionUserList.get(0).strUserID;
                nUser.strUserDomainCode = bean.listBean.sessionUserList.get(0).strUserDomainCode;
                nUser.strDomainCode = bean.listBean.sessionUserList.get(0).strUserDomainCode;
            } else {
                intent.putExtra("mOtherUserId", bean.listBean.sessionUserList.get(1).strUserID);
                nUser.strUserName = bean.listBean.sessionUserList.get(1).strUserName;
                nUser.strUserID = bean.listBean.sessionUserList.get(1).strUserID;
                nUser.strUserDomainCode = bean.listBean.sessionUserList.get(1).strUserDomainCode;
                nUser.strDomainCode = bean.listBean.sessionUserList.get(1).strUserDomainCode;

            }
            intent.putExtra("nUser", nUser);
            intent.putExtra("sessionUserList", bean.listBean.sessionUserList);
            intent.putExtra("mOtherUserDomainCode", nUser.getDomainCode());
            startActivity(intent);
        }
    }

    private void requestUser(VimMessageListBean bean) {
        mZeusLoadView.loadingText("正在加载").setLoading();
        ModelApis.Contacts().requestContacts(bean.groupDomainCode, bean.groupID, new ModelCallback<ContactsBean>() {
            @Override
            public void onSuccess(final ContactsBean contactsBean) {
                if (null != contactsBean && null != contactsBean.userList && contactsBean.userList.size() > 0) {
                    Intent intent = new Intent(SearchChatActivity.this, ChatGroupActivityNew.class);
                    CreateGroupContactData groupContactData = new CreateGroupContactData();
                    groupContactData.strGroupDomainCode = bean.groupDomainCode;
                    groupContactData.strGroupID = bean.groupID;
                    groupContactData.sessionName = bean.sessionName;
                    groupContactData.userList = contactsBean.userList;
                    intent.putExtra("mContactsBean", groupContactData);
                    startActivity(intent);
                    mZeusLoadView.dismiss();
                } else {
                    mZeusLoadView.dismiss();
                    showToast("获取部门联系人失败");
                }
            }

            @Override
            public void onFailure(HTTPResponse httpResponse) {
                super.onFailure(httpResponse);
                mZeusLoadView.dismiss();
                showToast("获取部门联系人失败");
            }
        });
    }

    private void initListener() {
        et_search_cancel.setOnClickListener(mOnClickListener);
        close.setOnClickListener(mOnClickListener);
        et_key.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                 /*if (System.currentTimeMillis() - time > 1000) {
                    time = System.currentTimeMillis();
                }*/
                    Editable s = et_key.getText();
                    if (s != null && s.length() > 0) {
                        refreshDatas(s.toString());
                    } else {
                        datas.clear();
                        adapter.notifyDataSetChanged();
                    }
                    return true;
                }
                return false;
            }
        });
    }

    private void refreshDatas(String key) {
        canShow = false;
        totalNum = 0;
        datas.clear();
        adapter.notifyDataSetChanged();

        maps.clear();

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
                return allBean;
            }

            @Override
            public void doOnMain(List<VimMessageListBean> data) {
                totalNum = data.size();
                for (VimMessageListBean temp : data) {
                    if (maps.containsKey(temp.sessionID)) {
                        VimMessageListBean tempLin = maps.get(temp.sessionID).listBean;
                        if (tempLin.mStrEncrypt.equals(temp.msgTxt)) {
                            temp.isUnEncrypt = tempLin.isUnEncrypt;
                            temp.msgTxt = tempLin.msgTxt;
                        }
                    }

                    maps.put(temp.sessionID, new SearchMessageBean(temp));
                    switch (temp.groupType) {
                        case 1:
                        case 2:
                            loadPageDataGroup(temp, temp.groupID, key);
                            break;
                        default:
                            String otherUserId = "";
                            for (SendUserBean user : temp.sessionUserList) {
                                if (!user.strUserID.equals(AppAuth.get().getUserID())) {
                                    otherUserId = user.strUserID;
                                    break;
                                }
                            }
                            loadPageDataUser(temp, otherUserId, key);
                            break;
                    }
                }
            }
        });
    }

    private void loadPageDataUser(VimMessageListBean listBean, String otherUserId, String key) {
        List<ChatSingleMsgBean> datas = AppDatas.MsgDB()
                .chatSingleMsgDao()
                .queryPagingItemWithoutLive(otherUserId, AppAuth.get().getUserID() + "", 0, 99999999);
        for (ChatSingleMsgBean temp : datas) {
            if ((temp.msgTxt != null && temp.msgTxt.contains(key)) ||
                    (temp.fileName != null && temp.fileName.contains(key)) ||
                    (temp.fileUrl != null && temp.fileUrl.contains(key)) ||
                    (temp.summary != null && temp.summary.contains(key))) {
                if (!maps.containsKey(listBean.sessionID)) {
                    maps.put(listBean.sessionID, new SearchMessageBean(listBean));
                }
                temp.index = datas.indexOf(temp);
                SearchMessageBean searchMessageBean = maps.get(listBean.sessionID);
                searchMessageBean.chatMessageBases.add(temp);
            }
        }

        totalNum--;
        if (totalNum == 0) {
            showData(key);
        }
    }

    private void loadPageDataGroup(VimMessageListBean listBean, String strGroupID, String key) {

        List<ChatGroupMsgBean> datas = AppDatas.MsgDB()
                .chatGroupMsgDao()
                .queryPagingItemWithoutLive(strGroupID, AppAuth.get().getUserID(), 0, 99999999);

        for (ChatGroupMsgBean temp : datas) {
            if ((temp.msgTxt != null && temp.msgTxt.contains(key)) ||
                    (temp.fileName != null && temp.fileName.contains(key)) ||
                    (temp.fileUrl != null && temp.fileUrl.contains(key)) ||
                    (temp.summary != null && temp.summary.contains(key))) {

                if (!maps.containsKey(listBean.sessionID)) {
                    maps.put(listBean.sessionID, new SearchMessageBean(listBean));
                }
                temp.index = datas.indexOf(temp);
                SearchMessageBean searchMessageBean = maps.get(listBean.sessionID);
                searchMessageBean.chatMessageBases.add(temp);
            }
        }

        totalNum--;
        if (totalNum == 0) {
            showData(key);
        }
    }

    private void showData(String key) {
        for (Map.Entry<String, SearchMessageBean> entry : maps.entrySet()) {
            String mapKey = entry.getKey();
            SearchMessageBean mapValue = entry.getValue();
            if (!mapValue.chatMessageBases.isEmpty() ||
                    (!TextUtils.isEmpty(mapValue.listBean.sessionName) && mapValue.listBean.sessionName.contains(key))) {
                datas.add(mapValue);
            }
        }
        adapter.notifyDataSetChanged();
        showEmpty();

        VimMessageListMessages.get().getMessagesUnReadNum();
    }

    private void showEmpty() {
        VimMessageListMessages.get().getMessagesUnReadNum();
        if (datas.size() > 0) {
            rct_view.setVisibility(View.VISIBLE);
            iv_empty_view.setVisibility(View.GONE);
        } else {
            rct_view.setVisibility(View.GONE);
            iv_empty_view.setVisibility(View.VISIBLE);
        }
    }


    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.et_search_cancel:
                    finish();
                    break;
                case R.id.close:
                    et_key.setText("");
                    break;
                case R.id.tv_choose_confirm:
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                    break;
                default:
                    break;
            }
        }
    };
}
