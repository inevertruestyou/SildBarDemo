package com.example.laowa.qqdemo;

import com.example.laowa.qqdemo.ColorUtil;

import android.animation.FloatEvaluator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.nineoldandroids.animation.IntEvaluator;
import com.nineoldandroids.view.ViewHelper;


public class SlideMenu extends FrameLayout {
    private View menuView;      //菜单的view
    private View mainView;      //主页的view
    private ViewDragHelper viewDragHelper;
    private int width;
    private float dragRange;        //拖拽范围
    private FloatEvaluator floatEvaluator;
    private IntEvaluator intEvaluator;

    public SlideMenu(@NonNull Context context) {
        super(context);
        init();
    }

    public SlideMenu(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SlideMenu(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public SlideMenu(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    //定义状态常量
    enum DragState{
        Open, Close;
    }

    /**
     * 获取当前的状态
     * @return
     */
    public DragState getCurrentState(){
        return currentState;
    }

    private DragState currentState = DragState.Close;

    private void init(){
        viewDragHelper = ViewDragHelper.create(this, callback);
        floatEvaluator = new FloatEvaluator();
        intEvaluator = new IntEvaluator();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount()!= 2){
            throw new IllegalArgumentException("SlideMenu only have 2 children!");
        }
        menuView = getChildAt(0);
        mainView = getChildAt(1);
    }

    /**
     * 该方法onmeasure执行完之后执行，那么可以在该方法中初始化自己和子view的宽高
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = getMeasuredWidth();
        dragRange = width * 0.6f;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return viewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        viewDragHelper.processTouchEvent(event);
        return true;
    }

    private ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == menuView || child == mainView;
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return (int) dragRange;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (child == mainView) {
                if (left < 0) left = 0;
                if (left > dragRange) left = (int) dragRange;
            }
            return left;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            if (changedView == menuView) {
                menuView.layout(0, 0, menuView.getMeasuredWidth(), menuView.getMeasuredHeight());
                int newLeft = mainView.getLeft() + dx;
                if (newLeft < 0) newLeft = 0;
                if (newLeft > dragRange) newLeft = (int) dragRange;
                mainView.layout(newLeft, mainView.getTop() + dy, newLeft + mainView.getMeasuredWidth(), mainView.getBottom() + dy);
            }
            //计算百分比
            float fraction = mainView.getLeft() / dragRange;
            //执行动画
            executeAnim(fraction);
            //更改状态回调listener方法
            if (fraction == 0 && currentState != DragState.Close){
                currentState = DragState.Close;
                if (listener != null) {
                    listener.onClose();
                }
            }else if(fraction == 1f && currentState != DragState.Open){
                if (listener != null){
                    listener.onOpen();
                }
            }
            if (listener != null){
                listener.onDraging(fraction);
            }

        }

        public void open() {
            viewDragHelper.smoothSlideViewTo(mainView,(int) dragRange,mainView.getTop());
            ViewCompat.postInvalidateOnAnimation(SlideMenu.this);
        }
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            if (mainView.getLeft() < dragRange / 2) {
                viewDragHelper.smoothSlideViewTo(mainView, 0, mainView.getTop());
                ViewCompat.postInvalidateOnAnimation(SlideMenu.this);
            } else {
                viewDragHelper.smoothSlideViewTo(mainView, (int) dragRange, mainView.getTop());
                ViewCompat.postInvalidateOnAnimation(SlideMenu.this);
            }
        }

        private void executeAnim(float fraction) {
            //缩小mainview
            //fraction:0-1
            ViewHelper.setScaleX(mainView, floatEvaluator.evaluate(fraction, 1f, 0.8f));
            ViewHelper.setScaleY(mainView, floatEvaluator.evaluate(fraction, 1f, 0.8f));
            //移动minuView
            ViewHelper.setTranslationX(menuView, intEvaluator.evaluate(fraction, -menuView.getMeasuredWidth() / 2, 0));
            //放大menuview
            ViewHelper.setScaleX(menuView, floatEvaluator.evaluate(fraction, 0.5f, 1f));
            ViewHelper.setScaleY(menuView, floatEvaluator.evaluate(fraction, 0.5f, 1f));
            //改变menuView透明度
            ViewHelper.setAlpha(menuView, floatEvaluator.evaluate(fraction, 0.3f, 1f));
            //背景变暗
            getBackground().setColorFilter((Integer) ColorUtil.evaluateColor(fraction, Color.BLACK, Color.TRANSPARENT), PorterDuff.Mode.SRC_OVER);
        }
    };

    private OnDragStateChangeListener listener;
    public void setOnDragStateChangeListener(OnDragStateChangeListener listener){
        this.listener = listener;
    }

        @Override
        public void computeScroll() {
            if (viewDragHelper.continueSettling(true)) {
                ViewCompat.postInvalidateOnAnimation(SlideMenu.this);
            }
        }


    public interface OnDragStateChangeListener{
        /**
         * 打开的回调
         */
        void onOpen();
        /**
         * 关闭的回调
         */
        void onClose();
        /**
         * 正在拖拽中的回调
         */
        void onDraging(float fraction);
    }
}
