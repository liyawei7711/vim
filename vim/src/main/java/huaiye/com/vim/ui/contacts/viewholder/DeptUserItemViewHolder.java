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
import huaiye.com.vim.dao.msgs.User;
import huaiye.com.vim.models.contacts.bean.DeptData;
import huaiye.com.vim.ui.home.FragmentContacts;

/**
 * Created by ywt on 2019/2/25.
 */

public class DeptUserItemViewHolder extends LiteViewHolder {

    @BindView(R.id.iv_user_head)
    ImageView iv_user_head;
    @BindView(R.id.tv_user_name)
    TextView tv_user_name;
    @BindView(R.id.tv_choose_added)
    TextView tv_choose_added;
    @BindView(R.id.tv_choose_dept)
    TextView tv_choose_dept;
    @BindView(R.id.view_divider)
    View view_divider;

    RequestOptions requestOptions;

    public DeptUserItemViewHolder(Context context, View view, View.OnClickListener ocl) {
        super(context, view, ocl);
        itemView.setOnClickListener(ocl);

        requestOptions = new RequestOptions();
        requestOptions.centerCrop()
                .dontAnimate()
                .format(DecodeFormat.PREFER_RGB_565)
                .placeholder(R.drawable.default_image_personal)
                .error(R.drawable.default_image_personal)
                .optionalTransform(new CircleCrop());
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void bindData(Object holder, int position, Object data, int size, List datas, Object extr) {
        User user = (User) data;
        itemView.setTag(user);

        Glide.with(context)
                .load(AppDatas.Constants().getAddressWithoutPort() + user.strHeadUrl)
                .apply(requestOptions)
                .into(iv_user_head);

        if (user.getUserDept() != null &&
                !user.getUserDept().isEmpty()) {
            tv_choose_dept.setVisibility(View.VISIBLE);
            String strParentID = "";
            for (DeptData temp : FragmentContacts.allDeptDatas) {
                if (temp.strDepID.equals(user.getUserDept().get(0).strDepID)) {
                    strParentID = temp.strParentID;
                    break;
                }
            }
            boolean has = false;
            for (DeptData temp : FragmentContacts.allDeptDatas) {
                if (temp.strDepID.equals(strParentID)) {
                    has = true;
                    tv_choose_dept.setText(temp.getName() + "-" + user.getUserDept().get(0).getName());
                    break;
                }
            }
            if (!has) {
                tv_choose_dept.setText(user.getUserDept().get(0).getName());
            }
        } else {
            tv_choose_dept.setVisibility(View.GONE);
            tv_choose_dept.setText("");
        }

        tv_user_name.setText(user.strUserName);
//        StringBuilder sb = new StringBuilder();
//        for (DeptData temp : user.getUserDept()) {
//            sb.append(temp.strDepName + " ");
//        }
//        StringBuilder sbStr = new StringBuilder();
//        if (!TextUtils.isEmpty(user.strPostName)) {
//            sbStr.append(user.strPostName);
//        }
//        if (!TextUtils.isEmpty(sb)) {
//            if (!TextUtils.isEmpty(sbStr)) {
//                sbStr.append("," + sb);
//            } else {
//                sbStr.append(sb);
//            }
//        }
//        tv_choose_added.setText(sbStr);
        tv_choose_added.setText(user.strPostName);
        if (TextUtils.isEmpty(user.strPostName)) {
            tv_choose_added.setVisibility(View.GONE);
        } else {
            tv_choose_added.setVisibility(View.VISIBLE);
        }

        if (position == datas.size() - 1) {
            view_divider.setVisibility(View.GONE);
        } else {
            view_divider.setVisibility(View.VISIBLE);
        }

    }
}
