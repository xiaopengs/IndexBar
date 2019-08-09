package cn.spirals.superindexbar;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

public class IndexBar extends View {
    private static final String TAG = "IndexBar";
    public static final String INDEX_STAR = "★";
    private OnIndexBarTouchListener listener;
    /** 是否触摸 */
    private boolean isTouch;
    private String[] mLetters;
    private int mChoose = -1;
    private Paint mPaint;
    private Paint mTipsTextPaint;
    private Paint mSelectedPaint;
    private Paint mCirclePaint;
    private float mTextSize;
    private float mTipsTextSize;
    private float mTextSizeChoose;
    private int mTextColor;
    private int mTextTipsColor;
    private int mTextColorChoose;
    private int mWidth;
    private int mHeight;
    private float mItemHeight;
    private float padding;

    private Drawable mTipsBackground;
    private Paint imagePaint;
    private int mTipsWidth;
    private int mTipsHeight;
    private int letterWidth;
    private int letterHeight;
    private Bitmap mTipsBitmap;

    //间隔距离
    private float distance = 80;
    //起始圆初始半径
    private float mRadius = 50;
    //起始圆变化半径
    private float mChangeRadius;
    //辅助圆变化半径
    private float mSupportChangeRadius;
    //起始圆圆心左边
    float mCenterPointX;
    float mCenterPointY;
    //辅助圆圆心坐标
    float mSupportCircleX;
    float mSupportCircleY;
    //第一阶段运动进度
    private float mProgress = 0;
    //第二阶段运动进度
    private float mProgress2 = 0;
    //整体运动进度 也是原始进度
    private float mOriginProgress;
    //第一阶段运动
    private int MOVE_STEP_ONE=1;
    //第二阶段运动
    private int MOVE_STEP_TWO=2;
    float controlPointX;
    float controlPointY;
    float mStartX;
    float mStartY;
    float endPointX ;
    float endPointY;
    private Path mPath;

    public IndexBar(Context context) {
        this(context, null);
    }

