package huaiye.com.vim.ui.contacts;

import android.content.Intent;
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

import huaiye.com.vim.R;
import huaiye.com.vim.common.AppBaseActivity;
import huaiye.com.vim.common.recycle.LiteBaseAdapter;
import huaiye.com.vim.common.recycle.SafeLinearLayoutManager;
import huaiye.com.vim.models.ModelApis;
import huaiye.com.vim.models.ModelCallback;
import huaiye.com.vim.models.contacts.bean.ContactOrganizationBean;
import huaiye.com.vim.models.contacts.bean.DeptData;
import huaiye.com.vim.models.contacts.bean.DomainInfoList;
import huaiye.com.vim.ui.contacts.viewholder.DepeItemViewHolder;
import huaiye.com.vim.ui.contacts.viewholder.DepeTopViewHolder;
import ttyy.com.jinnetwork.core.work.HTTPResponse;


/**
 * Created by ywt on 2019/3/21.
 */
@BindLayout(R.layout.activity_group_dept_list)
public class DeptListActivity extends AppBaseActivity {

    @BindView(R.id.et_key)
    TextView et_key;

    @BindView(R.id.rct_view_item)
    RecyclerView rct_view_item;
    @BindView(R.id.rct_view_dept)
    RecyclerView rct_view_dept;
    @BindView(R.id.iv_empty_view)
    View iv_empty_view;
    @BindView(R.id.refresh_view)
    SwipeRefreshLayout refresh_view;

    @BindExtra
    ArrayList<String> titleName;
    @BindExtra
    String domainName;
    @BindExtra
    DomainInfoList.DomainInfo domain;

    ArrayList<DeptData> allDeptDatas = new ArrayList<>();

    LiteBaseAdapter<String> strAdapter;
    ArrayList<String> strDatas = new ArrayList<>();
    LiteBaseAdapter<DeptData> deptAdapter;
    ArrayList<DeptData> deptDatas = new ArrayList<>();

    HashMap<String, ArrayList<DeptData>> map = new HashMap<>();

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
                            new DeptChatUtils().startGroup(DeptListActivity.this, deptData);
                            return;
                        }
                        jumpToNext(deptData);
                    }
                }, "false");
        rct_view_item.setAdapter(strAdapter);
        rct_view_item.setLayoutManager(new SafeLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rct_view_dept.setAdapter(deptAdapter);
        rct_view_dept.setLayoutManager(new SafeLinearLayoutManager(this));

        refresh_view.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestDeptList();
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

        requestDeptList();
    }

    private void requestDeptList() {
        ModelApis.Contacts().requestOrganization(domain.strDomainCode, "", new ModelCallback<ContactOrganizationBean>() {
            @Override
            public void onSuccess(final ContactOrganizationBean contactsBean) {
                if (null != contactsBean && null != contactsBean.departmentInfoList && contactsBean.departmentInfoList.size() > 0) {
                    map.clear();
                    for (DeptData temp : contactsBean.departmentInfoList) {
                        temp.strDomainCode = domain.strDomainCode;
                        if (map.containsKey(temp.strParentID)) {
                            ArrayList<DeptData> datas = map.get(temp.strParentID);
                            datas.add(temp);
                        } else {
                            ArrayList<DeptData> datas = new ArrayList<>();
                            datas.add(temp);
                            map.put(temp.strParentID, datas);
                        }
                    }
                    allDeptDatas.clear();
                    if(map.containsKey("")) {
                        allDeptDatas.addAll(map.get(""));
                    }
                    if(map.containsKey("0")) {
                        allDeptDatas.addAll(map.get("0"));
                    }
                    showData(et_key.getText().toString());
                }
            }

            @Override
            public void onFinish(HTTPResponse httpResponse) {
                super.onFinish(httpResponse);
            }
        });
    }

    private void jumpToNext(DeptData deptData) {
        ArrayList<String> titleName = new ArrayList<>();
        titleName.add(domainName);
        titleName.add(TextUtils.isEmpty(deptData.strName) ? deptData.strDepName : deptData.strName);
        Intent intent = new Intent(DeptListActivity.this, DeptDeepListActivity.class);
        intent.putExtra("domainName", domainName);
        intent.putExtra("titleName", titleName);
        intent.putExtra("deptData", deptData);
        intent.putExtra("map", map);
        startActivity(intent);
    }

    private void showData(String str) {
        deptDatas.clear();
        if (TextUtils.isEmpty(str)) {
            deptDatas.addAll(allDeptDatas);
        } else {
            for (DeptData temp : allDeptDatas) {
                if ((TextUtils.isEmpty(temp.strName) ? temp.strDepName : temp.strName).contains(str)) {
                    deptDatas.add(temp);
                }
            }
        }
        deptAdapter.notifyDataSetChanged();
    }

    @OnClick(R.id.tv_user_manager)
    public void onClick(View view) {
        Intent intent = new Intent(this, DomainUserListActivity.class);
        intent.putExtra("deptDatas", deptDatas);
        startActivity(intent);
    }

    @OnClick({R.id.ll_search, R.id.et_key})
    public void onClickSearch(View view) {
        Intent intent = new Intent(this, SearchDeptUserListActivity.class);
        startActivity(intent);
    }

}
