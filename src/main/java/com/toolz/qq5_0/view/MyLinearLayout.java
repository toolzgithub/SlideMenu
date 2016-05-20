package com.toolz.qq5_0.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import com.toolz.qq5_0.view.SlideMenu.DragState;

public class MyLinearLayout extends LinearLayout {

    public MyLinearLayout(Context context) {
        super(context);
    }

    public MyLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private SlideMenu slideMenu;

    public void setSlideMenu(SlideMenu slideMenu) {

        this.slideMenu = slideMenu;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (slideMenu.getDragState() == DragState.Open) {
            //菜单打开，需要拦截所有的触摸事件
            return true;
        }

        return super.onInterceptTouchEvent(ev);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(slideMenu.getDragState()== DragState.Open){
            //菜单打开，需要消费所有的触摸事件
            return true;
        }
        return super.onTouchEvent(event);
    }
}
