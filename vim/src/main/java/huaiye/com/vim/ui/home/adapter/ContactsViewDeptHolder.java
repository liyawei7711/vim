package huaiye.com.vim.ui.home.adapter;

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
import huaiye.com.vim.common.views.CheckableLinearLayout;
import huaiye.com.vim.dao.AppDatas;
import huaiye.com.vim.dao.msgs.ChangyongLianXiRenBean;
import huaiye.com.vim.models.contacts.bean.DeptData;

public class ContactsViewDeptHolder extends LiteViewHolder {
    @BindView(R.id.iv_user_head)
    ImageView iv_user_head;
    @BindView(R.id.tv_user_name)
    TextView tv_user_name;
    @BindView(R.id.tv_choose_added)
    TextView tv_choose_added;
    @BindView(R.id.view_divider)
    View view_divider;

    private RequestOptions requestOptions;


    public ContactsViewDeptHolder(Context context, View view, View.OnClickListener ocl) {
        super(context, view, ocl);
        requestOptions = new RequestOptions();
        requestOptions.centerCrop()
                .dontAnimate()
                .format(DecodeFormat.PREFER_RGB_565)
                .placeholder(R.drawable.default_image_personal)
                .error(R.drawable.default_image_personal)
                .optionalTransform(new CircleCrop());
        itemView.setOnClickListener(ocl);
    }

    @Override
    public void bindData(Object holder, int position, Object data, int size, List datas, Object extr) {
        ChangyongLianXiRenBean user = (ChangyongLianXiRenBean) data;
        itemView.setTag(user);

        Glide.with(context)
                .load(AppDatas.Constants().getAddressWithoutPort() + user.strHeadUrl)
                .apply(requestOptions)
                .into(iv_user_head);

        tv_user_name.setText(user.strUserName);
        StringBuilder sb = new StringBuilder();
        for (DeptData temp : user.getUserDept()) {
            sb.append(temp.strDepName + " ");
        }
        tv_choose_added.setText((TextUtils.isEmpty(user.strPostName) ? "" : (user.strPostName) + ",") + sb);
        itemView.setOnClickListener(ocl);

        if (position == datas.size() - 1) {
            view_divider.setVisibility(View.GONE);
        } else {
            view_divider.setVisibility(View.VISIBLE);
        }
    }

}