    public IndexBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IndexBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mTextColor = context.getResources().getColor(android.R.color.black);
        mTextTipsColor = context.getResources().getColor(android.R.color.black);
        mTextColorChoose = context.getResources().getColor(android.R.color.black);
        mTextSize = context.getResources().getDimensionPixelSize(R.dimen.indexbar_textSize);
        mTipsTextSize = context.getResources().getDimensionPixelSize(R.dimen.indexbar_tips_textSize);
        mTextSizeChoose = context.getResources().getDimensionPixelSize(R.dimen.indexbar_textSize_choose);
        mItemHeight = context.getResources().getDimension(R.dimen.indexbar_height_item);
        padding = context.getResources().getDimension(R.dimen.indexbar_padding);
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.IndexBar);

            mTextColor = a.getColor(R.styleable.IndexBar_sidebarTextColor, mTextColor);
            mTextTipsColor = a.getColor(R.styleable.IndexBar_sidebarTipsTextColorChoose, mTextTipsColor);
            mTextColorChoose = a.getColor(R.styleable.IndexBar_sidebarTextColorChoose, mTextColorChoose);
            mTextSize = a.getDimension(R.styleable.IndexBar_sidebarTextSize, mTextSize);
            mTipsTextSize = a.getDimension(R.styleable.IndexBar_sidebarTipsTextSize, mTipsTextSize);
            mTextSizeChoose = a.getDimension(R.styleable.IndexBar_sidebarTextSizeChoose, mTextSizeChoose);
            mItemHeight = a.getDimension(R.styleable.IndexBar_sidebarItemHeight, mItemHeight);
            padding = a.getDimension(R.styleable.IndexBar_sidebarTextPadding, padding);
            mTipsBackground = a.getDrawable(R.styleable.IndexBar_sidebarTipsDrawable);
            a.recycle();
        }
        initPaints();
        initTips();
    }

    private void initTips(){
        imagePaint = new Paint();
        imagePaint.setAntiAlias(true);
        if(mTipsBackground == null){
            return;
        }
        mTipsWidth = mTipsBackground.getIntrinsicWidth();
        mTipsHeight = mTipsBackground.getIntrinsicHeight();
        mTipsBitmap = this.drawableToBitmap(mTipsBackground);
    }

    private void initPaints() {
        mPaint = new Paint();
        mPaint.setTextSize(mTextSize);
        mPaint.setColor(mTextColor);
        mPaint.setTypeface(Typeface.DEFAULT);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setAntiAlias(true);

        mTipsTextPaint = new Paint();
        mTipsTextPaint.setTextSize(mTipsTextSize);
        mTipsTextPaint.setColor(mTextTipsColor);
        mTipsTextPaint.setTypeface(Typeface.DEFAULT);
        mTipsTextPaint.setTextAlign(Paint.Align.CENTER);
        mTipsTextPaint.setAntiAlias(true);

        mSelectedPaint = new Paint();
        mSelectedPaint.setTextSize(mTextSize);
        mSelectedPaint.setTypeface(Typeface.DEFAULT);
        mSelectedPaint.setTextAlign(Paint.Align.CENTER);
        mSelectedPaint.setColor(mTextColorChoose);
        mSelectedPaint.setAntiAlias(true);

        //动画的圆
        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setColor(Color.RED);
        mCirclePaint.setStyle(Paint.Style.FILL);
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setDither(true);

        mPath = new Path();
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) ((BitmapDrawable) drawable).getBitmap();
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap bitmap = this.createBitmapSafely(width, height, Bitmap.Config.ARGB_8888, 1);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

    public Bitmap createBitmapSafely(int width, int height, Bitmap.Config config, int retryCount) {
        try {
            return Bitmap.createBitmap(width, height, config);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            if (retryCount > 0) {
                System.gc();
                return createBitmapSafely(width, height, config, retryCount - 1);
            }
            return null;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Rect rectBound = new Rect();
        mPaint.getTextBounds("W",0,1,rectBound);
        letterWidth = rectBound.width() + (int)padding;
        letterHeight = rectBound.height() + (int)padding;

        int defaultWidth = getPaddingLeft() + letterWidth + getPaddingRight() + mTipsWidth;
        int lettersHeight = ((mLetters==null) ? 0 : letterHeight * mLetters.length);
        int defaultHeight = getPaddingTop() + lettersHeight + getPaddingBottom() + mTipsHeight;

        int measureSize = MeasureSpec.getSize(heightMeasureSpec);
        if(updateTextSize(measureSize, defaultHeight)){
            mPaint.getTextBounds("W",0,1,rectBound);
            letterHeight = rectBound.height() + (int)padding;
            defaultHeight = getPaddingTop() + lettersHeight + getPaddingBottom() + mTipsHeight;
        }
        int width = measureHandler(widthMeasureSpec,defaultWidth);
        int height = measureHandler(heightMeasureSpec,defaultHeight);
        setMeasuredDimension(width,height);
    }

    private int measureHandler(int measureSpec,int defaultSize){
        int result = defaultSize;
        int measureMode = MeasureSpec.getMode(measureSpec);
        int measureSize = MeasureSpec.getSize(measureSpec);
        if(measureMode == MeasureSpec.EXACTLY){
            result = measureSize;
        }else if(measureMode == MeasureSpec.AT_MOST){
            result = Math.min(defaultSize,measureSize);
        }
        return result;
    }

    private boolean updateTextSize(int heightMeasureSize, int needSize){
        if(mLetters == null){
            return false;
        }
        if(heightMeasureSize >= needSize){
            return false;
        }

        float fitTextSize = findLargestTextSizeWhichFits(heightMeasureSize, needSize);
        mPaint.setTextSize(fitTextSize);
        return true;
    }

    private float findLargestTextSizeWhichFits(int heightMeasureSize, int needSize){
        float curSize = mPaint.getTextSize();
        // find a suitable text size using binary search
        float minSize = 1;
        float maxSize = 100;

        int measuredHeight = heightMeasureSize - (getPaddingTop() + getPaddingBottom());
        while (minSize < maxSize) {
            curSize = (minSize + maxSize) / 2;
            if (calHeightOnMeasure(curSize) <= measuredHeight) {
                minSize = curSize + 1;
            } else {
                maxSize = curSize - 1;
            }
        }
        return curSize;
    }

    private int calHeightOnMeasure(float textSize) {
        int result = 0;
        int indexSize = mLetters.length;
        final Paint textPaint = mPaint;
        textPaint.setTextSize(textSize);
        textPaint.setAntiAlias(true);
        Rect bound = new Rect();
        for (int i = 0; i < indexSize; i++) {
            textPaint.getTextBounds(mLetters[i], 0, mLetters[i].length(), bound);
            int height = (bound.bottom - bound.top);
            result += height;
            result += (int)padding;
        }
        result = result + mTipsHeight;
        return result;
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mWidth = getWidth();
        mHeight = getHeight();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawLetters(canvas);
        //drawTips(canvas);
        drawBezierPath(canvas);
    }

    private void drawBezierPath(Canvas canvas){
        canvas.save();
        canvas.translate(getPaddingLeft(), getPaddingTop());
        canvas.drawCircle(mSupportCircleX, mSupportCircleY, mSupportChangeRadius, mCirclePaint);
        canvas.drawCircle(mCenterPointX, mCenterPointY, mChangeRadius, mCirclePaint);
        canvas.drawPath(mPath, mCirclePaint);
        if(mLetters != null && mChoose > -1){
            Rect rect = new Rect();
            mTipsTextPaint.getTextBounds(mLetters[mChoose], 0, mLetters[mChoose].length(), rect);
            float scale = accelerateinterpolator.getInterpolation(mOriginProgress);
            mTipsTextPaint.setTextSize(scale * mTipsTextSize);
            canvas.drawText(mLetters[mChoose], mSupportCircleX , mSupportCircleY + rect.height()/2.0f, mTipsTextPaint);
        }
        canvas.restore();
    }

    private void drawLetters(Canvas canvas){
        if(mLetters == null){
            return;
        }
        int len = mLetters.length;
        float startY = mTipsHeight / 2f;

        for (int i = 0; i < len; i++) {
            //计算位置
            Paint tempPaint = null;
            if(i == mChoose){
                tempPaint = mSelectedPaint;
            }else{
                tempPaint = mPaint;
            }

            //要画的字母的x,y坐标
            //float letterWidth = tempPaint.measureText(mLetters[i]);

            float x = mWidth - letterWidth / 2f;
            float y = startY + letterHeight * (i+1) - letterHeight/2f;
            //画字母
            canvas.drawText(mLetters[i], x, y, tempPaint);
        }
    }

    private void drawTips(Canvas canvas){
        if(mTipsBackground != null && isTouch){
            float startTop = letterHeight / 2f;
            if(mChoose > 0){
                startTop = letterHeight / 2f + letterHeight * mChoose;
            }
            canvas.drawBitmap(mTipsBitmap, 0, startTop, imagePaint);

            //draw tips letter
            Rect rect = new Rect();
            mTipsTextPaint.getTextBounds(mLetters[mChoose], 0, mLetters[mChoose].length(), rect);
            float startTipsX = (int)(mTipsWidth* 4.0 / 7.0f - rect.width()/2.0);
            float startTipsY = (int) (startTop + mTipsHeight / 2.0f + rect.height()/2.0);
            canvas.drawText(mLetters[mChoose], startTipsX, startTipsY, mTipsTextPaint);
        }
    }


    public void setProgress(float progress) {
        mOriginProgress=progress;
        if(progress<=0.5){
            mProgress = progress/0.5f;
            mProgress2=0;
        }else{
            mProgress2=(progress-0.5f)/0.5f;
            mProgress=1;
        }

        moveToPrivious();
        invalidate();
    }

    Interpolator accelerateinterpolator = new AccelerateDecelerateInterpolator();
    /**
     * 向左移动(与向右过程大致相同)
     */
    private void moveToPrivious() {
        float startTop = letterHeight / 2f;
        if(mChoose > 0){
            startTop = letterHeight / 2f + letterHeight * mChoose;
        }
        startTop = startTop + mRadius;

        mPath.reset();
        float mRadiusProgress = accelerateinterpolator.getInterpolation(mOriginProgress);
        //起始圆
        mCenterPointX = getValue(getCenterPointAt(2),getCenterPointAt(1)+mRadius,MOVE_STEP_TWO);
        mCenterPointY = startTop;//mRadius;
        mChangeRadius = getValue(mRadius,0,mRadiusProgress);

        double radian = Math.toRadians(getValue(45, 0,MOVE_STEP_ONE));
        float mX = (float) (Math.sin(radian) * mChangeRadius);
        float mY = (float) (Math.cos(radian) * mChangeRadius);

        //辅助圆
        mSupportCircleX = getValue(getCenterPointAt(2)-mRadius, getCenterPointAt(1),MOVE_STEP_ONE);
        mSupportCircleY = startTop;//mRadius;
        mSupportChangeRadius = getValue(0, mRadius, mRadiusProgress);

        double supportradian = Math.toRadians(getValue(0, 45,MOVE_STEP_TWO));
        float msupportradianX = (float) (Math.sin(supportradian) * mSupportChangeRadius);
        float msupportradianY = (float) (Math.cos(supportradian) * mSupportChangeRadius);

        mStartX = mCenterPointX - mX;
        mStartY = mCenterPointY - mY;

        endPointX = mSupportCircleX+msupportradianX;
        endPointY = startTop - msupportradianY;//mRadius - msupportradianY;

        controlPointX = getValueForAll(getCenterPointAt(2)-mRadius, getCenterPointAt(1)+mRadius);
        controlPointY = startTop;//mRadius;

        mPath.moveTo(mStartX, mStartY);
        mPath.quadTo(controlPointX, controlPointY, endPointX, endPointY);
        mPath.lineTo(endPointX, startTop + msupportradianY);
        mPath.quadTo(controlPointX, startTop , mStartX, mStartY + 2 * mY);
        mPath.lineTo(mStartX, mStartY);
    }

    /**
     * 获取当前值(适用分阶段变化的值)
     * @param start 初始值
     * @param end  终值
     * @param step  第几活动阶段
     * @return
     */
    public float getValue(float start, float end, int step) {
        if(step==MOVE_STEP_ONE) {
            return start + (end - start) * mProgress;
        }else{

            return start + (end - start) * mProgress2;
        }
    }
    /**
     * 获取当前值（适用全过程变化的值）
     * @param start 初始值
     * @param end  终值
     * @return
     */
    public float getValueForAll(float start, float end){
        return start + (end - start) * mOriginProgress;
    }

    /**
     * 通过进度获取当前值
     * @param start 初始值
     * @param end 终值
     * @param progress 当前进度
     * @return
     */
    public float getValue(float start, float end, float progress) {
        return start + (end - start) * progress;
    }

    /**
     * 获取圆心X坐标
     * @param index 第几个圆
     * @return
     */
    private float getCenterPointAt(int index) {
        if (index == 1) {
            return mRadius;
        }
        return mRadius*3+distance;
    }

    //展示QQ糖动画
    private ValueAnimator animatorStart;
    private ValueAnimator animatorEnd;
    private ValueAnimator animatorAlphaEnd;
    private TimeInterpolator timeInterpolator = new DecelerateInterpolator();
    private float animatedValue;
    private boolean isAniming = false;

    public void startAnimator() {
        if (animatorStart != null) {
            if (animatorStart.isRunning()) {
                return;
            }
            animatorStart.start();
        } else {
            animatorStart = ValueAnimator.ofFloat(0, 1f).setDuration(300);
            animatorStart.setInterpolator(timeInterpolator);
            animatorStart.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    isAniming = true;
                    animatedValue = (float) animation.getAnimatedValue();
                    setProgress(animatedValue);
                    invalidate();

                }
            });
            animatorStart.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    mCirclePaint.setAlpha(255);
                    isAniming = true;
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    isAniming = false;
                    //setProgress(1);
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                    isAniming = false;
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                }
            });

            animatorStart.start();
        }
    }

    public void endAnimator() {
        if (animatorEnd != null) {
            if (animatorEnd.isRunning()) {
                return;
            }
            animatorEnd.start();
        } else {
            animatorEnd = ValueAnimator.ofFloat(1f, 0).setDuration(300);
            animatorEnd.setInterpolator(timeInterpolator);
            animatorEnd.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    isAniming = true;
                    animatedValue = (float) animation.getAnimatedValue();
                    setProgress(animatedValue);
                    invalidate();

                }
            });
            animatorEnd.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    isAniming = true;
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    isAniming = false;
                    isTouch = false;
                    mChoose = -1;
                    if (listener != null) {
                        listener.onLetterTouching(false);
                    }
                    invalidate();
                    endAlphaAnimator();
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                    isAniming = false;
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                }
            });

            animatorEnd.start();
        }
    }

    public void endAlphaAnimator() {
        if (animatorAlphaEnd != null) {
            if (animatorAlphaEnd.isRunning()) {
                return;
            }
            animatorAlphaEnd.start();
        } else {
            animatorAlphaEnd = ValueAnimator.ofFloat(1f, 0).setDuration(300);
            animatorAlphaEnd.setInterpolator(timeInterpolator);
            animatorAlphaEnd.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int alpha = (int)((float) animation.getAnimatedValue() * 255);
                    mCirclePaint.setAlpha(alpha);
                    invalidate();

                }
            });

            animatorAlphaEnd.start();
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final float x = event.getX();
        final float y = event.getY();
        final int oldChoose = mChoose;
        //当前选中字母的索引
        int newChoose = -1;
        if(mLetters != null){
            float half = mTipsHeight / 2f;
            float deltY = 0f;
            float letterWhole = letterHeight * mLetters.length;
            if(y < half){
                deltY = 0;
            }else if(y > letterWhole + half){
                deltY = letterWhole;
            }else{
                deltY = y - half;
            }
            float n = (deltY/letterWhole);
            newChoose = (int)(n * mLetters.length);
            Log.i(TAG, "jerrypxiao newChoose =" + newChoose + ", y=" + y + ", half = " + half
                    + ", mHeight=" + mHeight + ", n=" + n + ", letterWhole =" + letterWhole + "， deltY =" + deltY
                    + "mLetters.length =" + mLetters.length);
        }
        switch (action) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                endAnimator();

                return true;
            case MotionEvent.ACTION_DOWN:
                boolean isValid = isValid(x, y);
                if(!isValid){
                    return super.onTouchEvent(event);
                }

            case MotionEvent.ACTION_MOVE:
                if (oldChoose != newChoose) {
                    if (mLetters != null
                            && newChoose >= 0 && newChoose < mLetters.length) {
                        mChoose = newChoose;
                        if (listener != null) {
                            //计算位置
                            Rect rect = new Rect();
                            mPaint.getTextBounds(mLetters[mChoose], 0, mLetters[mChoose].length(), rect);
                            //字母的个数
                            int len = mLetters.length;
                            //单个字母的高度
                            int singleHeight = mHeight / len;
                            float yPos = singleHeight * (mChoose + 1) - mPaint.measureText(mLetters[mChoose]) / 2;
                            listener.onLetterChanged(mLetters[newChoose], action, yPos);
                            //test
                            if(action == MotionEvent.ACTION_DOWN){
                                startAnimator();
                            }else{
                                setProgress(1);
                            }
                        }
                    }
                    invalidate();
                }

                if (event.getAction() == MotionEvent.ACTION_DOWN) {//按下调用 onLetterDownListener
                    isTouch = true;
                    if (listener != null) {
                        listener.onLetterTouching(true);
                    }
                    invalidate();
                }
                return true;
            default:
                return false;
        }
    }

    private boolean isValid(float x, float y){
        if(x >= mTipsWidth && x <= mWidth
                && y > mTipsHeight/2f && (y < mHeight - mTipsHeight/2f)){
            return true;
        }else {
            return false;
        }
    }

    public OnIndexBarTouchListener getListener() {
        return listener;
    }

    public void setOnIndexBarTouchListener(OnIndexBarTouchListener listener) {
        this.listener = listener;
    }

    public String[] getLetters() {
        return mLetters;
    }

    /**
     * 设置字母表
     * @param letters
     */
    public void setLetters(String[] letters) {
        this.mLetters = letters;
        requestLayout();
        invalidate();
    }

    public void setChooseIndex(int i){
        mChoose = i;
        invalidate();
    }


    public interface OnIndexBarTouchListener {
        void onLetterChanged(String letter, int action, float yPos);
        void onLetterTouching(boolean touching);
    }
}
