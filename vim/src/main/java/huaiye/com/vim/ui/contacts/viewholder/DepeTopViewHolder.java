package huaiye.com.vim.ui.contacts.viewholder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
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
import huaiye.com.vim.models.contacts.bean.ContactsGroupUserListBean;
import huaiye.com.vim.models.contacts.bean.GroupInfo;
import ttyy.com.jinnetwork.core.work.HTTPResponse;

/**
 * Created by ywt on 2019/2/25.
 */

public class DepeTopViewHolder extends LiteViewHolder {

    @BindView(R.id.tv_name)
    TextView tv_name;
    @BindView(R.id.iv_next)
    View iv_next;

    public DepeTopViewHolder(Context context, View view, View.OnClickListener ocl) {
        super(context, view, ocl);
        itemView.setOnClickListener(ocl);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void bindData(Object holder, int position, Object data, int size, List datas, Object extr) {
        String str = (String) data;
        itemView.setTag(str);

        tv_name.setText(str);

        if (position == datas.size() - 1) {
            iv_next.setVisibility(View.GONE);
            tv_name.setTextColor(Color.parseColor("#22a5ff"));
        } else {
            iv_next.setVisibility(View.VISIBLE);
            tv_name.setTextColor(Color.parseColor("#333333"));
        }
    }

}
