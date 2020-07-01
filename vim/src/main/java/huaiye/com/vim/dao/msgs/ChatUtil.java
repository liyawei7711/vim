package huaiye.com.vim.dao.msgs;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.huaiye.sdk.HYClient;
import com.huaiye.sdk.core.SdkCallback;
import com.huaiye.sdk.sdkabi._api.ApiSocial;
import com.huaiye.sdk.sdkabi._params.SdkParamsCenter;
import com.huaiye.sdk.sdpmsgs.social.CSendMsgToMuliteUserRsp;
import com.huaiye.sdk.sdpmsgs.social.SendUserBean;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import huaiye.com.vim.R;
import huaiye.com.vim.bus.NewMessage;
import huaiye.com.vim.common.AppUtils;
import huaiye.com.vim.common.SP;
import huaiye.com.vim.common.ScreenNotify;
import huaiye.com.vim.common.helper.ChatContactsGroupUserListHelper;
import huaiye.com.vim.dao.AppDatas;
import huaiye.com.vim.dao.auth.AppAuth;
import huaiye.com.vim.models.ModelApis;
import huaiye.com.vim.models.ModelCallback;
import huaiye.com.vim.models.contacts.bean.ContactsGroupUserListBean;
import ttyy.com.jinnetwork.core.work.HTTPResponse;

import static huaiye.com.vim.common.AppUtils.MESSAGE_TYPE_ADDRESS;
import static huaiye.com.vim.common.AppUtils.MESSAGE_TYPE_AUDIO_FILE;
import static huaiye.com.vim.common.AppUtils.MESSAGE_TYPE_FILE;
import static huaiye.com.vim.common.AppUtils.MESSAGE_TYPE_GROUP_MEET;
import static huaiye.com.vim.common.AppUtils.MESSAGE_TYPE_IMG;
import static huaiye.com.vim.common.AppUtils.MESSAGE_TYPE_JINJI;
import static huaiye.com.vim.common.AppUtils.MESSAGE_TYPE_SHARE;
import static huaiye.com.vim.common.AppUtils.MESSAGE_TYPE_SINGLE_CHAT_VIDEO;
import static huaiye.com.vim.common.AppUtils.MESSAGE_TYPE_SINGLE_CHAT_VOICE;
import static huaiye.com.vim.common.AppUtils.MESSAGE_TYPE_TEXT;
import static huaiye.com.vim.common.AppUtils.MESSAGE_TYPE_VIDEO_FILE;
import static huaiye.com.vim.common.AppUtils.NOTIFICATION_TYPE_DEVICE_PUSH;
import static huaiye.com.vim.common.AppUtils.NOTIFICATION_TYPE_GUANMO;
import static huaiye.com.vim.common.AppUtils.NOTIFICATION_TYPE_PERSON_PUSH;
import static huaiye.com.vim.ui.meet.adapter.ChatContentAdapter.CHAT_CONTENT_CUSTOM_NOTICE_ITEM;

/**
 * author: admin
 * date: 2018/05/30
 * version: 0
 * mail: secret
 * desc: ChatUtil
 */

public class ChatUtil {


    Gson gson;

    private ChatUtil() {
        if (gson == null) {
            gson = new Gson();
        }
    }

    static class Holder {
        static final ChatUtil SINGLETON = new ChatUtil();
    }

    public static ChatUtil get() {
        return ChatUtil.Holder.SINGLETON;
    }

