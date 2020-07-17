package huaiye.com.vim.ui.contacts;

import android.content.Intent;
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
import android.widget.ImageView;
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
import huaiye.com.vim.dao.auth.AppAuth;
import huaiye.com.vim.dao.msgs.User;
import huaiye.com.vim.models.ModelApis;
import huaiye.com.vim.models.ModelCallback;
import huaiye.com.vim.models.contacts.bean.ContactOrganizationBean;
import huaiye.com.vim.models.contacts.bean.ContactsBean;
import huaiye.com.vim.models.contacts.bean.ContactsGroupChatListBean;
import huaiye.com.vim.models.contacts.bean.ContactsGroupUserListBean;
import huaiye.com.vim.models.contacts.bean.DeptData;
import huaiye.com.vim.models.contacts.bean.DomainInfoList;
import huaiye.com.vim.models.contacts.bean.GroupInfo;
import huaiye.com.vim.models.contacts.bean.SelectedModeBean;
import huaiye.com.vim.ui.contacts.sharedata.ChoosedContactsNew;
import huaiye.com.vim.ui.contacts.viewholder.DepeItemViewOrgHolder;
import huaiye.com.vim.ui.contacts.viewholder.DeptUserItemViewOrgHolder;
import huaiye.com.vim.ui.contacts.viewholder.GroupInfoViewOrgHolder;
import huaiye.com.vim.ui.home.FragmentContacts;
import ttyy.com.jinnetwork.core.work.HTTPResponse;
import ttyy.com.recyclerexts.base.EXTRecyclerAdapter;
import ttyy.com.recyclerexts.base.EXTViewHolder;

import static huaiye.com.vim.ui.contacts.sharedata.ChoosedContactsNew.atData;
import static huaiye.com.vim.ui.contacts.sharedata.ChoosedContactsNew.selectedDept;
import static huaiye.com.vim.ui.contacts.sharedata.ChoosedContactsNew.userDeptMap;
import static huaiye.com.vim.ui.contacts.sharedata.ChoosedContactsNew.userGroupMap;


/**
 * Created by ywt on 2019/3/21.
 */
@BindLayout(R.layout.activity_search_dept_user_list_org)
public class SearchDeptUserListOrgActivity extends AppBaseActivity {

    @BindView(R.id.et_key)
    EditText et_key;
    @BindView(R.id.iv_selected_all)
    ImageView iv_selected_all;
    @BindView(R.id.ll_selected_all)
    View ll_selected_all;

    @BindView(R.id.tv_dept_title)
    View tv_dept_title;
    @BindView(R.id.tv_user_title)
    View tv_user_title;
    @BindView(R.id.tv_group_title)
    View tv_group_title;
    @BindView(R.id.rct_view_dept)
    RecyclerView rct_view_dept;
    @BindView(R.id.rct_view_user)
    RecyclerView rct_view_user;
    @BindView(R.id.rct_view_group)
    RecyclerView rct_view_group;
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

    boolean tagSelected = false;

    LiteBaseAdapter<DeptData> deptAdapter;
    ArrayList<DeptData> deptDatas = new ArrayList<>();
    LiteBaseAdapter<User> userAdapter;
    ArrayList<User> userInfos = new ArrayList<>();
    LiteBaseAdapter<GroupInfo> groupAdapter;
    ArrayList<GroupInfo> groupInfos = new ArrayList<>();
    EXTRecyclerAdapter<SelectedModeBean> mChoosedAdapter;

    @BindExtra
    int max;
    @BindExtra
    boolean isZhuanFa;

    ArrayList<String> selectedMap = new ArrayList<>();

    int requestUser = 0;
    int requestGroup = 0;
    int requestDept = 0;

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
        rct_view_group.setNestedScrollingEnabled(false);

