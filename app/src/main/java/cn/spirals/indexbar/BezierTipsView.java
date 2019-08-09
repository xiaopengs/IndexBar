package cn.spirals.indexbar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Xfermode;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

public class BezierTipsView extends View {
    private Paint mBezPaint;
    private Paint mRoundStrokePaint;
    private Paint mTouchPaint;
    private int mWidth;
    private int mHeight;
    private int mRadius;
    private final float bezFactor = 0.551915024494f;
    private Xfermode clearXfermode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
    private PointF p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11;
    private Path mBezPath;

    public BezierTipsView(Context context) {
        this(context, null);
    }

    public BezierTipsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BezierTipsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15, getResources().getDisplayMetrics());  //默认设置15dp
//
//        // 获得我们所定义的自定义样式属性
//        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.BezierRoundView, defStyleAttr, 0);
//        color_bez = array.getColor(R.styleable.BezierRoundView_color_bez, color_bez);
//        color_touch = array.getColor(R.styleable.BezierRoundView_color_touch, color_touch);
//        color_stroke = array.getColor(R.styleable.BezierRoundView_color_stroke, color_stroke);
//        time_animator = array.getInteger(R.styleable.BezierRoundView_time_animator, time_animator);
//        default_round_count = array.getInteger(R.styleable.BezierRoundView_round_count, default_round_count);
//        mRadius = array.getDimensionPixelSize(R.styleable.BezierRoundView_radius, mRadius);
//        array.recycle();
//
        init();
    }

    private void init(){
        int color_bez = 0xfffe626d;
        mBezPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBezPaint.setColor(color_bez);//默认QQ糖的颜色为粉红色
        mBezPaint.setStyle(Paint.Style.FILL);

        mBezPath = new Path();

        p0 = new PointF(0, -mRadius);//mRadius圆的半径
        p6 = new PointF(0, mRadius);

        p1 = new PointF(mRadius * bezFactor, -mRadius);//bezFactor即0.5519...
        p5 = new PointF(mRadius * bezFactor, mRadius);

        p2 = new PointF(mRadius, -mRadius * bezFactor);
        p4 = new PointF(mRadius, mRadius * bezFactor);

        p3 = new PointF(mRadius, 0);
        p9 = new PointF(-mRadius, 0);

        p11 = new PointF(-mRadius * bezFactor, -mRadius);
        p7 = new PointF(-mRadius * bezFactor, mRadius);

        p10 = new PointF(-mRadius, -mRadius * bezFactor);
        p8 = new PointF(-mRadius, mRadius * bezFactor);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mWidth = w;
        mHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(mWidth / 2, mHeight / 2);
        mBezPath.reset();

        mBezPath.moveTo(p0.x, p0.y);
        mBezPath.cubicTo(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y);
        mBezPath.cubicTo(p4.x, p4.y, p5.x, p5.y, p6.x, p6.y);
        mBezPath.cubicTo(p7.x, p7.y, p8.x, p8.y, p9.x, p9.y);
        mBezPath.cubicTo(p10.x, p10.y, p11.x, p11.y, p0.x, p0.y);
        mBezPath.close();

        canvas.drawPath(mBezPath, mBezPaint);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_DOWN:
                p2 = new PointF(event.getX() - mWidth / 2, -mRadius * bezFactor);
                p3 = new PointF(event.getX() - mWidth / 2, 0);
                p4 = new PointF(event.getX() - mWidth / 2, mRadius * bezFactor);

                invalidate();
                break;
        }

        return true;
    }
}
