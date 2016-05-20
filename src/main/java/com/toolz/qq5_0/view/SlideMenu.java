package com.toolz.qq5_0.view;

import android.animation.ArgbEvaluator;
import android.animation.FloatEvaluator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * 由于通常写ViewGroup的时候对子View并没有特殊的测量需求，
 * 所以我们一般可以选择继承系统已有的布局，比如FrameLayout
 *
 */
public class SlideMenu extends FrameLayout {

    ViewDragHelper viewDragHelper;
    private View menuView;
    private View mainView;
    private int menuWidth;
    private int mainWidth;
    private int dragRange;
    private FloatEvaluator floatEvaluator;
    private ArgbEvaluator argbEvaluator;

    public SlideMenu(Context context) {
        super(context);
        init();
    }

    public SlideMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SlideMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        viewDragHelper = ViewDragHelper.create(this, callback);

        floatEvaluator = new FloatEvaluator();
        argbEvaluator = new ArgbEvaluator();
    }

    public enum DragState {
        Open, Close
    }

    private DragState mState = DragState.Close;

    public DragState getDragState() {
        return mState;
    }

    /**
     * 当完成填充之后调用执行，在该方法中可以获得子View的引用
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        menuView = getChildAt(0);
        mainView = getChildAt(1);
    }

    /**
     * 该方法在onMeasure之后执行，所以在该方法中可以获取宽高
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        menuWidth = menuView.getMeasuredWidth();
        mainWidth = mainView.getMeasuredWidth();

        int width = getMeasuredWidth();
        dragRange = (int) (width * 0.6f);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //将触摸事件交给ViewDragHelper来判断是否拦截
        boolean result = viewDragHelper.shouldInterceptTouchEvent(ev);
        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //将触摸事件交给ViewDragHelper来处理
        viewDragHelper.processTouchEvent(event);
        return true;
    }

    ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {
        /**
         * 判断是否开始捕捉当前所触摸的子View的触摸事件
         * @param child：表示当前触摸的子View
         * @return：true表示捕捉， false不捕捉
         */
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == mainView || child == menuView;
        }

        /**
         * 当一个child被捕获的回调
         * capturedChild：当前被捕获触摸事件的child
         */
        @Override
        public void onViewCaptured(View capturedChild, int activePointerId) {
            super.onViewCaptured(capturedChild, activePointerId);
        }

        /**
         * 看起来好像是获取View水平拖拽范围的，然而并不起什么用，它目前主要用于判断是否可以水平移动的条件之一，和计算手指抬起的
         * 平滑动画时间的上面，该方法目前一般的返回值需要大于0
         */
        @Override
        public int getViewHorizontalDragRange(View child) {
            return dragRange;
        }

        /**
         * 控制子View在水平方向的移动
         * child： 当前触摸的子View
         * left：表示ViewDragHelper认为child的left将要变成的值，left=child.getLeft()+dx
         * dx:表示本次手指水平移动的距离
         * return ： 返回的值表示我们最终认为child的left将要变成的值
         */
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {

            if (child == mainView) {
                if (left > dragRange) {
                    left = dragRange;
                } else if (left < 0) {
                    left = 0;
                }
            }
            return left;
        }

        /**
         * 当View的位置改变后回调的方法，
         * changedView:位置改变的View
         * left:changedView的最新的left
         * top:changedView的最新的top
         * dx：表示changedView水平移动的距离
         * dy：表示changedView垂直移动的距离
         */
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);

            //根据手指在menuView上面滑动的距离，让mainView进行伴随移动
            if (changedView == menuView) {
                //让menuView不移动
                menuView.layout(0, 0, menuWidth, menuView.getBottom());
                //当前的left加上本次移动的距离
                int newLeft = mainView.getLeft() + dx;
                //对newLeft进行限制
                if (newLeft > dragRange) newLeft = dragRange;
                if (newLeft < 0) newLeft = 0;
                mainView.layout(newLeft, mainView.getTop(), newLeft + mainWidth, mainView.getBottom());
            }

            //1.计算mainView滑动的百分比
            float fraction = mainView.getLeft() * 1f / dragRange;
            //2.根据滑动的百分比执行伴随动画
            executeAnim(fraction);

            //3.状态改变的逻辑
            if (mainView.getLeft() == dragRange && mState != DragState.Open) {
                //变成打开
                mState = DragState.Open;
                if (listener != null) {
                    listener.open();
                }
            } else if (mainView.getLeft() == 0 && mState != DragState.Close) {
                //变成关闭
                mState = DragState.Close;
                if (listener != null) {
                    listener.close();
                }
            }
            if (listener != null) {
                listener.onDraging(fraction);
            }

        }

        /**
         * 手指抬起的时候会执行
         * releasedChild：手指抬起的子View
         */
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            if (mainView.getLeft() > dragRange / 2) {
                //滑向右边
                open();
            } else {
                //滑向左边
                close();
            }
        }

    };

    /**
     * 执行伴随动画
     *
     * @param fraction
     */
    private void executeAnim(float fraction) {
        //1.让mainView执行缩放, 缩放的值：1->0.8
        mainView.setScaleX(floatEvaluator.evaluate(fraction, 1f, 0.8f));
        mainView.setScaleY(floatEvaluator.evaluate(fraction, 1f, 0.8f));

        //2.让menuView执行缩放，平移效果
        menuView.setScaleX(floatEvaluator.evaluate(fraction, 0.4f, 1f));
        menuView.setScaleY(floatEvaluator.evaluate(fraction, 0.4f, 1f));
        menuView.setTranslationX(floatEvaluator.evaluate(fraction, -menuWidth / 2, 0));
        menuView.setAlpha(floatEvaluator.evaluate(fraction, 0.3f, 1f));

        //3.给SlideMenu的背景添加颜色渐变的遮罩
        if (getBackground() != null) {
            getBackground().setColorFilter((Integer) argbEvaluator.evaluate(fraction, Color.BLACK, Color.TRANSPARENT), PorterDuff.Mode.SRC_OVER);
        }
    }

    /**
     * 关闭菜单
     */
    private void close() {
        viewDragHelper.smoothSlideViewTo(mainView, 0, mainView.getTop());
        ViewCompat.postInvalidateOnAnimation(SlideMenu.this);
    }

    /**
     * 打开菜单
     */
    private void open() {
        viewDragHelper.smoothSlideViewTo(mainView, dragRange, mainView.getTop());
        //需要刷新
        ViewCompat.postInvalidateOnAnimation(SlideMenu.this);
    }

    /**
     * invalide是为了引起onDraw回调，onDraw又调用computeScroll；
     * 所以调用invalidate()是为了调用computeScroll()
     */
    @Override
    public void computeScroll() {

        //scroller的写法：
//		if(scroller.computeScrollOffset()){
//			//如果是true，表示滚动动画没有结束
//			scrollTo(scroller.getCurrX(), scroller.getCurrY());
//			invalidate();
//		}

        //ViewDragHelper的写法
        if (viewDragHelper.continueSettling(true)) {
            //如果是true，表示滚动动画没有结束
            //只需要刷新就行
            ViewCompat.postInvalidateOnAnimation(SlideMenu.this);
        }

    }


    private OnDragStateChangeListener listener;

    public void setOnDragStateChangeListener(OnDragStateChangeListener listener) {

        this.listener = listener;
    }

    /**
     * 定义拖拽状态改变的监听器
     */
    public interface OnDragStateChangeListener {
        /**
         * 关闭菜单的回调
         */
        void close();

        /**
         * 打开菜单的回调
         */
        void open();

        /**
         * 拖拽过程中的回调
         */
        void onDraging(float fraction);
    }
}
