package huaiye.com.vim.ui.zhuanfa;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
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
import com.huaiye.cmf.sdp.SdpMessageCmProcessIMReq;
import com.huaiye.sdk.HYClient;
import com.huaiye.sdk.core.SdkCallback;
import com.huaiye.sdk.sdkabi._api.ApiSocial;
import com.huaiye.sdk.sdkabi._params.SdkParamsCenter;
import com.huaiye.sdk.sdpmsgs.social.CSendMsgToMuliteUserRsp;
import com.huaiye.sdk.sdpmsgs.social.SendUserBean;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import huaiye.com.vim.R;
import huaiye.com.vim.bus.CloseZhuanFa;
import huaiye.com.vim.common.AppBaseActivity;
import huaiye.com.vim.common.AppUtils;
import huaiye.com.vim.common.SP;
import huaiye.com.vim.common.downloadutils.ChatContentDownload;
import huaiye.com.vim.common.utils.ChatUtil;
import huaiye.com.vim.common.utils.WeiXinDateFormat;
import huaiye.com.vim.dao.AppDatas;
import huaiye.com.vim.dao.auth.AppAuth;
import huaiye.com.vim.dao.msgs.ChatGroupMsgBean;
import huaiye.com.vim.dao.msgs.ChatMessageBase;
import huaiye.com.vim.dao.msgs.ChatMessageBean;
import huaiye.com.vim.dao.msgs.SendMsgUserBean;
import huaiye.com.vim.dao.msgs.User;
import huaiye.com.vim.dao.msgs.UserInfo;
import huaiye.com.vim.dao.msgs.VimMessageBean;
import huaiye.com.vim.models.ModelApis;
import huaiye.com.vim.models.ModelCallback;
import huaiye.com.vim.models.contacts.bean.ContactsGroupUserListBean;
import huaiye.com.vim.models.contacts.bean.GroupInfo;
import ttyy.com.jinnetwork.core.work.HTTPResponse;

import static huaiye.com.vim.common.AppBaseActivity.showToast;

public class ZhuanFaGroupPopupWindowDuoFa extends PopupWindow {
    private Context mContext;
    ChatMessageBase data;

    String strUserID; //老对话的id
    String strUserDomainCode;//老对话的domain
    ArrayList<SdpMessageCmProcessIMReq.UserInfo> users = new ArrayList<>();//老对话的所有user

    boolean isGroup;
    String strGroupID;
    String strGroupDomain;

    ArrayList<GroupInfo> groupInfos;//转发的对象
    Map<String, ArrayList<SendUserBean>> map = new HashMap<>();

    ImageView iv_head;
    TextView tv_name;
    TextView tv_content;
    ImageView iv_content;

    View fl_common;
    View ll_share;
    TextView tv_title;
    TextView tv_content_share;
    ImageView iv_content_share;
    TextView tv_from;
    TextView tv_send;

    RequestOptions requestOptions;
    File fC_CHUAN_SHU;
    File fC_BEANDI;
    File fC_MINGWEN;
    File fC_LINSHI;

    View.OnClickListener onClickListener;

    public ZhuanFaGroupPopupWindowDuoFa(Context context, ArrayList<UserInfo> users, String strUserID, String strUserDomainCode,
                                        boolean isGroup, String strGroupID, String strGroupDomain) {
        super(context);
        mContext = context;
        for (UserInfo temp : users) {
            SdpMessageCmProcessIMReq.UserInfo userInfo = new SdpMessageCmProcessIMReq.UserInfo();
            userInfo.strUserID = temp.strUserID;
            userInfo.strUserDomainCode = temp.strUserDomainCode;
            this.users.add(userInfo);
        }
        this.strUserID = strUserID;
        this.strUserDomainCode = strUserDomainCode;
        this.isGroup = isGroup;
        this.strGroupID = strGroupID;
        this.strGroupDomain = strGroupDomain;
        initView();
    }

