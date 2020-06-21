package huaiye.com.vim.ui.contacts.viewholder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import huaiye.com.vim.R;
import huaiye.com.vim.common.recycle.LiteViewHolder;
import huaiye.com.vim.models.contacts.bean.DeptData;

/**
 * Created by ywt on 2019/2/25.
 */

public class DepeContactItemViewHolder extends LiteViewHolder {

    @BindView(R.id.tv_name)
    TextView tv_name;
    @BindView(R.id.tv_message)
    TextView tv_message;
    @BindView(R.id.tv_num)
    TextView tv_num;

    public DepeContactItemViewHolder(Context context, View view, View.OnClickListener ocl) {
        super(context, view, ocl);
        itemView.setOnClickListener(ocl);
        tv_message.setOnClickListener(ocl);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void bindData(Object holder, int position, Object data, int size, List datas, Object extr) {
        DeptData deptData = (DeptData) data;
        itemView.setTag(deptData);
        tv_message.setTag(deptData);

        tv_name.setText(TextUtils.isEmpty(deptData.strName) ? deptData.strDepName : deptData.strName);
        tv_num.setText("");

    }

}
