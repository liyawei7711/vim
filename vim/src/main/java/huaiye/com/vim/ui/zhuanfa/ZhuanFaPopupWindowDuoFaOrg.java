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
import com.huaiye.cmf.sdp.SdpMessageCmProcessIMRsp;
import com.huaiye.sdk.HYClient;
import com.huaiye.sdk.core.SdkCallback;
import com.huaiye.sdk.sdkabi._api.ApiSocial;
import com.huaiye.sdk.sdkabi._params.SdkParamsCenter;
import com.huaiye.sdk.sdpmsgs.social.CSendMsgToMuliteUserRsp;
import com.huaiye.sdk.sdpmsgs.social.SendUserBean;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;

import huaiye.com.vim.EncryptUtil;
import huaiye.com.vim.R;
import huaiye.com.vim.bus.CloseZhuanFa;
import huaiye.com.vim.bus.MessageEvent;
import huaiye.com.vim.common.AppBaseActivity;
import huaiye.com.vim.common.AppUtils;
import huaiye.com.vim.common.SP;
import huaiye.com.vim.common.downloadutils.ChatContentDownload;
import huaiye.com.vim.common.rx.RxUtils;
import huaiye.com.vim.common.utils.ChatUtil;
import huaiye.com.vim.common.utils.WeiXinDateFormat;
import huaiye.com.vim.dao.AppDatas;
import huaiye.com.vim.dao.auth.AppAuth;
import huaiye.com.vim.dao.msgs.ChatMessageBase;
import huaiye.com.vim.dao.msgs.ChatMessageBean;
import huaiye.com.vim.dao.msgs.ChatSingleMsgBean;
import huaiye.com.vim.dao.msgs.User;
import huaiye.com.vim.dao.msgs.VimMessageBean;

import static huaiye.com.vim.common.AppBaseActivity.showToast;
import static huaiye.com.vim.common.AppUtils.nEncryptIMEnable;

public class ZhuanFaPopupWindowDuoFaOrg extends PopupWindow {
    private Context mContext;
    ChatMessageBase data;

    String strUserID; //老对话的id
    String strUserDomainCode;//老对话的domain

    boolean isGroup;
    String strGroupID;
    String strGroupDomain;

    ArrayList<User> users;//转发的对象

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

