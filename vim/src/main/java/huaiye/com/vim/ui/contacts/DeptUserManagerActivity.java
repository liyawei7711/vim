package huaiye.com.vim.ui.contacts;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.ttyy.commonanno.anno.BindLayout;
import com.ttyy.commonanno.anno.BindView;
import com.ttyy.commonanno.anno.route.BindExtra;

import java.util.ArrayList;

import huaiye.com.vim.R;
import huaiye.com.vim.common.AppBaseActivity;
import huaiye.com.vim.common.recycle.LiteBaseAdapter;
import huaiye.com.vim.common.recycle.SafeLinearLayoutManager;
import huaiye.com.vim.dao.msgs.LstOutUserBean;
import huaiye.com.vim.dao.msgs.User;
import huaiye.com.vim.models.ModelApis;
import huaiye.com.vim.models.ModelCallback;
import huaiye.com.vim.models.contacts.bean.ContactsBean;
import huaiye.com.vim.models.contacts.bean.CustomResponse;
import huaiye.com.vim.models.contacts.bean.DeptData;
import huaiye.com.vim.ui.contacts.viewholder.DepeItemViewHolder;
import huaiye.com.vim.ui.contacts.viewholder.DeptUserSelectedItemViewHolder;
import ttyy.com.jinnetwork.core.work.HTTPResponse;


/**
 * Created by ywt on 2019/3/21.
 */
@BindLayout(R.layout.activity_doamin_user_manager)
public class DeptUserManagerActivity extends AppBaseActivity {

    @BindView(R.id.rct_view_dept)
    RecyclerView rct_view_dept;
    @BindView(R.id.rct_view_user)
    RecyclerView rct_view_user;

    @BindView(R.id.refresh_view)
    SwipeRefreshLayout refresh_view;

    @BindExtra
    String domainName;
    @BindExtra
    ArrayList<String> titleName;
    @BindExtra
    DeptData deptData;

    ArrayList<DeptData> allDeptDatas = new ArrayList<>();
    ArrayList<User> allUserInfos = new ArrayList<>();

    LiteBaseAdapter<DeptData> deptAdapter;
    ArrayList<DeptData> deptDatas = new ArrayList<>();
    LiteBaseAdapter<User> userAdapter;
    ArrayList<User> userInfos = new ArrayList<>();

    @Override
    protected void initActionBar() {
        initNavigateView();
    }

    private void initNavigateView() {
        getNavigate().setVisibility(View.VISIBLE);
        getNavigate().setTitlText((TextUtils.isEmpty(deptData.strName) ? deptData.strDepName : deptData.strName) + "添加人员")
                .setRightText("确定")
                .setRightClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onComfirm();
                    }
                })
                .setLeftClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
    }

    @Override
    public void doInitDelay() {
        initView();
    }

    private void initView() {
        deptAdapter = new LiteBaseAdapter<>(this,
                deptDatas,
                DepeItemViewHolder.class,
                R.layout.item_dept_view,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        User user = (User) v.getTag();
                        user.isSelected = !user.isSelected;
                        userAdapter.notifyDataSetChanged();
                    }
                }, "false");
        rct_view_user.setAdapter(userAdapter);
        rct_view_user.setLayoutManager(new SafeLinearLayoutManager(this));

        userAdapter = new LiteBaseAdapter<>(this,
                userInfos,
                DeptUserSelectedItemViewHolder.class,
                R.layout.item_dept_selected_person,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        User user = (User) v.getTag();
                        user.isSelected = !user.isSelected;
                        userAdapter.notifyDataSetChanged();
                    }
                }, "false");
        rct_view_user.setAdapter(userAdapter);
        rct_view_user.setLayoutManager(new SafeLinearLayoutManager(this));

        refresh_view.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh_view.setRefreshing(false);
//                requestUser();
            }
        });

        requestUser();
    }

    private void requestUser() {
        ModelApis.Contacts().requestContacts(deptData.strDomainCode, "0", new ModelCallback<ContactsBean>() {
            @Override
            public void onSuccess(final ContactsBean contactsBean) {
                if (null != contactsBean && null != contactsBean.userList && contactsBean.userList.size() > 0) {
                    userInfos.clear();
                    userInfos.addAll(contactsBean.userList);
                    userAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFinish(HTTPResponse httpResponse) {
                super.onFinish(httpResponse);
            }
        });
    }

    private void onComfirm() {
        ArrayList<LstOutUserBean> lstUser = new ArrayList<>();
        for (User temp : userInfos) {
            if (temp.isSelected) {
                lstUser.add(new LstOutUserBean(temp.getDomainCode(), temp.strUserID, temp.strUserName));
            }
        }
        ModelApis.Contacts().addUserToDept(deptData.strDepID, lstUser, new ModelCallback<CustomResponse>() {
            @Override
            public void onSuccess(final CustomResponse response) {
                if (null != response && response.nResultCode == 0) {
                    showToast(response.strResultDescribe);
                    finish();
                } else {
                    showToast(response.strResultDescribe);
                }
            }

            @Override
            public void onFinish(HTTPResponse httpResponse) {
                super.onFinish(httpResponse);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
