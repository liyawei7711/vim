package huaiye.com.vim.ui.contacts.viewholder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import butterknife.BindView;
import huaiye.com.vim.R;
import huaiye.com.vim.common.helper.ChatContactsGroupUserListHelper;
import huaiye.com.vim.common.recycle.LiteViewHolder;
import huaiye.com.vim.common.views.CheckableLinearLayout;
import huaiye.com.vim.dao.AppDatas;
import huaiye.com.vim.models.ModelApis;
import huaiye.com.vim.models.ModelCallback;
import huaiye.com.vim.models.contacts.bean.ContactOrganizationBean;
import huaiye.com.vim.models.contacts.bean.ContactsGroupUserListBean;
import huaiye.com.vim.models.contacts.bean.GroupInfo;
import ttyy.com.jinnetwork.core.work.HTTPResponse;

/**
 * Created by ywt on 2019/2/25.
 */

public class GroupInfoViewHolder extends LiteViewHolder {

    @BindView(R.id.tv_checklayout)
    CheckableLinearLayout tv_checklayout;
    @BindView(R.id.iv_choice)
    ImageView iv_choice;
    @BindView(R.id.iv_user_head)
    ImageView iv_user_head;
    @BindView(R.id.tv_user_name)
    TextView tv_user_name;
    @BindView(R.id.tv_user_master)
    TextView tv_user_master;
    @BindView(R.id.tv_choose_added)
    TextView tv_choose_added;
    @BindView(R.id.view_divider)
    View view_divider;

    RequestOptions requestOptions;

    public GroupInfoViewHolder(Context context, View view, View.OnClickListener ocl) {
        super(context, view, ocl);
        itemView.setOnClickListener(ocl);

        requestOptions = new RequestOptions();
        requestOptions.centerCrop()
                .dontAnimate()
                .format(DecodeFormat.PREFER_RGB_565)
                .placeholder(R.drawable.ic_group_chat)
                .error(R.drawable.ic_group_chat)
                .optionalTransform(new CircleCrop());
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void bindData(Object holder, int position, Object data, int size, List datas, Object extr) {
        GroupInfo groupInfo = (GroupInfo) data;
        itemView.setTag(groupInfo);

        Glide.with(context)
                .load(AppDatas.Constants().getAddressWithoutPort() + groupInfo.strHeadUrl)
                .apply(requestOptions)
                .into(iv_user_head);

        if (TextUtils.isEmpty(groupInfo.strGroupName)) {
            ModelApis.Contacts().requestqueryGroupChatInfo(groupInfo.strGroupDomainCode, groupInfo.strGroupID,
                    new ModelCallback<ContactsGroupUserListBean>() {
                        @Override
                        public void onSuccess(final ContactsGroupUserListBean contactsBean) {
                            if(contactsBean != null) {
                                ChatContactsGroupUserListHelper.getInstance().cacheContactsGroupDetail(groupInfo.strGroupID + "", contactsBean);
                            }
                            if (null != contactsBean && null != contactsBean.lstGroupUser && contactsBean.lstGroupUser.size() > 0) {
                                StringBuilder sb = new StringBuilder("");
                                for (ContactsGroupUserListBean.LstGroupUser temp : contactsBean.lstGroupUser) {
                                    sb.append(temp.strUserName + "、");
                                }
                                if (null != sb && sb.indexOf("、") >= 0) {
                                    sb.deleteCharAt(sb.lastIndexOf("、"));
                                }
                                tv_user_name.setText(sb);
                                groupInfo.strGroupName = sb.toString();
                            }
                        }

                        @Override
                        public void onFailure(HTTPResponse httpResponse) {
                            super.onFailure(httpResponse);
                        }
                    });
        } else {
            tv_user_name.setText(groupInfo.strGroupName);
        }

        if (position == datas.size() - 1) {
            view_divider.setVisibility(View.GONE);
        } else {
            view_divider.setVisibility(View.VISIBLE);
        }

    }

}