    public void initView() {
        fC_CHUAN_SHU = new File(mContext.getExternalFilesDir(null) + File.separator + "Vim/chat/chuanshu");
        if (!fC_CHUAN_SHU.exists()) {
            fC_CHUAN_SHU.mkdirs();
        }

        fC_BEANDI = new File(mContext.getExternalFilesDir(null) + File.separator + "Vim/chat");
        if (!fC_BEANDI.exists()) {
            fC_BEANDI.mkdirs();
        }

        fC_MINGWEN = new File(mContext.getExternalFilesDir(null) + File.separator + "Vim/chat/mingwen");
        if (!fC_MINGWEN.exists()) {
            fC_MINGWEN.mkdirs();
        }
        fC_LINSHI = new File(mContext.getExternalFilesDir(null) + File.separator + "Vim/chat/linshi/");
        if (!fC_LINSHI.exists()) {
            fC_LINSHI.mkdirs();
        }
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

        View view = LayoutInflater.from(mContext).inflate(R.layout.zhuanfa_popwindow, null);
        setContentView(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        iv_head = view.findViewById(R.id.iv_head);
        tv_name = view.findViewById(R.id.tv_name);
        fl_common = view.findViewById(R.id.fl_common);
        ll_share = view.findViewById(R.id.ll_share);
        tv_content = view.findViewById(R.id.tv_content);
        iv_content = view.findViewById(R.id.iv_content);
        tv_title = view.findViewById(R.id.tv_title);
        tv_content_share = view.findViewById(R.id.tv_content_share);
        iv_content_share = view.findViewById(R.id.iv_content_share);
        tv_from = view.findViewById(R.id.tv_from);
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
            sendMessage(temp);
        }
    }

    public void setSendUser(ArrayList<GroupInfo> groupInfos) {
        this.groupInfos = groupInfos;
        initUserEncrypt();
    }

    private void sendMessage(GroupInfo groupInfo) {
        if (AppUtils.MESSAGE_TYPE_TEXT == data.type) {
            sendTxtMsg(groupInfo);
        } else if (AppUtils.MESSAGE_TYPE_IMG == data.type) {
            sendImg(groupInfo);
        } else if (AppUtils.MESSAGE_TYPE_FILE == data.type) {
            sendFile(groupInfo);
        } else if (AppUtils.MESSAGE_TYPE_VIDEO_FILE == data.type) {
            sendVideo(groupInfo);
        } else if (AppUtils.MESSAGE_TYPE_AUDIO_FILE == data.type) {
            sendAudio(groupInfo);
        } else if (AppUtils.MESSAGE_TYPE_SHARE == data.type) {
            sendShareMsg(groupInfo);
        } else {
            sendTxtMsg(groupInfo);
        }

    }

    private void sendImg(GroupInfo groupInfo) {
        String msgContent = ChatUtil.getChatContentJson(mContext, "", "",
                data.fileUrl, 0, data.fileSize, SP.getBoolean(getGroupSessionId(groupInfo) + AppUtils.SP_CHAT_SETTING_YUEHOUJIFENG, false), 10,
                0, 0, 0, data.fileName);
        sendWetherEncrypt(groupInfo, msgContent);
    }

    private void sendVideo(GroupInfo groupInfo) {
        ((AppBaseActivity) mContext).mZeusLoadView.loadingText(AppUtils.getString(R.string.is_upload_ing)).setLoading();

        if (null != data && !TextUtils.isEmpty(data.localFilePath) && new File(data.localFilePath).exists()) {
        } else {
            if ("文件上传失败".equals(data.fileUrl)) {
                showToast("文件加载失败");
                dismiss();
                return;
            }
        }

        String msgContent = ChatUtil.getChatContentJson(mContext, "", "", data.fileUrl,
                data.nDuration, data.fileSize, SP.getBoolean(getGroupSessionId(groupInfo) + AppUtils.SP_CHAT_SETTING_YUEHOUJIFENG, false),
                data.nDuration, 0, 0, 0, data.fileName);
        sendWetherEncrypt(groupInfo, msgContent);

    }

    private void sendFile(GroupInfo groupInfo) {
        ((AppBaseActivity) mContext).mZeusLoadView.loadingText(AppUtils.getString(R.string.is_upload_ing)).setLoading();

        String localFilePath = "";
        if (null != data && !TextUtils.isEmpty(data.localFilePath) && new File(data.localFilePath).exists()) {
            localFilePath = data.localFilePath;
        } else {
            try {
                localFilePath = fC_CHUAN_SHU + data.fileUrl.substring(data.fileUrl.lastIndexOf("/"));
            } catch (Exception e) {
            }
        }

        String msgContent = ChatUtil.getChatContentJson(mContext, "", "", data.fileUrl,
                0, data.fileSize, false, 0,
                0, 0, 0, data.fileName);
        sendWetherEncrypt(groupInfo, msgContent);

    }

