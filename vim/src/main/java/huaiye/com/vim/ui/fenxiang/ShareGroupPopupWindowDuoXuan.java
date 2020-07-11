package huaiye.com.vim.ui.fenxiang;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.huaiye.sdk.HYClient;
import com.huaiye.sdk.core.SdkCallback;
import com.huaiye.sdk.sdkabi._api.ApiSocial;
import com.huaiye.sdk.sdkabi._params.SdkParamsCenter;
import com.huaiye.sdk.sdpmsgs.social.CSendMsgToMuliteUserRsp;
import com.huaiye.sdk.sdpmsgs.social.SendUserBean;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import huaiye.com.vim.R;
import huaiye.com.vim.bus.CloseZhuanFa;
import huaiye.com.vim.common.AppBaseActivity;
import huaiye.com.vim.common.AppUtils;
import huaiye.com.vim.common.utils.ChatUtil;
import huaiye.com.vim.dao.AppDatas;
import huaiye.com.vim.dao.auth.AppAuth;
import huaiye.com.vim.dao.msgs.ChatGroupMsgBean;
import huaiye.com.vim.dao.msgs.ChatMessageBean;
import huaiye.com.vim.dao.msgs.SendMsgUserBean;
import huaiye.com.vim.dao.msgs.User;
import huaiye.com.vim.dao.msgs.VimMessageBean;
import huaiye.com.vim.models.ModelApis;
import huaiye.com.vim.models.ModelCallback;
import huaiye.com.vim.models.contacts.bean.ContactsGroupUserListBean;
import huaiye.com.vim.models.contacts.bean.CreateGroupContactData;
import huaiye.com.vim.models.contacts.bean.GroupInfo;
import huaiye.com.vim.ui.meet.ChatGroupActivityNew;
import ttyy.com.jinnetwork.core.work.HTTPResponse;

public class ShareGroupPopupWindowDuoXuan extends PopupWindow {
    private Context mContext;
    Bundle data;

    ArrayList<GroupInfo> groupInfos;//转发的对象
    Map<String, ArrayList<SendUserBean>> map = new HashMap<>();

    ImageView iv_head;
    TextView tv_name;
    TextView tv_title;
    TextView tv_content;
    TextView tv_from;
    ImageView iv_content;

    TextView tv_send;

    RequestOptions requestOptions;

    boolean hasJump = false;

    View.OnClickListener onClickListener;

    public ShareGroupPopupWindowDuoXuan(Context context) {
        super(context);
        mContext = context;
        initView();
    }

    public void initView() {

        setBackgroundDrawable(null);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);

        requestOptions = new RequestOptions().centerCrop()
                .dontAnimate()
                .format(DecodeFormat.PREFER_RGB_565)
                .placeholder(R.drawable.default_image_personal)
                .error(R.drawable.default_image_personal)
                .optionalTransform(new CircleCrop());

        Drawable drawable = new ColorDrawable(Color.parseColor("#00000000"));
        setBackgroundDrawable(drawable);// 点击外部消失
        setOutsideTouchable(true); // 点击外部消失
        setFocusable(true); // 点击back键消失

