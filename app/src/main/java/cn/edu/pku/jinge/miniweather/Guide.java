package cn.edu.pku.jinge.miniweather;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jinge on 2016/11/29.
 */
public class Guide extends Activity implements ViewPager.OnPageChangeListener{
    private ViewPagerAdapter vpAdapter;
    private ViewPager vp;
    private List<View> views;

    private ImageView[] dots;
    private int[] ids = {R.id.iv1, R.id.iv2, R.id.iv3};
    private ImageButton btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guide);

        // 如果不是第一次使用，直接跳转到主界面
        if (isUsed() == true) {
            Intent i = new Intent(Guide.this, MainActivity.class);
            startActivity(i);
            finish();
        }
        initViews();
        initDots();
        btn = (ImageButton) views.get(2).findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 记录本次状态
                SharedPreferences settings = (SharedPreferences)getSharedPreferences("shared", MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("isUsed", "yes");
                editor.commit();

                // 跳转
                Intent i = new Intent(Guide.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    private boolean isUsed(){
        SharedPreferences sharedPreferences = getSharedPreferences("shared", MODE_PRIVATE);
        String isUsed = sharedPreferences.getString("isUsed", "default");
        if (isUsed == "yes") {
            return true;
        } else {
            return false;
        }
    }

    void initDots() {
        dots = new ImageView[views.size()];
        for (int i = 0; i < views.size(); i++) {
            dots [i] = (ImageView) findViewById(ids[i]);
        }
    }

    private void initViews() {
        LayoutInflater inflater = LayoutInflater.from(this);
        views = new ArrayList<View>();
        views.add(inflater.inflate(R.layout.guidepage1, null));
        views.add(inflater.inflate(R.layout.guidepage2, null));
        views.add(inflater.inflate(R.layout.guidepage3, null));
        vpAdapter = new ViewPagerAdapter(views, this);
        vp = (ViewPager) findViewById(R.id.guidePage);
        vp.setAdapter(vpAdapter);
        vp.setOnPageChangeListener(this);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        for (int i = 0; i < ids.length; i++) {
            if (i == position) {
                dots[i].setImageResource(R.drawable.page_indicator_focused);
            } else {
                dots[i].setImageResource(R.drawable.page_indicator_unfocused);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