    /**
     * 接收到的消息
     *
     * @param bean
     * @param needSend
     */
    public void saveChangeMsg(VimMessageBean bean, boolean needSend) {
        if (gson == null) {
            gson = new Gson();
        }
        //一对一聊天的标题要特殊处理
        //标题换为:对方的名字+"的指令调度"
        if (bean.sessionUserList != null && bean.sessionUserList.size() == 2 && bean.groupType == 0) {
            String myUserID = AppAuth.get().getUserID() + "";
            for (SendUserBean item : bean.sessionUserList) {
                if (!item.strUserID.equals(myUserID)) {
                    bean.sessionName = item.strUserName;
                }
            }
        }

        if (bean.type == NOTIFICATION_TYPE_PERSON_PUSH) {
            String str = String.format(AppUtils.getString(R.string.person_share_from), bean.fromUserName);
            bean.sessionName = str;
        }

        if (bean.type == NOTIFICATION_TYPE_DEVICE_PUSH) {
            String str = String.format(AppUtils.getString(R.string.person_share_device_from), bean.fromUserName);
            bean.sessionName = str;
        }
        bean.nMsgTop = SP.getInteger(bean.sessionID + AppUtils.SP_SETTING_MSG_TOP, 0);
        bean.isSend = true;
        bean.nNoDisturb = SP.getInteger(bean.sessionID + AppUtils.SP_SETTING_NODISTURB, 0);
        long msgToptime = SP.getLong(bean.sessionID + AppUtils.SP_SETTING_MSG_TOP_TIME, 0l);
        if (msgToptime > 0) {
            bean.nMsgToptime = msgToptime;
        }

        List<VimMessageListBean> allBean = VimMessageListMessages.get().getMessages();
        if (allBean != null && allBean.size() > 0) {
            boolean has = false;
            for (VimMessageListBean temp : allBean) {
                if (temp.sessionID.equals(bean.sessionID)) {
                    bean.nMsgTop = temp.nMsgTop;
                    bean.nMsgToptime = temp.nMsgToptime;
                    bean.nNoDisturb = temp.nNoDisturb;
                    has = true;
                    break;
                }
            }
            if (has) {
                VimMessageListBean VimMessageListBean = getVimMessageListBean(bean);
                VimMessageListMessages.get().del(VimMessageListBean.sessionID);
                VimMessageListMessages.get().add(VimMessageListBean);
            } else {
                VimMessageListBean VimMessageListBean = getVimMessageListBean(bean);
                VimMessageListMessages.get().add(VimMessageListBean);
            }
        } else {
            VimMessageListBean VimMessageListBean = getVimMessageListBean(bean);
            VimMessageListMessages.get().add(VimMessageListBean);
        }

        if (needSend && bean.sessionID != null)
            EventBus.getDefault().post(bean);

        EventBus.getDefault().post(new NewMessage(VimMessageListMessages.get().getMessagesUnRead()));
        VimMessageListMessages.get().getMessagesUnReadNum();

        if (AppUtils.isHide) {
//            ScreenNotify.get().wakeUpAndUnlock();
//            ScreenNotify.get().openApplicationFromBackground();

            StringBuilder str = new StringBuilder();
            if (1 == bean.bFire && (AppUtils.MESSAGE_TYPE_TEXT == bean.type || AppUtils.MESSAGE_TYPE_IMG == bean.type || AppUtils.MESSAGE_TYPE_AUDIO_FILE == bean.type || AppUtils.MESSAGE_TYPE_VIDEO_FILE == bean.type)) {
                str.append("[" + AppUtils.getString(R.string.yuehoujifen) + "]");
            } else if (bean.type == MESSAGE_TYPE_IMG) {
                str.append("[" + AppUtils.getString(R.string.img) + "]");
            } else if (bean.type == MESSAGE_TYPE_FILE) {
                str.append("[" + AppUtils.getString(R.string.notice_file) + "]");
            } else if (bean.type == MESSAGE_TYPE_TEXT) {
                str.append(bean.msgTxt + "");
            } else if (bean.type == NOTIFICATION_TYPE_PERSON_PUSH || bean.type == NOTIFICATION_TYPE_DEVICE_PUSH) {
                if (bean.type == NOTIFICATION_TYPE_PERSON_PUSH) {
                    str.append("[" + AppUtils.getString(R.string.person_share) + "]");
                } else {
                    str.append("[" + AppUtils.getString(R.string.device_share) + "]");
                }
            } else if (bean.type == MESSAGE_TYPE_AUDIO_FILE) {
                str.append("[" + AppUtils.getString(R.string.audio) + "]");
            } else if (bean.type == MESSAGE_TYPE_VIDEO_FILE) {
                str.append("[" + AppUtils.getString(R.string.video) + "]");
            } else if (bean.type == MESSAGE_TYPE_SINGLE_CHAT_VOICE) {
                str.append("[" + AppUtils.getString(R.string.chat_voice_content) + "]");
            } else if (bean.type == MESSAGE_TYPE_SINGLE_CHAT_VIDEO) {
                str.append("[" + AppUtils.getString(R.string.chat_video_content) + "]");
            } else if (bean.type == MESSAGE_TYPE_ADDRESS) {
                str.append("[" + AppUtils.getString(R.string.chat_address) + "]");
            } else if (bean.type == MESSAGE_TYPE_GROUP_MEET) {
                str.append("[" + AppUtils.getString(R.string.chat_group_video) + "]");
            } else if (bean.type == MESSAGE_TYPE_JINJI) {
                str.append("[" + AppUtils.getString(R.string.chat_jinji) + "]");
            } else if (bean.type == MESSAGE_TYPE_SHARE) {
                str.append("[" + AppUtils.getString(R.string.chat_link) + "]" + "新消息");
            } else if (bean.type == CHAT_CONTENT_CUSTOM_NOTICE_ITEM) {
                str.append(bean.msgTxt);
            }

            ScreenNotify.get().showScreenNotify(AppUtils.ctx, bean.sessionName, str.toString());
        }
    }

