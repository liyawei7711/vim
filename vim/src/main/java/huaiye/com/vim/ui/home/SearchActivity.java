package huaiye.com.vim.ui.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.promeg.pinyinhelper.Pinyin;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import huaiye.com.vim.R;
import huaiye.com.vim.common.recycle.LiteBaseAdapter;
import huaiye.com.vim.common.recycle.SafeLinearLayoutManager;
import huaiye.com.vim.common.rx.RxUtils;
import huaiye.com.vim.common.views.FastRetrievalBar;
import huaiye.com.vim.dao.AppDatas;
import huaiye.com.vim.dao.msgs.User;
import huaiye.com.vim.models.ModelApis;
import huaiye.com.vim.models.ModelCallback;
import huaiye.com.vim.models.contacts.bean.ContactsBean;
import huaiye.com.vim.ui.contacts.ContactDetailNewActivity;
import huaiye.com.vim.ui.contacts.sharedata.ChoosedContactsNew;
import huaiye.com.vim.ui.contacts.sharedata.VimChoosedContacts;
import huaiye.com.vim.ui.home.adapter.ContactsViewHolder;
import ttyy.com.jinnetwork.core.work.HTTPRequest;
import ttyy.com.jinnetwork.core.work.HTTPResponse;
import ttyy.com.recyclerexts.base.EXTRecyclerAdapter;
import ttyy.com.recyclerexts.base.EXTViewHolder;

/**
 * Created by Administrator on 2018\3\14 0014.
 */

public class SearchActivity extends Activity {
    @BindView(R.id.et_key)
    EditText et_key;
    @BindView(R.id.et_search_cancel)
    TextView et_search_cancel;
    @BindView(R.id.close)
    ImageView close;
    @BindView(R.id.refresh_view)
    SwipeRefreshLayout refresh_view;
    @BindView(R.id.rct_view)
    RecyclerView rct_view;
    @BindView(R.id.contacts_retrieval_bar)
    FastRetrievalBar contacts_retrieval_bar;
    @BindView(R.id.iv_empty_view)
    RelativeLayout iv_empty_view;
    @BindView(R.id.ll_choosed_persons)
    LinearLayout ll_choosed_persons;
    @BindView(R.id.rct_choosed)
    RecyclerView rct_choosed;
    @BindView(R.id.tv_choose_confirm)
    TextView tv_choose_confirm;

    LiteBaseAdapter<User> adapter;
    private ArrayList<User> mCustomContacts = new ArrayList<>();
    private ArrayList<User> mAllContacts = new ArrayList<>();
    private EXTRecyclerAdapter<User> mChoosedAdapter;
    long time;
    private int mPage = 1;
    private int mTotalSize = 0;
    private String mSearchKey;
    private int mSource;//1--选择用户
    private int mJoinNum = 0;

