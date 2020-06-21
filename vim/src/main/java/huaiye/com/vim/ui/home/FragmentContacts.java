package huaiye.com.vim.ui.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import huaiye.com.vim.R;
import huaiye.com.vim.VIMApp;
import huaiye.com.vim.bus.MessageEvent;
import huaiye.com.vim.common.AppBaseFragment;
import huaiye.com.vim.common.AppUtils;
import huaiye.com.vim.common.recycle.LiteBaseAdapter;
import huaiye.com.vim.common.recycle.SafeLinearLayoutManager;
import huaiye.com.vim.common.rx.RxUtils;
import huaiye.com.vim.dao.AppDatas;
import huaiye.com.vim.dao.auth.AppAuth;
import huaiye.com.vim.dao.msgs.ChangyongLianXiRenBean;
import huaiye.com.vim.dao.msgs.User;
import huaiye.com.vim.models.ModelApis;
import huaiye.com.vim.models.ModelCallback;
import huaiye.com.vim.models.contacts.bean.ContactsBean;
import huaiye.com.vim.models.contacts.bean.DeptData;
import huaiye.com.vim.models.contacts.bean.DomainInfoList;
import huaiye.com.vim.ui.contacts.ContactDetailNewActivity;
import huaiye.com.vim.ui.contacts.DeptChatUtils;
import huaiye.com.vim.ui.contacts.DeptDeepListActivity;
import huaiye.com.vim.ui.contacts.DeptListActivity;
import huaiye.com.vim.ui.contacts.FriendActivity;
import huaiye.com.vim.ui.contacts.GroupListActivity;
import huaiye.com.vim.ui.contacts.SearchDeptUserListActivity;
import huaiye.com.vim.ui.home.adapter.ContactsDeptViewHolder;
import huaiye.com.vim.ui.home.adapter.ContactsDomainViewHolder;
import huaiye.com.vim.ui.home.adapter.ContactsViewDeptHolder;
import ttyy.com.jinnetwork.core.work.HTTPResponse;

/**
 * author: admin
 * date: 2017/12/28
 * version: 0
 * mail: secret
 * desc: FragmentContacts
 */
@BindLayout(R.layout.fragment_contacts)
public class FragmentContacts extends AppBaseFragment {

    public static final String TAG = "FragmentContacts";

    @BindView(R.id.refresh_view)
    SwipeRefreshLayout refresh_view;
    @BindView(R.id.et_key)
    TextView et_key;

    /*@BindView(R.id.rct_view_layout)
    RelativeLayout rct_view_layout;*/
    @BindView(R.id.ll_search)
    View ll_search;
    @BindView(R.id.tv_dept_at)
    View tv_dept_at;
    @BindView(R.id.rct_view_suozai)
    RecyclerView rct_view_suozai;
    @BindView(R.id.rct_view_dept)
    RecyclerView rct_view_dept;
    @BindView(R.id.rct_view)
    RecyclerView rct_view;
    @BindView(R.id.view_list)
    RecyclerView view_list;
    @BindView(R.id.tv_title)
    View tv_title;
    @BindView(R.id.tv_letter_high_fidelity_item)
    TextView tv_letter_high_fidelity_item;

    LiteBaseAdapter<ChangyongLianXiRenBean> adapter;
    LiteBaseAdapter<DomainInfoList.DomainInfo> adapterDomain;
    LiteBaseAdapter<DeptData> adapterAt;

    private ArrayList<ChangyongLianXiRenBean> mCustomContacts = new ArrayList<>();//常用联系人
    private ArrayList<DomainInfoList.DomainInfo> domainData = new ArrayList<>();//部门
    private ArrayList<DeptData> atData = new ArrayList<>();//所在部门

    private ArrayList<DomainInfoList.DomainInfo> allDomainDatas = new ArrayList<>();//all部门
    private ArrayList<ChangyongLianXiRenBean> mAllContacts = new ArrayList<>();//常用联系人

    private boolean isFreadList = true;
    private boolean isSOS;
    public static boolean isShow = true;

    @SuppressLint("ResourceAsColor")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EventBus.getDefault().register(this);
        getNavigate().hideLeftIcon()
                .setReserveStatusbarPlace()
                .setTitlText("组织架构")
                .setTitlClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View V) {
                    }
                })
