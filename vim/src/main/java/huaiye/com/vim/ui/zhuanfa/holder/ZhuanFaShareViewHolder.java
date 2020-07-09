package huaiye.com.vim.ui.zhuanfa.holder;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import butterknife.BindView;
import huaiye.com.vim.R;
import huaiye.com.vim.common.recycle.LiteViewHolder;
import huaiye.com.vim.dao.AppDatas;
import huaiye.com.vim.ui.zhuanfa.ZhuanFaShareBean;

/**
 * author: admin
 * date: 2018/05/28
 * version: 0
 * mail: secret
 * desc: ChatViewHolder
 */
public class ZhuanFaShareViewHolder extends LiteViewHolder {
    @BindView(R.id.iv_head)
    ImageView iv_head;
    @BindView(R.id.tv_name)
    TextView tv_name;

    private RequestOptions requestFriendHeadOptions;
    private RequestOptions requestGroupHeadOptions;

    public ZhuanFaShareViewHolder(Context context, View view, View.OnClickListener ocl) {
        super(context, view, ocl);
        requestFriendHeadOptions = new RequestOptions();
        requestFriendHeadOptions.centerCrop()
                .dontAnimate()
                .format(DecodeFormat.PREFER_RGB_565)
                .placeholder(R.drawable.default_image_personal)
                .error(R.drawable.default_image_personal)
                .optionalTransform(new CircleCrop());

        requestGroupHeadOptions = new RequestOptions();
        requestGroupHeadOptions.centerCrop()
                .dontAnimate()
                .format(DecodeFormat.PREFER_RGB_565)
                .placeholder(R.drawable.ic_group_chat)
                .error(R.drawable.ic_group_chat)
                .optionalTransform(new CircleCrop());

        itemView.setOnClickListener(ocl);
    }

    @Override
    public void bindData(Object holder, int position, Object data, int size, List datas, Object extr) {
        ZhuanFaShareBean bean = (ZhuanFaShareBean) data;
        itemView.setTag(bean);

        tv_name.setText(bean.name);
        Glide.with(context)
                .load(AppDatas.Constants().getAddressWithoutPort() + bean.header)
                .apply(bean.isGroup ? requestGroupHeadOptions : requestFriendHeadOptions)
                .into(iv_head);
    }

}
