package huaiye.com.vim.ui.contacts;

import android.content.Context;
import android.content.Intent;

import com.huaiye.sdk.logger.Logger;
import com.lcw.library.imagepicker.activity.BaseActivity;

import java.util.ArrayList;

import huaiye.com.vim.common.AppBaseActivity;
import huaiye.com.vim.dao.AppDatas;
import huaiye.com.vim.dao.auth.AppAuth;
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
        requestUser(context, deptData);
    }
    private void requestUser(Context context, DeptData deptData) {
        ModelApis.Contacts().requestContacts(deptData.strDomainCode, deptData.strDepID, new ModelCallback<ContactsBean>() {
            @Override
            public void onSuccess(final ContactsBean contactsBean) {
                if (null != contactsBean && null != contactsBean.userList && contactsBean.userList.size() > 0) {
                    Intent intent = new Intent(context, ChatGroupActivityNew.class);
                    CreateGroupContactData groupContactData = new CreateGroupContactData();
                    groupContactData.strGroupDomainCode = deptData.strDomainCode;
                    groupContactData.strGroupID = deptData.strDepID;
                    groupContactData.sessionName = deptData.getName();
                    groupContactData.userList = contactsBean.userList;
                    intent.putExtra("mContactsBean", groupContactData);
                    context.startActivity(intent);
                } else {
                    showToast("获取部门联系人失败");
                }
                ((AppBaseActivity)context).mZeusLoadView.dismiss();
            }

            @Override
            public void onFailure(HTTPResponse httpResponse) {
                super.onFailure(httpResponse);
                ((AppBaseActivity)context).mZeusLoadView.dismiss();
                showToast("获取部门联系人失败");
            }
        });
    }

}
