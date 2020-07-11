package huaiye.com.vim.ui.home.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import huaiye.com.vim.R;
import huaiye.com.vim.common.recycle.LiteViewHolder;
import huaiye.com.vim.models.contacts.bean.DeptData;

public class ContactsDeptViewOrgHolder extends LiteViewHolder {
    @BindView(R.id.iv_choice)
    ImageView iv_choice;
    @BindView(R.id.tv_user_name)
    TextView tv_user_name;
    @BindView(R.id.tv_next)
    TextView tv_next;
    @BindView(R.id.view_divider)
    View view_divider;
    public static boolean mIsChoice;

    public ContactsDeptViewOrgHolder(Context context, View view, View.OnClickListener ocl) {
        super(context, view, ocl);
        itemView.setOnClickListener(ocl);
        tv_next.setOnClickListener(ocl);
    }

    @Override
    public void bindData(Object holder, int position, Object data, int size, List datas, Object extr) {
        DeptData deptData = (DeptData) data;
        itemView.setTag(deptData);
        tv_next.setTag(deptData);

        itemView.setOnClickListener(ocl);
        tv_user_name.setText(TextUtils.isEmpty(deptData.strName) ? deptData.strDepName : deptData.strName);

        if (mIsChoice) {
            iv_choice.setVisibility(View.VISIBLE);
            if (deptData.isSelected) {
                iv_choice.setImageResource(R.drawable.ic_choice_checked);
            } else {
                iv_choice.setImageResource(R.drawable.ic_choice);
            }
        }

        if(position == datas.size() - 1) {
            view_divider.setVisibility(View.GONE);
        } else {
            view_divider.setVisibility(View.VISIBLE);
        }
    }

}
