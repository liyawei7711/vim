package huaiye.com.vim.ui.chat.holder;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
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

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import huaiye.com.vim.EncryptUtil;
import huaiye.com.vim.R;
import huaiye.com.vim.common.AppUtils;
import huaiye.com.vim.common.helper.ChatContactsGroupUserListHelper;
import huaiye.com.vim.common.recycle.LiteBaseAdapter;
import huaiye.com.vim.common.recycle.LiteViewHolder;
import huaiye.com.vim.common.recycle.SafeLinearLayoutManager;
import huaiye.com.vim.common.utils.ChatUtil;
import huaiye.com.vim.common.utils.WeiXinDateFormat;
import huaiye.com.vim.dao.AppDatas;
import huaiye.com.vim.dao.auth.AppAuth;
import huaiye.com.vim.dao.msgs.ChatMessageBase;
import huaiye.com.vim.dao.msgs.ContentBean;
import huaiye.com.vim.dao.msgs.SearchMessageBean;
import huaiye.com.vim.dao.msgs.User;
import huaiye.com.vim.dao.msgs.VimMessageListBean;
import huaiye.com.vim.dao.msgs.VimMessageListMessages;
import huaiye.com.vim.models.ModelApis;
import huaiye.com.vim.models.ModelCallback;
import huaiye.com.vim.models.contacts.bean.ContactsBean;
import huaiye.com.vim.models.contacts.bean.ContactsGroupUserListBean;
import huaiye.com.vim.models.contacts.bean.CreateGroupContactData;
import huaiye.com.vim.models.contacts.bean.DeptData;
import huaiye.com.vim.ui.contacts.DeptChatUtils;
import huaiye.com.vim.ui.contacts.DeptDeepListActivity;
import huaiye.com.vim.ui.contacts.viewholder.DepeContactItemViewHolder;
import huaiye.com.vim.ui.home.FragmentContacts;
import huaiye.com.vim.ui.home.FragmentMessages;
import huaiye.com.vim.ui.home.SearchChatActivity;
import huaiye.com.vim.ui.meet.ChatGroupActivityNew;
import huaiye.com.vim.ui.meet.ChatSingleActivity;
import ttyy.com.jinnetwork.core.work.HTTPResponse;

import static huaiye.com.vim.common.AppBaseActivity.showToast;
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
public class ChatListViewNewHolder extends LiteViewHolder {
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
    @BindView(R.id.rv_data)
    RecyclerView rv_data;

    private RequestOptions requestFriendHeadOptions;
    private RequestOptions requestGroupHeadOptions;


    public ChatListViewNewHolder(Context context, View view, View.OnClickListener ocl) {
        super(context, view, ocl);
        requestFriendHeadOptions = new RequestOptions();
        requestFriendHeadOptions.centerCrop()
                .dontAnimate()
                .format(DecodeFormat.PREFER_RGB_565)
                .placeholder(R.drawable.default_image_personal)
                .error(R.drawable.default_image_personal)
                .optionalTransform(new CircleCrop());
        requestGroupHeadOptions = new RequestOptions();
        requestGroupHeadOptions.centerCrop()
                .dontAnimate()
                .format(DecodeFormat.PREFER_RGB_565)
                .placeholder(R.drawable.ic_group_chat)
                .error(R.drawable.ic_group_chat)
                .optionalTransform(new CircleCrop());

        itemView.setOnClickListener(ocl);
    }

