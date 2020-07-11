package huaiye.com.vim.ui.contacts.viewholder;

import android.annotation.SuppressLint;
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
import huaiye.com.vim.ui.home.FragmentContacts;

/**
 * Created by ywt on 2019/2/25.
 */

public class DepeItemViewOrgHolder extends LiteViewHolder {

    @BindView(R.id.iv_choice)
    ImageView iv_choice;
    @BindView(R.id.tv_name)
    TextView tv_name;
    @BindView(R.id.tv_num)
    TextView tv_num;
    @BindView(R.id.tv_next)
    TextView tv_next;

    public static boolean mIsChoice;

    public DepeItemViewOrgHolder(Context context, View view, View.OnClickListener ocl) {
        super(context, view, ocl);
        itemView.setOnClickListener(ocl);
        tv_next.setOnClickListener(ocl);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void bindData(Object holder, int position, Object data, int size, List datas, Object extr) {
        DeptData deptData = (DeptData) data;
        itemView.setTag(deptData);
        tv_next.setTag(deptData);

        tv_name.setText(TextUtils.isEmpty(deptData.strName) ? deptData.strDepName : deptData.strName);

        tv_num.setText(" ");

        if (mIsChoice) {
            iv_choice.setVisibility(View.VISIBLE);
            if (deptData.isSelected) {
                iv_choice.setImageResource(R.drawable.ic_choice_checked);
            } else {
                iv_choice.setImageResource(R.drawable.ic_choice);
            }
        }

    }

}
