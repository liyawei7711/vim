package huaiye.com.vim.views;

import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.request.target.ImageViewTarget;

import huaiye.com.vim.common.AppUtils;


public class UniformScaleTransformationRight extends ImageViewTarget<Bitmap> {

    private ImageView target;

    public UniformScaleTransformationRight(ImageView target) {
        super(target);
        this.target = target;
    }

    @Override
    protected void setResource(Bitmap resource) {
        if (resource == null) {
            return;
        }
        view.setImageBitmap(resource);

        //获取原图的宽高
        int width = resource.getWidth();
        int height = resource.getHeight();

        //获取imageView的宽
        int imageViewWidth = target.getWidth();
        int imageViewHeigh = target.getHeight();
        if(imageViewWidth <= 0) {
            imageViewWidth = AppUtils.dp2px(target.getContext(), 150);
        }
        if(imageViewHeigh <= 0) {
            imageViewHeigh = AppUtils.dp2px(target.getContext(), 100);
        }

        if (height > imageViewHeigh) {
            float bili = (float) (height * 1.0 / imageViewHeigh);

            float widthNew = width / bili;
            float total;
            if (widthNew < imageViewWidth) {
                total = (widthNew - imageViewWidth) / 2;
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) target.getLayoutParams();
                if(total > 0) {
                    total = 0;
                }
                params.setMargins(0, 0, (int) total, 0);
                target.setLayoutParams(params);
            } else {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) target.getLayoutParams();
                params.setMargins(0, 0, 0, 0);
                target.setLayoutParams(params);
            }
        } else {
            float widthNew = width;
            float total;
            if (widthNew < imageViewWidth) {
                total = (widthNew - imageViewWidth) / 2;
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) target.getLayoutParams();
                if(total > 0) {
                    total = 0;
                }
                params.setMargins( 0, 0, (int) total,0);
                target.setLayoutParams(params);
            } else {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) target.getLayoutParams();
                params.setMargins(0, 0, 0, 0);
                target.setLayoutParams(params);
            }
        }

    }
}