    public ZhuanFaPopupWindowDuoFaOrg(Context context, String strUserID, String strUserDomainCode,
                                      boolean isGroup, String strGroupID, String strGroupDomain) {
        super(context);
        mContext = context;
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

                if(onClickListener != null) {
                    onClickListener.onClick(v);
                } else {
                    sendMessage();
                }
            }
        });
    }

    public void sendMessage() {
        for(User temp : users) {
            if(!temp.strUserID.equals(AppAuth.get().getUserID())) {
                sendMessage(temp);
            }
        }
    }

    public void setSendUser(ArrayList<User> users) {
        this.users = users;
    }

    private void sendMessage(User user) {
        if (AppUtils.MESSAGE_TYPE_TEXT == data.type) {
            sendTxtMsg(user);
        } else if (AppUtils.MESSAGE_TYPE_IMG == data.type) {
            sendImg(user);
        } else if (AppUtils.MESSAGE_TYPE_FILE == data.type) {
            sendFile(user);
        } else if (AppUtils.MESSAGE_TYPE_VIDEO_FILE == data.type) {
            sendVideo(user);
        } else if (AppUtils.MESSAGE_TYPE_AUDIO_FILE == data.type) {
            sendAudio(user);
        } else if (AppUtils.MESSAGE_TYPE_SHARE == data.type) {
            sendShareMsg(user);
        } else {
            sendTxtMsg(user);
        }

    }

    private void sendImg(User user) {
        String msgContent = ChatUtil.getChatContentJson(mContext, "", "",
                data.fileUrl, 0, data.fileSize,
                SP.getBoolean(getSessionId(user) + AppUtils.SP_CHAT_SETTING_YUEHOUJIFENG, false),
                10, 0, 0, 0,
                data.fileName);
        sendWetherEncrypt(msgContent, user);
    }

    private void sendVideo(User user) {
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
                data.nDuration, data.fileSize, SP.getBoolean(getSessionId(user) + AppUtils.SP_CHAT_SETTING_YUEHOUJIFENG, false),
                data.nDuration, 0, 0, 0, data.fileName);
        sendWetherEncrypt(msgContent, user);
    }

    private void sendFile(User user) {
        ((AppBaseActivity) mContext).mZeusLoadView.loadingText(AppUtils.getString(R.string.is_upload_ing)).setLoading();

        String msgContent = ChatUtil.getChatContentJson(mContext, "", "", data.fileUrl,
                0, data.fileSize, false, 0,
                0, 0, 0, data.fileName);
        sendWetherEncrypt(msgContent, user);
    }

    private void sendAudio(User user) {
        ((AppBaseActivity) mContext).mZeusLoadView.loadingText(AppUtils.getString(R.string.is_upload_ing)).setLoading();
        String fileLocal = "";
        try {
            fileLocal = fC_CHUAN_SHU + data.fileUrl.substring(data.fileUrl.lastIndexOf("/"));
        } catch (Exception e) {

        }
        if (null != data && !TextUtils.isEmpty(data.localFilePath) && new File(data.localFilePath).exists()) {
            fileLocal = data.localFilePath;
        }

        String msgContent = ChatUtil.getChatContentJson(mContext, "", "",
                data.fileUrl, data.nDuration, data.fileSize, SP.getBoolean(getSessionId(user) + AppUtils.SP_CHAT_SETTING_YUEHOUJIFENG, false),
                data.nDuration, 0, 0, 0, data.fileName);
        sendWetherEncrypt(msgContent, user);
    }

    private void sendTxtMsg(User user) {

        String msgContent = ChatUtil.getChatContentJson(mContext, data.msgTxt, "", "", 0, 0,
                SP.getBoolean(getSessionId(user) + AppUtils.SP_CHAT_SETTING_YUEHOUJIFENG, false),
                data.msgTxt.length(), 0, 0, 0, "");
        sendWetherEncrypt(msgContent, user);

    }

    private void sendShareMsg(User user) {
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

        sendWetherEncryptShare(msgContent, user);
    }

    private void sendWetherEncrypt(String msgContent, User user) {
        ChatMessageBean bean = new ChatMessageBean();
        bean.content = msgContent;
        bean.type = data.type;
        bean.sessionID = getSessionId(user);
        bean.sessionName = getSessionName(user);
        bean.fromUserDomain = AppDatas.Auth().getDomainCode();
        bean.fromUserId = AppDatas.Auth().getUserID() + "";
        bean.fromUserName = AppDatas.Auth().getUserName();
        bean.groupType = 0;
        bean.bEncrypt = 0;
        bean.groupDomainCode = user.getDomainCode();
        bean.groupID = user.strUserID;
        bean.time = System.currentTimeMillis() / 1000;

        SendUserBean mySelf = new SendUserBean(AppAuth.get().getUserID() + "", AppAuth.get().getDomainCode(), AppAuth.get().getUserName());
        SendUserBean otherUser = new SendUserBean(user.strUserID, user.getDomainCode(), user.strUserName);

        bean.sessionUserList = new ArrayList<>();
        bean.sessionUserList.add(mySelf);
        bean.sessionUserList.add(otherUser);

        ArrayList<SendUserBean> sessionUserList = new ArrayList<>();
        sessionUserList.add(new SendUserBean(otherUser.strUserID, otherUser.strUserDomainCode, otherUser.strUserName));
        Gson gson = new Gson();
        HYClient.getModule(ApiSocial.class).sendMessage(SdkParamsCenter.Social.SendMuliteMessage()
                        .setIsImportant(true)
                        .setMessage(gson.toJson(bean))
                        .setUser(sessionUserList), new SdkCallback<CSendMsgToMuliteUserRsp>() {
                    @Override
                    public void onSuccess(CSendMsgToMuliteUserRsp cSendMsgToMuliteUserRsp) {
                        dealSaveMessageAndLoad(bean, otherUser);
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

    private void sendWetherEncryptShare(String msgContent, User user) {
        ChatMessageBean bean = new ChatMessageBean();
        bean.content = msgContent;
        bean.type = AppUtils.MESSAGE_TYPE_SHARE;
        bean.sessionID = getSessionId(user);
        bean.sessionName = getSessionName(user);
        bean.fromUserDomain = AppDatas.Auth().getDomainCode();
        bean.fromUserId = AppDatas.Auth().getUserID() + "";
        bean.fromUserName = AppDatas.Auth().getUserName();
        bean.groupType = 0;
        bean.bEncrypt = 0;
        bean.groupDomainCode = user.getDomainCode();
        bean.groupID = user.strUserID;
        bean.time = System.currentTimeMillis() / 1000;

        SendUserBean mySelf = new SendUserBean(AppAuth.get().getUserID() + "", AppAuth.get().getDomainCode(), AppAuth.get().getUserName());
        SendUserBean otherUser = new SendUserBean(user.strUserID, user.getDomainCode(), user.strUserName);

        bean.sessionUserList = new ArrayList<>();
        bean.sessionUserList.add(mySelf);
        bean.sessionUserList.add(otherUser);

        ArrayList<SendUserBean> sessionUserList = new ArrayList<>();
//        sessionUserList.add(new SendUserBean(mySelf.strUserID, mySelf.strDomainCode, mySelf.strUserName));
        sessionUserList.add(new SendUserBean(otherUser.strUserID, otherUser.strUserDomainCode, otherUser.strUserName));
        Gson gson = new Gson();
        HYClient.getModule(ApiSocial.class).sendMessage(SdkParamsCenter.Social.SendMuliteMessage()
                        .setIsImportant(true)
                        .setMessage(gson.toJson(bean))
                        .setUser(sessionUserList), new SdkCallback<CSendMsgToMuliteUserRsp>() {
                    @Override
                    public void onSuccess(CSendMsgToMuliteUserRsp cSendMsgToMuliteUserRsp) {
                        dealSaveMessageAndLoad(bean, otherUser);
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

    private void dealSaveMessageAndLoad(ChatMessageBean bean, SendUserBean otherUser) {

        ChatSingleMsgBean singleMsgBean = ChatSingleMsgBean.from(bean, otherUser);
        singleMsgBean.read = 1;
        AppDatas.MsgDB()
                .chatSingleMsgDao()
                .insertAll(singleMsgBean);
        VimMessageBean vimMessageBean = VimMessageBean.from(bean);
        vimMessageBean.sessionID = singleMsgBean.sessionID;
        huaiye.com.vim.dao.msgs.ChatUtil.get().saveChangeMsg(vimMessageBean, true);

        MessageEvent messageEvent = new MessageEvent(AppUtils.EVENT_COMING_NEW_MESSAGE);
        messageEvent.obj2 = singleMsgBean.sessionID;
        messageEvent.groupId = strGroupID;
        messageEvent.groupDomain = strGroupDomain;
        EventBus.getDefault().post(messageEvent);

        ((AppBaseActivity) mContext).mZeusLoadView.dismiss();
        showToast("转发成功");
        dismiss();

        EventBus.getDefault().post(new CloseZhuanFa());

    }

    public void showData(ChatMessageBase data) {
        if (tv_send != null) {
            tv_send.setEnabled(true);
        }
        this.data = data;

        User user = new User();
        for(User temp : users) {
            if(!temp.strUserID.equals(AppAuth.get().getUserID())) {
                user = temp;
                break;
            }
        }

        Glide.with(mContext)
                .load(AppDatas.Constants().getFileServerURL() + user.strHeadUrl)
                .apply(requestOptions)
                .into(iv_head);

        if (users.size() == 2) {
            tv_name.setText(user.strUserName);
        } else {
            tv_name.setText(user.strUserName + "等对象");
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
    public void showData(ChatMessageBase data, View.OnClickListener onClickListener, String header, String name, boolean isMore) {
        this.onClickListener = onClickListener;
        if (tv_send != null) {
            tv_send.setEnabled(true);
        }
        this.data = data;
        if(onClickListener == null) {
            return;
        }

        Glide.with(mContext)
                .load(AppDatas.Constants().getFileServerURL() + header)
                .apply(requestOptions)
                .into(iv_head);

        if (isMore) {
            tv_name.setText(name);
        } else {
            tv_name.setText(name + "等对象");
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

    private String getSessionId(User user) {
        if (null == user) {
            return null;
        }
        return user.getDomainCode() + user.strUserID;
    }

    private String getSessionName(User user) {
        return user.strUserName;
    }

    private void showImg() {
        Glide.with(mContext)
                .load(AppDatas.Constants().getFileServerURL() + data.fileUrl)
//                    .apply(requestOptions)
                .into(iv_content);
    }

    private void loadVideo() {
        if (null != data && !TextUtils.isEmpty(data.localFilePath) && new File(data.localFilePath).exists()) {
            go2PlayVideo2(data.localFilePath, data.bEncrypt);
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
                        new RxUtils().doDelay(100, new RxUtils.IMainDelay() {
                            @Override
                            public void onMainDelay() {
                                go2PlayVideo2(finalFileLocal, data.bEncrypt);
                            }
                        }, "loadVideo");
                    }
                }
            }).start();

        }
    }

    private void go2PlayVideo2(String localFilePath, int encrypt) {
        if (encrypt == 1) {
            if (HYClient.getSdkOptions().encrypt().isEncryptBind() && nEncryptIMEnable) {
                File file = new File(EncryptUtil.getNewFileLocal(localFilePath, fC_BEANDI));
                File fileun = new File(EncryptUtil.getNewFileMingWen(file.getAbsolutePath(), fC_MINGWEN));
                if (file.exists()) {
                    if (fileun.exists()) {
                    } else {
                        EncryptUtil.localEncryptFile(localFilePath, fileun.getAbsolutePath(), false,
                                new SdkCallback<SdpMessageCmProcessIMRsp>() {
                                    @Override
                                    public void onSuccess(SdpMessageCmProcessIMRsp resp) {
                                    }

                                    @Override
                                    public void onError(ErrorInfo sessionRsp) {
                                        showToast("文件解密失败");
                                    }
                                }
                        );
                    }
                } else {

                    EncryptUtil.converEncryptFile(localFilePath, file.getAbsolutePath(),
                            false, isGroup ? strGroupID : "", isGroup ? strGroupDomain : "",
                            isGroup ? "" : strUserID, isGroup ? "" : strUserDomainCode, new SdkCallback<SdpMessageCmProcessIMRsp>() {
                                @Override
                                public void onSuccess(SdpMessageCmProcessIMRsp resp) {
                                    if (fileun.exists()) {
                                    } else {
                                        EncryptUtil.localEncryptFile(resp.m_strData, fileun.getAbsolutePath(), false,
                                                new SdkCallback<SdpMessageCmProcessIMRsp>() {
                                                    @Override
                                                    public void onSuccess(SdpMessageCmProcessIMRsp resp) {
                                                    }

                                                    @Override
                                                    public void onError(ErrorInfo sessionRsp) {
                                                        showToast("文件解密失败");
                                                    }
                                                }
                                        );
                                    }
                                }

                                @Override
                                public void onError(ErrorInfo sessionRsp) {
                                    showToast("文件解密失败");
                                }
                            }
                    );
                }
            }
        }
    }

    private void loadFile() {
        if (null != data && !TextUtils.isEmpty(data.localFilePath) && new File(data.localFilePath).exists()) {
            openFile2(data.localFilePath, data.bEncrypt, data.fileName);
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
                        new RxUtils().doDelay(100, new RxUtils.IMainDelay() {
                            @Override
                            public void onMainDelay() {
                                openFile2(finalFileLocal, data.bEncrypt, data.fileName);
                            }
                        }, "loadFile");
                    }
                }
            }).start();

        }
    }

    private void openFile2(String localFilePath, int encrypt, String name) {
        if (encrypt == 1) {
            if (HYClient.getSdkOptions().encrypt().isEncryptBind() && nEncryptIMEnable) {
                File file = new File(EncryptUtil.getNewFileLocal(localFilePath, fC_BEANDI));
                File fileun = new File(EncryptUtil.getNewFileMingWen(file.getAbsolutePath(), fC_MINGWEN));
                if (file.exists()) {
                    if (fileun.exists()) {
                    } else {
                        EncryptUtil.localEncryptFile(file.getAbsolutePath(), fileun.getAbsolutePath(), false,
                                new SdkCallback<SdpMessageCmProcessIMRsp>() {
                                    @Override
                                    public void onSuccess(SdpMessageCmProcessIMRsp resp) {
                                    }

                                    @Override
                                    public void onError(ErrorInfo sessionRsp) {
                                        showToast("文件解密失败");
                                    }
                                }
                        );
                    }
                } else {
                    EncryptUtil.converEncryptFile(localFilePath, file.getAbsolutePath(),
                            false, isGroup ? strGroupID : "", isGroup ? strGroupDomain : "",
                            isGroup ? "" : strUserID, isGroup ? "" : strUserDomainCode,
                            new SdkCallback<SdpMessageCmProcessIMRsp>() {
                                @Override
                                public void onSuccess(SdpMessageCmProcessIMRsp resp) {
                                    if (fileun.exists()) {
                                    } else {
                                        EncryptUtil.localEncryptFile(resp.m_strData, fileun.getAbsolutePath(), false,
                                                new SdkCallback<SdpMessageCmProcessIMRsp>() {
                                                    @Override
                                                    public void onSuccess(SdpMessageCmProcessIMRsp resp) {
                                                    }

                                                    @Override
                                                    public void onError(ErrorInfo sessionRsp) {
                                                        showToast("文件解密失败");
                                                    }
                                                }
                                        );
                                    }
                                }

                                @Override
                                public void onError(ErrorInfo sessionRsp) {
                                    showToast("文件解密失败");
                                }
                            }
                    );
                }
            } else {
                showToast("文件解密失败");
            }
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

}
