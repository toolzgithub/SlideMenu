package com.toolz.qq5_0;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.CycleInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nineoldandroids.view.ViewPropertyAnimator;
import com.toolz.qq5_0.view.MyLinearLayout;
import com.toolz.qq5_0.view.SlideMenu;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private ListView menu_listview;
    private ListView main_listView;
    private SlideMenu slideMenu;
    private ImageView iv_head;
    private MyLinearLayout my_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        menu_listview = (ListView) findViewById(R.id.menu_listview);
        main_listView = (ListView) findViewById(R.id.main_listview);
        slideMenu = (SlideMenu) findViewById(R.id.slide_menu);
        iv_head = (ImageView) findViewById(R.id.iv_head);
        my_layout = (MyLinearLayout) findViewById(R.id.my_layout);

        //填充数据
        menu_listview.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Constant.sCheeseStrings) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setTextColor(Color.WHITE);
                return textView;
            }
        });
        main_listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Constant.NAMES) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setTextColor(Color.BLACK);
                return textView;
            }
        });


        slideMenu.setOnDragStateChangeListener(new SlideMenu.OnDragStateChangeListener() {
            @Override
            public void close() {
                //使用NineOldAndroid中的兼容低版本的属性动画
                ViewPropertyAnimator.animate(iv_head).translationX(25).setInterpolator(new CycleInterpolator(4)).setDuration(500).start();
            }

            @Override
            public void open() {
                int nextInt = new Random().nextInt(Constant.sCheeseStrings.length);
                menu_listview.smoothScrollToPosition(nextInt);
            }

            @Override
            public void onDraging(float fraction) {
                iv_head.setAlpha(1 - fraction);
            }
        });
        my_layout.setSlideMenu(slideMenu);
    }
}
