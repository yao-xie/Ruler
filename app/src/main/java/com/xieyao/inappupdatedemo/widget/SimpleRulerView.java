package com.xieyao.inappupdatedemo.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.xieyao.inappupdatedemo.BuildConfig;
import com.xieyao.inappupdatedemo.R;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static android.util.TypedValue.COMPLEX_UNIT_IN;
import static android.util.TypedValue.COMPLEX_UNIT_MM;
import static android.util.TypedValue.COMPLEX_UNIT_SP;

/**
 * this view is full screen
 */
public class SimpleRulerView extends View {

    private final String TAG = getClass().getSimpleName();

    private static final int UNITS_CENTIMETER = 1;
    private static final int UNITS_INCHES = UNITS_CENTIMETER + 1;

    private int mUinit = UNITS_CENTIMETER;

    private Paint mIndicatorPaint, mPointerPaint, mTextBgPaint;
    private TextPaint mTextPaint;

    private float gap;
    private float textSize;
    private int bgColor, pointerColor, textColor, shortIndicatorColor, longIndicatorColor;
    private float pointerWidth;
    private float shortIndicatorWidth, shortIndicatorLength;
    private float longIndicatorWidth, longIndicatorLength;
    private int width, height;
    private float touchX, touchY;

    private Callback mCallback;

    public SimpleRulerView(Context context) {
        this(context, null, 0);
    }

