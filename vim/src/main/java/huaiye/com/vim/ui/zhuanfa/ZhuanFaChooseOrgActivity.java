package huaiye.com.vim.ui.zhuanfa;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.promeg.pinyinhelper.Pinyin;
import com.huaiye.sdk.sdpmsgs.social.SendUserBean;
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

import huaiye.com.vim.R;
import huaiye.com.vim.VIMApp;
import huaiye.com.vim.bus.CloseZhuanFa;
import huaiye.com.vim.bus.EventUserClick;
import huaiye.com.vim.bus.EventUserSelected;
import huaiye.com.vim.common.AppBaseActivity;
import huaiye.com.vim.common.AppUtils;
import huaiye.com.vim.common.recycle.LiteBaseAdapter;
import huaiye.com.vim.common.recycle.SafeLinearLayoutManager;
import huaiye.com.vim.dao.AppDatas;
import huaiye.com.vim.dao.auth.AppAuth;
import huaiye.com.vim.dao.msgs.ChangyongLianXiRenBean;
import huaiye.com.vim.dao.msgs.ChatMessageBase;
import huaiye.com.vim.dao.msgs.User;
import huaiye.com.vim.dao.msgs.UserInfo;
import huaiye.com.vim.models.ModelApis;
import huaiye.com.vim.models.ModelCallback;
import huaiye.com.vim.models.contacts.bean.ContactsBean;
import huaiye.com.vim.models.contacts.bean.DeptData;
import huaiye.com.vim.models.contacts.bean.DomainInfoList;
import huaiye.com.vim.models.contacts.bean.SelectedModeBean;
import huaiye.com.vim.ui.contacts.DeptDeepListOrgActivity;
import huaiye.com.vim.ui.contacts.DeptListOrgActivity;
import huaiye.com.vim.ui.contacts.GroupListOrgActivity;
import huaiye.com.vim.ui.contacts.SearchDeptUserListOrgActivity;
import huaiye.com.vim.ui.contacts.sharedata.ChoosedContactsNew;
import huaiye.com.vim.ui.contacts.viewholder.UserViewOrgHolder;
import huaiye.com.vim.ui.home.FragmentContacts;
import huaiye.com.vim.ui.home.adapter.ContactsDeptViewOrgHolder;
import ttyy.com.jinnetwork.core.work.HTTPResponse;
import ttyy.com.recyclerexts.base.EXTRecyclerAdapter;
import ttyy.com.recyclerexts.base.EXTViewHolder;

import static huaiye.com.vim.ui.contacts.sharedata.ChoosedContactsNew.atData;
import static huaiye.com.vim.ui.contacts.sharedata.ChoosedContactsNew.selectedDept;
import static huaiye.com.vim.ui.contacts.sharedata.ChoosedContactsNew.userDeptMap;

/**
 * author: admin
 * date: 2017/12/28
 * version: 0
 * mail: secret
 */
@BindLayout(R.layout.activity_zhuanfa_choose_org)
public class ZhuanFaChooseOrgActivity extends AppBaseActivity {

    @BindView(R.id.ll_root)
    View ll_root;

    @BindView(R.id.refresh_view)
    SwipeRefreshLayout refresh_view;
    @BindView(R.id.et_key)
    TextView et_key;
    @BindView(R.id.ll_selected_all)
    View ll_selected_all;
    @BindView(R.id.iv_selected_all)
    ImageView iv_selected_all;

    @BindView(R.id.ll_search)
    View ll_search;

    @BindView(R.id.rct_view_suozai)
    RecyclerView rct_view_suozai;
    @BindView(R.id.rct_view)
    RecyclerView rct_view;
    @BindView(R.id.tv_title)
    View tv_title;

    @BindView(R.id.tv_choose_confirm)
    TextView tv_choose_confirm;
    @BindView(R.id.ll_choosed_persons)
    LinearLayout llChoosedPersons;
    @BindView(R.id.rct_choosed)
    RecyclerView rct_choosed;

    LiteBaseAdapter<User> adapter;
    LiteBaseAdapter<DeptData> adapterAt;
    EXTRecyclerAdapter<SelectedModeBean> mChoosedAdapter;

    private ArrayList<User> mCustomContacts = new ArrayList<>();
    private ArrayList<User> mAllContacts = new ArrayList<>();//常用联系人

    boolean tagSelected = false;

    @BindExtra
    String strUserDomainCode;
    @BindExtra
    String strUserID;
    @BindExtra
    ChatMessageBase data;
    @BindExtra
    ArrayList<UserInfo> users;
    @BindExtra
    boolean isGroup;
    @BindExtra
    String strGroupID;
    @BindExtra
    String strGroupDomain;
    @BindExtra
    ArrayList<SendUserBean> mMessageUsersDate;