    public void saveChangeMsg(VimMessageBean bean) {
        VimMessageListBean VimMessageListBean = getVimMessageListBean(bean);
        VimMessageListBean.isRead = 1;
        VimMessageListMessages.get().del(VimMessageListBean.sessionID);
        VimMessageListMessages.get().add(VimMessageListBean);

        VimMessageListMessages.get().getMessagesUnReadNum();
    }

    /**
     * 自己发送的消息
     *
     * @param bean
     */
    public void saveMySendMsg(VimMessageBean bean) {
        List<VimMessageListBean> allBean = VimMessageListMessages.get().getMessages();
        if (allBean != null && allBean.size() > 0) {
            boolean has = false;
            for (VimMessageListBean temp : allBean) {
                if (temp.sessionID.equals(bean.sessionID)) {
                    has = true;
                    break;
                }
            }
            if (has) {
                VimMessageListBean VimMessageListBean = getVimMessageListBean(bean);
                VimMessageListMessages.get().del(VimMessageListBean.sessionID);
                VimMessageListMessages.get().add(VimMessageListBean);
            } else {
                VimMessageListBean VimMessageListBean = getVimMessageListBean(bean);
                VimMessageListMessages.get().add(VimMessageListBean);
            }
        } else {
            VimMessageListBean VimMessageListBean = getVimMessageListBean(bean);
            VimMessageListMessages.get().add(VimMessageListBean);
        }

    }

    @NonNull
    private VimMessageListBean getVimMessageListBean(VimMessageBean bean) {
        VimMessageListBean VimMessageListBean = new VimMessageListBean();
        VimMessageListBean.sessionID = bean.sessionID;
        VimMessageListBean.sessionName = bean.sessionName;
        VimMessageListBean.sessionUserList = bean.sessionUserList;
        VimMessageListBean.msgID = bean.msgID;
        VimMessageListBean.msgTxt = bean.msgTxt;
        VimMessageListBean.bEncrypt = bean.bEncrypt;
        VimMessageListBean.fileUrl = bean.fileUrl;
        VimMessageListBean.nDuration = bean.nDuration;
        VimMessageListBean.fileSize = bean.fileSize;
        VimMessageListBean.bFire = bean.bFire;
        VimMessageListBean.nCallState = bean.nCallState;
        VimMessageListBean.fireTime = bean.fireTime;

        VimMessageListBean.groupType = bean.groupType;
        VimMessageListBean.groupDomainCode = bean.groupDomainCode;
        VimMessageListBean.groupID = bean.groupID;
        VimMessageListBean.lastUserId = bean.fromUserId;
        VimMessageListBean.lastUserDomain = bean.fromUserDomain;
        VimMessageListBean.lastUserName = bean.fromUserName;
        VimMessageListBean.time = System.currentTimeMillis();
        VimMessageListBean.type = bean.type;
        if (TextUtils.isEmpty(bean.fromUserId)) {
            VimMessageListBean.isRead = 1;
        } else if (bean.fromUserId.equals(AppDatas.Auth().getUserID() + "")) {
            VimMessageListBean.isRead = 1;
        } else {
            VimMessageListBean.isRead = 0;
        }
        VimMessageListBean.nMsgTop = bean.nMsgTop;
        VimMessageListBean.nMsgToptime = bean.nMsgToptime;
        VimMessageListBean.nNoDisturb = bean.nNoDisturb;
        return VimMessageListBean;
    }

