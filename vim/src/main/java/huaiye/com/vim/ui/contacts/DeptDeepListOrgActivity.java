package huaiye.com.vim.ui.contacts;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

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
import huaiye.com.vim.models.contacts.bean.ContactsBean;
import huaiye.com.vim.models.contacts.bean.DeptData;
import huaiye.com.vim.models.contacts.bean.SelectedModeBean;
import huaiye.com.vim.ui.contacts.sharedata.ChoosedContactsNew;
import huaiye.com.vim.ui.contacts.viewholder.DepeItemViewOrgHolder;
import huaiye.com.vim.ui.contacts.viewholder.DepeTopViewHolder;
import huaiye.com.vim.ui.contacts.viewholder.DeptUserItemViewOrgHolder;
import ttyy.com.jinnetwork.core.work.HTTPResponse;
import ttyy.com.recyclerexts.base.EXTRecyclerAdapter;
import ttyy.com.recyclerexts.base.EXTViewHolder;

import static huaiye.com.vim.ui.contacts.sharedata.ChoosedContactsNew.atData;
import static huaiye.com.vim.ui.contacts.sharedata.ChoosedContactsNew.selectedDept;
import static huaiye.com.vim.ui.contacts.sharedata.ChoosedContactsNew.userDeptMap;


/**
 * Created by ywt on 2019/3/21.
 */
@BindLayout(R.layout.activity_group_dept_deep_list_org)
public class DeptDeepListOrgActivity extends AppBaseActivity {

    @BindView(R.id.ll_search)
    View ll_search;
    @BindView(R.id.ll_selected_all)
    View ll_selected_all;
    @BindView(R.id.iv_selected_all)
    ImageView iv_selected_all;

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

    @BindView(R.id.ll_choosed_persons)
    LinearLayout llChoosedPersons;
    @BindView(R.id.rct_choosed)
    RecyclerView rct_choosed;
    @BindView(R.id.tv_choose_confirm)
    TextView tv_choose_confirm;

    boolean tagSelected = false;

    @BindExtra
    boolean isHide;
    @BindExtra
    String domainName;
    @BindExtra
    ArrayList<String> titleName;
    @BindExtra
    DeptData deptData;
    @BindExtra
    int max;
    @BindExtra
    boolean isZhuanFa;
    @BindExtra
    HashMap<String, ArrayList<DeptData>> map;

    ArrayList<User> allUserInfos = new ArrayList<>();
    ArrayList<DeptData> allDeptDatas = new ArrayList<>();
    EXTRecyclerAdapter<SelectedModeBean> mChoosedAdapter;

    LiteBaseAdapter<String> strAdapter;
    ArrayList<String> strDatas = new ArrayList<>();
    LiteBaseAdapter<DeptData> deptAdapter;
    ArrayList<DeptData> deptDatas = new ArrayList<>();
    LiteBaseAdapter<User> userAdapter;
    ArrayList<User> userInfos = new ArrayList<>();

    static int totalRequestNum = 0;
    public static HashMap<String, String> allNum = new HashMap<>();

    @Override
    protected void initActionBar() {
        initNavigateView();
    }

