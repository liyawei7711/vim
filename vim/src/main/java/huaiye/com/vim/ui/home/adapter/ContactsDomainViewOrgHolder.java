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

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import huaiye.com.vim.R;
import huaiye.com.vim.bus.EventUserSelected;
import huaiye.com.vim.common.recycle.LiteBaseAdapter;
import huaiye.com.vim.common.recycle.LiteViewHolder;
import huaiye.com.vim.common.recycle.SafeLinearLayoutManager;
import huaiye.com.vim.dao.auth.AppAuth;
import huaiye.com.vim.dao.msgs.User;
import huaiye.com.vim.models.ModelApis;
import huaiye.com.vim.models.ModelCallback;
import huaiye.com.vim.models.contacts.bean.ContactOrganizationBean;
import huaiye.com.vim.models.contacts.bean.ContactsBean;
import huaiye.com.vim.models.contacts.bean.DeptData;
import huaiye.com.vim.models.contacts.bean.DomainInfoList;
import huaiye.com.vim.ui.contacts.DeptDeepListOrgActivity;
import huaiye.com.vim.ui.contacts.sharedata.ChoosedContactsNew;
import huaiye.com.vim.ui.contacts.viewholder.DepeContactItemViewOrgHolder;
import huaiye.com.vim.ui.home.FragmentContacts;
import ttyy.com.jinnetwork.core.work.HTTPResponse;

import static huaiye.com.vim.common.AppBaseActivity.showToast;
import static huaiye.com.vim.ui.contacts.sharedata.ChoosedContactsNew.atData;
import static huaiye.com.vim.ui.contacts.sharedata.ChoosedContactsNew.selectedDept;
import static huaiye.com.vim.ui.contacts.sharedata.ChoosedContactsNew.userDeptMap;

public class ContactsDomainViewOrgHolder extends LiteViewHolder {
    @BindView(R.id.tv_dept_name)
    TextView tv_dept_name;
    @BindView(R.id.rv_data)
    RecyclerView rv_data;
    @BindView(R.id.view_divider)
    View view_divider;

    public static boolean mIsChoice;
    public static boolean isZhuanFa;
    public static int max;

    private RequestOptions requestOptions;