    public void rspGuanMo(String fromUserId, String fromUserDomain, String fromUserName, String sessionID) {
        com.huaiye.sdk.logger.Logger.debug("CaptureViewLayout sendPlayerMessage rspGuanMo");
        final VimMessageBean bean = new VimMessageBean();
        bean.type = NOTIFICATION_TYPE_GUANMO;
        bean.sessionID = sessionID;
        bean.sessionName = AppUtils.getString(R.string.player);
        bean.fromUserDomain = AppDatas.Auth().getDomainCode();
        bean.fromUserId = AppDatas.Auth().getUserID() + "";
        bean.fromUserTokenId = HYClient.getSdkOptions().User().getUserTokenId();
        bean.fromUserName = AppDatas.Auth().getUserName();

        bean.sessionUserList.add(new SendUserBean(fromUserId, fromUserDomain, fromUserName));

        bean.time = System.currentTimeMillis();

        HYClient.getModule(ApiSocial.class).sendMessage(SdkParamsCenter.Social.SendMuliteMessage()
                        .setIsImportant(false)
                        .setMessage(bean.toString())
                        .setUser(bean.sessionUserList),
                new SdkCallback<CSendMsgToMuliteUserRsp>() {
                    @Override
                    public void onSuccess(CSendMsgToMuliteUserRsp resp) {
                    }

                    @Override
                    public void onError(ErrorInfo errorInfo) {
                    }
                });
    }

    public void broadcastPushVideo(ArrayList<SendUserBean> sendUserBeans) {
        com.huaiye.sdk.logger.Logger.debug("CaptureViewLayout sendPlayerMessage pushVideo");
        final ChatMessageBean bean = new ChatMessageBean();
        bean.type = NOTIFICATION_TYPE_PERSON_PUSH;
        bean.content = getContent();
        bean.sessionID = AppDatas.Auth().getDomainCode() + AppDatas.Auth().getUserID();
        bean.sessionName = AppUtils.getString(R.string.player);
        bean.fromUserDomain = AppDatas.Auth().getDomainCode();
        bean.fromUserId = AppDatas.Auth().getUserID() + "";
        bean.fromUserTokenId = HYClient.getSdkOptions().User().getUserTokenId();
        bean.fromUserName = AppDatas.Auth().getUserName();

        bean.sessionUserList = sendUserBeans;

        bean.time = System.currentTimeMillis() / 1000;
        Gson gson = new Gson();

        HYClient.getModule(ApiSocial.class).sendMessage(SdkParamsCenter.Social.SendMuliteMessage()
                        .setIsImportant(false)
                        .setMessage(gson.toJson(bean))
                        .setUser(sendUserBeans),
                new SdkCallback<CSendMsgToMuliteUserRsp>() {
                    @Override
                    public void onSuccess(CSendMsgToMuliteUserRsp resp) {
                    }

                    @Override
                    public void onError(ErrorInfo errorInfo) {
                    }
                });
    }

    private String getContent() {
        JsonObject json = new JsonObject();
        json.addProperty("strName", AppDatas.Auth().getUserName());
        json.addProperty("strDomainCode", AppDatas.Auth().getDomainCode());
        json.addProperty("strUserTokenID", HYClient.getSdkOptions().User().getUserTokenId());

        return json.toString();
    }

}