    private void initNavigateView() {
        EventBus.getDefault().register(this);
        getNavigate().setVisibility(View.VISIBLE);
        getNavigate().setTitlText(domainName)
                .setLeftClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });

        if (isHide) {
            ll_search.setVisibility(View.GONE);
        } else {
            ll_search.setVisibility(View.VISIBLE);
        }
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
                DepeItemViewOrgHolder.class,
                R.layout.item_dept_org_view,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DeptData deptData = (DeptData) v.getTag();
                        if (v.getId() == R.id.tv_next) {
                            jumpToNext(deptData);
                        } else {
                            handleChoice(deptData);
                        }

                    }
                }, "false");
        DepeItemViewOrgHolder.mIsChoice = true;

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

        rct_view_item.setLayoutManager(new SafeLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rct_view_item.setAdapter(strAdapter);

        rct_view_dept.setLayoutManager(new SafeLinearLayoutManager(this));
        rct_view_dept.setAdapter(deptAdapter);

        rct_view_user.setLayoutManager(new SafeLinearLayoutManager(this));
        rct_view_user.setAdapter(userAdapter);

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
                if(isZhuanFa) {
                    if(mChoosedAdapter.getDatas().get(i).isUser()) {
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
                    } else if(mChoosedAdapter.getDatas().get(i).isGroup()) {
                        ChoosedContactsNew.get().remove(mChoosedAdapter.getDatas().get(i));
                    } else {
                        for (DeptData item : atData) {
                            if(mChoosedAdapter.getDatas().get(i).strId.equals(item.strDepID)) {
                                handleChoice(item);
                                break;
                            }
                        }
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
                mChoosedAdapter.notifyDataSetChanged();
                changeShowSelected();
            }
        });
        rct_choosed.setAdapter(mChoosedAdapter);

        refresh_view.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestUser();
            }
        });

        requestUser();
        requestDept();

        changeShowSelected();
    }

    private void requestUser() {
//        if (deptData.nDepType == 0 || deptData.nDepType == 1) {
//            return;
//        }
        ModelApis.Contacts().requestContactsOnly(deptData.strDomainCode, deptData.strDepID, 1, new ModelCallback<ContactsBean>() {
            @Override
            public void onSuccess(final ContactsBean contactsBean) {
                if (null != contactsBean && null != contactsBean.userList && contactsBean.userList.size() > 0) {
                    allUserInfos.clear();
                    for (User temp : contactsBean.userList) {
                        if (ChoosedContactsNew.get().isContain(temp)) {
                            temp.isSelected = true;
                        }
                        for (DeptData dept : temp.getUserDept()) {
                            if (dept.strDepID.equals(deptData.strDepID)) {
                                allUserInfos.add(temp);
                                break;
                            }
                        }
                        temp.strDomainCode = deptData.strDomainCode;
                    }
                    showData("");
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
        if (map == null) {
            return;
        }
        allDeptDatas.clear();
        if (map.get(deptData.strDepID) != null) {
            allDeptDatas.addAll(map.get(deptData.strDepID));
        }
//        requestNum();
        showData("");
    }

    private void requestNum() {
        for (DeptData temp : allDeptDatas) {
            totalRequestNum++;
            ModelApis.Contacts().requestContacts(temp.strDomainCode, temp.strDepID, new ModelCallback<ContactsBean>() {
                @Override
                public void onSuccess(final ContactsBean contactsBean) {
                    totalRequestNum--;
                    allNum.put(temp.strDepID, contactsBean.nTotalSize + "");
                    deptAdapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(HTTPResponse httpResponse) {
                    super.onFailure(httpResponse);
                    totalRequestNum--;
                    allNum.put(temp.strDepID, "0");
                    deptAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void jumpToNext(DeptData deptData) {
        ArrayList<String> titleName = new ArrayList<>();
//        titleName.add(domainName);
        titleName.addAll(strDatas);
        titleName.add(TextUtils.isEmpty(deptData.strName) ? deptData.strDepName : deptData.strName);
        Intent intent = new Intent(this, DeptDeepListOrgActivity.class);
        intent.putExtra("domainName", domainName);
        intent.putExtra("titleName", titleName);
        intent.putExtra("deptData", deptData);
        intent.putExtra("map", map);
        intent.putExtra("isHide", isHide);
        intent.putExtra("max", max);
        startActivity(intent);
    }

    private void showData(String str) {
        userInfos.clear();
        deptDatas.clear();
        if (TextUtils.isEmpty(str)) {
            userInfos.addAll(allUserInfos);

            for (DeptData temp : allDeptDatas) {
                boolean canAdd = true;
                for (DeptData dept : deptDatas) {
                    if (dept.strDepID.equals(temp.strDepID)) {
                        canAdd = false;
                        break;
                    }
                }
                if (canAdd) {
                    deptDatas.add(temp);
                }
            }
        } else {
            for (User temp : allUserInfos) {
                if (temp.strUserName.contains(str) ||
                        temp.strLoginName.contains(str)) {
                    userInfos.add(temp);
                }
            }
            for (DeptData temp : allDeptDatas) {
                if ((TextUtils.isEmpty(deptData.strName) ? deptData.strDepName : deptData.strName).contains(str)) {
                    boolean canAdd = true;
                    for (DeptData dept : deptDatas) {
                        if (dept.strDepID.equals(temp.strDepID)) {
                            canAdd = false;
                            break;
                        }
                    }
                    if (canAdd) {
                        deptDatas.add(temp);
                    }
                }
            }
        }
        Collections.sort(deptDatas, new Comparator<DeptData>() {
            @Override
            public int compare(DeptData o1, DeptData o2) {
                return o1.nPpriority - o2.nPpriority;
            }
        });
        Collections.sort(userInfos, new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return o1.nPriority - o2.nPriority;
            }
        });

        if(userInfos.isEmpty()) {
            ll_selected_all.setVisibility(View.GONE);
        } else {
            ll_selected_all.setVisibility(View.VISIBLE);
        }
        userAdapter.notifyDataSetChanged();
        if (!deptDatas.isEmpty()) {
            deptAdapter.notifyDataSetChanged();
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
        mChoosedAdapter.notifyDataSetChanged();
        userAdapter.notifyDataSetChanged();
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

                mChoosedAdapter.notifyDataSetChanged();
                deptAdapter.notifyDataSetChanged();
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

    @OnClick(R.id.tv_user_manager)
    public void onClick(View view) {
        Intent intent = new Intent(this, DomainUserListActivity.class);
        intent.putExtra("deptData", deptData);
        startActivity(intent);
    }

    @OnClick({R.id.ll_search, R.id.et_key})
    public void onClickSearch(View view) {
        Intent intent = new Intent(this, SearchDeptUserListOrgActivity.class);
        intent.putExtra("max", max);
        startActivity(intent);
    }

    @OnClick(R.id.tv_choose_confirm)
    public void onClickView(View view) {
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
            if(tagSelected) {
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventUserSelectedComplete bean) {
        finish();
    }

}
