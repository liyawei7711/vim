package huaiye.com.vim.ui.meet.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huaiye.sdk.sdkabi._params.SdkBaseParams;
import com.huaiye.sdk.sdpmsgs.meet.CGetMeetingInfoRsp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import huaiye.com.vim.R;
import huaiye.com.vim.common.AppBaseActivity;
import huaiye.com.vim.common.ErrorMsg;
import huaiye.com.vim.common.views.pickers.CustomDatePicker;
import huaiye.com.vim.common.views.pickers.SelectItemDialog;
import huaiye.com.vim.models.ModelApis;
import huaiye.com.vim.models.ModelCallback;
import huaiye.com.vim.models.contacts.bean.ContactsGroupUserListBean;
import huaiye.com.vim.ui.contacts.ContactsChoiceByAllFriendOrgActivity;
import huaiye.com.vim.ui.contacts.sharedata.ChoosedContacts;
import ttyy.com.jinnetwork.core.work.HTTPResponse;

import static android.app.Activity.RESULT_OK;
import static huaiye.com.vim.common.ErrorMsg.create_group_err_code;

/**
 * author: admin
 * date: 2018/01/15
 * version: 0
 * mail: secret
 * desc: MeetCreateHeaderView
 */
public class MeetCreateHeaderView extends RelativeLayout implements View.OnClickListener {

    EditText create_meet_name;
    EditText create_meet_detail;
    LinearLayout create_meet_time_layout;
    TextView create_meet_time;
    LinearLayout create_meet_duration_layout;
    EditText create_meet_duration;
    LinearLayout create_meet_add_person;

    boolean isMaster;
    boolean isReq = false;
    public boolean needAddSelfMain = true;
    /**
     * 2--主辅布局 其他--均等布局
     */
    public int mMeetModel;

    public void setmGroupInfoListBean(ContactsGroupUserListBean mGroupInfoListBean) {
        this.mGroupInfoListBean = mGroupInfoListBean;
    }

    private ContactsGroupUserListBean mGroupInfoListBean;

    ArrayList<SelectItemDialog.SelectBean> selectBeans = new ArrayList<>();
    String Temp = "yyyy-MM-dd HH:mm";
    SimpleDateFormat sdf = new SimpleDateFormat(Temp, Locale.CHINA);

    public void setOrder(boolean isOrder) {
        if (!isOrder) {
            create_meet_time_layout.setVisibility(View.GONE);
            create_meet_duration_layout.setVisibility(View.GONE);
        } else {
            create_meet_time_layout.setVisibility(View.VISIBLE);

            create_meet_time.setOnClickListener(this);
            create_meet_time.setText(sdf.format(new Date(System.currentTimeMillis() + 30 * 60 * 1000)).substring(5));
            create_meet_time.setHint(sdf.format(new Date(System.currentTimeMillis() + 30 * 60 * 1000)) + ":00");
            create_meet_duration.setText("2");
        }
    }

    public MeetCreateHeaderView(Context context) {
        this(context, null);
    }

    public MeetCreateHeaderView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MeetCreateHeaderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        View view = LayoutInflater.from(context).inflate(R.layout.header_meet_create_new, this);
        setMaster(true);

        create_meet_name = view.findViewById(R.id.create_meet_name);
        create_meet_detail = view.findViewById(R.id.create_meet_detail);
        create_meet_time_layout = view.findViewById(R.id.create_meet_time_layout);
        create_meet_time = view.findViewById(R.id.create_meet_time);
        create_meet_duration_layout = view.findViewById(R.id.create_meet_duration_layout);
        create_meet_duration = view.findViewById(R.id.create_meet_duration);
        create_meet_add_person = view.findViewById(R.id.create_meet_add_person);