        deptAdapter = new LiteBaseAdapter<>(this,
                deptDatas,
                DepeItemViewOrgHolder.class,
                R.layout.item_dept_org_view,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DeptData deptData = (DeptData) v.getTag();
                        if (v.getId() == R.id.tv_next) {
                            jumpToNext(deptData);
                        } else {
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
                            handleChoice(deptData);
                        }
                    }
                }, "false");
        DepeItemViewOrgHolder.mIsChoice = true;
        rct_view_dept.setLayoutManager(new SafeLinearLayoutManager(this));
        rct_view_dept.setAdapter(deptAdapter);

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

        groupAdapter = new LiteBaseAdapter<>(this,
                groupInfos,
                GroupInfoViewOrgHolder.class,
                R.layout.item_contacts_person_org,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GroupInfo groupInfo = (GroupInfo) v.getTag();
                        if (v.getId() == R.id.tv_next) {
                            Intent intent = new Intent(SearchDeptUserListOrgActivity.this, GroupUserListOrgActivity.class);
                            intent.putExtra("max", max);
                            intent.putExtra("groupInfo", groupInfo);
                            intent.putExtra("isZhuanFa", isZhuanFa);
                            intent.putExtra("isHide", true);
                            startActivity(intent);
                        } else {
                            handleChoice(groupInfo);
                        }
                    }
                }, "false");
        GroupInfoViewOrgHolder.mIsChoice = true;
        rct_view_group.setAdapter(groupAdapter);
        rct_view_group.setLayoutManager(new SafeLinearLayoutManager(this));

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
                if (isZhuanFa) {
                    if (mChoosedAdapter.getDatas().get(i).isUser()) {
                        boolean isDel = false;
                        for (User item : userInfos) {
                            if (mChoosedAdapter.getDatas().get(i).strId.equals(item.strUserID)) {
                                handleChoice(item);
                                isDel = true;
                                break;
                            }
                        }
                        if (!isDel) {
                            ChoosedContactsNew.get().remove(mChoosedAdapter.getDatas().get(i));
                        }
                    } else if (mChoosedAdapter.getDatas().get(i).isGroup()) {
                        ChoosedContactsNew.get().remove(mChoosedAdapter.getDatas().get(i));
                    } else {
                        ChoosedContactsNew.get().remove(mChoosedAdapter.getDatas().get(i));
                    }
                } else {
                    boolean isDel = false;
                    for (User item : userInfos) {
                        if (mChoosedAdapter.getDatas().get(i).strId.equals(item.strUserID)) {
                            handleChoice(item);
                            isDel = true;
                            break;
                        }
                    }
                    if (!isDel) {
                        ChoosedContactsNew.get().remove(mChoosedAdapter.getDatas().get(i));
                    }
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
                requestDept();
                requestGroupContacts();
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
                requestDept();
                requestGroupContacts();
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
                    requestDept();
                    requestGroupContacts();
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

        if (requestUser != 0) {
            return;
        }

        userInfos.clear();
        if (null != VIMApp.getInstance().mDomainInfoList && VIMApp.getInstance().mDomainInfoList.size() > 0) {
            for (DomainInfoList.DomainInfo temp : VIMApp.getInstance().mDomainInfoList) {
                requestUser++;
                ModelApis.Contacts().requestContactsByKey(temp.strDomainCode, null, new ModelCallback<ContactsBean>() {
                    @Override
                    public void onSuccess(final ContactsBean contactsBean) {
                        if (null != contactsBean && null != contactsBean.userList && contactsBean.userList.size() > 0) {

                            for (User temp : contactsBean.userList) {
                                if (!TextUtils.isEmpty(temp.strUserName) &&
                                        temp.strUserName.contains(et_key.getText().toString())) {
                                    userInfos.add(temp);
                                } else if (!TextUtils.isEmpty(temp.strPostName) &&
                                        temp.strPostName.contains(et_key.getText().toString())) {
                                    userInfos.add(temp);
                                }
                            }

                            for (User user : userInfos) {
                                if (ChoosedContactsNew.get().isContain(user)) {
                                    user.isSelected = true;
                                }
                            }

                            if (userInfos.isEmpty()) {
                                ll_selected_all.setVisibility(View.GONE);
                            } else {
                                ll_selected_all.setVisibility(View.VISIBLE);
                            }

                            changeShowSelected();

                            refresh_view.setRefreshing(false);

                            requestUser--;
                        }
                    }

                    @Override
                    public void onFinish(HTTPResponse httpResponse) {
                        super.onFinish(httpResponse);
                        refresh_view.setRefreshing(false);
                    }

                    @Override
                    public void onFailure(HTTPResponse httpResponse) {
                        super.onFailure(httpResponse);
                        requestUser--;
                    }
                });
            }
        }
    }

    private void requestDept() {
        if (TextUtils.isEmpty(et_key.getText().toString())) {
            return;
        }

        if (requestDept != 0) {
            return;
        }

        deptDatas.clear();
        if (null != VIMApp.getInstance().mDomainInfoList && VIMApp.getInstance().mDomainInfoList.size() > 0) {
            for (DomainInfoList.DomainInfo info : VIMApp.getInstance().mDomainInfoList) {
                requestDept++;
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

                        if (deptDatas.isEmpty()) {
                            tv_dept_title.setVisibility(View.GONE);
                        } else {
                            tv_dept_title.setVisibility(View.VISIBLE);
                        }

                        changeShowSelected();
                        refresh_view.setRefreshing(false);

                        requestDept--;
                    }

                    @Override
                    public void onFinish(HTTPResponse httpResponse) {
                        super.onFinish(httpResponse);
                        refresh_view.setRefreshing(false);
                    }

                    @Override
                    public void onFailure(HTTPResponse httpResponse) {
                        super.onFailure(httpResponse);
                        requestDept--;
                    }
                });
            }
        }
    }

    private void requestGroupContacts() {
        if (TextUtils.isEmpty(et_key.getText().toString())) {
            return;
        }

        if (requestGroup != 0) {
            return;
        }

        groupInfos.clear();
        if (null != VIMApp.getInstance().mDomainInfoList && VIMApp.getInstance().mDomainInfoList.size() > 0) {
            for (DomainInfoList.DomainInfo domainInfo : VIMApp.getInstance().mDomainInfoList) {
                requestGroup++;
                ModelApis.Contacts().requestGroupBuddyContacts(-1, 0, 0, et_key.getText().toString(), domainInfo.strDomainCode, new ModelCallback<ContactsGroupChatListBean>() {
                    @Override
                    public void onSuccess(final ContactsGroupChatListBean contactsBean) {
                        if (contactsBean != null && contactsBean.lstGroupInfo != null) {
                            groupInfos.addAll(contactsBean.lstGroupInfo);
                        }

                        if (groupInfos.isEmpty()) {
                            tv_group_title.setVisibility(View.GONE);
                        } else {
                            tv_group_title.setVisibility(View.VISIBLE);
                        }

                        changeShowSelected();
                        refresh_view.setRefreshing(false);

                        requestGroup--;
                    }

                    @Override
                    public void onFinish(HTTPResponse httpResponse) {
                        super.onFinish(httpResponse);
                        refresh_view.setRefreshing(false);
                    }

                    @Override
                    public void onFailure(HTTPResponse httpResponse) {
                        super.onFailure(httpResponse);
                        requestGroup--;
                    }
                });
            }
        } else {
            refresh_view.setRefreshing(false);
            VIMApp.getInstance().getDomainCodeList();
        }
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
    }

    private void handleChoice(final DeptData deptData) {
        ModelApis.Contacts().requestContactsOnly(deptData.strDomainCode, deptData.strDepID, 1, new ModelCallback<ContactsBean>() {
            @Override
            public void onSuccess(final ContactsBean contactsBean) {
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

                if (!isZhuanFa) {
                    if (null != contactsBean && null != contactsBean.userList && contactsBean.userList.size() > 0) {
                        for (User user : contactsBean.userList) {
                            if (!user.strUserID.equals(AppAuth.get().getUserID())) {
                                if (deptData.isSelected) {
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
                    }
                }

                changeShowSelected();

            }
        });
    }

    private void handleChoice(GroupInfo groupInfo) {
        ModelApis.Contacts().requestqueryGroupChatInfo(groupInfo.strGroupDomainCode, groupInfo.strGroupID, new ModelCallback<ContactsGroupUserListBean>() {
            @Override
            public void onSuccess(final ContactsGroupUserListBean contactsBean) {
                groupInfo.isSelected = !groupInfo.isSelected;
                if (groupInfo.isSelected) {
                    ChoosedContactsNew.get().add(groupInfo);
                    if (!selectedMap.contains(groupInfo.strGroupID)) {
                        selectedMap.add(groupInfo.strGroupID);
                    }
                } else {
                    ChoosedContactsNew.get().remove(groupInfo);
                    if (selectedMap.contains(groupInfo.strGroupID)) {
                        selectedMap.remove(groupInfo.strGroupID);
                    }
                }
                if (!isZhuanFa) {
                    if (contactsBean != null && contactsBean.lstGroupUser != null && contactsBean.lstGroupUser.size() > 0) {
                        for (User user : contactsBean.lstGroupUser) {
                            if (!user.strUserID.equals(AppAuth.get().getUserID())) {

                                if (groupInfo.isSelected) {
                                    if (!ChoosedContactsNew.get().isContain(user)) {
                                        if (ChoosedContactsNew.get().getContacts().size() >= max + 1) {
                                            showToast("最多选" + max + "人，已达人数上限");
                                            return;
                                        }
                                        userGroupMap.put(user.strUserID, groupInfo.strGroupID);
                                        user.isSelected = true;
                                        ChoosedContactsNew.get().add(user, false);
                                    }
                                } else {
                                    if (ChoosedContactsNew.get().isContain(user)) {
                                        user.isSelected = false;
                                        ChoosedContactsNew.get().remove(user);

                                        if (userGroupMap.containsKey(user.strUserID)) {
                                            userGroupMap.remove(user.strUserID);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                changeShowSelected();
            }
        });

    }

    private void changeShowSelected() {
        if (userAdapter != null) {
            userAdapter.notifyDataSetChanged();
        }
        if (mChoosedAdapter != null) {
            mChoosedAdapter.notifyDataSetChanged();
        }
        if (deptAdapter != null) {
            deptAdapter.notifyDataSetChanged();
        }
        if (groupAdapter != null) {
            groupAdapter.notifyDataSetChanged();
        }

        boolean hasNoSelected = false;

        for (User user : userInfos) {
            if (!user.isSelected) {
                hasNoSelected = true;
                break;
            }
        }

        if (hasNoSelected) {
            tagSelected = false;
            iv_selected_all.setImageResource(R.drawable.ic_choice);
        }

        if (ChoosedContactsNew.get().getContactsSize() == 0 &&
                ChoosedContactsNew.get().getGroups().isEmpty() &&
                ChoosedContactsNew.get().getDepts().isEmpty()) {
            llChoosedPersons.setVisibility(View.GONE);
            tv_choose_confirm.setText("确定(0)");
        } else {
            llChoosedPersons.setVisibility(View.VISIBLE);
            tv_choose_confirm.setText("确定(" + ChoosedContactsNew.get().getShowTotalSize() + ")");
        }
    }

    private void jumpToNext(DeptData deptData) {
        ArrayList<String> titleName = new ArrayList<>();
        titleName.add(deptData.domainInfo.strDomainName);
        titleName.add(TextUtils.isEmpty(deptData.strName) ? deptData.strDepName : deptData.strName);
        Intent intent = new Intent(this, DeptDeepListOrgActivity.class);
        intent.putExtra("domainName", deptData.domainInfo.strDomainName);
        intent.putExtra("titleName", titleName);
        intent.putExtra("deptData", deptData);
        intent.putExtra("map", FragmentContacts.map);
        intent.putExtra("max", max);
        startActivity(intent);
    }

    @OnClick(R.id.tv_choose_confirm)
    public void onClick(View view) {
        EventBus.getDefault().post(new EventUserSelectedComplete());
        finish();
    }

    @OnClick(R.id.ll_selected_all)
    void selectedAll() {
        if (tagSelected) {
            tagSelected = false;
            iv_selected_all.setImageResource(R.drawable.ic_choice);
        } else {
            tagSelected = true;
            iv_selected_all.setImageResource(R.drawable.ic_choice_checked);
        }

        for (User user : userInfos) {
            if (tagSelected) {
                user.isSelected = true;
                if (!ChoosedContactsNew.get().isContain(user)) {
                    ChoosedContactsNew.get().add(user, true);
                }
            } else {
                user.isSelected = false;
                if (ChoosedContactsNew.get().isContain(user)) {
                    ChoosedContactsNew.get().remove(user);
                }
            }
        }

        changeShowSelected();
    }

}
