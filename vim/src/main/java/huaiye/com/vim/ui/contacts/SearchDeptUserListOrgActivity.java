package huaiye.com.vim.ui.contacts;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ttyy.commonanno.anno.BindLayout;
import com.ttyy.commonanno.anno.BindView;
import com.ttyy.commonanno.anno.OnClick;
import com.ttyy.commonanno.anno.route.BindExtra;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import huaiye.com.vim.R;
import huaiye.com.vim.VIMApp;
import huaiye.com.vim.bus.EventUserSelectedComplete;
import huaiye.com.vim.common.AppBaseActivity;
import huaiye.com.vim.common.recycle.LiteBaseAdapter;
import huaiye.com.vim.common.recycle.SafeLinearLayoutManager;
import huaiye.com.vim.dao.AppDatas;
import huaiye.com.vim.dao.msgs.User;
import huaiye.com.vim.models.ModelApis;
import huaiye.com.vim.models.ModelCallback;
import huaiye.com.vim.models.contacts.bean.ContactsBean;
import huaiye.com.vim.models.contacts.bean.DomainInfoList;
import huaiye.com.vim.ui.contacts.sharedata.ChoosedContactsNew;
import huaiye.com.vim.ui.contacts.viewholder.DeptUserItemViewOrgHolder;
import huaiye.com.vim.ui.home.FragmentContacts;
import ttyy.com.jinnetwork.core.work.HTTPResponse;
import ttyy.com.recyclerexts.base.EXTRecyclerAdapter;
import ttyy.com.recyclerexts.base.EXTViewHolder;


/**
 * Created by ywt on 2019/3/21.
 */
@BindLayout(R.layout.activity_search_dept_user_list_org)
public class SearchDeptUserListOrgActivity extends AppBaseActivity {

    @BindView(R.id.et_key)
    EditText et_key;

    @BindView(R.id.rct_view_user)
    RecyclerView rct_view_user;
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

    LiteBaseAdapter<User> userAdapter;
    ArrayList<User> userInfos = new ArrayList<>();
    EXTRecyclerAdapter<User> mChoosedAdapter;

    @BindExtra
    int max;

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
        userAdapter = new LiteBaseAdapter<>(this,
                userInfos,
                DeptUserItemViewOrgHolder.class,
                R.layout.item_dept_person_org,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        User user = (User) v.getTag();
                        handleChoice(user);
                        changeShowSelected();
                    }
                }, "false");
        DeptUserItemViewOrgHolder.mIsChoice = true;
        rct_view_user.setAdapter(userAdapter);
        rct_view_user.setLayoutManager(new SafeLinearLayoutManager(this));

        rct_choosed.setLayoutManager(new SafeLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mChoosedAdapter = new EXTRecyclerAdapter<User>(R.layout.item_contacts_person_choosed) {
            @Override
            public void onBindViewHolder(EXTViewHolder extViewHolder, int i, User contactData) {
                if (contactData.nJoinStatus != 2) {
                    extViewHolder.setText(R.id.tv_contact_name, contactData.strUserName);
                } else {
                    extViewHolder.setVisibility(R.id.tv_contact_name, View.GONE);
                }
            }
        };
        mChoosedAdapter.setDatas(ChoosedContactsNew.get().getContacts());
        mChoosedAdapter.setOnItemClickListener(new EXTRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClicked(View view, int i) {
                if (mChoosedAdapter.getDatas().get(i).strUserID.equals(AppDatas.Auth().getUserID())) {
                    return;
                }
                boolean isDel = false;
                for (User item : userInfos) {
                    if (mChoosedAdapter.getDatas().get(i).strUserID.equals(item.strUserID)) {
                        handleChoice(item);
                        isDel = true;
                        break;
                    }
                }
                if(!isDel) {
                    ChoosedContactsNew.get().removeContacts(mChoosedAdapter.getDatas().get(i));
                    mChoosedAdapter.notifyDataSetChanged();
                }
                changeShowSelected();
            }
        });
        rct_choosed.setAdapter(mChoosedAdapter);

        refresh_view.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                FragmentContacts.requestDeptAll();
                requestUser();
            }
        });
        et_key.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                FragmentContacts.requestDeptAll();
                requestUser();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        et_key.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    FragmentContacts.requestDeptAll();
                    requestUser();
                    return true;
                }
                return false;
            }
        });

        FragmentContacts.requestDeptAll();

        changeShowSelected();
    }

    private void requestUser() {
        if (TextUtils.isEmpty(et_key.getText().toString())) {
            return;
        }
        userInfos.clear();
        if (null != VIMApp.getInstance().mDomainInfoList && VIMApp.getInstance().mDomainInfoList.size() > 0) {
            for (DomainInfoList.DomainInfo temp : VIMApp.getInstance().mDomainInfoList) {
                ModelApis.Contacts().requestContactsByKey(temp.strDomainCode, et_key.getText().toString(), new ModelCallback<ContactsBean>() {
                    @Override
                    public void onSuccess(final ContactsBean contactsBean) {
                        if (null != contactsBean && null != contactsBean.userList && contactsBean.userList.size() > 0) {
                            userInfos.addAll(contactsBean.userList);
                            for (User user : userInfos) {
                                if (ChoosedContactsNew.get().isContain(user)) {
                                    user.isSelected = true;
                                }
                            }
                            userAdapter.notifyDataSetChanged();
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

    private void handleChoice(User user) {
        if (user == null) {
            return;
        }
        if (ChoosedContactsNew.get().isContain(user)) {
            user.isSelected = false;
            ChoosedContactsNew.get().removeContacts(user);
        } else {
            if (ChoosedContactsNew.get().getContacts().size() >= max + 1) {
                showToast("最多选" + max + "人，已达人数上限");
                return;
            }
            user.isSelected = true;
            ChoosedContactsNew.get().addContacts(user);
        }
        mChoosedAdapter.notifyDataSetChanged();
        userAdapter.notifyDataSetChanged();
    }

    private void changeShowSelected() {
        if (ChoosedContactsNew.get().getContacts().isEmpty()) {
            llChoosedPersons.setVisibility(View.GONE);
            tv_choose_confirm.setText("确定(0)");
        } else {
            llChoosedPersons.setVisibility(View.VISIBLE);
            tv_choose_confirm.setText("确定("+ChoosedContactsNew.get().getContacts().size()+")");
        }
    }

    @OnClick(R.id.tv_choose_confirm)
    public void onClick(View view) {
        EventBus.getDefault().post(new EventUserSelectedComplete());
        finish();
    }

}
