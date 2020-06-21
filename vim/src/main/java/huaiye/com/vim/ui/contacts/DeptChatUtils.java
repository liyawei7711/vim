package huaiye.com.vim.ui.contacts;

import android.content.Context;
import android.content.Intent;

import com.huaiye.sdk.logger.Logger;
import com.lcw.library.imagepicker.activity.BaseActivity;

import java.util.ArrayList;

import huaiye.com.vim.common.AppBaseActivity;
import huaiye.com.vim.dao.AppDatas;
import huaiye.com.vim.dao.msgs.User;
import huaiye.com.vim.models.ModelApis;
import huaiye.com.vim.models.ModelCallback;
import huaiye.com.vim.models.contacts.bean.ContactsBean;
import huaiye.com.vim.models.contacts.bean.ContactsGroupChatListBean;
import huaiye.com.vim.models.contacts.bean.CreateGroupContactData;
import huaiye.com.vim.models.contacts.bean.DeptData;
import huaiye.com.vim.models.contacts.bean.GroupInfo;
import huaiye.com.vim.ui.meet.ChatGroupActivityNew;
import ttyy.com.jinnetwork.core.work.HTTPRequest;
import ttyy.com.jinnetwork.core.work.HTTPResponse;

import static huaiye.com.vim.common.AppBaseActivity.showToast;

public class DeptChatUtils {

    public void startGroup(Context context, DeptData deptData) {
        ((AppBaseActivity)context).mZeusLoadView.loadingText("正在加载").setLoading();
        getHasGroup(context, deptData);
    }
    private void requestUser(Context context, DeptData deptData) {
        ModelApis.Contacts().requestContacts(deptData.strDomainCode, deptData.strDepID, new ModelCallback<ContactsBean>() {
            @Override
            public void onSuccess(final ContactsBean contactsBean) {
                if (null != contactsBean && null != contactsBean.userList && contactsBean.userList.size() > 0) {
                    createGroup(context, deptData, contactsBean.userList);
                }
            }

            @Override
            public void onFailure(HTTPResponse httpResponse) {
                super.onFailure(httpResponse);
                ((AppBaseActivity)context).mZeusLoadView.dismiss();
                showToast("获取列表失败");
            }

        });
    }

    private void createGroup(Context context, DeptData deptData, ArrayList<User> users) {
        ModelApis.Contacts().requestCreateGroupChat(deptData.getName(), users, new ModelCallback<CreateGroupContactData>() {
            @Override
            public void onSuccess(final CreateGroupContactData contactsBean) {
                Intent intent = new Intent(context, ChatGroupActivityNew.class);
                intent.putExtra("mContactsBean", contactsBean);
                context.startActivity(intent);
                ((AppBaseActivity)context).mZeusLoadView.dismiss();
            }

            @Override
            public void onFailure(HTTPResponse httpResponse) {
                super.onFailure(httpResponse);
                showToast("群创建失败");
                ((AppBaseActivity)context).mZeusLoadView.dismiss();
            }
        });
    }

    private void getHasGroup(Context context, DeptData deptData) {
        ModelApis.Contacts().requestGroupByDept(deptData.strDomainCode, deptData.getName(), new ModelCallback<ContactsGroupChatListBean>() {
            @Override
            public void onSuccess(final ContactsGroupChatListBean contactsBean) {
                GroupInfo groupInfo = null;
                if(contactsBean != null && contactsBean.lstGroupInfo != null && !contactsBean.lstGroupInfo.isEmpty()) {
                    for (GroupInfo temp : contactsBean.lstGroupInfo) {
                        if (temp.strGroupID == deptData.strDepID) {
                            groupInfo = temp;
                        }
                    }
                }
                if(groupInfo != null){
                    CreateGroupContactData bean = new CreateGroupContactData();
                    bean.strGroupDomainCode = groupInfo.strGroupDomainCode;
                    bean.strGroupID = groupInfo.strGroupID;
                    bean.sessionName = groupInfo.strGroupName;
                    Intent intent = new Intent(context, ChatGroupActivityNew.class);
                    intent.putExtra("mContactsBean", bean);
                    context.startActivity(intent);
                    ((AppBaseActivity)context).mZeusLoadView.dismiss();
                } else {
                    requestUser(context, deptData);
                }
            }

            @Override
            public void onFailure(HTTPResponse httpResponse) {
                super.onFailure(httpResponse);
                requestUser(context, deptData);
            }
        });
    }
}
