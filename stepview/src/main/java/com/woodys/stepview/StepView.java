package com.woodys.stepview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.woodys.stepview.animation.AnimatorListener;
import com.woodys.stepview.callback.OnViewClickListenerWrapper;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StepView extends View {
    public static final int ANIMATION_LINE = 0;
    public static final int ANIMATION_CIRCLE = 1;
    public static final int ANIMATION_ALL = 2;
    public static final int ANIMATION_NONE = 3;

    public static final int DISPLAY_MODE_WITH_TEXT = 0;
    public static final int DISPLAY_MODE_NO_TEXT = 1;
    //是否领取礼物
    private boolean isReceive;
    private Drawable undoDrawable;
    private Drawable underwayDrawable;
    private Drawable completedDrawable;
    private Drawable giftDrawable;
    private Drawable openGiftDrawable;
    private float iconDrawableHeight;
    private float iconDrawableWith;
    private float giftDrawableHeight;
    private float giftDrawableWith;
    private boolean isShowStepNumber;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ANIMATION_LINE, ANIMATION_CIRCLE, ANIMATION_ALL, ANIMATION_NONE})
    public @interface AnimationType {
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({DISPLAY_MODE_WITH_TEXT, DISPLAY_MODE_NO_TEXT})
    public @interface DisplayMode {
    }

    private static final int ANIMATE_STEP_TRANSITION = 0;
    private static final int IDLE = 1;

    private static final int START_STEP = 0;
    @DisplayMode
    private int displayMode = DISPLAY_MODE_WITH_TEXT;
    private List<String> steps = new ArrayList<>();
    // for display mode DISPLAY_MODE_NO_TEXT
    private int stepsNumber = 0;
    private int currentStep = START_STEP;
    private int nextAnimatedStep;
    private int state = IDLE;

    @AnimationType
    private int animationType;

    private float drawablePadding;

    @ColorInt
    private int selectedCircleColor;
    @ColorInt
    private int unSelectedCircleColor;

    private int unSelectedCircleRadius;
    private int selectedCircleRadius;
    @ColorInt
    private int selectedTextColor;
    @ColorInt
    private int doneCircleColor;

    private int doneCircleRadius;
    @ColorInt
    private int doneTextColor;
    @ColorInt
    private int nextTextColor;
    private int stepPadding;
    @ColorInt
    private int nextStepLineColor;
    @ColorInt
    private int doneStepLineColor;
    private int stepLineWidth;
    private float textSize;

    private int textPadding;
    private int selectedStepNumberColor;
    private float stepNumberTextSize;
    @ColorInt
    private int doneStepMarkColor;
    private int animationDuration;

    private Paint paint;
    private ValueAnimator animator;

    private int[] circlesX;
    private int[] startLinesX;
    private int[] endLinesX;
    private int circlesY;
    private int textY;
    private float animatedFraction;
    private boolean isDone;

    private Map<Integer, Rect> drawIconRects = new HashMap<Integer, Rect>();
    private OnViewClickListenerWrapper viewClickListenerWrapper;

    public StepView(Context context) {
        this(context, null);
    }

    public StepView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.sv_stepViewStyle);
    }

    public StepView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextAlign(Paint.Align.CENTER);
        applyStyles(context, attrs, defStyleAttr);
        drawEditMode();
    }

    private void applyStyles(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.StepView, defStyleAttr, R.style.StepView);
        unSelectedCircleColor = ta.getColor(R.styleable.StepView_sv_unSelectedCircleColor, 0);
        selectedCircleColor = ta.getColor(R.styleable.StepView_sv_selectedCircleColor, 0);
        selectedCircleRadius = ta.getDimensionPixelSize(R.styleable.StepView_sv_selectedCircleRadius, 0);
        unSelectedCircleRadius = ta.getDimensionPixelSize(R.styleable.StepView_sv_unSelectedCircleRadius, 0);
        selectedTextColor = ta.getColor(R.styleable.StepView_sv_selectedTextColor, 0);
        selectedStepNumberColor = ta.getColor(R.styleable.StepView_sv_selectedStepNumberColor, 0);
        isShowStepNumber = ta.getBoolean(R.styleable.StepView_sv_isShowStepNumber, false);

        doneStepMarkColor = ta.getColor(R.styleable.StepView_sv_doneStepMarkColor, 0);
        doneCircleColor = ta.getColor(R.styleable.StepView_sv_doneCircleColor, 0);
        doneCircleRadius = ta.getDimensionPixelSize(R.styleable.StepView_sv_doneCircleRadius, 0);
        doneTextColor = ta.getColor(R.styleable.StepView_sv_doneTextColor, 0);
        nextTextColor = ta.getColor(R.styleable.StepView_sv_nextTextColor, 0);
        stepPadding = ta.getDimensionPixelSize(R.styleable.StepView_sv_stepPadding, 0);
        nextStepLineColor = ta.getColor(R.styleable.StepView_sv_nextStepLineColor, 0);
        doneStepLineColor = ta.getColor(R.styleable.StepView_sv_doneStepLineColor, 0);
        stepLineWidth = ta.getDimensionPixelSize(R.styleable.StepView_sv_stepLineWidth, 0);
        textPadding = ta.getDimensionPixelSize(R.styleable.StepView_sv_textPadding, 0);
        stepNumberTextSize = ta.getDimension(R.styleable.StepView_sv_stepNumberTextSize, 0);
        textSize = ta.getDimension(R.styleable.StepView_sv_bottomTextSize, 0);
        animationDuration = ta.getInteger(R.styleable.StepView_sv_viewAnimationDuration, 0);
        animationType = ta.getInteger(R.styleable.StepView_sv_viewAnimationType, ANIMATION_LINE);
        stepsNumber = ta.getInteger(R.styleable.StepView_sv_stepsNumber, 0);
        CharSequence[] descriptions = ta.getTextArray(R.styleable.StepView_sv_steps);
        if (descriptions != null) {
            for (CharSequence item : descriptions) {
                steps.add(item.toString());
            }
            displayMode = DISPLAY_MODE_WITH_TEXT;
        } else {
            displayMode = DISPLAY_MODE_NO_TEXT;
        }
        Drawable background = ta.getDrawable(R.styleable.StepView_sv_viewBackground);
        if (background != null) {
            setBackgroundDrawable(background);
        }
        isReceive = ta.getBoolean(R.styleable.StepView_sv_isReceive, false);

        iconDrawableHeight = ta.getDimension(R.styleable.StepView_sv_iconDrawableHeight, 0);
        iconDrawableWith = ta.getDimension(R.styleable.StepView_sv_iconDrawableWith, 0);


        undoDrawable = ta.getDrawable(R.styleable.StepView_sv_undoDrawable);
        underwayDrawable = ta.getDrawable(R.styleable.StepView_sv_underwayDrawable);
        completedDrawable = ta.getDrawable(R.styleable.StepView_sv_completedDrawable);
        giftDrawable = ta.getDrawable(R.styleable.StepView_sv_giftDrawable);
        openGiftDrawable = ta.getDrawable(R.styleable.StepView_sv_openGiftDrawable);

        giftDrawableHeight = ta.getDimension(R.styleable.StepView_sv_giftDrawableHeight, 0);
        giftDrawableWith = ta.getDimension(R.styleable.StepView_sv_giftDrawableWith, 0);
        drawablePadding = ta.getDimension(R.styleable.StepView_sv_iconDrawablePadding, 0);
        ta.recycle();
    }

    private void drawEditMode() {
        if (isInEditMode()) {
            if (displayMode == DISPLAY_MODE_WITH_TEXT) {
                if (steps.isEmpty()) {
                    steps.add("Step 1");
                    steps.add("Step 2");
                    steps.add("Step 3");
                }
                setSteps(steps);
            } else {
                if (stepsNumber == 0) {
                    stepsNumber = 4;
                }
                setStepsNumber(stepsNumber);
            }
        }
    }

    public void setSteps(List<String> steps) {
        stepsNumber = 0;
        displayMode = DISPLAY_MODE_WITH_TEXT;
        this.steps.clear();
        if (steps != null) {
            this.steps.addAll(steps);
        }
        requestLayout();
        go(START_STEP, false);
    }

    public void setStepsNumber(int number) {
        steps.clear();
        displayMode = DISPLAY_MODE_NO_TEXT;
        stepsNumber = number;
        requestLayout();
        go(START_STEP, false);
    }

    public void go(int step, boolean animate) {
        if (step >= START_STEP && step < getStepCount()) {
            if (animate && animationType != ANIMATION_NONE) {
                if (Math.abs(step - currentStep) > 1) {
                    endAnimation();
                    currentStep = step;
                    invalidate();
                } else {
                    nextAnimatedStep = step;
                    state = ANIMATE_STEP_TRANSITION;
                    animate(step);
                    invalidate();
                }
            } else {
                currentStep = step;
                invalidate();
            }
        }
    }

    public void isDone(boolean isDone) {
        this.isDone = isDone;
        invalidate();
    }

    public boolean isDone(){
        return isDone;
    }

    public void isReceive(boolean receive) {
        isReceive = receive;
        invalidate();
    }
    public boolean isReceive(){
        return isReceive;
    }

    private void endAnimation() {
        if (animator != null && animator.isRunning()) {
            animator.end();
        }
    }

    private void animate(final int step) {
        endAnimation();
        animator = getAnimator(step);
        if (animator == null) {
            return;
        }
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                animatedFraction = valueAnimator.getAnimatedFraction();
                invalidate();
            }
        });
        animator.addListener(new AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animator) {
                state = IDLE;
                currentStep = step;
                invalidate();
            }
        });
        animator.setDuration(animationDuration);
        animator.start();
    }

    @Nullable
    private ValueAnimator getAnimator(int step) {
        ValueAnimator animator = null;
        final int i;
        if (step > currentStep) {
            if (animationType == ANIMATION_LINE) {
                i = step - 1;
                animator = ValueAnimator.ofInt(startLinesX[i], endLinesX[i]);
            } else if (animationType == ANIMATION_CIRCLE) {
                animator = ValueAnimator.ofInt(0, selectedCircleRadius);
            } else if (animationType == ANIMATION_ALL) {
                i = step - 1;
                animator = ValueAnimator.ofInt(0, (endLinesX[i] - startLinesX[i] + selectedCircleRadius) / 2);
            }
        } else if (step < currentStep) {
            if (animationType == ANIMATION_LINE) {
                i = step;
                animator = ValueAnimator.ofInt(endLinesX[i], startLinesX[i]);
            } else if (animationType == ANIMATION_CIRCLE) {
                animator = ValueAnimator.ofInt(0, selectedCircleRadius);
            } else if (animationType == ANIMATION_ALL) {
                i = step;
                animator = ValueAnimator.ofInt(0, (endLinesX[i] - startLinesX[i] + selectedCircleRadius) / 2);
            }
        }
        return animator;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public int getStepCount() {
        return displayMode == DISPLAY_MODE_WITH_TEXT ? steps.size() : stepsNumber;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
        measureAttributes();
    }

    private int measureWidth(int widthMeasureSpec) {
        return MeasureSpec.getSize(widthMeasureSpec);
    }

    private int measureHeight(int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (heightMode == MeasureSpec.AT_MOST) {
            height = getPaddingTop()
                    + getPaddingBottom()
                    + measureIconDrawableHeight()
                    + (Math.max(unSelectedCircleRadius, Math.max(selectedCircleRadius, doneCircleRadius))) * 2
                    + (displayMode == DISPLAY_MODE_WITH_TEXT ? textPadding : 0);
            if (!steps.isEmpty()) {
                height += measureStepsHeight();
            }
        }

        return height;
    }

    /**
     * 测试图片的高度
     *
     * @return
     */
    private int measureIconDrawableHeight() {
        return (int) drawablePadding
                + Math.max((int) iconDrawableHeight, (int) giftDrawableHeight);
    }

    private int fontHeight() {
        return (int) Math.ceil(paint.descent() - paint.ascent());
    }

    private int measureStepsHeight() {
        paint.setTextSize(textSize);
        int fontHeight = fontHeight();
        int max = 0;
        for (int i = 0; i < steps.size(); i++) {
            String text = steps.get(i);
            String[] split = text.split("\\n");
            if (split.length == 1) {
                max = Math.max(fontHeight, max);
            } else {
                max = Math.max(fontHeight * split.length, max);
            }
        }
        return max;
    }

    private void measureAttributes() {
        circlesY = (getMeasuredHeight() - measureIconDrawableHeight() - getPaddingTop() - getPaddingBottom()) / 2 + measureIconDrawableHeight();
        if (displayMode == DISPLAY_MODE_NO_TEXT) {
            circlesY += getPaddingTop();
        }
        circlesX = getCirclePositions();
        if (displayMode == DISPLAY_MODE_NO_TEXT) {
            paint.setTextSize(stepNumberTextSize);
        } else {
            paint.setTextSize(stepNumberTextSize);
            paint.setTextSize(textSize);
            textY = circlesY + selectedCircleRadius + textPadding + fontHeight() / 2;
        }
        measureLines();
    }

    private int[] getCirclePositions() {
        if (displayMode == DISPLAY_MODE_WITH_TEXT) {
            return getCirclePositionsWithText(measureSteps());
        } else {
            return getCirclePositionsWithoutText();
        }
    }

    private int[] measureSteps() {
        int[] result = new int[steps.size()];
        for (int i = 0; i < steps.size(); i++) {
            result[i] = (int) paint.measureText(steps.get(i)) + /* correct possible conversion error */ 1;
        }
        return result;
    }

    private int[] getCirclePositionsWithText(int[] textWidth) {
        int[] result = new int[textWidth.length];
        if (result.length == 0) {
            return result;
        }
        result[0] = getPaddingLeft() + Math.max(textWidth[0] / 2, selectedCircleRadius);
        if (result.length == 1) {
            return result;
        }
        result[textWidth.length - 1] = getMeasuredWidth() - getPaddingRight() -
                Math.max(textWidth[textWidth.length - 1] / 2, selectedCircleRadius);
        if (result.length < 3) {
            return result;
        }
        float spaceLeft = result[textWidth.length - 1] - result[0];
        int margin = (int) (spaceLeft / (textWidth.length - 1));
        for (int i = 1; i < textWidth.length - 1; i++) {
            result[i] = result[i - 1] + margin;
        }
        return result;
    }

    private int[] getCirclePositionsWithoutText() {
        int[] result = new int[getStepCount()];
        if (result.length == 0) {
            return result;
        }
        result[0] = getPaddingLeft() + selectedCircleRadius;
        if (result.length == 1) {
            return result;
        }
        result[result.length - 1] = getMeasuredWidth() - getPaddingRight();
        float spaceLeft = result[result.length - 1] - result[0];
        int margin = (int) (spaceLeft / (result.length - 1));
        for (int i = 1; i < result.length - 1; i++) {
            result[i] = result[i - 1] + margin;
        }
        return result;
    }

    private void measureLines() {
        startLinesX = new int[getStepCount() - 1];
        endLinesX = new int[getStepCount() - 1];
        int padding = stepPadding + selectedCircleRadius;

        for (int i = 1; i < getStepCount(); i++) {
            startLinesX[i - 1] = circlesX[i - 1] + padding;
            endLinesX[i - 1] = circlesX[i] - padding;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int stepSize = getStepCount();

        if (stepSize == 0) {
            return;
        }

        for (int i = 0; i < stepSize; i++) {
            drawStep(canvas, i, circlesX[i], circlesY);
        }

        for (int i = 0; i < startLinesX.length; i++) {
            if (state == ANIMATE_STEP_TRANSITION && i == nextAnimatedStep - 1
                    && nextAnimatedStep > currentStep && (animationType == ANIMATION_LINE || animationType == ANIMATION_ALL)) {
                int animatedX = (int) (startLinesX[i] + animatedFraction * (endLinesX[i] - startLinesX[i]));
                drawLine(canvas, startLinesX[i], animatedX, circlesY, true);
                drawLine(canvas, animatedX, endLinesX[i], circlesY, false);
            } else if (state == ANIMATE_STEP_TRANSITION && i == nextAnimatedStep
                    && nextAnimatedStep < currentStep && (animationType == ANIMATION_LINE || animationType == ANIMATION_ALL)) {
                int animatedX = (int) (endLinesX[i] - animatedFraction * (endLinesX[i] - startLinesX[i]));
                drawLine(canvas, startLinesX[i], animatedX, circlesY, true);
                drawLine(canvas, animatedX, endLinesX[i], circlesY, false);
            } else if (i < currentStep) {
                drawLine(canvas, startLinesX[i], endLinesX[i], circlesY, true);
            } else {
                drawLine(canvas, startLinesX[i], endLinesX[i], circlesY, false);
            }
        }
    }

    private void drawStep(Canvas canvas, int step, int circleCenterX, int circleCenterY) {
        final String text = displayMode == DISPLAY_MODE_WITH_TEXT ? steps.get(step) : "";
        final boolean isSelected = step == currentStep;
        final boolean isDone = this.isDone ? step <= currentStep : step < currentStep;
        final String number = String.valueOf(step + 1);

        if (isSelected && !isDone) {
            if (step == getStepCount() - 1) {
                drawIcon(canvas, step, giftDrawable, giftDrawableWith, giftDrawableHeight, circleCenterX);
            } else {
                drawIcon(canvas, step, underwayDrawable, iconDrawableWith, iconDrawableHeight, circleCenterX);
            }
            paint.setColor(selectedCircleColor);
            int radius;
            if (state == ANIMATE_STEP_TRANSITION && (animationType == ANIMATION_CIRCLE || animationType == ANIMATION_ALL)
                    && nextAnimatedStep < currentStep) {
                radius = (int) (selectedCircleRadius - selectedCircleRadius * animatedFraction);
            } else {
                radius = selectedCircleRadius;
            }
            canvas.drawCircle(circleCenterX, circleCenterY, radius, paint);

            paint.setColor(selectedStepNumberColor);
            paint.setTextSize(stepNumberTextSize);
            drawNumber(canvas, number, circleCenterX, paint);

            paint.setColor(selectedTextColor);
            paint.setTextSize(textSize);
            drawText(canvas, text, circleCenterX, textY, paint);
        } else if (isDone) {
            if (step == getStepCount() - 1) {
                drawIcon(canvas, step, isReceive ? openGiftDrawable : giftDrawable, giftDrawableWith, giftDrawableHeight, circleCenterX);
            } else {
                drawIcon(canvas, step, completedDrawable, iconDrawableWith, iconDrawableHeight, circleCenterX);
            }

            paint.setColor(doneCircleColor);

            canvas.drawCircle(circleCenterX, circleCenterY, doneCircleRadius, paint);

            drawCheckMark(canvas, circleCenterX, circleCenterY);

            if (state == ANIMATE_STEP_TRANSITION && step == nextAnimatedStep && nextAnimatedStep < currentStep) {
                paint.setColor(selectedTextColor);
                int alpha = Math.max(Color.alpha(doneTextColor), (int) (animatedFraction * 255));
                paint.setAlpha(alpha);
            } else {
                paint.setColor(doneTextColor);
            }
            paint.setTextSize(textSize);
            drawText(canvas, text, circleCenterX, textY, paint);
        } else {
            if (step == getStepCount() - 1) {
                drawIcon(canvas, step, giftDrawable, giftDrawableWith, giftDrawableHeight, circleCenterX);
            } else {
                drawIcon(canvas, step, undoDrawable, iconDrawableWith, iconDrawableHeight, circleCenterX);
            }
            if (state == ANIMATE_STEP_TRANSITION && step == nextAnimatedStep && nextAnimatedStep > currentStep) {
                if (animationType == ANIMATION_CIRCLE || animationType == ANIMATION_ALL) {
                    int animatedRadius = (int) (selectedCircleRadius * animatedFraction);
                    paint.setColor(selectedCircleRadius);
                    canvas.drawCircle(circleCenterX, circleCenterY, animatedRadius, paint);
                }
                if (animationType != ANIMATION_NONE) {
                    if (animationType == ANIMATION_CIRCLE || animationType == ANIMATION_ALL) {
                        paint.setColor(selectedStepNumberColor);
                        int alpha = (int) (animatedFraction * 255);
                        paint.setAlpha(alpha);
                        paint.setTextSize(stepNumberTextSize * animatedFraction);
                        drawNumber(canvas, number, circleCenterX, paint);
                    } else {
                        paint.setTextSize(stepNumberTextSize);
                        paint.setColor(nextTextColor);
                        drawNumber(canvas, number, circleCenterX, paint);
                    }
                } else {
                    paint.setTextSize(stepNumberTextSize);
                    paint.setColor(nextTextColor);
                    drawNumber(canvas, number, circleCenterX, paint);
                }

                paint.setTextSize(textSize);
                paint.setColor(nextTextColor);
                int alpha = (int) Math.max(Color.alpha(nextTextColor), animatedFraction * 255);
                paint.setAlpha(alpha);
                drawText(canvas, text, circleCenterX, textY, paint);
            } else {
                paint.setColor(unSelectedCircleColor);
                canvas.drawCircle(circleCenterX, circleCenterY, unSelectedCircleRadius, paint);

                paint.setColor(nextTextColor);
                paint.setTextSize(stepNumberTextSize);
                drawNumber(canvas, number, circleCenterX, paint);

                paint.setTextSize(textSize);
                drawText(canvas, text, circleCenterX, textY, paint);
            }
        }
    }

    private void drawNumber(Canvas canvas, String number, int circleCenterX, Paint paint) {
        if (!isShowStepNumber) return;
        Rect bounds = new Rect();
        paint.getTextBounds(number, 0, number.length(), bounds);
        float y = circlesY + bounds.height() / 2f - bounds.bottom;
        canvas.drawText(number, circleCenterX, y, paint);
    }

    private void drawCheckMark(Canvas canvas, int circleCenterX, int circleCenterY) {
        if (!isShowStepNumber) return;
        paint.setColor(doneStepMarkColor);
        float width = stepNumberTextSize * 0.1f;
        paint.setStrokeWidth(width);
        Rect bounds = new Rect(
                (int) (circleCenterX - width * 4.5),
                (int) (circleCenterY - width * 3.5),
                (int) (circleCenterX + width * 4.5),
                (int) (circleCenterY + width * 3.5));
        canvas.drawLine(
                bounds.left + 0.5f * width,
                bounds.bottom - 3.25f * width,
                bounds.left + 3.25f * width,
                bounds.bottom - 0.75f * width, paint);
        canvas.drawLine(
                bounds.left + 2.75f * width,
                bounds.bottom - 0.75f * width,
                bounds.right - 0.375f * width,
                bounds.top + 0.75f * width, paint);
    }

    private void drawIcon(Canvas canvas, int step, Drawable drawable, float intrinsicWidth, float intrinsicHeight, int circleCenterX) {
        if (null == drawable) return;
        float y = circlesY - selectedCircleRadius - drawablePadding - intrinsicHeight;
        Rect rect = new Rect((int) (circleCenterX - intrinsicWidth), (int) (y - intrinsicHeight), (int) (circleCenterX + intrinsicWidth), (int) (y + intrinsicHeight));
        //保存图标的Rects集合
        drawIconRects.put(step, rect);
        drawable.setBounds(rect);
        drawable.draw(canvas);
    }

    private void drawText(Canvas canvas, String text, int x, int y, Paint paint) {
        if (text.isEmpty()) {
            return;
        }
        String[] split = text.split("\\n");
        if (split.length == 1) {
            canvas.drawText(text, x, y, paint);
        } else {
            for (int i = 0; i < split.length; i++) {
                canvas.drawText(split[i], x, y + i * fontHeight(), paint);
            }
        }
    }

    private void drawLine(Canvas canvas, int startX, int endX, int centerY, boolean highlight) {
        if (highlight) {
            paint.setColor(doneStepLineColor);
            paint.setStrokeWidth(stepLineWidth);
            canvas.drawLine(startX, centerY, endX, centerY, paint);
        } else {
            paint.setColor(nextStepLineColor);
            paint.setStrokeWidth(stepLineWidth);
            canvas.drawLine(startX, centerY, endX, centerY, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getActionMasked();
        if (MotionEvent.ACTION_DOWN == action) {
            int x = (int) ev.getX();
            int y = (int) ev.getY();
            if (drawIconRects != null && !drawIconRects.isEmpty()) {
                for (Map.Entry<Integer, Rect> entry : drawIconRects.entrySet()) {
                    int entryKey = entry.getKey();
                    Rect entryValue = entry.getValue();
                    if (entryValue.contains(x, y)) {
                        if (null != viewClickListenerWrapper)
                            viewClickListenerWrapper.onClick(ev, entryKey);
                    }
                }
            }
        }
        return super.onTouchEvent(ev);
    }

    public void setViewClickListenerWrapper(OnViewClickListenerWrapper viewClickListener) {
        this.viewClickListenerWrapper = viewClickListener;
    }
}