    public ContactsDomainViewOrgHolder(Context context, View view, View.OnClickListener ocl) {
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

        ModelApis.Contacts().requestOrganization("adapter 65 ", domain.strDomainCode, "", new ModelCallback<ContactOrganizationBean>() {
            @Override
            public void onSuccess(final ContactOrganizationBean contactsBean) {
                if (null != contactsBean && null != contactsBean.departmentInfoList && contactsBean.departmentInfoList.size() > 0) {
                    ArrayList<DeptData> datas = new ArrayList<>();
                    for (DeptData temp : contactsBean.departmentInfoList) {
                        if (selectedDept.contains(temp.strDepID)) {
                            temp.isSelected = true;
                        } else {
                            temp.isSelected = false;
                        }
                        temp.strDomainCode = domain.strDomainCode;
                        if ("0".equals(temp.strParentID) || "".equals(temp.strParentID)) {
                            boolean canAdd = true;
                            for (DeptData dept : datas) {
                                if (dept.strDepID.equals(temp.strDepID)) {
                                    canAdd = false;
                                    break;
                                }
                            }
                            if (canAdd) {
                                datas.add(temp);
                            }
                        }
                    }
                    Collections.sort(datas, new Comparator<DeptData>() {
                        @Override
                        public int compare(DeptData o1, DeptData o2) {
                            return o1.nPpriority - o2.nPpriority;
                        }
                    });
                    final LiteBaseAdapter<DeptData> deptAdapter = new LiteBaseAdapter<>(context,
                            datas,
                            DepeContactItemViewOrgHolder.class,
                            R.layout.item_dept_view_org,
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    DeptData deptData = (DeptData) v.getTag();
                                    deptData.strDomainCode = domain.strDomainCode;
                                    if (v.getId() == R.id.tv_next) {
                                        ArrayList<String> titleName = new ArrayList<>();
                                        titleName.add(TextUtils.isEmpty(deptData.strName) ? deptData.strDepName : deptData.strName);
                                        Intent intent = new Intent(context, DeptDeepListOrgActivity.class);
                                        intent.putExtra("domainName", domain.strDomainName);
                                        intent.putExtra("titleName", titleName);
                                        intent.putExtra("deptData", deptData);
                                        intent.putExtra("map", FragmentContacts.map);
                                        intent.putExtra("max", max);
                                        intent.putExtra("isZhuanFa", isZhuanFa);
                                        context.startActivity(intent);
                                    } else {
                                        if(isZhuanFa) {
                                            boolean canSend = false;
                                            for(DeptData temp : atData) {
                                                if(temp.strDepID.equals(deptData.strDepID)) {
                                                    canSend = true;
                                                    break;
                                                }
                                            }
                                            if(!canSend) {
                                                showToast("只能给所在部门发送消息");
                                                return;
                                            }
                                        }
                                        deptData.isSelected = !deptData.isSelected;
                                        if (deptData.isSelected) {
                                            ChoosedContactsNew.get().add(deptData);
                                            if (!selectedDept.contains(deptData.strDepID)) {
                                                selectedDept.add(deptData.strDepID);
                                            }
                                        } else {
                                            ChoosedContactsNew.get().remove(deptData);
                                            if (selectedDept.contains(deptData.strDepID)) {
                                                selectedDept.remove(deptData.strDepID);
                                            }
                                        }
                                        rv_data.getAdapter().notifyDataSetChanged();
                                        handleChoice(deptData);
                                    }
                                }
                            }, "false");
                    DepeContactItemViewOrgHolder.mIsChoice = mIsChoice;
                    rv_data.setAdapter(deptAdapter);
                    rv_data.setLayoutManager(new SafeLinearLayoutManager(context));
                }
            }

            @Override
            public void onFinish(HTTPResponse httpResponse) {
                super.onFinish(httpResponse);
            }
        });

        if (position == datas.size() - 1) {
            view_divider.setVisibility(View.GONE);
        } else {
            view_divider.setVisibility(View.VISIBLE);
        }
    }

    private void handleChoice(final DeptData deptData) {
        if(isZhuanFa) {
            EventBus.getDefault().post(new EventUserSelected());
            return;
        }
        ModelApis.Contacts().requestContactsOnly(deptData.strDomainCode, deptData.strDepID, 1, new ModelCallback<ContactsBean>() {
            @Override
            public void onSuccess(final ContactsBean contactsBean) {
                if (null != contactsBean && null != contactsBean.userList && contactsBean.userList.size() > 0) {
                    for (User user : contactsBean.userList) {
                        if (!user.strUserID.equals(AppAuth.get().getUserID())) {
                            if (deptData.isSelected) {
                                ChoosedContactsNew.get().add(deptData);
                                if (!ChoosedContactsNew.get().isContain(user)) {
                                    if (ChoosedContactsNew.get().getContacts().size() >= max + 1) {
                                        showToast("最多选" + max + "人，已达人数上限");
                                        return;
                                    }
                                    userDeptMap.put(user.strUserID, deptData.strDepID);
                                    user.isSelected = true;
                                    ChoosedContactsNew.get().add(user, false);
                                }
                            } else {
                                ChoosedContactsNew.get().remove(deptData);
                                if (ChoosedContactsNew.get().isContain(user)) {
                                    user.isSelected = false;
                                    ChoosedContactsNew.get().remove(user);

                                    if (userDeptMap.containsKey(user.strUserID)) {
                                        userDeptMap.remove(user.strUserID);
                                    }
                                }
                            }
                        }
                    }

                    EventBus.getDefault().post(new EventUserSelected());
                }
            }

            @Override
            public void onFinish(HTTPResponse httpResponse) {
                super.onFinish(httpResponse);
                EventBus.getDefault().post(new EventUserSelected());
            }
        });
    }

}
