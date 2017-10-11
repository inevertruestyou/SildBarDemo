package com.example.laowa.qqdemo;

import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.ViewDragHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.CycleInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private ListView menu_listView;
    private ListView main_listview;
    private SlideMenu slideMenu;
    private ImageView imageView;
    private MyLinearLayout myLinearLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        init();
    }

    public void initView(){
        menu_listView = (ListView) findViewById(R.id.menu_listview);
        main_listview = (ListView) findViewById(R.id.main_listview);
        imageView = (ImageView) findViewById(R.id.iv_head);
        slideMenu = (SlideMenu) findViewById(R.id.myslidemenu);
        myLinearLayout = (MyLinearLayout) findViewById(R.id.my_layout);
    }

    public void init(){
        menu_listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,Constant.sCheeseStrings){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setTextColor(Color.WHITE);
                return textView;
            }
        });

        main_listview.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,Constant.NAMES){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ViewHelper.setScaleX(view, 0.5f);
                ViewHelper.setScaleY(view, 0.5f);
                ViewPropertyAnimator.animate(view).scaleX(1).setDuration(350).start();
                ViewPropertyAnimator.animate(view).scaleY(1).setDuration(350).start();
                return view;
            }
        });

        slideMenu.setOnDragStateChangeListener(new SlideMenu.OnDragStateChangeListener() {
            @Override
            public void onOpen() {

            }
            @Override
            public void onClose() {
                ViewPropertyAnimator.animate(imageView).translationXBy(15)
                        .setInterpolator(new CycleInterpolator(4))
                        .setDuration(500)
                        .start();
            }
            @Override
            public void onDraging(float fraction) {
                ViewHelper.setAlpha(imageView, 1 - fraction);
            }
        });

    }
}
