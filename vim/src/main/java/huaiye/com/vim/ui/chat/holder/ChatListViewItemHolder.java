package huaiye.com.vim.ui.chat.holder;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.huaiye.cmf.sdp.SdpMessageCmProcessIMRsp;
import com.huaiye.sdk.HYClient;
import com.huaiye.sdk.core.SdkCallback;

import java.util.List;

import butterknife.BindView;
import huaiye.com.vim.EncryptUtil;
import huaiye.com.vim.R;
import huaiye.com.vim.common.AppUtils;
import huaiye.com.vim.common.helper.ChatContactsGroupUserListHelper;
import huaiye.com.vim.common.recycle.LiteViewHolder;
import huaiye.com.vim.common.utils.ChatUtil;
import huaiye.com.vim.common.utils.WeiXinDateFormat;
import huaiye.com.vim.dao.AppDatas;
import huaiye.com.vim.dao.msgs.ChatMessageBase;
import huaiye.com.vim.dao.msgs.ContentBean;
import huaiye.com.vim.models.contacts.bean.ContactsGroupUserListBean;
import huaiye.com.vim.ui.home.FragmentMessages;

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
import static huaiye.com.vim.common.AppUtils.NOTIFICATION_TYPE_PERSON_PUSH;
import static huaiye.com.vim.common.AppUtils.nEncryptIMEnable;
import static huaiye.com.vim.ui.meet.adapter.ChatContentAdapter.CHAT_CONTENT_CUSTOM_NOTICE_ITEM;

/**
 * author: admin
 * date: 2018/05/28
 * version: 0
 * mail: secret
 * desc: ChatViewHolder
 */
public class ChatListViewItemHolder extends LiteViewHolder {
    @BindView(R.id.view_divider)
    View view_divider;
    @BindView(R.id.time)
    TextView time;

    @BindView(R.id.left_Image)
    ImageView left_Image;
    @BindView(R.id.item_name)
    TextView item_name;
    @BindView(R.id.item_content)
    TextView item_content;

    private RequestOptions requestFriendHeadOptions;

    public ChatListViewItemHolder(Context context, View view, View.OnClickListener ocl) {
        super(context, view, ocl);
        requestFriendHeadOptions = new RequestOptions();
        requestFriendHeadOptions.centerCrop()
                .dontAnimate()
                .format(DecodeFormat.PREFER_RGB_565)
                .placeholder(R.drawable.default_image_personal)
                .error(R.drawable.default_image_personal)
                .optionalTransform(new CircleCrop());

        itemView.setOnClickListener(ocl);
    }

    @Override
    public void bindData(Object holder, int position, Object data, int size, List datas, Object extr) {
        ChatMessageBase bean = (ChatMessageBase) data;
        itemView.setTag(bean);

        time.setVisibility(View.VISIBLE);
        time.setText(WeiXinDateFormat.getChatTime(bean.time));
        item_name.setText(bean.fromUserName);

        if (1 == bean.bFire && (AppUtils.MESSAGE_TYPE_TEXT == bean.type || AppUtils.MESSAGE_TYPE_IMG == bean.type || AppUtils.MESSAGE_TYPE_AUDIO_FILE == bean.type || AppUtils.MESSAGE_TYPE_VIDEO_FILE == bean.type)) {
            item_content.setText("[" + AppUtils.getString(R.string.yuehoujifen) + "]");
        } else if (bean.type == MESSAGE_TYPE_IMG) {
            item_content.setText("[" + AppUtils.getString(R.string.img) + "]");
        } else if (bean.type == MESSAGE_TYPE_FILE) {
            item_content.setText("[" + AppUtils.getString(R.string.notice_file) + "]");
        } else if (bean.type == MESSAGE_TYPE_TEXT) {
            showTextContent2(bean, item_content);
        } else if (bean.type == NOTIFICATION_TYPE_PERSON_PUSH || bean.type == NOTIFICATION_TYPE_DEVICE_PUSH) {
            if (bean.type == NOTIFICATION_TYPE_PERSON_PUSH) {
                item_content.setText("[" + AppUtils.getString(R.string.person_share) + "]");
            } else {
                item_content.setText("[" + AppUtils.getString(R.string.device_share) + "]");
            }
        } else if (bean.type == MESSAGE_TYPE_AUDIO_FILE) {
            item_content.setText("[" + AppUtils.getString(R.string.audio) + "]");
        } else if (bean.type == MESSAGE_TYPE_VIDEO_FILE) {
            item_content.setText("[" + AppUtils.getString(R.string.video) + "]");
        } else if (bean.type == MESSAGE_TYPE_SINGLE_CHAT_VOICE) {
            item_content.setText("[" + AppUtils.getString(R.string.chat_voice_content) + "]");
        } else if (bean.type == MESSAGE_TYPE_SINGLE_CHAT_VIDEO) {
            item_content.setText("[" + AppUtils.getString(R.string.chat_video_content) + "]");
        } else if (bean.type == MESSAGE_TYPE_ADDRESS) {
            item_content.setText("[" + AppUtils.getString(R.string.chat_address) + "]");
        } else if (bean.type == MESSAGE_TYPE_GROUP_MEET) {
            item_content.setText("[" + AppUtils.getString(R.string.chat_group_video) + "]");
        } else if (bean.type == MESSAGE_TYPE_JINJI) {
            item_content.setText("[" + AppUtils.getString(R.string.chat_jinji) + "]");
        } else if (bean.type == MESSAGE_TYPE_SHARE) {
            showTextContent2(bean, item_content);
        } else if (bean.type == CHAT_CONTENT_CUSTOM_NOTICE_ITEM) {
            item_content.setText(bean.msgTxt);
        } else {
            item_content.setText("");
        }

        if (position == datas.size() - 1) {
            view_divider.setVisibility(View.GONE);
        } else {
            view_divider.setVisibility(View.VISIBLE);
        }

        setHeadImage(left_Image, bean);
    }

