package huaiye.com.vim.ui.contacts.viewholder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
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

public class UserDetailDepeItemViewHolder extends LiteViewHolder {

    @BindView(R.id.tv_name)
    TextView tv_name;
    @BindView(R.id.tv_num)
    TextView tv_num;
    @BindView(R.id.tv_message)
    TextView tv_message;

    public UserDetailDepeItemViewHolder(Context context, View view, View.OnClickListener ocl) {
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

        String strParentID = "";
        for(DeptData temp : FragmentContacts.allDeptDatas) {
            if (temp.strDepID.equals(deptData.strDepID)) {
                strParentID = temp.strParentID;
                break;
            }
        }
        boolean has = false;
        for(DeptData temp : FragmentContacts.allDeptDatas) {
            if(temp.strDepID.equals(strParentID)) {
                has = true;
                tv_name.setText(temp.getName()+"-"+deptData.getName());
                break;
            }
        }
        if(!has) {
            tv_name.setText(deptData.getName());
        }

        tv_message.setVisibility(View.GONE);
        for (DeptData temp : FragmentContacts.atData) {
            if (temp.strDepID.equals(deptData.strDepID)) {
                tv_message.setVisibility(View.VISIBLE);
                break;
            }
        }

        tv_message.setVisibility(View.GONE);
        for (DeptData temp : FragmentContacts.atData) {
            if (temp.strDepID.equals(deptData.strDepID)) {
                tv_message.setVisibility(View.VISIBLE);
                break;
            }
        }

        tv_num.setText(" ");
//        if(TextUtils.isEmpty(DeptDeepListActivity.allNum.get(deptData.strDepID))) {
//            ModelApis.Contacts().requestContacts(deptData.strDomainCode, deptData.strDepID, new ModelCallback<ContactsBean>() {
//                @Override
//                public void onSuccess(final ContactsBean contactsBean) {
//                    DeptDeepListActivity.allNum.put(deptData.strDepID, contactsBean.nTotalSize + "");
//                    tv_num.setText("(" + DeptDeepListActivity.allNum.get(deptData.strDepID) + ")");
//                }
//
//                @Override
//                public void onFailure(HTTPResponse httpResponse) {
//                    super.onFailure(httpResponse);
//                    DeptDeepListActivity.allNum.put(deptData.strDepID, "0");
//                    tv_num.setText("(" + DeptDeepListActivity.allNum.get(deptData.strDepID) + ")");
//                }
//            });
//        } else {
//            tv_num.setText("(" + DeptDeepListActivity.allNum.get(deptData.strDepID) + ")");
//        }

    }

}