    private void sendAudio(GroupInfo groupInfo) {
        ((AppBaseActivity) mContext).mZeusLoadView.loadingText(AppUtils.getString(R.string.is_upload_ing)).setLoading();
        if (null != data && !TextUtils.isEmpty(data.localFilePath) && new File(data.localFilePath).exists()) {
        }

        String msgContent = ChatUtil.getChatContentJson(mContext, "", "",
                data.fileUrl, data.nDuration, data.fileSize, SP.getBoolean(getGroupSessionId(groupInfo) + AppUtils.SP_CHAT_SETTING_YUEHOUJIFENG, false),
                data.nDuration, 0, 0, 0, data.fileName);
        sendWetherEncrypt(groupInfo, msgContent);

    }

    private void sendTxtMsg(GroupInfo groupInfo) {
        String msgContent = ChatUtil.getChatContentJson(mContext, data.msgTxt, "", "", 0, 0,
                SP.getBoolean(getGroupSessionId(groupInfo) + AppUtils.SP_CHAT_SETTING_YUEHOUJIFENG, false),
                data.msgTxt.length(), 0, 0, 0, "");
        sendWetherEncrypt(groupInfo, msgContent);
    }

    private void sendShareMsg(GroupInfo groupInfo) {
        if (TextUtils.isEmpty(tv_title.getHint())) {
            tv_title.setHint("");
        }
        if (TextUtils.isEmpty(tv_title.getText())) {
            tv_title.setText("");
        }
        if (TextUtils.isEmpty(tv_content_share.getText())) {
            tv_content_share.setText("");
        }
        String msgContent = ChatUtil.getChatContentJson(mContext, tv_title.getText().toString(),
                tv_content_share.getText().toString(),
                tv_title.getHint().toString(), 0, 0,
                false,
                tv_title.getText().toString().length(), 0, 0, 0, "");

        sendWetherEncryptShare(groupInfo, msgContent);

    }

    private void sendWetherEncryptShare(GroupInfo groupInfo, String msgContent) {
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
                        showToast("分享成功");
                        dismiss();
                        EventBus.getDefault().post(new CloseZhuanFa(groupInfo.strGroupID, groupInfo.strGroupDomainCode));
                    }

