package huaiye.com.vim.ui.fenxiang;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.github.promeg.pinyinhelper.Pinyin;
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
import huaiye.com.vim.bus.CloseZhuanFa;
import huaiye.com.vim.bus.MessageEvent;
import huaiye.com.vim.common.AppBaseActivity;
import huaiye.com.vim.common.AppUtils;
import huaiye.com.vim.common.recycle.LiteBaseAdapter;
import huaiye.com.vim.common.recycle.SafeLinearLayoutManager;
import huaiye.com.vim.common.rx.RxUtils;
import huaiye.com.vim.common.views.FastRetrievalBar;
import huaiye.com.vim.dao.AppDatas;
import huaiye.com.vim.dao.auth.AppAuth;
import huaiye.com.vim.dao.msgs.User;
import huaiye.com.vim.models.ModelApis;
import huaiye.com.vim.models.ModelCallback;
import huaiye.com.vim.models.contacts.bean.ContactsBean;
import huaiye.com.vim.models.contacts.bean.DomainInfoList;
import huaiye.com.vim.ui.auth.StartActivity;
import huaiye.com.vim.ui.contacts.sharedata.ChoosedContactsNew;
import huaiye.com.vim.ui.contacts.viewholder.UserViewHolder;
import huaiye.com.vim.ui.zhuanfa.ZhuanFaChooseActivity;
import huaiye.com.vim.ui.zhuanfa.ZhuanFaGroupPopupWindowDuoFa;
import huaiye.com.vim.ui.zhuanfa.ZhuanFaUserAndGroup;
import ttyy.com.jinnetwork.core.work.HTTPResponse;

/**
 * author: admin
 * date: 2017/12/28
 * version: 0
 * mail: secret
 */
@BindLayout(R.layout.activity_zhuanfa_choose)
public class ShareChooseActivity extends AppBaseActivity {

    @BindView(R.id.fl_root)
    View fl_root;
    @BindView(R.id.refresh_view)
    SwipeRefreshLayout refresh_view;
    @BindView(R.id.contacts_retrieval_bar)
    FastRetrievalBar contacts_retrieval_bar;
    @BindView(R.id.rct_view)
    RecyclerView rct_view;
    @BindView(R.id.tv_letter_high_fidelity_item)
    TextView tv_letter_high_fidelity_item;
    @BindView(R.id.et_key)
    EditText et_key;

    LiteBaseAdapter<User> adapter;

    private ArrayList<User> mCustomContacts = new ArrayList<>();
    private ArrayList<User> mAllContacts = new ArrayList<>();

    private int totalRequest = 0;

