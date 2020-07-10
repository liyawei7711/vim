package huaiye.com.vim.common.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ProgressBar;

public class DownLoadProgressbar extends ProgressBar {

    String text;
    Paint Paint;


    public DownLoadProgressbar(Context context) {
        super(context);
        initText();
    }

    public DownLoadProgressbar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initText();
    }


    public DownLoadProgressbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initText();
    }

    @Override
    public synchronized void setProgress(int progress) {
        setText(progress);
        super.setProgress(progress);

    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //this.setText();
        Rect rect = new Rect();
        this.Paint.getTextBounds(this.text, 0, this.text.length(), rect);
        Paint.setTextSize(dp2px(18));
        int x = (getWidth() / 2) - rect.centerX();
        int y = (getHeight() / 2) - rect.centerY();
        canvas.drawText(this.text, x, y, this.Paint);
    }

    private int dp2px(int value) {

        float density = getContext().getResources().getDisplayMetrics().density;

        return (int) (density * value);
    }

    //初始化
    private void initText() {
        this.Paint = new Paint();
        this.Paint.setColor(Color.WHITE);

    }

    private void setText() {
        setText(this.getProgress());
    }

    //设置文字内容
    private void setText(int progress) {
        int i = (progress * 100) / this.getMax();
        this.text = String.valueOf(i) + "%";
    }

}
