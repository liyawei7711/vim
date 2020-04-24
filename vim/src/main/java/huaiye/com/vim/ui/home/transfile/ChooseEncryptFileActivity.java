package huaiye.com.vim.ui.home.transfile;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.huaiye.cmf.sdp.SdpMessageCmCtrlRsp;
import com.huaiye.cmf.sdp.SdpMessageCmProcessIMRsp;
import com.huaiye.sdk.HYClient;
import com.huaiye.sdk.core.SdkCallback;
import com.huaiye.sdk.sdkabi._api.ApiEncrypt;
import com.huaiye.sdk.sdkabi._params.SdkParamsCenter;
import com.ttyy.commonanno.anno.BindLayout;
import com.ttyy.commonanno.anno.BindView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import huaiye.com.vim.EncryptUtil;
import huaiye.com.vim.R;
import huaiye.com.vim.bus.TransMsgBean;
import huaiye.com.vim.common.AppBaseActivity;
import huaiye.com.vim.common.AppUtils;
import huaiye.com.vim.common.recycle.LiteBaseAdapter;
import huaiye.com.vim.models.meet.bean.FileBean;
import huaiye.com.vim.neety.FileTransferServer;
import huaiye.com.vim.ui.meet.viewholder.FileHolder;

/**
 * author: admin
 * date: 2018/04/23
 * version: 0
 * mail: secret
 * desc: ChoosePhotoActivity
 */

@BindLayout(R.layout.activity_choose_photo)
public class ChooseEncryptFileActivity extends AppBaseActivity {

    @BindView(R.id.rv_data)
    RecyclerView rv_data;

    LiteBaseAdapter<FileBean> adapter;

    ArrayList<FileBean> arrays = new ArrayList<>();
    LinearLayoutManager linearLayoutManager;
    ArrayList<FileBean> temps = new ArrayList<>();//待发送数据
    File fC_BEANDI;
    File fC_LINSHI;


    @Override
    protected void initActionBar() {
        EventBus.getDefault().register(this);

        fC_BEANDI = new File(getExternalFilesDir(null) + File.separator + "Vim/chat/");
        if (!fC_BEANDI.exists()) {
            fC_BEANDI.mkdirs();
        }
        fC_LINSHI = new File(getExternalFilesDir(null) + File.separator + "Vim/chat/linshi/");
        if (!fC_LINSHI.exists()) {
            fC_LINSHI.mkdirs();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FileTransferServer.get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        getNavigate().setLeftClickListener(v -> {
            onBackPressed();
        }).setTitlText("传输文件").setRightText("确定").setRightClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for (FileBean temp : arrays) {
                    if (temp.isChecked) {
                        temps.add(temp);
                    }
                }
                if (temps.isEmpty()) {
                    finish();
                } else {
                    FileBean bean = temps.get(0);
                    temps.remove(0);
                    doChuanShu(bean);
                }
            }

        });
    }

    private void doChuanShu(FileBean temp) {
        if (!FileTransferServer.get().isConnected()) {
            showToast("服务器尚未开启");
            return;
        }
        temp.isChecked = false;
        mZeusLoadView.loadingText("正在传输").setLoading();

        String linshi = EncryptUtil.getNewFileChuanShu(temp.path, fC_LINSHI);
        EncryptUtil.localEncryptFile(temp.path, linshi, false,
                new SdkCallback<SdpMessageCmProcessIMRsp>() {
                    @Override
                    public void onSuccess(SdpMessageCmProcessIMRsp resp) {
                        FileTransferServer.get().sendFile(new File(resp.m_strData));
                    }

                    @Override
                    public void onError(SdkCallback.ErrorInfo sessionRsp) {
                        showToast("文件解密失败");
                        onEvent(new TransMsgBean(3, "error"));
                    }
                }
        );
    }

    @Override
    public void doInitDelay() {

        adapter = new LiteBaseAdapter<>(this,
                arrays,
                FileHolder.class,
                R.layout.item_file,
                v -> {
                    FileBean bean = (FileBean) v.getTag();
                    if (bean.isFile) {
                        bean.isChecked = !bean.isChecked;
                        adapter.notifyDataSetChanged();
                    } else {
                    }
                }, null);
        rv_data.setLayoutManager(linearLayoutManager = new LinearLayoutManager(this));
        rv_data.setAdapter(adapter);

        getFiles();
    }


    /**
     * 加载文档
     */
    public void getFiles() {
        arrays.clear();
        File[] files = fC_BEANDI.listFiles();
        if (files == null) {
            files = new File[0];
        }
        for (File f : files) {
//                if (f.canRead() && !f.isHidden() && f.canWrite() && f.length() > 0) {//可读且不是隐藏文件
            if (!f.isHidden() && f.isFile()) {//可读且不是隐藏文件
                FileBean bean = new FileBean();
                bean.name = f.getName();
                bean.showName = f.getName();
                bean.first = AppUtils.getFirstSpell(f.getName()).substring(0, 1);
                bean.path = f.getPath();
//                bean.end = f.getName().substring(f.getName().lastIndexOf("."));
                bean.parent = f.getParent();
                bean.isFile = f.isFile();
                arrays.add(bean);
            }

        }
        adapter.notifyDataSetChanged();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(TransMsgBean bean) {
        switch (bean.type) {
            case 1:
                String devId = bean.devId;
                String pwd = bean.pwd;
                HYClient.getModule(ApiEncrypt.class)
                        .encryptVerifyExternPwd(SdkParamsCenter.Encrypt.EncryptVerifyExternPwd()
                                        .setDevId(devId)
                                        .setPwd(pwd),
                                new SdkCallback<SdpMessageCmCtrlRsp>() {
                                    @Override
                                    public void onSuccess(SdpMessageCmCtrlRsp resp) {
                                        FileTransferServer.get().sendResponse(0x00);
                                    }

                                    @Override
                                    public void onError(ErrorInfo error) {
                                        FileTransferServer.get().sendResponse(0x01);
                                    }
                                });
                break;
            case 2:
                break;
            case 3:
                if (bean.file != null) {
                    File file = bean.file;
                    File fileNew = new File(fC_BEANDI + "/" + file.getName());
//                    if(!fileNew.exists()) {
//                        try {
//                            fileNew.createNewFile();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
                    EncryptUtil.localEncryptFile(file.getAbsolutePath(),
                            fileNew.getAbsolutePath(),
                            true,
                            new SdkCallback<SdpMessageCmProcessIMRsp>() {
                                @Override
                                public void onSuccess(SdpMessageCmProcessIMRsp resp) {
                                    showToast("文件已加密保存");
                                    getFiles();
                                }

                                @Override
                                public void onError(SdkCallback.ErrorInfo sessionRsp) {
                                    showToast("文件加密失败");
                                }
                            }
                    );
                } else {
                    if (temps.isEmpty()) {
                        mZeusLoadView.dismiss();
                        showToast("传输完成");
                        adapter.notifyDataSetChanged();
                        return;
                    }
                    FileBean fileBean = temps.get(0);
                    temps.remove(0);
                    doChuanShu(fileBean);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        File[] files = fC_LINSHI.listFiles();
        if (files == null) {
            files = new File[0];
        }
        for (File temp : files) {
            if (temp.exists()) {
                temp.delete();
            }
        }
    }
}
