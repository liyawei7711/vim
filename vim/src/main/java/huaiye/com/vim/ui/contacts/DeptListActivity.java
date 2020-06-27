package huaiye.com.vim.ui.contacts;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.ttyy.commonanno.anno.BindLayout;
import com.ttyy.commonanno.anno.BindView;
import com.ttyy.commonanno.anno.OnClick;

import java.util.ArrayList;
import java.util.HashMap;

import huaiye.com.vim.R;
import huaiye.com.vim.VIMApp;
import huaiye.com.vim.common.AppBaseActivity;
import huaiye.com.vim.common.recycle.LiteBaseAdapter;
import huaiye.com.vim.models.ModelApis;
import huaiye.com.vim.models.ModelCallback;
import huaiye.com.vim.models.contacts.bean.ContactOrganizationBean;
import huaiye.com.vim.models.contacts.bean.DeptData;
import huaiye.com.vim.models.contacts.bean.DomainInfoList;
import huaiye.com.vim.ui.home.FragmentContacts;
import huaiye.com.vim.ui.home.adapter.ContactsDomainViewHolder;
import ttyy.com.jinnetwork.core.work.HTTPResponse;


/**
 * Created by ywt on 2019/3/21.
 */
@BindLayout(R.layout.activity_group_dept_list)
public class DeptListActivity extends AppBaseActivity {

    @BindView(R.id.et_key)
    TextView et_key;

    @BindView(R.id.rct_view_dept)
    RecyclerView rct_view_dept;
    @BindView(R.id.iv_empty_view)
    View iv_empty_view;
    @BindView(R.id.refresh_view)
    SwipeRefreshLayout refresh_view;

    int totalRequest = 0;

    LiteBaseAdapter<DomainInfoList.DomainInfo> adapterDomain;
    ArrayList<DomainInfoList.DomainInfo> domainData = new ArrayList<>();//部门

    @Override
    protected void initActionBar() {
        initNavigateView();
    }

    private void initNavigateView() {
        getNavigate().setVisibility(View.VISIBLE);
        getNavigate().setTitlText("组织架构")
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
        adapterDomain = new LiteBaseAdapter<>(this,
                domainData,
                ContactsDomainViewHolder.class,
                R.layout.item_contacts_domain_view,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                }, "false");
        rct_view_dept.setLayoutManager(new LinearLayoutManager(this));
        rct_view_dept.setAdapter(adapterDomain);

        refresh_view.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestDept();
                FragmentContacts.requestDeptAll();
            }
        });

        requestDept();
        FragmentContacts.requestDeptAll();
    }

    private void requestDept() {
        if (null != VIMApp.getInstance().mDomainInfoList && VIMApp.getInstance().mDomainInfoList.size() > 0) {
            domainData.clear();
            domainData.addAll(VIMApp.getInstance().mDomainInfoList);
            adapterDomain.notifyDataSetChanged();
        }
    }

    @OnClick({R.id.ll_search, R.id.et_key})
    public void onClickSearch(View view) {
        Intent intent = new Intent(this, SearchDeptUserListActivity.class);
        startActivity(intent);
    }

}
