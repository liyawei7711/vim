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
import huaiye.com.vim.dao.msgs.User;
import huaiye.com.vim.models.contacts.bean.ContactOrganizationBean;
import huaiye.com.vim.models.contacts.bean.DeptData;

public class ContactsDeptViewHolder extends LiteViewHolder {
    @BindView(R.id.tv_user_name)
    TextView tv_user_name;
    @BindView(R.id.tv_message)
    TextView tv_message;
    @BindView(R.id.view_divider)
    View view_divider;

    public ContactsDeptViewHolder(Context context, View view, View.OnClickListener ocl) {
        super(context, view, ocl);
        itemView.setOnClickListener(ocl);
        tv_message.setOnClickListener(ocl);
    }

    @Override
    public void bindData(Object holder, int position, Object data, int size, List datas, Object extr) {
        DeptData deptData = (DeptData) data;
        itemView.setTag(deptData);
        tv_message.setTag(deptData);

        itemView.setOnClickListener(ocl);
        tv_user_name.setText(TextUtils.isEmpty(deptData.strName) ? deptData.strDepName : deptData.strName);

        if(position == datas.size() - 1) {
            view_divider.setVisibility(View.GONE);
        } else {
            view_divider.setVisibility(View.VISIBLE);
        }
    }

}
