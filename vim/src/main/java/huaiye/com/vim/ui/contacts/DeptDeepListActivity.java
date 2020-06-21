package huaiye.com.vim.ui.contacts;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.ttyy.commonanno.anno.BindLayout;
import com.ttyy.commonanno.anno.BindView;
import com.ttyy.commonanno.anno.OnClick;
import com.ttyy.commonanno.anno.route.BindExtra;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import huaiye.com.vim.R;
import huaiye.com.vim.common.AppBaseActivity;
import huaiye.com.vim.common.recycle.LiteBaseAdapter;
import huaiye.com.vim.common.recycle.SafeLinearLayoutManager;
import huaiye.com.vim.dao.msgs.User;
import huaiye.com.vim.models.ModelApis;
import huaiye.com.vim.models.ModelCallback;
import huaiye.com.vim.models.contacts.bean.ContactsBean;
import huaiye.com.vim.models.contacts.bean.DeptData;
import huaiye.com.vim.ui.contacts.viewholder.DepeItemViewHolder;
import huaiye.com.vim.ui.contacts.viewholder.DepeTopViewHolder;
import huaiye.com.vim.ui.contacts.viewholder.DeptUserItemViewHolder;
import ttyy.com.jinnetwork.core.work.HTTPResponse;


/**
 * Created by ywt on 2019/3/21.
 */
@BindLayout(R.layout.activity_group_dept_deep_list)
public class DeptDeepListActivity extends AppBaseActivity {

    @BindView(R.id.et_key)
    TextView et_key;

    @BindView(R.id.rct_view_item)
    RecyclerView rct_view_item;
    @BindView(R.id.rct_view_dept)
    RecyclerView rct_view_dept;
    @BindView(R.id.rct_view_user)
    RecyclerView rct_view_user;
    @BindView(R.id.iv_empty_view)
    View iv_empty_view;
    @BindView(R.id.refresh_view)
    SwipeRefreshLayout refresh_view;

    @BindExtra
    String domainName;
    @BindExtra
    ArrayList<String> titleName;
    @BindExtra
    DeptData deptData;
    @BindExtra
    HashMap<String, ArrayList<DeptData>> map;

    ArrayList<User> allUserInfos = new ArrayList<>();
    ArrayList<DeptData> allDeptDatas = new ArrayList<>();

    LiteBaseAdapter<String> strAdapter;
    ArrayList<String> strDatas = new ArrayList<>();
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
        getNavigate().setTitlText(domainName)
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

        strDatas.clear();
        strDatas.addAll(titleName);
        strAdapter = new LiteBaseAdapter<>(this,
                strDatas,
                DepeTopViewHolder.class,
                R.layout.item_dept_top,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }, "false");
        deptAdapter = new LiteBaseAdapter<>(this,
                deptDatas,
                DepeItemViewHolder.class,
                R.layout.item_dept_view,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DeptData deptData = (DeptData) v.getTag();
                        if (v.getId() == R.id.tv_message) {
                            new DeptChatUtils().startGroup(DeptDeepListActivity.this, deptData);
                            return;
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
                        Intent intent = new Intent(DeptDeepListActivity.this, ContactDetailNewActivity.class);
                        intent.putExtra("nUser", user);
                        startActivity(intent);
                    }
                }, "false");
        rct_view_item.setAdapter(strAdapter);
        rct_view_item.setLayoutManager(new SafeLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
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
                    String s = et_key.getText().toString();
                    if (s != null && s.length() > 0) {
                        showData(s.toString());
                    } else {
                        showData("");
                    }
                    return true;
                }
                return false;
            }
        });

        requestUser();
        requestDept();
    }

    private void requestUser() {
        ModelApis.Contacts().requestContacts(deptData.strDomainCode, deptData.strDepID, new ModelCallback<ContactsBean>() {
            @Override
            public void onSuccess(final ContactsBean contactsBean) {
                if (null != contactsBean && null != contactsBean.userList && contactsBean.userList.size() > 0) {
                    allUserInfos.clear();
                    allUserInfos.addAll(contactsBean.userList);
                    showData(et_key.getText().toString());
                }
            }

            @Override
            public void onFinish(HTTPResponse httpResponse) {
                super.onFinish(httpResponse);
                refresh_view.setRefreshing(false);
            }
        });
    }

    private void requestDept() {
        if(map == null) {
            return;
        }
        allDeptDatas.clear();
        if(map.get(deptData.strDepID) != null) {
            allDeptDatas.addAll(map.get(deptData.strDepID));
        }
        showData(et_key.getText().toString());
    }

    private void jumpToNext(DeptData deptData) {
        ArrayList<String> titleName = new ArrayList<>();
        titleName.add(domainName);
        titleName.add(TextUtils.isEmpty(deptData.strName) ? deptData.strDepName : deptData.strName);
        Intent intent = new Intent(this, DeptDeepListActivity.class);
        intent.putExtra("domainName", domainName);
        intent.putExtra("titleName", titleName);
        intent.putExtra("deptData", deptData);
        intent.putExtra("map", map);
        startActivity(intent);
    }

    private void showData(String str) {
        userInfos.clear();
        if (TextUtils.isEmpty(str)) {
            userInfos.addAll(allUserInfos);
            deptDatas.addAll(allDeptDatas);
        } else {
            for (User temp : userInfos) {
                if (temp.strUserName.contains(str) ||
                        temp.strLoginName.contains(str)) {
                    userInfos.add(temp);
                }
            }
            for (DeptData temp : allDeptDatas) {
                if ((TextUtils.isEmpty(deptData.strName) ? deptData.strDepName : deptData.strName).contains(str)) {
                    deptDatas.add(temp);
                }
            }
        }
        userAdapter.notifyDataSetChanged();
        deptAdapter.notifyDataSetChanged();
    }

    @OnClick(R.id.tv_user_manager)
    public void onClick(View view) {
        Intent intent = new Intent(this, DomainUserListActivity.class);
        intent.putExtra("deptData", deptData);
        startActivity(intent);
    }

    @OnClick({R.id.ll_search, R.id.et_key})
    public void onClickSearch(View view) {
        Intent intent = new Intent(this, SearchDeptUserListActivity.class);
        startActivity(intent);
    }

}