    boolean isSelectUser = true;
    boolean isZhuanFa = true;

    @Override
    protected void initActionBar() {
        EventBus.getDefault().register(this);

        ChoosedContactsNew.get().clear();

        getNavigate().setVisibility(View.VISIBLE);
        getNavigate().getRightTextView().setPadding(AppUtils.dp2px(this, 8f), AppUtils.dp2px(this, 4f), AppUtils.dp2px(this, 8f), AppUtils.dp2px(this, 4f));
        getNavigate().setTitlText("联系人")
                .setRightText("确定")
                .setLeftClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                })
                .setRightClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (ChoosedContactsNew.get().getGroups().isEmpty() &&
                                ChoosedContactsNew.get().getContactsSize() == 0 &&
                                ChoosedContactsNew.get().getDepts().isEmpty()) {
                            showToast("请选择发送对象");
                            return;
                        }


                        String header;
                        String name;
                        if (ChoosedContactsNew.get().getContactsSize() != 0) {
                            header = ChoosedContactsNew.get().getContacts().get(0).strHeadUrl;
                            name = ChoosedContactsNew.get().getContacts().get(0).strUserName;
                        } else if (!ChoosedContactsNew.get().getGroups().isEmpty()) {
                            header = ChoosedContactsNew.get().getGroups().get(0).strHeadUrl;
                            name = ChoosedContactsNew.get().getGroups().get(0).strGroupName;
                        } else {
                            header = "";
                            name = ChoosedContactsNew.get().getDepts().get(0).getName();
                        }
                        int total = ChoosedContactsNew.get().getContactsSize() + ChoosedContactsNew.get().getGroups().size() + ChoosedContactsNew.get().getDepts().size();

                        ZhuanFaGroupPopupWindowDuoFa zhuanFaGroupPopupWindow = new ZhuanFaGroupPopupWindowDuoFa(ZhuanFaChooseOrgActivity.this, users, strUserID, strUserDomainCode, isGroup, strGroupID, strGroupDomain);
                        zhuanFaGroupPopupWindow.setSendUser(ChoosedContactsNew.get().getGroups());
                        zhuanFaGroupPopupWindow.showData(ZhuanFaChooseOrgActivity.this.data, null);

                        ZhuanFaDeptPopupWindowDuoFa zhuanFaDeptPopupWindowDuoFa = new ZhuanFaDeptPopupWindowDuoFa(ZhuanFaChooseOrgActivity.this, users, strUserID, strUserDomainCode, isGroup, strGroupID, strGroupDomain);
                        zhuanFaDeptPopupWindowDuoFa.setSendUser(ChoosedContactsNew.get().getDepts());
                        zhuanFaDeptPopupWindowDuoFa.showData(ZhuanFaChooseOrgActivity.this.data, null);

                        ZhuanFaPopupWindowDuoFaOrg zhuanFaPopupWindow = new ZhuanFaPopupWindowDuoFaOrg(ZhuanFaChooseOrgActivity.this, strUserID,
                                strUserDomainCode, isGroup, strGroupID, strGroupDomain);
                        zhuanFaPopupWindow.setSendUser(ChoosedContactsNew.get().getContacts());
                        zhuanFaPopupWindow.showAtLocation(ll_root, Gravity.CENTER, 0, 0);
                        zhuanFaPopupWindow.showData(ZhuanFaChooseOrgActivity.this.data, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                zhuanFaGroupPopupWindow.sendMessage();
                                zhuanFaDeptPopupWindowDuoFa.sendMessage();

                                zhuanFaPopupWindow.sendMessage();
                            }
                        }, header, name, total > 1);

                    }
                });
    }

    @Override
    public void doInitDelay() {

        tv_choose_confirm.setVisibility(View.GONE);

        refresh_view.setColorSchemeColors(ContextCompat.getColor(this, R.color.blue),
                ContextCompat.getColor(this, R.color.colorPrimary));
        refresh_view.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestOnLine(false);
            }
        });
        rct_view.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                int[] location2 = new int[2];
                rct_view.getLocationInWindow(location2);

            }
        });
        refresh_view.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestDatas();
            }
        });

        rct_view.setLayoutManager(new SafeLinearLayoutManager(this));

        adapter = new LiteBaseAdapter<>(this,
                mCustomContacts,
                UserViewOrgHolder.class,
                R.layout.letter_item_layout_org,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        User user = (User) v.getTag();
                        if (user.nJoinStatus != 2) {
                            handleChoice(user);
                        }
                        changeShowSelected();
                    }
                }, "false");
        UserViewOrgHolder.mIsChoice = true;
        rct_view.setAdapter(adapter);

        rct_view_suozai.setLayoutManager(new SafeLinearLayoutManager(this));
        adapterAt = new LiteBaseAdapter<>(this,
                atData,
                ContactsDeptViewOrgHolder.class,
                R.layout.item_contacts_person_chat_org,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DeptData deptData = (DeptData) v.getTag();

                        for (DeptData temp : FragmentContacts.allDeptDatas) {
                            if (deptData.strDepID.equals(temp.strDepID)) {
                                deptData.nDepType = temp.nDepType;
                                break;
                            }
                        }
                        deptData.strDomainCode = AppAuth.get().getDomainCode();
                        if (v.getId() == R.id.tv_next) {
                            DomainInfoList.DomainInfo domain = null;
                            if (null != VIMApp.getInstance().mDomainInfoList && VIMApp.getInstance().mDomainInfoList.size() > 0) {
                                for (DomainInfoList.DomainInfo temp : VIMApp.getInstance().mDomainInfoList) {
                                    if (temp.strDomainCode.equals(AppAuth.get().getDomainCode())) {
                                        domain = temp;
                                        break;
                                    }
                                }
                            }
                            deptData.strDomainCode = domain == null ? "" : domain.strDomainCode;
                            ArrayList<String> titleName = new ArrayList<>();
                            titleName.add(TextUtils.isEmpty(deptData.strName) ? deptData.strDepName : deptData.strName);
                            Intent intent = new Intent(ZhuanFaChooseOrgActivity.this, DeptDeepListOrgActivity.class);
                            intent.putExtra("domainName", (domain == null ? "" : domain.strDomainName));
                            intent.putExtra("titleName", titleName);
                            intent.putExtra("deptData", deptData);
                            intent.putExtra("max", 10000);
                            intent.putExtra("map", FragmentContacts.map);
                            intent.putExtra("isZhuanFa", isZhuanFa);
                            startActivity(intent);
                        } else {
                            handleChoice(deptData);
                        }
                    }
                }, "false");
        ContactsDeptViewOrgHolder.mIsChoice = isSelectUser;
        rct_view_suozai.setAdapter(adapterAt);

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
                        for (User item : mAllContacts) {
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
                        for (DeptData item : atData) {
                            if (mChoosedAdapter.getDatas().get(i).strId.equals(item.strDepID)) {
                                handleChoice(item);
                                break;
                            }
                        }
                    }
                } else {
                    boolean isDel = false;
                    for (User item : mAllContacts) {
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
        changeShowSelected();
        requestDatas();
    }

    private void changeShowSelected() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        if (adapterAt != null) {
            adapterAt.notifyDataSetChanged();
        }
        if (mChoosedAdapter != null) {
            mChoosedAdapter.notifyDataSetChanged();
        }

        boolean hasNoSelected = false;

        for (User user : mCustomContacts) {
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
            getNavigate().setRightText("确定(0)");
        } else {
            llChoosedPersons.setVisibility(View.VISIBLE);
            getNavigate().setRightText("确定(" + ChoosedContactsNew.get().getShowTotalSize() + ")");
        }
        getNavigate().getRightTextView().setBackgroundResource(R.drawable.shape_choosed_confirm);
    }

    void requestDatas() {
        requestSelfDept();
        requestChangYong();
        FragmentContacts.requestDeptAll();
    }

    private void requestSelfDept() {
        ModelApis.Contacts().requestBuddyContacts(new ModelCallback<ContactsBean>() {
            @Override
            public void onSuccess(final ContactsBean contactsBean) {
                if (null != contactsBean && null != contactsBean.userList && contactsBean.userList.size() > 0) {
                    for (User temp : contactsBean.userList) {
                        if (temp.strUserID.equals(AppAuth.get().getUserID())) {
                            atData.clear();
                            if (temp.getUserDept() != null) {
                                atData.addAll(temp.getUserDept());
                                for (DeptData dep : atData) {
                                    if (selectedDept.contains(dep.strDepID)) {
                                        dep.isSelected = true;
                                    }
                                }
                            } else {
                            }
                            Collections.sort(atData, new Comparator<DeptData>() {
                                @Override
                                public int compare(DeptData o1, DeptData o2) {
                                    return o1.nDepType - o2.nDepType;
                                }
                            });

                            adapterAt.notifyDataSetChanged();
                            break;
                        }
                    }
                }
            }

            @Override
            public void onFinish(HTTPResponse httpResponse) {
                super.onFinish(httpResponse);
                refresh_view.setRefreshing(false);
            }
        });
    }

    private void requestChangYong() {
        mAllContacts.clear();
        for (ChangyongLianXiRenBean temp : AppDatas.MsgDB().getChangYongLianXiRen().queryAll(AppAuth.get().getUserID(), AppAuth.get().getDomainCode())) {
            mAllContacts.add(ChangyongLianXiRenBean.converToUser(temp));
            for (User user : mAllContacts) {
                if (ChoosedContactsNew.get().isContain(user)) {
                    user.isSelected = true;
                }
            }
        }
        Collections.sort(mAllContacts, new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                if (TextUtils.isEmpty(o1.saveTime)) {
                    o1.saveTime = "0";
                }
                if (TextUtils.isEmpty(o2.saveTime)) {
                    o2.saveTime = "0";
                }
                long t1 = Long.parseLong(o1.saveTime);
                long t2 = Long.parseLong(o2.saveTime);
                return (int) (t2 - t1);
            }
        });
        showData(et_key.getText().toString());
    }

    private void showData(String str) {
        mCustomContacts.clear();
        for (User temp : mAllContacts) {
            if (TextUtils.isEmpty(str)) {
                mCustomContacts.add(temp);
            } else if (temp.strUserName.contains(str) || temp.strLoginName.contains(str)) {
                mCustomContacts.add(temp);
            }
        }

        if (mCustomContacts.isEmpty()) {
            ll_selected_all.setVisibility(View.GONE);
        } else {
            ll_selected_all.setVisibility(View.VISIBLE);
        }

        adapter.notifyDataSetChanged();

        refresh_view.setRefreshing(false);
    }

    protected void updateContacts() {
        mCustomContacts.clear();
        mCustomContacts.addAll(getCustomContacts(mAllContacts));
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        if (mChoosedAdapter != null) {
            mChoosedAdapter.notifyDataSetChanged();
        }

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
            user.isSelected = true;
            ChoosedContactsNew.get().add(user, true);
        }
        adapter.notifyDataSetChanged();
        mChoosedAdapter.notifyDataSetChanged();
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
                                        if (ChoosedContactsNew.get().getContacts().size() >= 10000 + 1) {
                                            showToast("最多选" + 10000 + "人，已达人数上限");
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
                adapterAt.notifyDataSetChanged();
                changeShowSelected();

            }
        });
    }

    @OnClick({R.id.tv_group})
    public void onClick(View view) {
        Intent intent = new Intent(this, GroupListOrgActivity.class);
        intent.putExtra("max", 100000);
        intent.putExtra("isZhuanFa", isZhuanFa);
        startActivity(intent);
    }

    @OnClick({R.id.ll_search, R.id.et_key})
    public void onClickSearch(View view) {
        Intent intent = new Intent(this, SearchDeptUserListOrgActivity.class);
        intent.putExtra("max", 100000);
        intent.putExtra("isZhuanFa", isZhuanFa);
        startActivity(intent);
    }

    @OnClick({R.id.tv_dept_title})
    public void onClickTitle(View view) {
        Intent intent = new Intent(this, DeptListOrgActivity.class);
        intent.putExtra("max", 100000);
        intent.putExtra("isZhuanFa", isZhuanFa);
        startActivity(intent);
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

        for (User user : mCustomContacts) {
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventUserClick bean) {
        for (User item : mAllContacts) {
            if (bean.user.strId.equals(item.strUserID)) {
                handleChoice(item);
                break;
            }
        }
        changeShowSelected();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventUserSelected bean) {
        mChoosedAdapter.notifyDataSetChanged();
        changeShowSelected();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(CloseZhuanFa messageEvent) {
        finish();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        updateContacts();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ChoosedContactsNew.get().clear();
        EventBus.getDefault().unregister(this);
    }

    private ArrayList<User> getCustomContacts(ArrayList<User> data) {
        if (data == null || data.size() <= 0) {
            return new ArrayList<>();
        }
        for (User item : data) {
            String upPinYin = "";
            item.isSelected = false;
            for (User temp : ChoosedContactsNew.get().getContacts()) {
                if (temp.strUserName.equals(item.strUserName)) {
                    item.isSelected = true;
                    break;
                }
            }
            if (TextUtils.isEmpty(item.strUserNamePinYin)) {
                item.strUserNamePinYin = Pinyin.toPinyin(item.strUserName, "_");
                if (TextUtils.isEmpty(item.strUserNamePinYin)) {
                    item.strUserNamePinYin = "#";
                }
                upPinYin = item.strUserNamePinYin.toUpperCase();
            } else {
                upPinYin = item.strUserNamePinYin.toUpperCase();
            }
            String a = "#";
            item.pinyin = String.valueOf(TextUtils.isEmpty(upPinYin) ? a.charAt(0) : upPinYin.charAt(0));
        }

        return data;
    }


}