                    @Override
                    public void onError(ErrorInfo errorInfo) {
                        ((AppBaseActivity) mContext).mZeusLoadView.dismiss();
                        showToast("分享失败" + errorInfo.getMessage());
                        dismiss();
                    }
                }
        );
    }

    private void sendWetherEncrypt(GroupInfo groupInfo, String msgContent) {
        final ChatMessageBean bean = new ChatMessageBean();
        bean.content = msgContent;
        bean.type = data.type;
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
        if (map.get(groupInfo.strGroupID).isEmpty()) {
            return;
        }
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
                        showToast("转发成功");
                        dismiss();
                        EventBus.getDefault().post(new CloseZhuanFa(groupInfo.strGroupID, groupInfo.strGroupDomainCode));
                    }

                    @Override
                    public void onError(ErrorInfo errorInfo) {
                        ((AppBaseActivity) mContext).mZeusLoadView.dismiss();
                        showToast("发送失败" + errorInfo.getMessage());
                        dismiss();
                    }
                }
        );
    }

    public void showData(ChatMessageBase data) {
        if (tv_send != null) {
            tv_send.setEnabled(true);
        }
        this.data = data;

        Glide.with(mContext)
                .load(AppDatas.Constants().getFileServerURL() + groupInfos.get(0).strHeadUrl)
                .apply(requestOptions)
                .into(iv_head);
        if (groupInfos.size() == 1) {
            tv_name.setText(groupInfos.get(0).strGroupName);
        } else {
            tv_name.setText(groupInfos.get(0).strGroupName + "等对象");
        }

        if (AppUtils.MESSAGE_TYPE_TEXT == data.type) {
            fl_common.setVisibility(View.VISIBLE);
            ll_share.setVisibility(View.GONE);
            iv_content.setVisibility(View.GONE);
            tv_content.setVisibility(View.VISIBLE);

            tv_content.setText(data.msgTxt);

        } else if (AppUtils.MESSAGE_TYPE_IMG == data.type) {
            fl_common.setVisibility(View.VISIBLE);
            ll_share.setVisibility(View.GONE);
            iv_content.setVisibility(View.VISIBLE);
            tv_content.setVisibility(View.GONE);
            showImg();
        } else if (AppUtils.MESSAGE_TYPE_FILE == data.type) {
            fl_common.setVisibility(View.VISIBLE);
            ll_share.setVisibility(View.GONE);
            iv_content.setVisibility(View.GONE);
            tv_content.setVisibility(View.VISIBLE);
            tv_content.setText(data.fileName == null ? data.fileUrl.substring(data.fileUrl.lastIndexOf("_") + 1) : data.fileName);
            loadFile();
        } else if (AppUtils.MESSAGE_TYPE_AUDIO_FILE == data.type) {
            fl_common.setVisibility(View.VISIBLE);
            ll_share.setVisibility(View.GONE);
            iv_content.setVisibility(View.GONE);
            tv_content.setVisibility(View.VISIBLE);
            tv_content.setText("转发语音信息:" + WeiXinDateFormat.getChatTime(data.time));
            loadAudio();
        } else if (AppUtils.MESSAGE_TYPE_VIDEO_FILE == data.type) {
            fl_common.setVisibility(View.VISIBLE);
            ll_share.setVisibility(View.GONE);
            iv_content.setVisibility(View.GONE);
            tv_content.setVisibility(View.VISIBLE);
            tv_content.setText("转发视频信息:" + WeiXinDateFormat.getChatTime(data.time));
            loadVideo();
        } else if (AppUtils.MESSAGE_TYPE_SHARE == data.type) {
            fl_common.setVisibility(View.GONE);
            ll_share.setVisibility(View.VISIBLE);
            try {
                tv_title.setText(data.msgTxt);
                tv_title.setHint(data.fileUrl);
            } catch (Exception e) {
            }
            tv_content_share.setHint(data.summary);
        } else {
            fl_common.setVisibility(View.VISIBLE);
            ll_share.setVisibility(View.GONE);
            iv_content.setVisibility(View.GONE);
            tv_content.setVisibility(View.VISIBLE);

            tv_content.setText(data.msgTxt);

        }

    }

    public void showData(ChatMessageBase data, View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
        if (tv_send != null) {
            tv_send.setEnabled(true);
        }
        this.data = data;
        if (onClickListener == null) {
            return;
        }

        Glide.with(mContext)
                .load(AppDatas.Constants().getFileServerURL() + groupInfos.get(0).strHeadUrl)
                .apply(requestOptions)
                .into(iv_head);
        if (groupInfos.size() == 1) {
            tv_name.setText(groupInfos.get(0).strGroupName);
        } else {
            tv_name.setText(groupInfos.get(0).strGroupName + "等对象");
        }

        if (AppUtils.MESSAGE_TYPE_TEXT == data.type) {
            fl_common.setVisibility(View.VISIBLE);
            ll_share.setVisibility(View.GONE);
            iv_content.setVisibility(View.GONE);
            tv_content.setVisibility(View.VISIBLE);

            tv_content.setText(data.msgTxt);

        } else if (AppUtils.MESSAGE_TYPE_IMG == data.type) {
            fl_common.setVisibility(View.VISIBLE);
            ll_share.setVisibility(View.GONE);
            iv_content.setVisibility(View.VISIBLE);
            tv_content.setVisibility(View.GONE);
            showImg();
        } else if (AppUtils.MESSAGE_TYPE_FILE == data.type) {
            fl_common.setVisibility(View.VISIBLE);
            ll_share.setVisibility(View.GONE);
            iv_content.setVisibility(View.GONE);
            tv_content.setVisibility(View.VISIBLE);
            tv_content.setText(data.fileName == null ? data.fileUrl.substring(data.fileUrl.lastIndexOf("_") + 1) : data.fileName);
            loadFile();
        } else if (AppUtils.MESSAGE_TYPE_AUDIO_FILE == data.type) {
            fl_common.setVisibility(View.VISIBLE);
            ll_share.setVisibility(View.GONE);
            iv_content.setVisibility(View.GONE);
            tv_content.setVisibility(View.VISIBLE);
            tv_content.setText("转发语音信息:" + WeiXinDateFormat.getChatTime(data.time));
            loadAudio();
        } else if (AppUtils.MESSAGE_TYPE_VIDEO_FILE == data.type) {
            fl_common.setVisibility(View.VISIBLE);
            ll_share.setVisibility(View.GONE);
            iv_content.setVisibility(View.GONE);
            tv_content.setVisibility(View.VISIBLE);
            tv_content.setText("转发视频信息:" + WeiXinDateFormat.getChatTime(data.time));
            loadVideo();
        } else if (AppUtils.MESSAGE_TYPE_SHARE == data.type) {
            fl_common.setVisibility(View.GONE);
            ll_share.setVisibility(View.VISIBLE);
            try {
                tv_title.setText(data.msgTxt);
                tv_title.setHint(data.fileUrl);
            } catch (Exception e) {
            }
            tv_content_share.setHint(data.summary);
        } else {
            fl_common.setVisibility(View.VISIBLE);
            ll_share.setVisibility(View.GONE);
            iv_content.setVisibility(View.GONE);
            tv_content.setVisibility(View.VISIBLE);

            tv_content.setText(data.msgTxt);

        }

    }

    private String getGroupSessionId(GroupInfo groupInfo) {
        return groupInfo.strGroupDomainCode + groupInfo.strGroupID;
    }

    private String getSessionName(GroupInfo groupInfo) {
        return groupInfo.strGroupName;
    }

    private void showImg() {
        Glide.with(mContext)
                .load(AppDatas.Constants().getFileServerURL() + data.fileUrl)
//                    .apply(requestOptions)
                .into(iv_content);
    }

    private void loadVideo() {
        if (null != data && !TextUtils.isEmpty(data.localFilePath) && new File(data.localFilePath).exists()) {
        } else {
            if ("文件上传失败".equals(data.fileUrl)) {
                showToast("文件加载失败");
                return;
            }
            String fileLocal = "";
            try {
                fileLocal = fC_CHUAN_SHU + data.fileUrl.substring(data.fileUrl.lastIndexOf("/"));
            } catch (Exception e) {
            }
            String finalFileLocal = fileLocal;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (ChatContentDownload.downloadFileByUrl(AppDatas.Constants().getFileServerURL() + data.fileUrl, finalFileLocal, data.type)) {
                    }
                }
            }).start();

        }
    }

    private void loadFile() {
        if (null != data && !TextUtils.isEmpty(data.localFilePath) && new File(data.localFilePath).exists()) {
        } else {
            if ("文件上传失败".equals(data.fileUrl)) {
                return;
            }
            String fileLocal = "";
            try {
                fileLocal = fC_CHUAN_SHU + data.fileUrl.substring(data.fileUrl.lastIndexOf("/"));
            } catch (Exception e) {

            }
            String finalFileLocal = fileLocal;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (ChatContentDownload.downloadFileByUrl(AppDatas.Constants().getFileServerURL() + data.fileUrl, finalFileLocal, data.type)) {
                    }
                }
            }).start();

        }
    }

    private void loadAudio() {
        String fileLocal = "";
        try {
            fileLocal = fC_CHUAN_SHU + data.fileUrl.substring(data.fileUrl.lastIndexOf("/"));
        } catch (Exception e) {

        }
        final File ffLocal = new File(fileLocal);
        data.localFilePath = ffLocal.getAbsolutePath();
        if (null != data && !TextUtils.isEmpty(data.localFilePath) && new File(data.localFilePath).exists()) {
        } else {
            String finalFileLocal = fileLocal;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (ChatContentDownload.downloadFileByUrl(AppDatas.Constants().getFileServerURL() + data.fileUrl, finalFileLocal, data.type)) {
                    }
                }
            }).start();

        }
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
                        }

                        @Override
                        public void onFailure(HTTPResponse httpResponse) {
                            super.onFailure(httpResponse);
                            showToast("onFailure");
                        }
                    });
        }
    }

}
