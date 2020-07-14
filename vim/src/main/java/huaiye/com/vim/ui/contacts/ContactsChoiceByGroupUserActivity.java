package huaiye.com.vim.ui.contacts;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.promeg.pinyinhelper.Pinyin;
import com.ttyy.commonanno.anno.BindLayout;
import com.ttyy.commonanno.anno.BindView;
import com.ttyy.commonanno.anno.OnClick;
import com.ttyy.commonanno.anno.route.BindExtra;

import java.util.ArrayList;

import huaiye.com.vim.R;
import huaiye.com.vim.common.AppBaseActivity;
import huaiye.com.vim.common.recycle.LiteBaseAdapter;
import huaiye.com.vim.common.recycle.SafeLinearLayoutManager;
import huaiye.com.vim.common.views.FastRetrievalBar;
import huaiye.com.vim.dao.AppDatas;
import huaiye.com.vim.dao.msgs.User;
import huaiye.com.vim.models.contacts.bean.ContactsGroupUserListBean;
import huaiye.com.vim.ui.contacts.sharedata.ChoosedContactsNew;
import huaiye.com.vim.ui.home.adapter.ContactsViewHolder;
import ttyy.com.recyclerexts.base.EXTRecyclerAdapter;
import ttyy.com.recyclerexts.base.EXTViewHolder;

/**
 * author: admin
 * date: 2018/01/15
 * version: 0
 * mail: secret
 * desc: ContactsChoiceActivity
 */
@BindLayout(R.layout.activity_contacts_root)
public class ContactsChoiceByGroupUserActivity extends AppBaseActivity {
    public static final String SELECTED_CONTACTS = "selectedContacts";
    public static final String RESULT_CONTACTS = "resultContacts";
    @BindView(R.id.refresh_view)
    SwipeRefreshLayout refresh_view;
    @BindView(R.id.rct_view)
    RecyclerView rct_view;
    @BindView(R.id.contacts_retrieval_bar)
    FastRetrievalBar contacts_retrieval_bar;
    @BindView(R.id.rct_choosed)
    RecyclerView rct_choosed;
    @BindView(R.id.tv_choose_confirm)
    TextView tv_choose_confirm;
    @BindView(R.id.tv_letter_high_fidelity_item)
    TextView tv_letter_high_fidelity_item;
    @BindView(R.id.ll_choosed_persons)
    LinearLayout llChoosedPersons;

    @BindExtra
    String titleName;
    @BindExtra
    boolean isSelectUser;
    @BindExtra
    boolean needAddSelf;
    @BindExtra
    ContactsGroupUserListBean mGroupUserListBean;

    LiteBaseAdapter<User> adapter;

    EXTRecyclerAdapter<User> mChoosedAdapter;
    //    ArrayList<User> stricts = new ArrayList<>();
    private ArrayList<User> mCustomContacts = new ArrayList<>();

    private ArrayList<User> mAllContacts = new ArrayList<>();
    private ArrayList<User> mOnlineContacts = new ArrayList<>();
    private int mPage = 1;
    private int mTotalSize = 0;
    private boolean mIsShowAll = false;
    private int mJoinNum = 0;

