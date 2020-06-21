package huaiye.com.vim.ui.home.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import huaiye.com.vim.R;
import huaiye.com.vim.common.recycle.LiteBaseAdapter;
import huaiye.com.vim.common.recycle.LiteViewHolder;
import huaiye.com.vim.common.recycle.SafeLinearLayoutManager;
import huaiye.com.vim.models.ModelApis;
import huaiye.com.vim.models.ModelCallback;
import huaiye.com.vim.models.contacts.bean.ContactOrganizationBean;
import huaiye.com.vim.models.contacts.bean.DeptData;
import huaiye.com.vim.models.contacts.bean.DomainInfoList;
import huaiye.com.vim.ui.contacts.DeptChatUtils;
import huaiye.com.vim.ui.contacts.DeptDeepListActivity;
import huaiye.com.vim.ui.contacts.viewholder.DepeContactItemViewHolder;
import huaiye.com.vim.ui.home.FragmentContacts;
import ttyy.com.jinnetwork.core.work.HTTPResponse;

public class ContactsDomainViewHolder extends LiteViewHolder {
    @BindView(R.id.tv_dept_name)
    TextView tv_dept_name;
    @BindView(R.id.rv_data)
    RecyclerView rv_data;
    @BindView(R.id.view_divider)
    View view_divider;

    private RequestOptions requestOptions;

    public ContactsDomainViewHolder(Context context, View view, View.OnClickListener ocl) {
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
        DomainInfoList.DomainInfo domain = (DomainInfoList.DomainInfo) data;
        itemView.setTag(domain);

        itemView.setOnClickListener(ocl);
        tv_dept_name.setText(domain.strDomainName);

        if (FragmentContacts.isShow) {
            rv_data.setVisibility(View.VISIBLE);
            ModelApis.Contacts().requestOrganization(domain.strDomainCode, "", new ModelCallback<ContactOrganizationBean>() {
                @Override
                public void onSuccess(final ContactOrganizationBean contactsBean) {
                    if (null != contactsBean && null != contactsBean.departmentInfoList && contactsBean.departmentInfoList.size() > 0) {
                        ArrayList<DeptData> datas = new ArrayList<>();
                        for(DeptData temp : contactsBean.departmentInfoList) {
                            if("0".equals(temp.strParentID) || "".equals(temp.strParentID)) {
                                datas.add(temp);
                            }
                        }
                        LiteBaseAdapter<DeptData> deptAdapter = new LiteBaseAdapter<>(context,
                                datas,
                                DepeContactItemViewHolder.class,
                                R.layout.item_dept_view,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        DeptData deptData = (DeptData) v.getTag();
                                        deptData.strDomainCode = domain.strDomainCode;
                                        if (v.getId() == R.id.tv_message) {
                                            new DeptChatUtils().startGroup(context, deptData);
                                            return;
                                        }
                                        ArrayList<String> titleName = new ArrayList<>();
                                        titleName.add(domain.strDomainName);
                                        titleName.add(TextUtils.isEmpty(deptData.strName) ? deptData.strDepName : deptData.strName);
                                        Intent intent = new Intent(context, DeptDeepListActivity.class);
                                        intent.putExtra("domainName", domain.strDomainName);
                                        intent.putExtra("titleName", titleName);
                                        intent.putExtra("deptData", deptData);
                                        context.startActivity(intent);
                                    }
                                }, "false");
                        rv_data.setAdapter(deptAdapter);
                        rv_data.setLayoutManager(new SafeLinearLayoutManager(context));
                    }
                }

                @Override
                public void onFinish(HTTPResponse httpResponse) {
                    super.onFinish(httpResponse);
                }
            });
        } else {
            rv_data.setVisibility(View.GONE);
        }

        if (position == datas.size() - 1) {
            view_divider.setVisibility(View.GONE);
        } else {
            view_divider.setVisibility(View.VISIBLE);
        }
    }

}