        View view = LayoutInflater.from(mContext).inflate(R.layout.share_popwindow, null);
        setContentView(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        iv_head = view.findViewById(R.id.iv_head);
        tv_name = view.findViewById(R.id.tv_name);
        tv_title = view.findViewById(R.id.tv_title);
        tv_content = view.findViewById(R.id.tv_content);
        tv_from = view.findViewById(R.id.tv_from);
        iv_content = view.findViewById(R.id.iv_content);
        TextView tv_cancel = view.findViewById(R.id.tv_cancel);
        tv_send = view.findViewById(R.id.tv_send);
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        tv_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_send.setEnabled(false);
                if (onClickListener != null) {
                    onClickListener.onClick(v);
                } else {
                    sendMessage();
                }
            }
        });
    }

    public void sendMessage() {
        for (GroupInfo temp : groupInfos) {
            sendTxtMsg(temp);
        }
    }

    public void setSendUser(ArrayList<GroupInfo> groupInfos, Bundle data) {
        this.data = data;
        this.groupInfos = groupInfos;
        initUserEncrypt();
    }

    private void sendTxtMsg(GroupInfo groupInfo) {
        if (TextUtils.isEmpty(tv_title.getHint())) {
            tv_title.setHint("");
        }
        if (TextUtils.isEmpty(tv_title.getText())) {
            tv_title.setText("");
        }
        String msgContent = ChatUtil.getChatContentJson(mContext, tv_title.getText().toString(),
                tv_content.getText().toString(),
                tv_title.getHint().toString(), 0, 0,
                false,
                tv_title.getText().toString().length(), 0, 0, 0, "");
        sendWetherEncrypt(groupInfo, msgContent);

    }

    private void sendWetherEncrypt(GroupInfo groupInfo, String msgContent) {
        final ChatMessageBean bean = new ChatMessageBean();
        bean.content = msgContent;
        bean.type = AppUtils.MESSAGE_TYPE_SHARE;
        bean.sessionID = getGroupSessionId(groupInfo);
        bean.sessionName = getSessionName(groupInfo);
        bean.fromUserDomain = AppDatas.Auth().getDomainCode();
        bean.fromUserId = AppDatas.Auth().getUserID() + "";
        bean.fromUserName = AppDatas.Auth().getUserName();
        bean.groupType = 1;
        bean.bEncrypt = 0;
        bean.groupDomainCode = groupInfo.strGroupDomainCode;
        bean.groupID = groupInfo.strGroupID;
        bean.time = System.currentTimeMillis() / 1000;
        bean.sessionUserList = new ArrayList<>();
        bean.sessionUserList.addAll(map.get(groupInfo.strGroupID));
        Gson gson = new Gson();
        HYClient.getModule(ApiSocial.class).sendMessage(SdkParamsCenter.Social.SendMuliteMessage()
                        .setIsImportant(true)
                        .setMessage(gson.toJson(bean))
                        .setUser(map.get(groupInfo.strGroupID)), new SdkCallback<CSendMsgToMuliteUserRsp>() {
                    @Override
                    public void onSuccess(CSendMsgToMuliteUserRsp cSendMsgToMuliteUserRsp) {
                        ChatGroupMsgBean groupMsgBean = ChatGroupMsgBean.from(bean);
                        groupMsgBean.read = 1;
                        AppDatas.MsgDB()
                                .chatGroupMsgDao()
                                .insert(groupMsgBean);
                        VimMessageBean vimMessageBean = VimMessageBean.from(bean);
                        huaiye.com.vim.dao.msgs.ChatUtil.get().saveChangeMsg(vimMessageBean, true);

                        for (SendUserBean temp : bean.sessionUserList) {
                            SendMsgUserBean sendUserInfo = AppDatas.MsgDB().getSendUserListDao().getSendUserInfo(groupMsgBean.sessionID);
                            if (sendUserInfo != null) {
                                sendUserInfo.strUserID = temp.strUserID;
                                sendUserInfo.strUserDomainCode = temp.strUserDomainCode;
                                AppDatas.MsgDB().getSendUserListDao().update(sendUserInfo);
                            } else {
                                AppDatas.MsgDB().getSendUserListDao().insert(new SendMsgUserBean(groupMsgBean.sessionID, temp.strUserID, temp.strUserDomainCode));
                            }
                        }

                        ((AppBaseActivity) mContext).mZeusLoadView.dismiss();
                        AppBaseActivity.showToast("分享成功");
                        dismiss();
                        jumpToChat();
                        EventBus.getDefault().post(new CloseZhuanFa());
                    }

                    @Override
                    public void onError(ErrorInfo errorInfo) {
                        ((AppBaseActivity) mContext).mZeusLoadView.dismiss();
                        AppBaseActivity.showToast("分享失败" + errorInfo.getMessage());
                        dismiss();
                    }
                }
        );
    }

    private void jumpToChat() {
        if (hasJump) {
            return;
        }
        hasJump = true;
        Intent intent = new Intent(mContext, ChatGroupActivityNew.class);
        CreateGroupContactData contactsBean = new CreateGroupContactData();
        contactsBean.strGroupDomainCode = groupInfos.get(0).strGroupDomainCode;
        contactsBean.strGroupID = groupInfos.get(0).strGroupID;
        contactsBean.sessionName = getSessionName(groupInfos.get(0));
        intent.putExtra("mContactsBean", contactsBean);
        intent.putExtra("from", "share");
        mContext.startActivity(intent);
    }

    public void showData() {
        if (tv_send != null) {
            tv_send.setEnabled(true);
        }
        Glide.with(mContext)
                .load(AppDatas.Constants().getFileServerURL() + groupInfos.get(0).strHeadUrl)
                .apply(requestOptions)
                .into(iv_head);
        if (groupInfos.size() == 1) {
            tv_name.setText(groupInfos.get(0).strGroupName);
        } else {
            tv_name.setText(groupInfos.get(0).strGroupName + "等群组");
        }
        //url   //title  //content  //share_source_from  //file
        //android.intent.extra.SUBJECT
        //android.intent.extra.TEXT
        if (TextUtils.isEmpty(data.getString("title"))) {
            if (data.containsKey("android.intent.extra.SUBJECT")) {
                tv_title.setText(data.getString("android.intent.extra.SUBJECT"));
            }
        } else {
            tv_title.setText(data.getString("title"));
        }

        if (TextUtils.isEmpty(data.getString("url"))) {
            if (TextUtils.isEmpty(data.getString("android.intent.extra.TEXT"))) {
                tv_title.setHint("url is empty");
            } else {
                try {
                    tv_title.setHint(data.getString("android.intent.extra.TEXT").substring(data.getString("android.intent.extra.TEXT").indexOf("http")));
                } catch (Exception e) {
                    tv_title.setHint("url is empty");
                }
            }
        } else {
            tv_title.setHint(data.getString("url"));
        }
        if (TextUtils.isEmpty(data.getString("content"))) {
            tv_content.setText(tv_title.getText());
        } else {
            tv_content.setText(data.getString("content"));
        }
        tv_from.setText(TextUtils.isEmpty(data.getString("share_source_from")) ? "" : data.getString("share_source_from"));
        Glide.with(mContext)
                .load(data.getString("file"))
                .apply(requestOptions)
                .into(iv_content);
    }

    public void showData(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
        if (tv_send != null) {
            tv_send.setEnabled(true);
        }

        Glide.with(mContext)
                .load(AppDatas.Constants().getFileServerURL() + groupInfos.get(0).strHeadUrl)
                .apply(requestOptions)
                .into(iv_head);
        if (groupInfos.size() == 1) {
            tv_name.setText(groupInfos.get(0).strGroupName);
        } else {
            tv_name.setText(groupInfos.get(0).strGroupName + "等群组");
        }
        //url   //title  //content  //share_source_from  //file
        //android.intent.extra.SUBJECT
        //android.intent.extra.TEXT
        if (TextUtils.isEmpty(data.getString("title"))) {
            if (data.containsKey("android.intent.extra.SUBJECT")) {
                tv_title.setText(data.getString("android.intent.extra.SUBJECT"));
            }
        } else {
            tv_title.setText(data.getString("title"));
        }

        if (TextUtils.isEmpty(data.getString("url"))) {
            if (TextUtils.isEmpty(data.getString("android.intent.extra.TEXT"))) {
                tv_title.setHint("url is empty");
            } else {
                try {
                    tv_title.setHint(data.getString("android.intent.extra.TEXT").substring(data.getString("android.intent.extra.TEXT").indexOf("http")));
                } catch (Exception e) {
                    tv_title.setHint("url is empty");
                }
            }
        } else {
            tv_title.setHint(data.getString("url"));
        }
        if (TextUtils.isEmpty(data.getString("content"))) {
            tv_content.setText(tv_title.getText());
        } else {
            tv_content.setText(data.getString("content"));
        }
        tv_from.setText(TextUtils.isEmpty(data.getString("share_source_from")) ? "" : data.getString("share_source_from"));
        Glide.with(mContext)
                .load(data.getString("file"))
                .apply(requestOptions)
                .into(iv_content);
    }

    private String getGroupSessionId(GroupInfo groupInfo) {
        return groupInfo.strGroupDomainCode + groupInfo.strGroupID;
    }

    private String getSessionName(GroupInfo groupInfo) {
        return groupInfo.strGroupName;
    }

    void initUserEncrypt() {
        for (GroupInfo groupInfo : groupInfos) {
            ModelApis.Contacts().requestqueryGroupChatInfo(groupInfo.strGroupDomainCode, groupInfo.strGroupID,
                    new ModelCallback<ContactsGroupUserListBean>() {
                        @Override
                        public void onSuccess(final ContactsGroupUserListBean contactsBean) {
                            ArrayList<SendUserBean> sendUserBeans = new ArrayList<>();
                            if (contactsBean != null && contactsBean.lstGroupUser != null) {
                                for (User temp : contactsBean.lstGroupUser) {
                                    if (!AppAuth.get().getUserID().equals(temp.strUserID)) {
                                        SendUserBean sendUserBean = new SendUserBean();
                                        sendUserBean.strUserID = temp.strUserID;
                                        sendUserBean.strUserDomainCode = temp.strUserDomainCode;
                                        sendUserBean.strUserName = temp.strUserName;
                                        sendUserBeans.add(sendUserBean);
                                    }
                                }
                            }
                            map.put(groupInfo.strGroupID, sendUserBeans);

                            showData();
                        }

                        @Override
                        public void onFailure(HTTPResponse httpResponse) {
                            super.onFailure(httpResponse);
                            AppBaseActivity.showToast("onFailure");
                        }
                    });
        }

    }

}
