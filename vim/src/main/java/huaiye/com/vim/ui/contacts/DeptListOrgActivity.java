package huaiye.com.vim.ui.contacts;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ttyy.commonanno.anno.BindLayout;
import com.ttyy.commonanno.anno.BindView;
import com.ttyy.commonanno.anno.OnClick;
import com.ttyy.commonanno.anno.route.BindExtra;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import huaiye.com.vim.R;
import huaiye.com.vim.VIMApp;
import huaiye.com.vim.bus.EventUserSelected;
import huaiye.com.vim.bus.EventUserSelectedComplete;
import huaiye.com.vim.common.AppBaseActivity;
import huaiye.com.vim.common.recycle.LiteBaseAdapter;
import huaiye.com.vim.common.recycle.SafeLinearLayoutManager;
import huaiye.com.vim.dao.AppDatas;
import huaiye.com.vim.models.contacts.bean.DomainInfoList;
import huaiye.com.vim.models.contacts.bean.SelectedModeBean;
import huaiye.com.vim.ui.contacts.sharedata.ChoosedContactsNew;
import huaiye.com.vim.ui.home.FragmentContacts;
import huaiye.com.vim.ui.home.adapter.ContactsDomainViewOrgHolder;
import ttyy.com.recyclerexts.base.EXTRecyclerAdapter;
import ttyy.com.recyclerexts.base.EXTViewHolder;

import static huaiye.com.vim.ui.contacts.sharedata.ChoosedContactsNew.selectedDept;


/**
 * Created by ywt on 2019/3/21.
 */
@BindLayout(R.layout.activity_group_dept_list_org)
public class DeptListOrgActivity extends AppBaseActivity {

    @BindView(R.id.et_key)
    TextView et_key;

    @BindView(R.id.rct_view_dept)
    RecyclerView rct_view_dept;
    @BindView(R.id.iv_empty_view)
    View iv_empty_view;
    @BindView(R.id.refresh_view)
    SwipeRefreshLayout refresh_view;

    @BindView(R.id.ll_choosed_persons)
    LinearLayout llChoosedPersons;
    @BindView(R.id.rct_choosed)
    RecyclerView rct_choosed;
    @BindView(R.id.tv_choose_confirm)
    TextView tv_choose_confirm;

    @BindExtra
    int max;
    @BindExtra
    boolean isZhuanFa;

    EXTRecyclerAdapter<SelectedModeBean> mChoosedAdapter;
    LiteBaseAdapter<DomainInfoList.DomainInfo> adapterDomain;
    ArrayList<DomainInfoList.DomainInfo> domainData = new ArrayList<>();//部门

    @Override
    protected void initActionBar() {
        EventBus.getDefault().register(this);
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

    @Override
    protected void onResume() {
        super.onResume();
        onEvent(new EventUserSelected());
    }

    private void initView() {
        adapterDomain = new LiteBaseAdapter<>(this,
                domainData,
                ContactsDomainViewOrgHolder.class,
                R.layout.item_contacts_domain_view,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                }, "false");
        ContactsDomainViewOrgHolder.mIsChoice = true;
        ContactsDomainViewOrgHolder.isZhuanFa = isZhuanFa;
        ContactsDomainViewOrgHolder.max = max;

        rct_view_dept.setLayoutManager(new LinearLayoutManager(this));
        rct_view_dept.setAdapter(adapterDomain);

        rct_choosed.setLayoutManager(new SafeLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mChoosedAdapter = new EXTRecyclerAdapter<SelectedModeBean>(R.layout.item_contacts_person_choosed) {
            @Override
            public void onBindViewHolder(EXTViewHolder extViewHolder, int i, SelectedModeBean contactData) {
                extViewHolder.setText(R.id.tv_contact_name, contactData.strName);
            }
        };
        mChoosedAdapter.setDatas(ChoosedContactsNew.get().getSelectedMode());
        mChoosedAdapter.setOnItemClickListener(new EXTRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClicked(View view, int i) {
                if (mChoosedAdapter.getDatas().get(i).strId.equals(AppDatas.Auth().getUserID())) {
                    return;
                }

                selectedDept.remove(mChoosedAdapter.getDatas().get(i).strId);
                rct_view_dept.setAdapter(adapterDomain);

                ChoosedContactsNew.get().remove(mChoosedAdapter.getDatas().get(i));
                mChoosedAdapter.notifyDataSetChanged();
                changeShowSelected();
            }
        });
        rct_choosed.setAdapter(mChoosedAdapter);

        refresh_view.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestDept();
                FragmentContacts.requestDeptAll();
            }
        });

        requestDept();
        FragmentContacts.requestDeptAll();

        changeShowSelected();
    }

    private void requestDept() {
        if (null != VIMApp.getInstance().mDomainInfoList && VIMApp.getInstance().mDomainInfoList.size() > 0) {
            domainData.clear();
            domainData.addAll(VIMApp.getInstance().mDomainInfoList);
            adapterDomain.notifyDataSetChanged();
            refresh_view.setRefreshing(false);
        }
    }

    @OnClick({R.id.ll_search, R.id.et_key})
    public void onClickSearch(View view) {
        Intent intent = new Intent(this, SearchDeptUserListOrgActivity.class);
        intent.putExtra("max", max);
        intent.putExtra("isZhuanFa", isZhuanFa);
        startActivity(intent);
    }

    @OnClick(R.id.tv_choose_confirm)
    public void onClick(View view) {
        EventBus.getDefault().post(new EventUserSelectedComplete());
        finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventUserSelected bean) {
        if (mChoosedAdapter != null) {
            mChoosedAdapter.notifyDataSetChanged();
        }
        changeShowSelected();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventUserSelectedComplete bean) {
        finish();
    }

    private void changeShowSelected() {
        if (ChoosedContactsNew.get().getContactsSize() == 0 &&
                ChoosedContactsNew.get().getGroups().isEmpty() &&
                ChoosedContactsNew.get().getDepts().isEmpty()) {
            llChoosedPersons.setVisibility(View.GONE);
            tv_choose_confirm.setText("确定(0)");
        } else {
            llChoosedPersons.setVisibility(View.VISIBLE);
            tv_choose_confirm.setText("确定(" + ChoosedContactsNew.get().getSelectedModeSize() + ")");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

}
