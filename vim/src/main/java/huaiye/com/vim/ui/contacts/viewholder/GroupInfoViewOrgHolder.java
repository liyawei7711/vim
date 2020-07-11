package huaiye.com.vim.ui.contacts.viewholder;

import android.annotation.SuppressLint;
import android.content.Context;
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
import huaiye.com.vim.common.recycle.LiteViewHolder;
import huaiye.com.vim.dao.AppDatas;
import huaiye.com.vim.models.contacts.bean.GroupInfo;
import huaiye.com.vim.ui.home.FragmentMessages;

/**
 * Created by ywt on 2019/2/25.
 */

public class GroupInfoViewOrgHolder extends LiteViewHolder {

    @BindView(R.id.iv_choice)
    ImageView iv_choice;
    @BindView(R.id.tv_next)
    View tv_next;
    @BindView(R.id.iv_user_head)
    ImageView iv_user_head;
    @BindView(R.id.tv_user_name)
    TextView tv_user_name;
    @BindView(R.id.view_divider)
    View view_divider;

    RequestOptions requestOptions;

    public static boolean mIsChoice;


    public GroupInfoViewOrgHolder(Context context, View view, View.OnClickListener ocl) {
        super(context, view, ocl);
        itemView.setOnClickListener(ocl);
        tv_next.setOnClickListener(ocl);

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
        tv_next.setTag(groupInfo);

        Glide.with(context)
                .load(AppDatas.Constants().getAddressWithoutPort() + groupInfo.strHeadUrl)
                .apply(requestOptions)
                .into(iv_user_head);

        if (TextUtils.isEmpty(groupInfo.strGroupName)) {
            if (FragmentMessages.mapGroupName.get(groupInfo.strGroupID) != null) {
                groupInfo.strGroupName = FragmentMessages.mapGroupName.get(groupInfo.strGroupID);
            } else {
                groupInfo.strGroupName = "群组(0)";
            }
        }
        tv_user_name.setText(groupInfo.strGroupName);

        if (position == datas.size() - 1) {
            view_divider.setVisibility(View.GONE);
        } else {
            view_divider.setVisibility(View.VISIBLE);
        }

        if (mIsChoice) {
            iv_choice.setVisibility(View.VISIBLE);
            if (groupInfo.isSelected) {
                iv_choice.setImageResource(R.drawable.ic_choice_checked);
            } else {
                iv_choice.setImageResource(R.drawable.ic_choice);
            }
        }

    }

}