    public SimpleRulerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleRulerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //draw background
        canvas.drawColor(bgColor);
        //draw indicators
        drawIndicators(canvas);
        //draw pointer
        drawPointer(canvas);
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        touchX = event.getX();
        touchY = event.getY();
        logD("onTouchEvent: action=%d", action);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                invalidate();
                break;
            default:
                break;
        }
        return true;
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    private void init(Context context, AttributeSet attrs) {
        initAttrs(context, attrs);
        initPaints();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SimpleRulerView);
        reset();
        pointerWidth = ta.getDimension(R.styleable.SimpleRulerView_r_pointerWidth, dp2px(3f));
        pointerColor = ta.getColor(R.styleable.SimpleRulerView_r_pointerColor, Color.GRAY);
        bgColor = ta.getColor(R.styleable.SimpleRulerView_r_bgColor, Color.WHITE);
        textSize = ta.getDimensionPixelSize(R.styleable.SimpleRulerView_r_textSize, dp2px(14));
        textColor = ta.getColor(R.styleable.SimpleRulerView_r_textColor, Color.GRAY);
        shortIndicatorColor = ta.getColor(R.styleable.SimpleRulerView_r_shortIndicatorColor, Color.GRAY);
        shortIndicatorLength = ta.getDimension(R.styleable.SimpleRulerView_r_shortIndicatorLenth, dp2px(44));
        shortIndicatorWidth = ta.getDimension(R.styleable.SimpleRulerView_r_shortIndicatorWidth, dp2px(1));
        longIndicatorColor = ta.getColor(R.styleable.SimpleRulerView_r_longIndicatorColor, Color.GRAY);
        longIndicatorLength = ta.getDimension(R.styleable.SimpleRulerView_r_longindicatorLenth, dp2px(60));
        longIndicatorWidth = ta.getDimension(R.styleable.SimpleRulerView_r_longIndicatorWidth, dp2px(2));
    }

    private void reset() {
        touchX = -1;
        touchY = -1;
        setupGap();
    }

    private void setupGap() {
        if (mUinit == UNITS_INCHES) {
            gap = TypedValue.applyDimension(COMPLEX_UNIT_IN, 1, getResources().getDisplayMetrics()) / 10;
        } else {
            gap = TypedValue.applyDimension(COMPLEX_UNIT_MM, 1, getResources().getDisplayMetrics());
        }
    }

    private void initPaints() {
        mIndicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mIndicatorPaint.setStrokeCap(Paint.Cap.ROUND);

        mPointerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPointerPaint.setStrokeCap(Paint.Cap.ROUND);
        mPointerPaint.setColor(pointerColor);

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(textSize);
        mTextPaint.setColor(textColor);

        mTextBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextBgPaint.setStyle(Paint.Style.FILL);
        mTextBgPaint.setColor(bgColor);
    }

    private void setUnit(int unit) {
        this.mUinit = unit;
        setupGap();
        invalidate();
    }

    public void showInches() {
        setUnit(UNITS_INCHES);
    }

    public void showMillimeters() {
        setUnit(UNITS_CENTIMETER);
    }

    private void drawIndicators(Canvas canvas) {
        int startNum = 0;
        int maxNum = Math.round(width / gap);
        final int perUnitCount = 10;
        float distance = 0;
        while (startNum <= maxNum) {
            if (startNum % perUnitCount == 0) {
                mIndicatorPaint.setColor(longIndicatorColor);
                mIndicatorPaint.setStrokeWidth(longIndicatorWidth);
                //draw long indicator above
                canvas.drawLine(distance, 0, distance, longIndicatorLength, mIndicatorPaint);
                //draw long indicator below
                canvas.drawLine(distance, height - longIndicatorLength, distance, height, mIndicatorPaint);

                String text = String.valueOf(Math.round(startNum / 10f));
                logD("drawIndicator: text=%s", text);
                final float textWidth = mTextPaint.measureText(text);
                //draw text above
                canvas.drawText(text, distance - textWidth * .5f, longIndicatorLength + gap + textSize, mTextPaint);
                //draw text below
                canvas.drawText(text, distance - textWidth * .5f, height - longIndicatorLength - gap - textSize, mTextPaint);

            } else {
                mIndicatorPaint.setColor(shortIndicatorColor);
                mIndicatorPaint.setStrokeWidth(shortIndicatorWidth);
                //draw short indicator above
                canvas.drawLine(distance, 0, distance, shortIndicatorLength, mIndicatorPaint);
                //draw short indicator below
                canvas.drawLine(distance, height - shortIndicatorLength, distance, height, mIndicatorPaint);
            }
            startNum += 1;
            distance += gap;
        }
    }

    private void drawPointer(Canvas canvas) {
        if (-1 != touchX) {
            canvas.drawLine(touchX, 0, touchX, height, mPointerPaint);
            StringBuffer distance = new StringBuffer(String.format("%.2f", touchX / (gap * 10)));
            distance.append(" ");
            distance.append(mUinit == UNITS_INCHES ? "Inches" : "CM");
            String distanceStr = distance.toString();
            float textWidth = mTextPaint.measureText(distanceStr);
            Paint.FontMetrics fm = mTextPaint.getFontMetrics();
            float textHeight = fm.bottom - fm.top;
            if (touchY > height / 2) { //draw text below
                canvas.drawRect(touchX - textWidth / 2, height * 2 / 3 - textHeight * 0.67f, touchX + textWidth / 2, height * 2 / 3 + textHeight * 0.33f, mTextBgPaint);
                canvas.drawText(distanceStr, touchX - textWidth / 2, height * 2 / 3, mTextPaint);
            } else { //draw text above
                canvas.drawRect(touchX - textWidth / 2, height / 3 - textHeight * 0.67f, touchX + textWidth / 2, height / 3 + textHeight * 0.33f, mTextBgPaint);
                canvas.drawText(distanceStr, touchX - textWidth / 2, height / 3, mTextPaint);
            }
            if ("4.60 Inches".equals(distanceStr)) {
                mCallback.onShowUpdateButton();
            }
        }
    }

    private int dp2px(float dp) {
        return (int) TypedValue.applyDimension(COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private int sp2px(float sp) {
        return (int) TypedValue.applyDimension(COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
    }

    private float getScreenWidth() {
        return getResources().getDisplayMetrics().widthPixels;
    }

    private float getScreenHeight() {
        return getResources().getDisplayMetrics().heightPixels;
    }

    @SuppressWarnings("all")
    private void logD(String format, Object... args) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, String.format("xieyao@" + format, args));
        }
    }

    public interface Callback {

        void onShowUpdateButton();

    }
}