    @Override
    public void bindData(Object holder, int position, Object data, int size, List datas, Object extr) {
        SearchMessageBean bean = (SearchMessageBean) data;
        itemView.setTag(bean);

        if (TextUtils.isEmpty(bean.listBean.sessionName)) {
            ContactsGroupUserListBean group = ChatContactsGroupUserListHelper.getInstance().getContactsGroupDetail(bean.listBean.groupID);
            if (group != null) {
                bean.listBean.sessionName = group.strGroupName;
            }
        }
        if (TextUtils.isEmpty(bean.listBean.sessionName)) {
            if (FragmentMessages.mapGroupName.get(bean.listBean.groupID) != null) {
                bean.listBean.sessionName = FragmentMessages.mapGroupName.get(bean.listBean.groupID);
            } else {
                bean.listBean.sessionName = "群组(0)";
            }
        }
        item_name.setText(bean.listBean.sessionName);

        if (bean.chatMessageBases.isEmpty()) {
            time.setVisibility(View.VISIBLE);
            time.setText(WeiXinDateFormat.getChatTime(bean.listBean.time));
            left_Image.setVisibility(View.VISIBLE);
            item_content.setVisibility(View.VISIBLE);

            if (1 == bean.listBean.bFire && (AppUtils.MESSAGE_TYPE_TEXT == bean.listBean.type || AppUtils.MESSAGE_TYPE_IMG == bean.listBean.type || AppUtils.MESSAGE_TYPE_AUDIO_FILE == bean.listBean.type || AppUtils.MESSAGE_TYPE_VIDEO_FILE == bean.listBean.type)) {
                item_content.setText("[" + AppUtils.getString(R.string.yuehoujifen) + "]");
            } else if (bean.listBean.type == MESSAGE_TYPE_IMG) {
                item_content.setText("[" + AppUtils.getString(R.string.img) + "]");
            } else if (bean.listBean.type == MESSAGE_TYPE_FILE) {
                item_content.setText("[" + AppUtils.getString(R.string.notice_file) + "]");
            } else if (bean.listBean.type == MESSAGE_TYPE_TEXT) {
                showTextContent2(bean.listBean, item_content);
            } else if (bean.listBean.type == NOTIFICATION_TYPE_PERSON_PUSH || bean.listBean.type == NOTIFICATION_TYPE_DEVICE_PUSH) {
                if (bean.listBean.type == NOTIFICATION_TYPE_PERSON_PUSH) {
                    item_content.setText("[" + AppUtils.getString(R.string.person_share) + "]");
                } else {
                    item_content.setText("[" + AppUtils.getString(R.string.device_share) + "]");
                }
            } else if (bean.listBean.type == MESSAGE_TYPE_AUDIO_FILE) {
                item_content.setText("[" + AppUtils.getString(R.string.audio) + "]");
            } else if (bean.listBean.type == MESSAGE_TYPE_VIDEO_FILE) {
                item_content.setText("[" + AppUtils.getString(R.string.video) + "]");
            } else if (bean.listBean.type == MESSAGE_TYPE_SINGLE_CHAT_VOICE) {
                item_content.setText("[" + AppUtils.getString(R.string.chat_voice_content) + "]");
            } else if (bean.listBean.type == MESSAGE_TYPE_SINGLE_CHAT_VIDEO) {
                item_content.setText("[" + AppUtils.getString(R.string.chat_video_content) + "]");
            } else if (bean.listBean.type == MESSAGE_TYPE_ADDRESS) {
                item_content.setText("[" + AppUtils.getString(R.string.chat_address) + "]");
            } else if (bean.listBean.type == MESSAGE_TYPE_GROUP_MEET) {
                item_content.setText("[" + AppUtils.getString(R.string.chat_group_video) + "]");
            } else if (bean.listBean.type == MESSAGE_TYPE_JINJI) {
                item_content.setText("[" + AppUtils.getString(R.string.chat_jinji) + "]");
            } else if (bean.listBean.type == MESSAGE_TYPE_SHARE) {
                showTextContent2(bean.listBean, item_content);
            } else if (bean.listBean.type == CHAT_CONTENT_CUSTOM_NOTICE_ITEM) {
                item_content.setText(bean.listBean.msgTxt);
            } else {
                item_content.setText("");
            }

        } else {
            item_content.setVisibility(View.GONE);
            time.setVisibility(View.GONE);
            left_Image.setVisibility(View.GONE);

            LiteBaseAdapter<ChatMessageBase> deptAdapter = new LiteBaseAdapter<>(context,
                    bean.chatMessageBases,
                    ChatListViewItemHolder.class,
                    R.layout.item_chat_list_item_view,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ChatMessageBase chatMessageBase = (ChatMessageBase) v.getTag();
                            dealAdapterItemClick(chatMessageBase);
                        }
                    }, "false");
            rv_data.setAdapter(deptAdapter);
            rv_data.setLayoutManager(new SafeLinearLayoutManager(context));
        }

        if (position == datas.size() - 1) {
            view_divider.setVisibility(View.GONE);
        } else {
            view_divider.setVisibility(View.VISIBLE);
        }

        setHeadImage(left_Image, bean.listBean);
    }

    private void showTextContent2(VimMessageListBean bean, TextView textView) {
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

    private void setHeadImage(ImageView headPicView, VimMessageListBean bean) {
        Glide.with(context)
                .load(AppDatas.Constants().getAddressWithoutPort() + bean.strHeadUrl)
                .apply((bean.groupType == 1 || bean.groupType == 2) ? requestGroupHeadOptions : requestFriendHeadOptions)
                .into(headPicView);
    }

    private void dealAdapterItemClick(ChatMessageBase bean) {
        bean.read = 1;
        VimMessageListMessages.get().isRead(bean.sessionID);
        adapter.notifyDataSetChanged();

        Intent intent;
        if (bean.groupType == 2) {
            requestUser(bean);
        } else if (bean.groupType == 1) {
            intent = new Intent(context, ChatGroupActivityNew.class);
            CreateGroupContactData contactsBean = new CreateGroupContactData();
            contactsBean.strGroupDomainCode = bean.groupDomainCode;
            contactsBean.strGroupID = bean.groupID;
            contactsBean.sessionName = bean.sessionName;
            intent.putExtra("mContactsBean", contactsBean);
            intent.putExtra("indexDatas", bean.index);
            context.startActivity(intent);
        } else {
            intent = new Intent(context, ChatSingleActivity.class);
            intent.putExtra("mOtherUserName", bean.sessionName);
            intent.putExtra("indexDatas", bean.index);
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
            intent.putExtra("mOtherUserDomainCode", nUser.getDomainCode());
            context.startActivity(intent);
        }
    }

    private void requestUser(ChatMessageBase bean) {
        ((SearchChatActivity)context).mZeusLoadView.loadingText("正在加载").setLoading();
        ModelApis.Contacts().requestContacts(bean.groupDomainCode, bean.groupID, new ModelCallback<ContactsBean>() {
            @Override
            public void onSuccess(final ContactsBean contactsBean) {
                if (null != contactsBean && null != contactsBean.userList && contactsBean.userList.size() > 0) {
                    Intent intent = new Intent(context, ChatGroupActivityNew.class);
                    CreateGroupContactData groupContactData = new CreateGroupContactData();
                    groupContactData.strGroupDomainCode = bean.groupDomainCode;
                    groupContactData.strGroupID = bean.groupID;
                    groupContactData.sessionName = bean.sessionName;
                    groupContactData.userList = contactsBean.userList;
                    intent.putExtra("mContactsBean", groupContactData);
                    intent.putExtra("indexDatas", bean.index);
                    context.startActivity(intent);
                    ((SearchChatActivity)context).mZeusLoadView.dismiss();
                } else {
                    ((SearchChatActivity)context).mZeusLoadView.dismiss();
                    showToast("获取部门联系人失败");
                }
            }

            @Override
            public void onFailure(HTTPResponse httpResponse) {
                super.onFailure(httpResponse);
                ((SearchChatActivity)context).mZeusLoadView.dismiss();
                showToast("获取部门联系人失败");
            }
        });
    }

}
