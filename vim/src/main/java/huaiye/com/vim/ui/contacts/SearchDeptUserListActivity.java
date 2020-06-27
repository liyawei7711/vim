package huaiye.com.vim.ui.contacts;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.ttyy.commonanno.anno.BindLayout;
import com.ttyy.commonanno.anno.BindView;

import java.util.ArrayList;

import huaiye.com.vim.R;
import huaiye.com.vim.VIMApp;
import huaiye.com.vim.common.AppBaseActivity;
import huaiye.com.vim.common.recycle.LiteBaseAdapter;
import huaiye.com.vim.common.recycle.SafeLinearLayoutManager;
import huaiye.com.vim.dao.msgs.User;
import huaiye.com.vim.models.ModelApis;
import huaiye.com.vim.models.ModelCallback;
import huaiye.com.vim.models.contacts.bean.ContactOrganizationBean;
import huaiye.com.vim.models.contacts.bean.ContactsBean;
import huaiye.com.vim.models.contacts.bean.DeptData;
import huaiye.com.vim.models.contacts.bean.DomainInfoList;
import huaiye.com.vim.ui.contacts.viewholder.DepeItemViewHolder;
import huaiye.com.vim.ui.contacts.viewholder.DeptUserItemViewHolder;
import huaiye.com.vim.ui.home.FragmentContacts;
import ttyy.com.jinnetwork.core.work.HTTPResponse;


/**
 * Created by ywt on 2019/3/21.
 */
@BindLayout(R.layout.activity_search_dept_user_list)
public class SearchDeptUserListActivity extends AppBaseActivity {

    @BindView(R.id.et_key)
    EditText et_key;

    @BindView(R.id.rct_view_dept)
    RecyclerView rct_view_dept;
    @BindView(R.id.rct_view_user)
    RecyclerView rct_view_user;
    @BindView(R.id.tv_dept_title)
    View tv_dept_title;
    @BindView(R.id.tv_user_title)
    View tv_user_title;
    @BindView(R.id.iv_empty_view)
    View iv_empty_view;
    @BindView(R.id.refresh_view)
    SwipeRefreshLayout refresh_view;

    LiteBaseAdapter<DeptData> deptAdapter;
    ArrayList<DeptData> deptDatas = new ArrayList<>();
    LiteBaseAdapter<User> userAdapter;
    ArrayList<User> userInfos = new ArrayList<>();

    int totalRequest = 0;
    ArrayList<DeptData> allDeptDatas = new ArrayList<>();

    @Override
    protected void initActionBar() {
        initNavigateView();
    }

    private void initNavigateView() {
        getNavigate().setVisibility(View.VISIBLE);
        getNavigate().setTitlText("查询结果")
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

        rct_view_dept.setNestedScrollingEnabled(false);
        rct_view_user.setNestedScrollingEnabled(false);

        deptAdapter = new LiteBaseAdapter<>(this,
                deptDatas,
                DepeItemViewHolder.class,
                R.layout.item_dept_view,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DeptData deptData = (DeptData) v.getTag();
                        if (v.getId() == R.id.tv_message) {
                            new DeptChatUtils().startGroup(SearchDeptUserListActivity.this, deptData);
                            return;
                        }
                        for (DomainInfoList.DomainInfo temp : VIMApp.getInstance().mDomainInfoList) {
                            if (temp.strDomainCode.equals(deptData.strDomainCode)) {
                                deptData.domainInfo = temp;
                                break;
                            }
                        }
                        jumpToNext(deptData);
                    }
                }, "false");
        userAdapter = new LiteBaseAdapter<>(this,
                userInfos,
                DeptUserItemViewHolder.class,
                R.layout.item_dept_person,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        User user = (User) v.getTag();
                        Intent intent = new Intent(SearchDeptUserListActivity.this, ContactDetailNewActivity.class);
                        intent.putExtra("nUser", user);
                        startActivity(intent);
                    }
                }, "false");
        rct_view_dept.setAdapter(deptAdapter);
        rct_view_dept.setLayoutManager(new SafeLinearLayoutManager(this));
        rct_view_user.setAdapter(userAdapter);
        rct_view_user.setLayoutManager(new SafeLinearLayoutManager(this));

        refresh_view.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestUser();
            }
        });

        et_key.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    FragmentContacts.requestDeptAll();
                    requestUser();
                    requestDept();
                    return true;
                }
                return false;
            }
        });

//        requestUser();
//        requestDept();
        FragmentContacts.requestDeptAll();
    }

    private void requestUser() {
        userInfos.clear();
        if (null != VIMApp.getInstance().mDomainInfoList && VIMApp.getInstance().mDomainInfoList.size() > 0) {
            for (DomainInfoList.DomainInfo temp : VIMApp.getInstance().mDomainInfoList) {
                ModelApis.Contacts().requestContactsByKey(temp.strDomainCode, et_key.getText().toString(), new ModelCallback<ContactsBean>() {
                    @Override
                    public void onSuccess(final ContactsBean contactsBean) {
                        if (null != contactsBean && null != contactsBean.userList && contactsBean.userList.size() > 0) {
                            userInfos.addAll(contactsBean.userList);
                            userAdapter.notifyDataSetChanged();
                        }

                        if (userInfos.isEmpty()) {
                            tv_user_title.setVisibility(View.GONE);
                        } else {
                            tv_user_title.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFinish(HTTPResponse httpResponse) {
                        super.onFinish(httpResponse);
                        refresh_view.setRefreshing(false);
                    }
                });
            }
        }
    }

    private void requestDept() {
        deptDatas.clear();
        if (null != VIMApp.getInstance().mDomainInfoList && VIMApp.getInstance().mDomainInfoList.size() > 0) {
            for (DomainInfoList.DomainInfo info : VIMApp.getInstance().mDomainInfoList) {
                ModelApis.Contacts().requestOrganization("search 181 ", info.strDomainCode, et_key.getText().toString(), new ModelCallback<ContactOrganizationBean>() {
                    @Override
                    public void onSuccess(final ContactOrganizationBean contactsBean) {
                        for (DeptData deptData : contactsBean.departmentInfoList) {
                            deptData.strDomainCode = info.strDomainCode;
                            for (DomainInfoList.DomainInfo temp : VIMApp.getInstance().mDomainInfoList) {
                                if (temp.strDomainCode.equals(deptData.strDomainCode)) {
                                    deptData.domainInfo = temp;
                                    break;
                                }
                            }
                        }
                        deptDatas.addAll(contactsBean.departmentInfoList);
                        deptAdapter.notifyDataSetChanged();

                        if (deptDatas.isEmpty()) {
                            tv_dept_title.setVisibility(View.GONE);
                        } else {
                            tv_dept_title.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFinish(HTTPResponse httpResponse) {
                        super.onFinish(httpResponse);
                        refresh_view.setRefreshing(false);
                    }
                });
            }
        }
    }

    private void jumpToNext(DeptData deptData) {
        ArrayList<String> titleName = new ArrayList<>();
        titleName.add(deptData.domainInfo.strDomainName);
        titleName.add(TextUtils.isEmpty(deptData.strName) ? deptData.strDepName : deptData.strName);
        Intent intent = new Intent(this, DeptDeepListActivity.class);
        intent.putExtra("domainName", deptData.domainInfo.strDomainName);
        intent.putExtra("titleName", titleName);
        intent.putExtra("deptData", deptData);
        intent.putExtra("map", FragmentContacts.map);
        startActivity(intent);
    }

}