    @Override
    protected void initActionBar() {
        if (TextUtils.isEmpty(titleName)) {
            titleName = "联系人";
        }
        getNavigate().setTitlText(titleName)
                .setLeftClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                })
                .setRightClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switchOnline();
                    }
                });
    }

    @Override
    public void doInitDelay() {
        refresh_view.setEnabled(false);
        contacts_retrieval_bar.setVisibility(View.GONE);
        rct_view.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                int[] location = new int[2];
//                header.getViewGroup().getLocationInWindow(location);

                int[] location2 = new int[2];
                rct_view.getLocationInWindow(location2);

                /*if (header.hasData()) {
                    if (location[1] != location2[1]) {
                        refresh_view.setEnabled(false);
                    } else {
                        refresh_view.setEnabled(true);
                    }
                } else {
                    refresh_view.setEnabled(true);
                }*/
            }
        });
        refresh_view.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

            }
        });

        rct_view.setLayoutManager(new SafeLinearLayoutManager(this));

        adapter = new LiteBaseAdapter<>(this,
                mCustomContacts,
                ContactsViewHolder.class,
                R.layout.item_contacts_person,
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
        ContactsViewHolder.mIsChoice = true;

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

        contacts_retrieval_bar.setTextView(tv_letter_high_fidelity_item);
//        FragmentContactsHeaderView fragmentContactsHeaderView = new FragmentContactsHeaderView(this);
//        adapter.addHeaderView(fragmentContactsHeaderView);
        rct_view.setAdapter(adapter);

        rct_choosed.setLayoutManager(new SafeLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mChoosedAdapter = new EXTRecyclerAdapter<User>(R.layout.item_contacts_person_choosed) {
            @Override
            public void onBindViewHolder(EXTViewHolder extViewHolder, int i, User contactData) {
                if (contactData.nJoinStatus != 2) {
                    extViewHolder.setText(R.id.tv_contact_name, contactData.strUserName);
                } else {
                    extViewHolder.setVisibility(R.id.tv_contact_name, View.GONE);
                    mJoinNum++;
                }
            }
        };
        mChoosedAdapter.setDatas(ChoosedContactsNew.get().getContacts());
        mChoosedAdapter.setOnItemClickListener(new EXTRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClicked(View view, int i) {
                String loginName = mChoosedAdapter.getDatas().get(i).strUserName;
                if (loginName.equals(AppDatas.Auth().getUserName())) {
                    return;
                }
//                ChoosedContacts.get().delTemp(mChoosedAdapter.getDatas().get(i));

//                mChoosedAdapter.getDatas().remove(i);
                for (User item : mCustomContacts) {
                    if (loginName.equals(item.strUserName)) {
                        handleChoice(item);
                        break;
                    }
                }
                changeShowSelected();
                /*for (int c : adapter.getSelectedPositions()) {
                    if (loginName.equals(adapter.getDataForItemPosition(c).loginName)) {
                        adapter.setItemChecked(c, false);
                        adapter.notifyDataSetChanged();
                        break;
                    }
                }*/
            }
        });
        rct_choosed.setAdapter(mChoosedAdapter);
        changeShowSelected();
        // requestDatas();
        updateContacts();
    }

    private void changeShowSelected() {
        if (ChoosedContactsNew.get().getContacts().isEmpty()) {
            llChoosedPersons.setVisibility(View.GONE);
        } else {
            llChoosedPersons.setVisibility(View.VISIBLE);
        }
        if (mChoosedAdapter != null) {
            changeNum(mChoosedAdapter.getDatas().size() - mJoinNum);
        }
    }

    protected void updateContacts() {
        mCustomContacts.clear();
        mCustomContacts.addAll(getCustomContacts(mGroupUserListBean.lstGroupUser));

        adapter.notifyDataSetChanged();
    }

    private void changeNum(int num) {
        tv_choose_confirm.setText("确定(" + num + ")");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            setResult(RESULT_OK, data);
            finish();
        }
    }


    @OnClick(R.id.tv_choose_confirm)
    void onChoosedConfirmClicked() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        ChoosedContacts.get().deleteSelf();
//        ChoosedContacts.get().clearTemp();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        updateContacts();
    }


    private ArrayList<User> getCustomContacts(ArrayList<User> data) {
        if (data == null || data.size() <= 0) {
            return new ArrayList<>();
        }

        for (User temp : data) {
            temp.isSelected = false;
            for (User temp2 : ChoosedContactsNew.get().getContacts()) {
                if (temp2.strUserName.equals(temp.strUserName)) {
                    temp.isSelected = true;
                    break;
                }
            }

            String upPinYin = "";
            if (TextUtils.isEmpty(temp.strUserNamePinYin)) {
                temp.strUserNamePinYin = Pinyin.toPinyin(temp.strUserName, "_");
                upPinYin = temp.strUserNamePinYin.toUpperCase();
            } else {
                upPinYin = temp.strUserNamePinYin.toUpperCase();
            }
            String a = "#";
            temp.pinyin = String.valueOf(TextUtils.isEmpty(upPinYin) ? a.charAt(0) : upPinYin.charAt(0));

        }
        return data;
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
        /*if (mChoicedContacts != null) {
            for (User item : mChoicedContacts) {
                if (item.strUserID.equals(user.strUserID)) {
                    mChoicedContacts.remove(item);
                    mChoosedAdapter.notifyDataSetChanged();
                    adapter.notifyDataSetChanged();
                    return;
                }
            }
        }
        mChoicedContacts.add(user);*/
        adapter.notifyDataSetChanged();
        mChoosedAdapter.notifyDataSetChanged();
        changeNum(mChoosedAdapter.getDatas().size() - mJoinNum);
        mJoinNum = 0;
    }

    private void switchOnline() {
        /*切换是否显示全部联系人*/
        AppDatas.Constants().switchShowAllContacts();

        updateContacts();
    }

    private ArrayList<User> getOnlineContacts() {
        mOnlineContacts.clear();
        for (User item : mAllContacts) {
            if (item.nStatus > 0) {
                mOnlineContacts.add(item);
            }
        }
        return mOnlineContacts;
    }

    @Override
    protected void onResume() {
        super.onResume();
        ContactsViewHolder.mIsChoice = true;
    }
}