        create_meet_add_person.setOnClickListener(this);
    }

    public void setMaster(boolean isMaster) {
        this.isMaster = isMaster;
    }

    public void setMeetName(String name) {
        if (create_meet_name == null) {
            return;
        }
        create_meet_name.setText(name);
    }

    public String getMeetName() {
        return create_meet_name.getText().toString();
    }

    public EditText getNameView() {
        return create_meet_name;
    }

    public String getMeetDesc() {
        return create_meet_detail.getText().toString();
    }

    public EditText getMeetDescView() {
        return create_meet_detail;
    }

    public String getMeetStartTime() {
        if (!create_meet_time.getText().toString().contains("-")) {
            return "";
        }
        return create_meet_time.getHint().toString();
    }

    public int getMeetLong() {
        if (TextUtils.isEmpty(create_meet_duration.getText()) || !TextUtils.isDigitsOnly(create_meet_duration.getText().toString())) {
            return 0;
        }
        return Integer.parseInt(create_meet_duration.getText().toString()) * 60 * 60;
    }

    public SdkBaseParams.MeetMode getModel() {
        return mMeetModel == 2 ? SdkBaseParams.MeetMode.Host : SdkBaseParams.MeetMode.Normal;
    }

    public boolean isMeetRecord() {
        return false;
    }

    public void createGroup(final AppBaseActivity activity) {
        if (isReq) return;
        isReq = true;
        ModelApis.Contacts().createGroup(create_meet_name.getText().toString(),
                ChoosedContacts.get().getContacts(false),
                new ModelCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        activity.showToast("创建成功");
                        activity.setResult(RESULT_OK);
                        activity.finish();
                        isReq = false;
                    }

                    @Override
                    public void onFailure(HTTPResponse httpResponse) {
                        super.onFailure(httpResponse);
                        activity.showToast(ErrorMsg.getMsg(create_group_err_code));
                        isReq = false;
                    }

                });
    }

    @Override
    public void onClick(View v) {
        if (!isMaster) return;
        switch (v.getId()) {

            case R.id.create_meet_time:
                CustomDatePicker customDatePicker = new CustomDatePicker(2, getContext(), new CustomDatePicker.ResultHandler() {
                    @Override
                    public void handle(String time, long timelong) {
                        create_meet_time.setText(time.substring(5));
                        create_meet_time.setHint(time + ":00");
                    }
                }, System.currentTimeMillis() + 60 * 1000, System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000);
                customDatePicker.showYear(false).setIsLoop(false);
                customDatePicker.show("");
                break;
            case R.id.create_meet_add_person:
                if (null != mGroupInfoListBean) {
                    Intent intent = new Intent(getContext(), ContactsChoiceByAllFriendOrgActivity.class);
                    intent.putExtra("isSelectUser", true);
                    intent.putExtra("needAddSelf", needAddSelfMain);
                    intent.putExtra("mGroupUserListBean", mGroupInfoListBean);
                    intent.putExtra("titleName", getContext().getString(R.string.add_meet_person));
                    ((Activity) getContext()).startActivityForResult(intent, 1000);
                } else {
                    Intent intent = new Intent(getContext(), ContactsChoiceByAllFriendOrgActivity.class);

                    intent.putExtra("isSelectUser", true);
                    intent.putExtra("needAddSelf", needAddSelfMain);
                    intent.putExtra("titleName", getContext().getString(R.string.add_meet_person));
                    ((Activity) getContext()).startActivityForResult(intent, 1000);
                }

                break;
            default:
                break;
        }
    }

    /**
     * 展示信息
     *
     * @param cGetMeetingInfoRsp
     */
    public void showInfo(CGetMeetingInfoRsp cGetMeetingInfoRsp) {
        mMeetModel = cGetMeetingInfoRsp.nMeetingMode;
        create_meet_name.setText(cGetMeetingInfoRsp.strMeetingName);
        create_meet_detail.setText(cGetMeetingInfoRsp.strMeetingDesc);
        create_meet_time.setText(cGetMeetingInfoRsp.strStartTime.substring(5, 16));
        create_meet_time.setHint(cGetMeetingInfoRsp.strStartTime);
        create_meet_duration.setText(cGetMeetingInfoRsp.nTimeDuration / 3600 + "");
    }
}