    @SuppressLint("ResourceAsColor")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_home_meetings_history);
        ButterKnife.bind(this);
        initListener();
        initData();
    }

    private void initData() {
        mSource = getIntent().getIntExtra("mSource", 0);
        if (mSource == 1) {
            ll_choosed_persons.setVisibility(View.VISIBLE);
        }
        adapter = new LiteBaseAdapter<>(this,
                mCustomContacts,
                ContactsViewHolder.class,
                R.layout.item_contacts_person,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        User user = (User) v.getTag();
                        if (mSource == 1 ? true : false) {
                            handleChoice(user);
                            changeShowSelected();
                        } else {
                            Intent intent = new Intent(SearchActivity.this, ContactDetailNewActivity.class);
                            intent.putExtra("nUser", user);
                            startActivity(intent);
                        }
                    }
                }, "false");
        ContactsViewHolder.mIsChoice = mSource == 1 ? true : false;
        ContactsViewHolder.mOnLoadMoreListener = new ContactsViewHolder.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                loadMore();
            }
        };

        rct_view.setLayoutManager(new SafeLinearLayoutManager(this));
        rct_view.setAdapter(adapter);

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
                if (mChoosedAdapter.getDatas().get(i).strUserName.equals(AppDatas.Auth().getUserName())) {
                    return;
                }
                for (User item : mCustomContacts) {
                    if (mChoosedAdapter.getDatas().get(i).strUserName.equals(item.strUserName)) {
                        handleChoice(item);
                        break;
                    }
                }
                changeShowSelected();
            }
        });
        rct_choosed.setLayoutManager(new SafeLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rct_choosed.setAdapter(mChoosedAdapter);
        changeShowSelected();
    }

    private void initListener() {
        et_search_cancel.setOnClickListener(mOnClickListener);
        close.setOnClickListener(mOnClickListener);
        tv_choose_confirm.setOnClickListener(mOnClickListener);
        et_key.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                 /*if (System.currentTimeMillis() - time > 1000) {
                    time = System.currentTimeMillis();
                }*/
                    Editable s = et_key.getText();
                    if (s != null && s.length() > 0) {
                        refreshDatas(s.toString());
                    } else {
                        mCustomContacts.clear();
                        adapter.notifyDataSetChanged();
                    }
                    return true;
                }
                return false;
            }
        });

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
    }

    private void refreshDatas(String key) {
        mSearchKey = key;
        mPage = 1;
        mCustomContacts.clear();
        adapter.notifyDataSetChanged();
        ModelApis.Contacts().searchContacts(mPage, key, new ModelCallback<ContactsBean>() {
            @Override
            public void onSuccess(final ContactsBean contactsBean) {
                mTotalSize = contactsBean.nTotalSize;
                mAllContacts.clear();
                mAllContacts.addAll(contactsBean.userList);
                mCustomContacts.clear();
                mCustomContacts.addAll(getCustomContacts(mAllContacts));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFinish(HTTPResponse httpResponse) {
                super.onFinish(httpResponse);
                refresh_view.setRefreshing(false);
            }

            @Override
            public void onCancel(HTTPRequest httpRequest) {
                super.onCancel(httpRequest);
                refresh_view.setRefreshing(false);
            }
        });
    }

    private void loadMore() {
        if (mAllContacts.size() >= mTotalSize) {
            return;
        }
        ModelApis.Contacts().searchContacts(mPage + 1, mSearchKey, new ModelCallback<ContactsBean>() {
            @Override
            public void onSuccess(final ContactsBean contactsBean) {
                new RxUtils<ArrayList<User>>()
                        .doOnThreadObMain(new RxUtils.IThreadAndMainDeal<ArrayList<User>>() {
                            @Override
                            public ArrayList<User> doOnThread() {
                                mTotalSize = contactsBean.nTotalSize;
                                return contactsBean.userList;
                            }

                            @Override
                            public void doOnMain(ArrayList<User> data) {
                                mPage++;
                                mAllContacts.addAll(data);
                                mCustomContacts.clear();
                                mCustomContacts.addAll(getCustomContacts(mAllContacts));
                                adapter.notifyDataSetChanged();
                            }
                        });
            }

            @Override
            public void onFinish(HTTPResponse httpResponse) {
                super.onFinish(httpResponse);
                refresh_view.setRefreshing(false);
            }
        });
    }

    private ArrayList<User> getCustomContacts(ArrayList<User> data) {
        if (data == null || data.size() <= 0) {
            return null;
        }
        for (User item : data) {
            String upPinYin = "";
            item.isSelected = false;
            for (User temp : VimChoosedContacts.get().getContacts()) {
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

    private void handleChoice(User user) {
        if (user == null) {
            return;
        }
        if (ChoosedContactsNew.get().isContain(user)) {
            user.isSelected = false;
            ChoosedContactsNew.get().removeContacts(user);
            mChoosedAdapter.getDatas().remove(user);
        } else {
            user.isSelected = true;
            ChoosedContactsNew.get().addContacts(user);
        }
        adapter.notifyDataSetChanged();
        mChoosedAdapter.notifyDataSetChanged();
        changeNum(mChoosedAdapter.getDatas().size());
        mJoinNum = 0;
    }

    private void changeShowSelected() {
        if (mSource == 1) {
            if (ChoosedContactsNew.get().getContacts().isEmpty()) {
                ll_choosed_persons.setVisibility(View.GONE);
            } else {
                ll_choosed_persons.setVisibility(View.VISIBLE);
            }
        } else {
            ll_choosed_persons.setVisibility(View.GONE);
        }
        if (mChoosedAdapter != null) {
            changeNum(mChoosedAdapter.getDatas().size() - mJoinNum);
        }
    }

    private void changeNum(int num) {
        tv_choose_confirm.setText(getString(R.string.makesure) + "(" + num + ")");
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.et_search_cancel:
                    finish();
                    break;
                case R.id.close:
                    et_key.setText("");
                    break;
                case R.id.tv_choose_confirm:
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ContactsViewHolder.mOnLoadMoreListener = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        ContactsViewHolder.mIsChoice = mSource == 1 ? true : false;
    }

}
