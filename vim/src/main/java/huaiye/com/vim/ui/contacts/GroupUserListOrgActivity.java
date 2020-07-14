package huaiye.com.vim.ui.contacts;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
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
import huaiye.com.vim.bus.EventUserSelectedComplete;
import huaiye.com.vim.common.AppBaseActivity;
import huaiye.com.vim.common.recycle.LiteBaseAdapter;
import huaiye.com.vim.common.recycle.SafeLinearLayoutManager;
import huaiye.com.vim.dao.AppDatas;
import huaiye.com.vim.dao.auth.AppAuth;
import huaiye.com.vim.dao.msgs.User;
import huaiye.com.vim.models.ModelApis;
import huaiye.com.vim.models.ModelCallback;
import huaiye.com.vim.models.contacts.bean.ContactsGroupUserListBean;
import huaiye.com.vim.models.contacts.bean.GroupInfo;
import huaiye.com.vim.models.contacts.bean.SelectedModeBean;
import huaiye.com.vim.ui.contacts.sharedata.ChoosedContactsNew;
import huaiye.com.vim.ui.contacts.viewholder.GroupUserItemViewOrgHolder;
import ttyy.com.jinnetwork.core.work.HTTPResponse;
import ttyy.com.recyclerexts.base.EXTRecyclerAdapter;
import ttyy.com.recyclerexts.base.EXTViewHolder;


/**
 * Created by ywt on 2019/3/21.
 */
@BindLayout(R.layout.activity_group_user_list_org)
public class GroupUserListOrgActivity extends AppBaseActivity {

    @BindView(R.id.et_key)
    TextView et_key;
    @BindView(R.id.close)
    ImageView close;

    @BindView(R.id.rct_view)
    RecyclerView rct_view;
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

    private ArrayList<User> mCustomContacts = new ArrayList<>();
    private ArrayList<User> mAllContacts = new ArrayList<>();//常用联系人
    LiteBaseAdapter<User> adapter;
    EXTRecyclerAdapter<SelectedModeBean> mChoosedAdapter;

    @BindExtra
    int max;
    @BindExtra
    boolean isZhuanFa;
    @BindExtra
    GroupInfo groupInfo;

    boolean isFreadList = false;
    boolean isRef = false;

    @Override
    protected void initActionBar() {
        initNavigateView();
    }

    private void initNavigateView() {
        EventBus.getDefault().register(this);
        getNavigate().setVisibility(View.VISIBLE);
        getNavigate().setTitlText("我的群组")
                .setLeftClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void doInitDelay() {
        initView();
    }

    private void initView() {
        adapter = new LiteBaseAdapter<>(this,
                mCustomContacts,
                GroupUserItemViewOrgHolder.class,
                R.layout.item_contacts_person_group_org,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        User user = (User) v.getTag();
                        handleChoice(user);
                    }
                }, "false");
        GroupUserItemViewOrgHolder.mIsChoice = true;

        rct_view.setAdapter(adapter);
        rct_view.setLayoutManager(new SafeLinearLayoutManager(this));

        refresh_view.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isFreadList) {
                } else {
                    requestGroupContacts();
                }
            }
        });

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

                boolean isDel = false;
                for (User item : mAllContacts) {
                    if (mChoosedAdapter.getDatas().get(i).strId.equals(item.strUserID)) {
                        handleChoice(item);
                        isDel = true;
                        break;
                    }
                }
                if(!isDel) {
                    ChoosedContactsNew.get().remove(mChoosedAdapter.getDatas().get(i));
                    mChoosedAdapter.notifyDataSetChanged();
                }
                changeShowSelected();
            }
        });
        rct_choosed.setAdapter(mChoosedAdapter);

        changeShowSelected();
    }

    private void handleChoice(User user) {
        if (user == null) {
            return;
        }
        if (ChoosedContactsNew.get().isContain(user)) {
            user.isSelected = false;
            ChoosedContactsNew.get().remove(user);
        } else {
            if (ChoosedContactsNew.get().getContacts().size() >= max + 1) {
                showToast("最多选" + max + "人，已达人数上限");
                return;
            }
            user.isSelected = true;

            ChoosedContactsNew.get().add(user, true);
        }
        changeShowSelected();
        mChoosedAdapter.notifyDataSetChanged();
        adapter.notifyDataSetChanged();
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
    protected void onResume() {
        super.onResume();
        requestGroupContacts();
    }

    private void requestGroupContacts() {
        if (isRef) {
            return;
        }
        isRef = true;

        mAllContacts.clear();
        ModelApis.Contacts().requestqueryGroupChatInfo(groupInfo.strGroupDomainCode, groupInfo.strGroupID, new ModelCallback<ContactsGroupUserListBean>() {
            @Override
            public void onSuccess(final ContactsGroupUserListBean contactsBean) {
                if (contactsBean != null && contactsBean.lstGroupUser != null && contactsBean.lstGroupUser.size() > 0) {
                    for (User user : contactsBean.lstGroupUser) {
                        if (!user.strUserID.equals(AppAuth.get().getUserID())) {
                            if (ChoosedContactsNew.get().isContain(user)) {
                                user.isSelected = true;
                            }
                            mAllContacts.add(user);
                        }
                    }
                }
                showData(et_key.getText().toString());
            }

            @Override
            public void onFinish(HTTPResponse httpResponse) {
                super.onFinish(httpResponse);
                refresh_view.setRefreshing(false);
            }
        });

    }

    private void showData(String str) {
        mCustomContacts.clear();
        for (User temp : mAllContacts) {
            if (TextUtils.isEmpty(str)) {
                mCustomContacts.add(temp);
            } else {
                if (temp.strUserName.contains(str)) {
                    mCustomContacts.add(temp);
                }
            }
        }

        if (!isFreadList) {
            adapter.notifyDataSetChanged();
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
    public void onEvent(EventUserSelectedComplete bean) {
        finish();
    }

}