//                .showTopSearch()
//                .setTopSearchClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        if (isSOS) {
//                            return;
//                        }
//                        if (isFreadList) {
//                            Intent intent = new Intent(getContext(), SearchActivity.class);
//                            intent.putExtra("mSource", 0);
//                            startActivity(intent);
//                        } else {
//                            Intent intent = new Intent(getContext(), SearchGroupActivity.class);
//                            intent.putExtra("mSource", 0);
//                            startActivity(intent);
//                        }
//                    }
//                })
                .showTopAdd()
                .setTopAddClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isSOS) {
                            return;
                        }
                        showChatMoreStylePopupWindow(v);
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
        rct_view_suozai.setNestedScrollingEnabled(false);
        rct_view.setNestedScrollingEnabled(false);
        rct_view_dept.setNestedScrollingEnabled(false);
        refresh_view.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.blue),
                ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        rct_view.setLayoutManager(new SafeLinearLayoutManager(getActivity()));
        adapter = new LiteBaseAdapter<>(getActivity(),
                mCustomContacts,
                ContactsViewDeptHolder.class,
                R.layout.item_contacts_person_changyong,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ChangyongLianXiRenBean user = (ChangyongLianXiRenBean) v.getTag();
                        Intent intent = new Intent(getActivity(), ContactDetailNewActivity.class);
                        intent.putExtra("nUser", ChangyongLianXiRenBean.converToUser(user));
                        startActivity(intent);
                    }
                }, "false");
        rct_view.setAdapter(adapter);

        rct_view_dept.setLayoutManager(new SafeLinearLayoutManager(getActivity()));
        adapterDomain = new LiteBaseAdapter<>(getActivity(),
                domainData,
                ContactsDomainViewHolder.class,
                R.layout.item_contacts_domain_view,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DomainInfoList.DomainInfo domainInfo = (DomainInfoList.DomainInfo) v.getTag();
                        ArrayList<String> titleName = new ArrayList<>();
                        titleName.add(domainInfo.strDomainName);
                        Intent intent = new Intent(getActivity(), DeptListActivity.class);
                        intent.putExtra("domainName", domainInfo.strDomainName);
                        intent.putExtra("titleName", titleName);
                        intent.putExtra("domain", domainInfo);
                        startActivity(intent);
                    }
                }, "false");
        rct_view_dept.setAdapter(adapterDomain);

        rct_view_suozai.setLayoutManager(new SafeLinearLayoutManager(getActivity()));
        adapterAt = new LiteBaseAdapter<>(getActivity(),
                atData,
                ContactsDeptViewHolder.class,
                R.layout.item_contacts_person_chat,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DeptData deptData = (DeptData) v.getTag();
                        if (v.getId() == R.id.tv_message) {
                            new DeptChatUtils().startGroup(getActivity(), deptData);
                            return;
                        }
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
                        titleName.add(domain == null ? "" : domain.strDomainName);
                        titleName.add(TextUtils.isEmpty(deptData.strName) ? deptData.strDepName : deptData.strName);
                        Intent intent = new Intent(getActivity(), DeptDeepListActivity.class);
                        intent.putExtra("domainName", (domain == null ? "" : domain.strDomainName));
                        intent.putExtra("titleName", titleName);
                        intent.putExtra("deptData", deptData);
                        startActivity(intent);

                    }
                }, "false");
        rct_view_suozai.setAdapter(adapterAt);

        refresh_view.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh_view.setRefreshing(true);
                initData();
            }
        });

        if (!isSOS) {
            initData();
        }
    }

    private void initData() {
        requestSelfDept();
        requestDept();
        requestChangYong();
    }

    private void showData(String str) {
        mCustomContacts.clear();
        for (ChangyongLianXiRenBean temp : mAllContacts) {
            if (TextUtils.isEmpty(str)) {
                mCustomContacts.add(temp);
            } else if (temp.strUserName.contains(str) || temp.strLoginName.contains(str)) {
                mCustomContacts.add(temp);
            }
        }
        domainData.clear();
        for (DomainInfoList.DomainInfo temp : allDomainDatas) {
            if (TextUtils.isEmpty(str)) {
                domainData.add(temp);
            } else if (temp.strDomainName.contains(str) || temp.strDomainCode.contains(str)) {
                domainData.add(temp);
            }
        }
        adapter.notifyDataSetChanged();
        adapterDomain.notifyDataSetChanged();

        refresh_view.setRefreshing(false);
    }

    private void requestDept() {
        if (null != VIMApp.getInstance().mDomainInfoList && VIMApp.getInstance().mDomainInfoList.size() > 0) {
            allDomainDatas.clear();
            allDomainDatas.addAll(VIMApp.getInstance().mDomainInfoList);
            showData(et_key.getText().toString());
        }
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
                                tv_dept_at.setVisibility(View.VISIBLE);
                            } else {
                                tv_dept_at.setVisibility(View.GONE);
                            }
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
        mAllContacts.addAll(AppDatas.MsgDB().getChangYongLianXiRen().queryAll(AppAuth.get().getUserID(), AppAuth.get().getDomainCode()));
        showData(et_key.getText().toString());
    }

    private void refreshCurrentUserData(User user) {
        new RxUtils<List<ChangyongLianXiRenBean>>().doOnThreadObMain(new RxUtils.IThreadAndMainDeal<List<ChangyongLianXiRenBean>>() {
            @Override
            public List<ChangyongLianXiRenBean> doOnThread() {
                if (null != mAllContacts && mAllContacts.size() > 0 && null != user) {
                    for (ChangyongLianXiRenBean userAll : mAllContacts) {
                        if (userAll.strDomainCode.equals(user.strDomainCode) && userAll.strUserID.equals(user.strUserID)) {
                            ChangyongLianXiRenBean lianxiren = ChangyongLianXiRenBean.converToChangyongLianXiRen(user);
                            mAllContacts.set(mAllContacts.indexOf(userAll), lianxiren);
                            mCustomContacts.set(mCustomContacts.indexOf(userAll), lianxiren);
                            AppDatas.MsgDB().getChangYongLianXiRen().deleteByUser(AppAuth.get().getUserID(), AppAuth.get().getDomainCode(), user.strUserID, TextUtils.isEmpty(user.strDomainCode) ? user.strUserDomainCode : user.strDomainCode);
                            AppDatas.MsgDB().getChangYongLianXiRen().insertAll(lianxiren);
                            continue;
                        }
                    }
                }
                return mAllContacts;
            }

            @Override
            public void doOnMain(List<ChangyongLianXiRenBean> data) {
                if (null != adapter) {
                    adapter.notifyDataSetChanged();
                }
            }
        });

    }

    @OnClick({R.id.tv_group})
    public void onClick(View view) {
        if (isSOS) {
            return;
        }
        Intent intent = new Intent(getContext(), GroupListActivity.class);
        startActivity(intent);
    }

    @OnClick({R.id.tv_dept_title})
    public void onClickTitle(View view) {
        if (isSOS) {
            return;
        }
        isShow = !isShow;
        adapterDomain.notifyDataSetChanged();
    }

    @OnClick({R.id.ll_search, R.id.et_key})
    public void onClickSearch(View view) {
        if (isSOS) {
            return;
        }
        Intent intent = new Intent(getContext(), SearchDeptUserListActivity.class);
        startActivity(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final MessageEvent messageEvent) {
        if (null == messageEvent) {
            return;
        }
        switch (messageEvent.what) {
            case AppUtils.EVENT_CREATE_GROUP_SUCCESS_ADDGROUP_TO_LIST:
            case AppUtils.EVENT_MESSAGE_MODIFY_GROUP:
                break;
            case AppUtils.EVENT_MESSAGE_ADD_FRIEND:
            case AppUtils.EVENT_MESSAGE_DEL_FRIEND:
                break;
            case AppUtils.EVENT_DEL_GROUP_SUCCESS:
            case AppUtils.EVENT_LEAVE_GROUP_SUCCESS:
                break;
            case AppUtils.EVENT_MESSAGE_MODIFY_HEAD_PIC:
                User user = (User) messageEvent.obj1;
                refreshCurrentUserData(user);
                break;
            default:
                break;
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    public void setSos(boolean isSOS) {
        this.isSOS = isSOS;
    }

    @Override
    public void onResume() {
        super.onResume();
        initData();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(hidden) {

        } else {
            initData();
        }
    }
}
