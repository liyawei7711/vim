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
import huaiye.com.vim.models.ModelApis;
import huaiye.com.vim.models.ModelCallback;
import huaiye.com.vim.models.contacts.bean.ContactsBean;
import huaiye.com.vim.models.contacts.bean.DeptData;
import ttyy.com.jinnetwork.core.work.HTTPResponse;

/**
 * Created by ywt on 2019/2/25.
 */

public class DepeItemViewHolder extends LiteViewHolder {

    @BindView(R.id.tv_name)
    TextView tv_name;
    @BindView(R.id.tv_num)
    TextView tv_num;

    public DepeItemViewHolder(Context context, View view, View.OnClickListener ocl) {
        super(context, view, ocl);
        itemView.setOnClickListener(ocl);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void bindData(Object holder, int position, Object data, int size, List datas, Object extr) {
        DeptData deptData = (DeptData) data;
        itemView.setTag(deptData);

        tv_name.setText(TextUtils.isEmpty(deptData.strName) ? deptData.strDepName : deptData.strName);
        tv_num.setText("(0)");

        ModelApis.Contacts().requestContacts(deptData.strDomainCode, deptData.strDepID, new ModelCallback<ContactsBean>() {
            @Override
            public void onSuccess(final ContactsBean contactsBean) {
                if (null != contactsBean && null != contactsBean.userList && contactsBean.userList.size() > 0) {
                    tv_num.setText("(" + contactsBean.userList.size() + ")");
                } else {
                    tv_num.setText("(0)");
                }
            }

            @Override
            public void onFinish(HTTPResponse httpResponse) {
                super.onFinish(httpResponse);
            }
        });
    }

}