    @Override
    protected void initActionBar() {
        ZhuanFaUserAndGroup.get().clearAll();

        EventBus.getDefault().register(this);
        getNavigate().setVisibility(View.VISIBLE);
        getNavigate().setTitlText("联系人")
                .setRightText("确定")
                .setLeftClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                })
                .setRightClickListener(new View.OnClickListener() {

                    public void onClick(View v) {
                        ZhuanFaUserAndGroup.get().clearUser();
                        for (User temp : mAllContacts) {
                            if (temp.isSelected) {
                                ZhuanFaUserAndGroup.get().add(temp);
                            }
                        }

                        if (ZhuanFaUserAndGroup.get().getGroupInfos().isEmpty() &&
                                ZhuanFaUserAndGroup.get().getUsers().isEmpty()) {
                            showToast("请选择分享对象");
                            return;
                        }

                        if (!ZhuanFaUserAndGroup.get().getUsers().isEmpty() &&
                                !ZhuanFaUserAndGroup.get().getGroupInfos().isEmpty()) {

                            ShareGroupPopupWindowDuoXuan shareGroupPopupWindowDuoXuan = new ShareGroupPopupWindowDuoXuan(ShareChooseActivity.this);
                            shareGroupPopupWindowDuoXuan.setSendUser(ZhuanFaUserAndGroup.get().getGroupInfos(), getIntent().getExtras());
                            shareGroupPopupWindowDuoXuan.showData(null);

                            SharePopupWindowDuoXuan sharePopupWindow = new SharePopupWindowDuoXuan(ShareChooseActivity.this, ZhuanFaUserAndGroup.get().getUsers());
                            sharePopupWindow.showAtLocation(fl_root, Gravity.CENTER, 0, 0);
                            sharePopupWindow.showData(getIntent().getExtras(), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    shareGroupPopupWindowDuoXuan.sendMessage();

                                    sharePopupWindow.sendMessage();
                                }
                            });

                        } else if (!ZhuanFaUserAndGroup.get().getUsers().isEmpty() &&
                                ZhuanFaUserAndGroup.get().getGroupInfos().isEmpty()) {
                            SharePopupWindowDuoXuan sharePopupWindow = new SharePopupWindowDuoXuan(ShareChooseActivity.this, ZhuanFaUserAndGroup.get().getUsers());
                            sharePopupWindow.showAtLocation(fl_root, Gravity.CENTER, 0, 0);
                            sharePopupWindow.showData(getIntent().getExtras());
                        } else if (ZhuanFaUserAndGroup.get().getUsers().isEmpty() &&
                                !ZhuanFaUserAndGroup.get().getGroupInfos().isEmpty()) {
                            ShareGroupPopupWindowDuoXuan shareGroupPopupWindow = new ShareGroupPopupWindowDuoXuan(ShareChooseActivity.this);
                            shareGroupPopupWindow.setSendUser(ZhuanFaUserAndGroup.get().getGroupInfos(), getIntent().getExtras());
                            shareGroupPopupWindow.showAtLocation(fl_root, Gravity.CENTER, 0, 0);
                            shareGroupPopupWindow.showData();
                        }
                    }
                });
    }

    @Override
    public void doInitDelay() {
        Intent intent = getIntent();
//        Set<String> getKey = intent.getExtras().keySet();
//        for (String key : getKey) {
//            try {
//                System.out.println("ccccccccccccccc1 key = " + key + "; value= " + intent.getExtras().get(key).toString().replaceAll("\n", ""));
//                System.out.println("ccccccccccccccc2 key = " + key + "; value= " + intent.getExtras().getString("android.intent.extra.TEXT").substring(intent.getExtras().getString("android.intent.extra.TEXT").indexOf("http")));
//            } catch (Exception e) {
//            }
//        }
        if (!VIMApp.getInstance().isLogin()) {
            intent.setClass(this, StartActivity.class);
            intent.putExtra("from", "share");
            startActivity(intent);
            finish();
            return;
        }

        adapter = new LiteBaseAdapter<>(this,
                mCustomContacts,
                UserViewHolder.class,
                R.layout.letter_item_layout_new,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        User user = (User) v.getTag();
                        if (user == null) {
                            return;
                        }
                        user.isSelected = !user.isSelected;
                        adapter.notifyItemChanged(mCustomContacts.indexOf(user));
                    }
                }, "false");
        UserViewHolder.mIsChoice = true;

        refresh_view.setColorSchemeColors(ContextCompat.getColor(this, R.color.blue),
                ContextCompat.getColor(this, R.color.colorPrimary));
        rct_view.setLayoutManager(new SafeLinearLayoutManager(this));
        rct_view.setAdapter(adapter);

        refresh_view.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestContacts();
            }
        });

        contacts_retrieval_bar.setTextView(tv_letter_high_fidelity_item);
        contacts_retrieval_bar.setOnTouchingLetterChangedListener(new FastRetrievalBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                int position = -1;
                if (mCustomContacts.size() <= 0) {
                    return;
                }
                for (int i = 0; i < mCustomContacts.size(); i++) {
                    if (s.equals(String.valueOf(mCustomContacts.get(i).pinyin))) {
                        position = i;
                        break;
                    }
                }
                if (position == -1) {
                    return;
                }
                if (position == 0) {
                    rct_view.scrollToPosition(0);
                } else {
                    rct_view.scrollToPosition(position);
                }
            }

            @Override
            public void setSidePressed() {

            }

            @Override
            public void setSideUnPressed() {

            }
        });

        et_key.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateContacts();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        requestContacts();
    }

    private void requestContacts() {
        mAllContacts.clear();
        if (null != VIMApp.getInstance().mDomainInfoList && VIMApp.getInstance().mDomainInfoList.size() > 0) {
            totalRequest++;
            for (DomainInfoList.DomainInfo domainInfo : VIMApp.getInstance().mDomainInfoList) {
                ModelApis.Contacts().requestAllContacts(domainInfo.strDomainCode, new ModelCallback<ContactsBean>() {
                    @Override
                    public void onSuccess(ContactsBean contactsBean) {
                        if (null != contactsBean && null != contactsBean.userList && contactsBean.userList.size() > 0) {
                            for (User user : contactsBean.userList) {
                                if (user.strUserID.equals(AppAuth.get().getUserID())) {
                                    contactsBean.userList.remove(user);
                                    break;
                                }
                            }
                            mAllContacts.addAll(contactsBean.userList);
                        }
                        doCallBack();
                    }

                    @Override
                    public void onFinish(HTTPResponse httpResponse) {
                        doCallBack();
                    }
                });
            }
        }
    }

    private void doCallBack() {
        totalRequest--;
        if (totalRequest == 0) {
            refresh_view.setRefreshing(false);
            new RxUtils<ArrayList<User>>()
                    .doOnThreadObMain(new RxUtils.IThreadAndMainDeal<ArrayList<User>>() {
                        @Override
                        public ArrayList<User> doOnThread() {
                            ArrayList<User> userList = new ArrayList<>();
                            for (User item : mAllContacts) {
                                if (!item.strUserID.equals(AppDatas.Auth().getUserID())) {
                                    item.strHeadUrl = AppDatas.MsgDB().getFriendListDao().getFriendHeadPic(item.strUserID, item.strDomainCode);
                                    userList.add(item);
                                }
                            }
                            return userList;
                        }

                        @Override
                        public void doOnMain(ArrayList<User> data) {
                            getCustomContacts(mAllContacts);
                            updateContacts();
                        }
                    });
        }
    }

    private void refreshCurrentUserData(User user) {
        new RxUtils<List<User>>().doOnThreadObMain(new RxUtils.IThreadAndMainDeal<List<User>>() {
            @Override
            public List<User> doOnThread() {
                if (null != mAllContacts && mAllContacts.size() > 0 && null != user) {
                    for (User userAll : mAllContacts) {
                        if (userAll.strDomainCode.equals(user.strDomainCode) && userAll.strUserID.equals(user.strUserID)) {
                            mAllContacts.set(mAllContacts.indexOf(userAll), user);
                            continue;
                        }
                    }
                }
                return mAllContacts;
            }

            @Override
            public void doOnMain(List<User> data) {
                if (null != adapter) {
                    getCustomContacts(mAllContacts);
                    updateContacts();
                }
            }
        });
    }

    public void updateContacts() {
        mCustomContacts.clear();
        if(TextUtils.isEmpty(et_key.getText().toString())) {
            mCustomContacts.addAll(mAllContacts);
        } else {
            for(User temp : mAllContacts) {
                if(temp.strUserName.contains(et_key.getText().toString())) {
                    mCustomContacts.add(temp);
                }
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
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
                upPinYin = item.strUserNamePinYin.toUpperCase();
            } else {
                upPinYin = item.strUserNamePinYin.toUpperCase();
            }
            String a = "#";
            item.pinyin = String.valueOf(TextUtils.isEmpty(upPinYin) ? a.charAt(0) : upPinYin.charAt(0));
        }

        return data;
    }

    @OnClick({R.id.tv_group})
    public void onClick(View view) {

        ZhuanFaUserAndGroup.get().clearUser();
        for (User temp : mAllContacts) {
            if (temp.isSelected) {
                ZhuanFaUserAndGroup.get().add(temp);
            }
        }

        Intent intent = getIntent();
        intent.setClass(this, ShareGroupListActivity.class);
        startActivity(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final MessageEvent messageEvent) {
        if (null == messageEvent) {
            return;
        }
        switch (messageEvent.what) {
            case AppUtils.EVENT_MESSAGE_ADD_FRIEND:
            case AppUtils.EVENT_MESSAGE_DEL_FRIEND:
                requestContacts();
                break;
            case AppUtils.EVENT_MESSAGE_MODIFY_HEAD_PIC:
                User user = (User) messageEvent.obj1;
                refreshCurrentUserData(user);
                break;
            default:
                break;
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final CloseZhuanFa messageEvent) {
        finish();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ZhuanFaUserAndGroup.get().clearAll();
        EventBus.getDefault().unregister(this);
    }

}