    private void showTextContent2(ChatMessageBase bean, TextView textView) {
        if (!TextUtils.isEmpty(bean.msgTxt)) {
            if (bean.bEncrypt == 1 && !bean.isUnEncrypt) {
                if (HYClient.getSdkOptions().encrypt().isEncryptBind() && nEncryptIMEnable) {
                    EncryptUtil.localEncryptText(bean.msgTxt, false,
                            new SdkCallback<SdpMessageCmProcessIMRsp>() {
                                @Override
                                public void onSuccess(SdpMessageCmProcessIMRsp sessionRsp) {
                                    bean.isUnEncrypt = true;
                                    bean.mStrEncrypt = bean.msgTxt;
                                    ContentBean cb = ChatUtil.analysisChatContentJson(sessionRsp.m_lstData.get(0).strData);
                                    bean.msgID = cb.msgID;
                                    bean.msgTxt = cb.msgTxt;
                                    bean.fileUrl = cb.fileUrl;
                                    bean.bFire = cb.bFire;
                                    bean.fireTime = cb.fireTime;
                                    bean.fileSize = cb.fileSize;
                                    bean.nCallState = cb.nCallState;
                                    bean.nDuration = cb.nDuration;
                                    if (bean.type == MESSAGE_TYPE_SHARE) {
                                        textView.setText("[" + AppUtils.getString(R.string.chat_link) + "]" + bean.msgTxt);
                                    } else {
                                        textView.setText(bean.msgTxt + "");
                                    }
                                }

                                @Override
                                public void onError(ErrorInfo sessionRsp) {
                                    if (bean.type == MESSAGE_TYPE_SHARE) {
                                        textView.setText("[" + AppUtils.getString(R.string.chat_link) + "]信息已加密");
                                    } else {
                                        textView.setText("信息已加密");
                                    }
                                }
                            });
                } else {
                    if (bean.type == MESSAGE_TYPE_SHARE) {
                        textView.setText("[" + AppUtils.getString(R.string.chat_link) + "]信息已加密");
                    } else {
                        textView.setText("信息已加密");
                    }
                }
            } else {
                if (bean.type == MESSAGE_TYPE_SHARE) {
                    textView.setText("[" + AppUtils.getString(R.string.chat_link) + "]" + bean.msgTxt);
                } else {
                    textView.setText(bean.msgTxt + "");
                }
            }
        } else {
            if (bean.type == MESSAGE_TYPE_SHARE) {
                textView.setText("[" + AppUtils.getString(R.string.chat_link) + "]" + "新消息");
            } else {
                textView.setText("新消息");
            }
        }
    }

    private void setHeadImage(ImageView headPicView, ChatMessageBase bean) {
        Glide.with(context)
                .load(AppDatas.Constants().getAddressWithoutPort() + bean.headPic)
                .apply(requestFriendHeadOptions)
                .into(headPicView);
    }

}
