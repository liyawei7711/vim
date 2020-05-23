package huaiye.com.vim.ui.meet.viewholder;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import huaiye.com.vim.R;
import huaiye.com.vim.common.recycle.LiteViewHolder;
import huaiye.com.vim.models.meet.bean.FileBean;

/**
 * Created by Administrator on 2018\2\27 0027.
 */

public class FileTransHolder extends LiteViewHolder {

    @BindView(R.id.form_pc)
    View form_pc;
    @BindView(R.id.iv_type_pc)
    ImageView iv_type_pc;
    @BindView(R.id.tv_name_pc)
    TextView tv_name_pc;
    @BindView(R.id.cb_status_pc)
    CheckBox cb_status_pc;
    @BindView(R.id.cb_view_pc)
    View cb_view_pc;

    @BindView(R.id.form_local)
    View form_local;
    @BindView(R.id.iv_type_local)
    ImageView iv_type_local;
    @BindView(R.id.tv_name_local)
    TextView tv_name_local;
    @BindView(R.id.cb_status_local)
    CheckBox cb_status_local;
    @BindView(R.id.cb_view_local)
    View cb_view_local;

    public FileTransHolder(Context context, View view, View.OnClickListener ocl) {
        super(context, view, ocl);

        itemView.setOnClickListener(ocl);
        cb_view_pc.setOnClickListener(ocl);
        cb_view_local.setOnClickListener(ocl);
    }

    @Override
    public void bindData(Object holder, int position, Object data, int size, List datas, Object extr) {
        FileBean bean = (FileBean) data;
        itemView.setTag(bean);
        cb_view_pc.setTag(bean);
        cb_view_local.setTag(bean);

        iv_type_pc.setImageResource(R.drawable.icon_wendang);
        iv_type_local.setImageResource(R.drawable.icon_wendang);

        cb_status_pc.setChecked(bean.isChecked);
        cb_status_local.setChecked(bean.isChecked);

        tv_name_pc.setText(bean.showName);
        tv_name_local.setText(bean.showName);

        if (bean.isPC) {
            form_pc.setVisibility(View.VISIBLE);
            form_local.setVisibility(View.GONE);
        } else {
            form_pc.setVisibility(View.GONE);
            form_local.setVisibility(View.VISIBLE);
        }

    }
}
