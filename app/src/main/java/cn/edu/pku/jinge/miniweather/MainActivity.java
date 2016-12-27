package cn.edu.pku.jinge.miniweather;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.audiofx.BassBoost;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cn.edu.pku.jinge.bean.TodayWeather;
import cn.edu.pku.jinge.bean.futureWeather;
import cn.edu.pku.jinge.bean.yesterdayWeather;
import cn.edu.pku.jinge.util.NetUtil;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

// 定位功能使用
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;

/**
 * Created by jinge on 2016/9/27.
 */
public class MainActivity extends Activity implements View.OnClickListener, ViewPager.OnPageChangeListener{
    private static final int UPDATE_TODAY_WEATHER = 1;
    private ImageView mUpdateBtn;
    private ProgressBar mUpdateProg;
    private ImageView mLocateBtn;
    private ImageView mCitySelect;
    private ImageView mShareBtn;
    private TextView cityTv, timeTv, humidityTv, weekTv, pmDataTv, pmQualityTv,
            temperatureTv, climateTv, windTv, city_name_Tv;
    private ImageView weatherImg, pmImg;
    private TextView weekDay1, weekDay2, weekDay3, weekDay4, weekDay5, weekDay6;
    private TextView temperature1, temperature2, temperature3, temperature4, temperature5, temperature6;
    private TextView dayType1, dayType2, dayType3, dayType4, dayType5, dayType6,
            nightType1, nightType2, nightType3, nightType4, nightType5, nightType6,
            dayFeng1, dayFeng2, dayFeng3, dayFeng4, dayFeng5, dayFeng6,
            nightFeng1, nightFeng2, nightFeng3, nightFeng4, nightFeng5, nightFeng6;

    //未来6日天气滑动，变量定义
    private futureviewPagerAdapter vpAdapter;
    private ViewPager vp;
    private List<View> views;
    yesterdayWeather yesterdayweather = null;
    futureWeather futureweather = null;
    private GoogleApiClient client;

    //百度定位变量定义
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();
    public String addr;

    // 屏幕下方小圆点使用
    private ImageView[] dots;
    private int[] ids = {R.id.dot1, R.id.dot2};

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_info);

        // 启动service
        startService(new Intent(getBaseContext(), AutoUpdate.class));

        //百度定位
        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);    //注册监听函数
        initLocation(); //使用初始化
        mLocationClient.start();//开始定位

        mUpdateBtn = (ImageView) findViewById(R.id.title_update_btn);
        mUpdateBtn.setOnClickListener(this);
        mLocateBtn = (ImageView) findViewById(R.id.title_location);
        mLocateBtn.setOnClickListener(this);
        mUpdateProg = (ProgressBar) findViewById(R.id.title_update_progress);
        mShareBtn = (ImageView) findViewById(R.id.title_share);

        if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
            Log.d("myWeather", "网络OK");
            Toast.makeText(MainActivity.this, "网络OK!", Toast.LENGTH_LONG).show();
        } else {
            Log.d("myWeather", "网络挂了");
            Toast.makeText(MainActivity.this, "网络挂了!", Toast.LENGTH_LONG).show();
        }

        mCitySelect = (ImageView) findViewById(R.id.title_city_manager);
        mCitySelect.setOnClickListener(this);
        initView();
        initViewPage();
        //初始化6日天气
        initFutureSix();

        //小圆点
        initDots();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.title_update_btn) {
            view.setVisibility(View.GONE);
            //mShareBtn.setOnClickListener(this);
            mUpdateProg.setVisibility(View.VISIBLE);
            // 调整布局
            adjustLayout(R.id.title_update_progress);

            SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
            String cityCode = sharedPreferences.getString("main_city_code", "101010100");
            Log.d("myWeather", cityCode);

            if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
                Log.d("myWeather", "网络OK");
                queryWeatherCode(cityCode);
            } else {
                Log.d("myWeather", "网络挂了");
                Toast.makeText(MainActivity.this, "网络挂了!", Toast.LENGTH_LONG).show();
            }
        }
        if (view.getId() == R.id.title_city_manager) {
            Intent i = new Intent(this, SelectCity.class);
            startActivityForResult(i, 1);
        }
        if (view.getId() == R.id.title_location) {
            AlertDialog.Builder bdr = new AlertDialog.Builder(MainActivity.this);
            bdr.setTitle("您所在的位置为:");
            bdr.setMessage(addr + "\n" + "\n请切换城市,并点击屏幕退出");
            bdr.show();
        }

        if (view.getId() == R.id.title_share) {
            // 后面添加
        }
    }

    private void queryWeatherCode(final String cityCode) {
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        Log.d("myWeather", address);
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection con = null;
                TodayWeather todayWeather = null;
                try {
                    URL url = new URL(address);
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(8000);
                    con.setReadTimeout(8000);
                    InputStream in = con.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String str;
                    while ((str = reader.readLine()) != null) {
                        response.append(str);
                        Log.d("myWeather", str);
                    }
                    String responseStr = response.toString();
                    Log.d("myWeather", responseStr);
                    todayWeather = parseXML(responseStr, cityCode);
                    if (todayWeather != null) {
                        Log.d("myWeather", todayWeather.toString());
                        Message msg = new Message();
                        msg.what = UPDATE_TODAY_WEATHER;
                        msg.obj = todayWeather;
                        mHandler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (con != null) {
                        con.disconnect();
                    }
                }
            }
        }).start();
    }

    public void updateTodayWeather(TodayWeather todayWeather) {
        city_name_Tv.setText(todayWeather.getCity() + "天气");
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime() + "发布");
        humidityTv.setText("湿度:" + todayWeather.getShidu());
        pmDataTv.setText(todayWeather.getPm25());
        pmQualityTv.setText(todayWeather.getQuality());
        weekTv.setText(todayWeather.getDate());
        temperatureTv.setText(todayWeather.getLow() + "~" + todayWeather.getHigh());
        climateTv.setText(todayWeather.getType());
        windTv.setText("风力:" + todayWeather.getFengli());
        updateImage(todayWeather);

        // 更新昨天天气
        weekDay1.setText(yesterdayweather.getDate_1());
        temperature1.setText(yesterdayweather.getLow_1() + "~" + yesterdayweather.getHigh_1());
        dayType1.setText(yesterdayweather.getDayType_1());
        dayFeng1.setText(yesterdayweather.getDayFx_1() + ":" + yesterdayweather.getDayFl_1());
        nightType1.setText(yesterdayweather.getNightType_1());
        nightFeng1.setText(yesterdayweather.getNightFx_1() + ":" + yesterdayweather.getNightFl_1());
        //更新未来五天的天气的第二天
        //weekDay2.setText();
        weekDay2.setText(todayWeather.getDate());
        temperature2.setText(todayWeather.getLow() + "~" + todayWeather.getHigh());
        dayType2.setText(futureweather.getDayType2());
        nightType2.setText(futureweather.getNightType2());
        dayFeng2.setText(futureweather.getDayFx2() + ":" + futureweather.getDayFl2());
        nightFeng2.setText(futureweather.getNightFx2() + ":" + futureweather.getNightFl2());
        //第三天
        weekDay3.setText(futureweather.getDate3());
        temperature3.setText(futureweather.getLow3() + "~" + futureweather.getHigh3());
        dayType3.setText(futureweather.getDayType3());
        nightType3.setText(futureweather.getNightType3());
        dayFeng3.setText(futureweather.getDayFx3() + ":" + futureweather.getDayFl3());
        nightFeng3.setText(futureweather.getNightFx3() + ":" + futureweather.getNightFl3());
//第4天
        weekDay4.setText(futureweather.getDate4());
        temperature4.setText(futureweather.getLow4() + "~" + futureweather.getHigh4());
        dayType4.setText(futureweather.getDayType4());
        nightType4.setText(futureweather.getNightType4());
        dayFeng4.setText(futureweather.getDayFx4() + ":" + futureweather.getDayFl4());
        nightFeng4.setText(futureweather.getNightFx4() + ":" + futureweather.getNightFl4());
//第5天
        weekDay5.setText(futureweather.getDate5());
        temperature5.setText(futureweather.getLow5() + "~" + futureweather.getHigh5());
        dayType5.setText(futureweather.getDayType5());
        nightType5.setText(futureweather.getNightType5());
        dayFeng5.setText(futureweather.getDayFx5() + ":" + futureweather.getDayFl5());
        nightFeng5.setText(futureweather.getNightFx5() + ":" + futureweather.getNightFl5());
        //第6天
        weekDay6.setText(futureweather.getDate6());
        temperature6.setText(futureweather.getLow6() + "~" + futureweather.getHigh6());
        dayType6.setText(futureweather.getDayType6());
        nightType6.setText(futureweather.getNightType6());
        dayFeng6.setText(futureweather.getDayFx6() + ":" + futureweather.getDayFl6());
        nightFeng6.setText(futureweather.getNightFx6() + ":" + futureweather.getNightFl6());

        Toast.makeText(MainActivity.this, "更新成功!", Toast.LENGTH_SHORT).show();
        mUpdateProg.setVisibility(View.GONE);
        mUpdateBtn.setVisibility(View.VISIBLE);
        // 更新后修改布局
        adjustLayout(R.id.title_update_btn);
    }

    private void adjustLayout(int anchor) {
        RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
        int width = mShareBtn.getLayoutParams().width;
        Log.d("myWeather", "adjustLayout1");
        int height = mShareBtn.getLayoutParams().height;
        Log.d("myWeather", "adjustLayout2");
        param = new RelativeLayout.LayoutParams(width, height);
        Log.d("myWeather", "adjustLayout3");
        param.addRule(RelativeLayout.LEFT_OF,anchor);
        mShareBtn.setLayoutParams(param);
    }

    // 根据天气和空气质量情况更新图片
    public void updateImage(TodayWeather todayWeather) {
        weatherImg = (ImageView) findViewById(R.id.weather_img);
        pmImg = (ImageView) findViewById(R.id.pm2_5_img);
        if (todayWeather.getType().equals("雾")) {
            weatherImg.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.biz_plugin_weather_wu));
        } else if (todayWeather.getType().equals("小雨")) {
            weatherImg.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.biz_plugin_weather_xiaoyu));
        } else if (todayWeather.getType().equals("中雨")) {
            weatherImg.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.biz_plugin_weather_zhongyu));
        } else if (todayWeather.getType().equals("大雨")) {
            weatherImg.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.biz_plugin_weather_dayu));
        } else if (todayWeather.getType().equals("暴雨")) {
            weatherImg.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.biz_plugin_weather_baoyu));
        } else if (todayWeather.getType().equals("大暴雨")) {
            weatherImg.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.biz_plugin_weather_dabaoyu));
        } else if (todayWeather.getType().equals("特大暴雨")) {
            weatherImg.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.biz_plugin_weather_tedabaoyu));
        } else if (todayWeather.getType().equals("雷阵雨")) {
            weatherImg.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.biz_plugin_weather_leizhenyu));
        } else if (todayWeather.getType().equals("雷阵雨冰雹")) {
            weatherImg.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.biz_plugin_weather_leizhenyubingbao));
        } else if (todayWeather.getType().equals("阵雨")) {
            weatherImg.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.biz_plugin_weather_zhenyu));
        } else if (todayWeather.getType().equals("小雪")) {
            weatherImg.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.biz_plugin_weather_xiaoxue));
        } else if (todayWeather.getType().equals("中雪")) {
            weatherImg.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.biz_plugin_weather_zhongxue));
        } else if (todayWeather.getType().equals("大雪")) {
            weatherImg.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.biz_plugin_weather_daxue));
        } else if (todayWeather.getType().equals("暴雪")) {
            weatherImg.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.biz_plugin_weather_baoxue));
        } else if (todayWeather.getType().equals("阵雪")) {
            weatherImg.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.biz_plugin_weather_zhenxue));
        } else if (todayWeather.getType().equals("雨夹雪")) {
            weatherImg.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.biz_plugin_weather_yujiaxue));
        } else if (todayWeather.getType().equals("阴")) {
            weatherImg.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.biz_plugin_weather_yin));
        } else if (todayWeather.getType().equals("多云")) {
            weatherImg.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.biz_plugin_weather_duoyun));
        } else if (todayWeather.getType().equals("晴")) {
            weatherImg.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.biz_plugin_weather_qing));
        } else if (todayWeather.getType().equals("沙尘暴")) {
            weatherImg.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.biz_plugin_weather_shachenbao));
        }

        if (todayWeather.getQuality().equals("优")) {
            pmImg.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.biz_plugin_weather_0_50));
        } else if (todayWeather.getQuality().equals("良")) {
            pmImg.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.biz_plugin_weather_101_150));
        } else if (todayWeather.getQuality().equals("轻度污染") || todayWeather.getQuality().equals("中度污染")) {
            pmImg.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.biz_plugin_weather_151_200));
        } else if (todayWeather.getQuality().equals("重度污染")) {
            pmImg.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.biz_plugin_weather_201_300));
        }
    }

    private TodayWeather parseXML(String xmldata, String cityCode) {
        TodayWeather todayWeather = null;
        // 当天天气使用
        int fengxiangCount = 0;
        int fengliCount = 0;
        int dateCount = 0;
        int highCount = 0;
        int lowCount = 0;
        int typeCount = 0;

        // 六日天气使用
        int type_1count = 0;
        int fx_1count = 0;
        int fl_1count = 0;
        try {
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldata));
            int eventType = xmlPullParser.getEventType();
            //Log.d("myWeather", "parseXML");
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    // 判断当前事件是否为文档开始事件
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    // 判断当前事件是否为标签元素开始事件
                    case XmlPullParser.START_TAG:
                        if (xmlPullParser.getName().equals("resp")) {
                            todayWeather = new TodayWeather();
                            yesterdayweather = new yesterdayWeather();
                            futureweather = new futureWeather();
                        }
                        if (todayWeather != null) {
                            if (xmlPullParser.getName().equals("city")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setCity(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("updatetime")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setUpdatetime(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("shidu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setShidu(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("wendu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWendu(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("pm25")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setPm25(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("quality")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setQuality(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengxiang(xmlPullParser.getText());
                                fengxiangCount++;
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 1) {
                                eventType = xmlPullParser.next();
                                // 6天天气中的今日天气从第2个开始记录避免错位
                                futureweather.setDayFx2(xmlPullParser.getText());
                                fengxiangCount++;
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 2) {
                                eventType = xmlPullParser.next();
                                futureweather.setNightFx2(xmlPullParser.getText());
                                fengxiangCount++;
                            }
                            else if (xmlPullParser.getName().equals("fengli") && fengliCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli(xmlPullParser.getText());
                                futureweather.setDayFl2(xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 1) {
                                eventType = xmlPullParser.next();
                                // 6天天气中的今日天气从第2个开始记录避免错位
                                futureweather.setDayFl2(xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 2) {
                                eventType = xmlPullParser.next();
                                futureweather.setNightFl2(xmlPullParser.getText());
                                fengliCount++;
                            }
                            else if (xmlPullParser.getName().equals("date") && dateCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setDate(xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setLow(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setType(xmlPullParser.getText());
                                futureweather.setDayType2(xmlPullParser.getText());
                                typeCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 1) {
                                eventType = xmlPullParser.next();
                                todayWeather.setType(xmlPullParser.getText());
                                futureweather.setNightType2(xmlPullParser.getText());
                                typeCount++;
                            }
                            //yesterday的XML解析
                            else if (xmlPullParser.getName().equals("date_1")) {
                                eventType = xmlPullParser.next();
                                yesterdayweather.setDate_1(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("high_1")) {
                                eventType = xmlPullParser.next();
                                yesterdayweather.setHigh_1(xmlPullParser.getText().substring(2).trim());
                            } else if (xmlPullParser.getName().equals("low_1")) {
                                eventType = xmlPullParser.next();
                                yesterdayweather.setLow_1(xmlPullParser.getText().substring(2).trim());
                            } else if (xmlPullParser.getName().equals("type_1") && type_1count == 0) {
                                eventType = xmlPullParser.next();
                                yesterdayweather.setDayType_1(xmlPullParser.getText());
                                type_1count++;
                            } else if (xmlPullParser.getName().equals("type_1") && type_1count == 1) {
                                eventType = xmlPullParser.next();
                                yesterdayweather.setNightType_1(xmlPullParser.getText());
                                type_1count++;
                            } else if (xmlPullParser.getName().equals("fx_1") && fx_1count == 0) {
                                eventType = xmlPullParser.next();
                                yesterdayweather.setDayFx_1(xmlPullParser.getText());
                                fx_1count++;
                            } else if (xmlPullParser.getName().equals("fx_1") && fx_1count == 1) {
                                eventType = xmlPullParser.next();
                                yesterdayweather.setNightFx_1(xmlPullParser.getText());
                                fx_1count++;
                            } else if (xmlPullParser.getName().equals("fl_1") && fl_1count == 0) {
                                eventType = xmlPullParser.next();
                                yesterdayweather.setDayFl_1(xmlPullParser.getText());
                                fl_1count++;
                            } else if (xmlPullParser.getName().equals("fl_1") && fl_1count == 1) {
                                eventType = xmlPullParser.next();
                                yesterdayweather.setNightFl_1(xmlPullParser.getText());
                                fl_1count++;
                            }
                            //第3天的解析
                            else if (xmlPullParser.getName().equals("date") && dateCount == 1) {
                                eventType = xmlPullParser.next();
                                futureweather.setDate3(xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 1) {
                                eventType = xmlPullParser.next();
                                futureweather.setLow3(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 1) {
                                eventType = xmlPullParser.next();
                                futureweather.setHigh3(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 2) {
                                eventType = xmlPullParser.next();
                                futureweather.setDayType3(xmlPullParser.getText());
                                typeCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 3) {
                                eventType = xmlPullParser.next();
                                futureweather.setNightType3(xmlPullParser.getText());
                                typeCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 3) {
                                eventType = xmlPullParser.next();
                                futureweather.setDayFl3(xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 4) {
                                eventType = xmlPullParser.next();
                                futureweather.setNightFl3(xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 3) {
                                eventType = xmlPullParser.next();
                                futureweather.setDayFx3(xmlPullParser.getText());
                                fengxiangCount++;
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 4) {
                                eventType = xmlPullParser.next();
                                futureweather.setNightFx3(xmlPullParser.getText());
                                fengxiangCount++;
                            }
                            //第4天的解析
                            else if (xmlPullParser.getName().equals("date") && dateCount == 2) {
                                eventType = xmlPullParser.next();
                                futureweather.setDate4(xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 2) {
                                eventType = xmlPullParser.next();
                                futureweather.setLow4(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 2) {
                                eventType = xmlPullParser.next();
                                futureweather.setHigh4(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 4) {
                                eventType = xmlPullParser.next();
                                futureweather.setDayType4(xmlPullParser.getText());
                                typeCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 5) {
                                eventType = xmlPullParser.next();
                                futureweather.setNightType4(xmlPullParser.getText());
                                typeCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 5) {
                                eventType = xmlPullParser.next();
                                futureweather.setDayFl4(xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 6) {
                                eventType = xmlPullParser.next();
                                futureweather.setNightFl4(xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 5) {
                                eventType = xmlPullParser.next();
                                futureweather.setDayFx4(xmlPullParser.getText());
                                fengxiangCount++;
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 6) {
                                eventType = xmlPullParser.next();
                                futureweather.setNightFx4(xmlPullParser.getText());
                                fengxiangCount++;
                            }
                            //第5天的解析
                            else if (xmlPullParser.getName().equals("date") && dateCount == 3) {
                                eventType = xmlPullParser.next();
                                futureweather.setDate5(xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 3) {
                                eventType = xmlPullParser.next();
                                futureweather.setLow5(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 3) {
                                eventType = xmlPullParser.next();
                                futureweather.setHigh5(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 6) {
                                eventType = xmlPullParser.next();
                                futureweather.setDayType5(xmlPullParser.getText());
                                typeCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 7) {
                                eventType = xmlPullParser.next();
                                futureweather.setNightType5(xmlPullParser.getText());
                                typeCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 7) {
                                eventType = xmlPullParser.next();
                                futureweather.setDayFl5(xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 8) {
                                eventType = xmlPullParser.next();
                                futureweather.setNightFl5(xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 7) {
                                eventType = xmlPullParser.next();
                                futureweather.setDayFx5(xmlPullParser.getText());
                                fengxiangCount++;
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 8) {
                                eventType = xmlPullParser.next();
                                futureweather.setNightFx5(xmlPullParser.getText());
                                fengxiangCount++;
                            }
                            //第6天的解析
                            else if (xmlPullParser.getName().equals("date") && dateCount == 4) {
                                eventType = xmlPullParser.next();
                                futureweather.setDate6(xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 4) {
                                eventType = xmlPullParser.next();
                                futureweather.setLow6(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 4) {
                                eventType = xmlPullParser.next();
                                futureweather.setHigh6(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 8) {
                                eventType = xmlPullParser.next();
                                futureweather.setDayType6(xmlPullParser.getText());
                                typeCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 9) {
                                eventType = xmlPullParser.next();
                                futureweather.setNightType6(xmlPullParser.getText());
                                typeCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 9) {
                                eventType = xmlPullParser.next();
                                futureweather.setDayFl6(xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 10) {
                                eventType = xmlPullParser.next();
                                futureweather.setNightFl6(xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 9) {
                                eventType = xmlPullParser.next();
                                futureweather.setDayFx6(xmlPullParser.getText());
                                fengxiangCount++;
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 10) {
                                eventType = xmlPullParser.next();
                                futureweather.setNightFx6(xmlPullParser.getText());
                                fengxiangCount++;
                            }
                        }
                        break;
                    // 判断当前事件是否为标签元素结束事件
                    case XmlPullParser.END_TAG:
                        break;
                }
                // 进入下一个元素并触发相应事件
                eventType = xmlPullParser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 若pm25和quality数据为空，取其上级城市的数据
        if (todayWeather.getPm25() == null || todayWeather.getQuality() == null) {
            // 直辖市前5位为10101到10104
            if (cityCode.substring(0,5).equals("10101") || cityCode.substring(0,5).equals("10102")
                    || cityCode.substring(0,5).equals("10103") || cityCode.substring(0,5).equals("10104")) {
                cityCode = cityCode.substring(0,5) + "0100";
                Log.d("new code", cityCode);
            } else {
                // 其他省省会城市为0101结尾
                cityCode = cityCode.substring(0,5) + "0101";
                Log.d("new code", cityCode);
            }
            final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
            HttpURLConnection con = null;
            try {
                URL url = new URL(address);
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setConnectTimeout(8000);
                con.setReadTimeout(8000);
                InputStream in = con.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder response = new StringBuilder();
                String str;
                while ((str = reader.readLine()) != null) {
                    response.append(str);
                }
                String responseStr = response.toString();
                try {
                    XmlPullParserFactory fac1 = XmlPullParserFactory.newInstance();
                    XmlPullParser xmlPullParser1 = fac1.newPullParser();
                    xmlPullParser1.setInput(new StringReader(responseStr));
                    int eventType1 = xmlPullParser1.getEventType();
                    while (eventType1 != XmlPullParser.END_DOCUMENT) {
                        switch (eventType1) {
                            // 判断当前事件是否为文档开始事件
                            case XmlPullParser.START_DOCUMENT:
                                break;
                            // 判断当前事件是否为标签元素开始事件
                            case XmlPullParser.START_TAG:
                                if (todayWeather.getCity() == null || todayWeather.getUpdatetime() == null) {
                                    // 一种情况是所有数据都没有，则都取上级城市的数据
                                    Log.d("XmlPullParser", xmlPullParser1.getName());
                                    if (xmlPullParser1.getName().equals("city")) {
                                        eventType1 = xmlPullParser1.next();
                                        todayWeather.setCity(xmlPullParser1.getText());
                                    } else if (xmlPullParser1.getName().equals("updatetime")) {
                                        eventType1 = xmlPullParser1.next();
                                        todayWeather.setUpdatetime(xmlPullParser1.getText());
                                        Log.d("setUpdatetime", todayWeather.getUpdatetime());
                                    } else if (xmlPullParser1.getName().equals("shidu")) {
                                        eventType1 = xmlPullParser1.next();
                                        todayWeather.setShidu(xmlPullParser1.getText());
                                    } else if (xmlPullParser1.getName().equals("wendu")) {
                                        eventType1 = xmlPullParser1.next();
                                        todayWeather.setWendu(xmlPullParser1.getText());
                                        Log.d("setUpdatetime", todayWeather.getWendu());
                                    }else if (xmlPullParser1.getName().equals("fengxiang") && fengxiangCount == 0) {
                                        eventType1 = xmlPullParser1.next();
                                        todayWeather.setFengxiang(xmlPullParser1.getText());
                                        fengxiangCount++;
                                    } else if (xmlPullParser1.getName().equals("fengli") && fengliCount == 0) {
                                        eventType1 = xmlPullParser1.next();
                                        todayWeather.setFengli(xmlPullParser1.getText());
                                        fengliCount++;
                                    } else if (xmlPullParser1.getName().equals("date") && dateCount == 0) {
                                        eventType1 = xmlPullParser1.next();
                                        todayWeather.setDate(xmlPullParser1.getText());
                                        dateCount++;
                                    } else if (xmlPullParser1.getName().equals("high") && highCount == 0) {
                                        eventType1 = xmlPullParser1.next();
                                        todayWeather.setHigh(xmlPullParser1.getText().substring(2).trim());
                                        highCount++;
                                    } else if (xmlPullParser1.getName().equals("low") && lowCount == 0) {
                                        eventType1 = xmlPullParser1.next();
                                        todayWeather.setLow(xmlPullParser1.getText().substring(2).trim());
                                        lowCount++;
                                    } else if (xmlPullParser1.getName().equals("type") && typeCount == 0) {
                                        eventType1 = xmlPullParser1.next();
                                        todayWeather.setType(xmlPullParser1.getText());
                                        typeCount++;
                                    }
                                }
                                // 若只有pm25和quality为空，只更新pm25和quality两个属性
                                if (xmlPullParser1.getName().equals("pm25")) {
                                    eventType1 = xmlPullParser1.next();
                                    todayWeather.setPm25(xmlPullParser1.getText());
                                } else if (xmlPullParser1.getName().equals("quality")) {
                                    eventType1 = xmlPullParser1.next();
                                    todayWeather.setQuality(xmlPullParser1.getText());
                                }
                                break;
                            // 判断当前事件是否为标签元素结束事件
                            case XmlPullParser.END_TAG:
                                break;
                        }
                        // 进入下一个元素并触发相应事件
                        eventType1 = xmlPullParser1.next();
                    }
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (con != null) {
                    con.disconnect();
                }
            }
        }
        return todayWeather;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String newCityCode = data.getStringExtra("cityCode");
            Log.d("myWeather", "选择的城市代码为" + newCityCode);

            if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
                Log.d("myWeather", "网络OK");
                queryWeatherCode(newCityCode);
            } else {
                Log.d("myWeather", "网络挂了");
                Toast.makeText(MainActivity.this, "网络挂了!", Toast.LENGTH_LONG).show();
            }
        }
    }
    // 打开GPS
    private void openGPSSettings() {
        LocationManager alm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (alm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "GPS模块正常", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "请开启GPS", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(Settings.ACTION_SECURITY_SETTINGS);
        startActivityForResult(i, 0);
    }

    /*private void getLocation() {
        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Criteria cr = new Criteria();
        cr.setAccuracy(Criteria.ACCURACY_FINE);
        cr.setAltitudeRequired(false);
        cr.setBearingRequired(false);
        cr.setCostAllowed(true);
        cr.setPowerRequirement(Criteria.POWER_LOW);
        String provider = lm.getBestProvider(cr, true);
        Location location = lm.getLastKnownLocation(provider);
        updateToNewLocation(location);
        // 设置监听器
        lm.requestLocationUpdates(provider, 3600 * 1000, 10000, locationListener);
    }

    private void updateToNewLocation(Location location) {
        TextView tv1;
        tv1 = (TextView)this.findViewById(R.id.tv1);
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude=location.getLongitude();
            tv1.setText("维度： "+ latitude+ "\n经度 "+longitude);
        } else {
            tv1.setText("无法获取地理信息");
            }
    }*/

    void initView() {
        city_name_Tv = (TextView) findViewById(R.id.title_city_name);
        cityTv = (TextView) findViewById(R.id.city);
        timeTv = (TextView) findViewById(R.id.time);
        humidityTv = (TextView) findViewById(R.id.humidity);
        weekTv = (TextView) findViewById(R.id.week_today);
        pmDataTv = (TextView) findViewById(R.id.pm_data);
        pmQualityTv = (TextView) findViewById(R.id.pm2_5_quality);
        pmImg = (ImageView) findViewById(R.id.pm2_5_img);
        temperatureTv = (TextView) findViewById(R.id.temperature);
        climateTv = (TextView) findViewById(R.id.climate);
        windTv = (TextView) findViewById(R.id.wind);
        weatherImg = (ImageView) findViewById(R.id.weather_img);

        city_name_Tv.setText("N/A");
        cityTv.setText("N/A");
        timeTv.setText("N/A");
        humidityTv.setText("N/A");
        weekTv.setText("N/A");
        pmDataTv.setText("N/A");
        pmQualityTv.setText("N/A");
        temperatureTv.setText("N/A");
        climateTv.setText("N/A");
        windTv.setText("N/A");
    }

    void initViewPage(){

        LayoutInflater inflater=LayoutInflater.from(this);
        views=new ArrayList<View>();
        views.add(inflater.inflate(R.layout.future1to3,null));
        views.add(inflater.inflate(R.layout.future4to6,null));
        vpAdapter=new futureviewPagerAdapter(views,this);
        vp=(ViewPager) findViewById(R.id.futureviewpager);
        vp.setAdapter(vpAdapter);
        vp.setOnPageChangeListener(this);
    }

    void initFutureSix() {
        weekDay1 = (TextView) views.get(0).findViewById(R.id.week_today1);
        weekDay2 = (TextView) views.get(0).findViewById(R.id.week_today2);
        weekDay3 = (TextView) views.get(0).findViewById(R.id.week_today3);
        weekDay4 = (TextView) views.get(1).findViewById(R.id.week_today4);
        weekDay5 = (TextView) views.get(1).findViewById(R.id.week_today5);
        weekDay6 = (TextView) views.get(1).findViewById(R.id.week_today6);

        temperature1 = (TextView) views.get(0).findViewById(R.id.temperature1);
        temperature2 = (TextView) views.get(0).findViewById(R.id.temperature2);
        temperature3 = (TextView) views.get(0).findViewById(R.id.temperature3);
        temperature4 = (TextView) views.get(1).findViewById(R.id.temperature4);
        temperature5 = (TextView) views.get(1).findViewById(R.id.temperature5);
        temperature6 = (TextView) views.get(1).findViewById(R.id.temperature6);

        dayType1 = (TextView) views.get(0).findViewById(R.id.dayclimate1);
        dayType2 = (TextView) views.get(0).findViewById(R.id.dayclimate2);
        dayType3 = (TextView) views.get(0).findViewById(R.id.dayclimate3);
        dayType4 = (TextView) views.get(1).findViewById(R.id.dayclimate4);
        dayType5 = (TextView) views.get(1).findViewById(R.id.dayclimate5);
        dayType6 = (TextView) views.get(1).findViewById(R.id.dayclimate6);

        nightType1 = (TextView) views.get(0).findViewById(R.id.nightclimate1);
        nightType2 = (TextView) views.get(0).findViewById(R.id.nightclimate2);
        nightType3 = (TextView) views.get(0).findViewById(R.id.nightclimate3);
        nightType4 = (TextView) views.get(1).findViewById(R.id.nightclimate4);
        nightType5 = (TextView) views.get(1).findViewById(R.id.nightclimate5);
        nightType6 = (TextView) views.get(1).findViewById(R.id.nightclimate6);

        dayFeng1 = (TextView) views.get(0).findViewById(R.id.daywind1);
        dayFeng2 = (TextView) views.get(0).findViewById(R.id.daywind2);
        dayFeng3 = (TextView) views.get(0).findViewById(R.id.daywind3);
        dayFeng4 = (TextView) views.get(1).findViewById(R.id.daywind4);
        dayFeng5 = (TextView) views.get(1).findViewById(R.id.daywind5);
        dayFeng6 = (TextView) views.get(1).findViewById(R.id.daywind6);

        nightFeng1 = (TextView) views.get(0).findViewById(R.id.nightwind1);
        nightFeng2 = (TextView) views.get(0).findViewById(R.id.nightwind2);
        nightFeng3 = (TextView) views.get(0).findViewById(R.id.nightwind3);
        nightFeng4 = (TextView) views.get(1).findViewById(R.id.nightwind4);
        nightFeng5 = (TextView) views.get(1).findViewById(R.id.nightwind5);
        nightFeng6 = (TextView) views.get(1).findViewById(R.id.nightwind6);
        weekDay1.setText("N/A");
        weekDay2.setText("N/A");
        weekDay3.setText("N/A");
        weekDay4.setText("N/A");
        weekDay5.setText("N/A");
        weekDay6.setText("N/A");

        temperature1.setText("N/A");
        temperature2.setText("N/A");
        temperature3.setText("N/A");
        temperature4.setText("N/A");
        temperature5.setText("N/A");
        temperature6.setText("N/A");

        dayType1.setText("N/A");
        dayType2.setText("N/A");
        dayType3.setText("N/A");
        dayType4.setText("N/A");
        dayType5.setText("N/A");
        dayType6.setText("N/A");

        nightType1.setText("N/A");
        nightType2.setText("N/A");
        nightType3.setText("N/A");
        nightType4.setText("N/A");
        nightType5.setText("N/A");
        nightType6.setText("N/A");

        dayFeng1.setText("N/A");
        dayFeng2.setText("N/A");
        dayFeng3.setText("N/A");
        dayFeng4.setText("N/A");
        dayFeng5.setText("N/A");
        dayFeng6.setText("N/A");

        nightFeng1.setText("N/A");
        nightFeng2.setText("N/A");
        nightFeng3.setText("N/A");
        nightFeng4.setText("N/A");
        nightFeng5.setText("N/A");
        nightFeng6.setText("N/A");
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://cn.edu.pku.jinge.miniweather/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://cn.edu.pku.jinge.miniweather/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    //百度定位
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 1000 * 60 * 60;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }


    //百度定位的函数实现
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            //Receive Location

            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());// 单位：公里每小时
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
                sb.append("\nheight : ");
                sb.append(location.getAltitude());// 单位：米
                sb.append("\ndirection : ");
                sb.append(location.getDirection());// 单位度
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());

                sb.append("\ndescribe : ");
                sb.append("gps定位成功");

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                //运营商信息
                sb.append("\noperationers : ");
                sb.append(location.getOperators());
                sb.append("\ndescribe : ");
                sb.append("网络定位成功");
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                sb.append("\ndescribe : ");
                sb.append("离线定位成功，离线定位结果也是有效的");
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                sb.append("\ndescribe : ");
                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                sb.append("\ndescribe : ");
                sb.append("网络不同导致定位失败，请检查网络是否通畅");
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                sb.append("\ndescribe : ");
                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
            }
            sb.append("\nlocationdescribe : ");
            sb.append(location.getLocationDescribe());// 位置语义化信息
            List<Poi> list = location.getPoiList();// POI数据
            if (list != null) {
                sb.append("\npoilist size = : ");
                sb.append(list.size());
                for (Poi p : list) {
                    sb.append("\npoi= : ");
                    sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
                }
            }
            Log.d("BaiduLocationApiDem", sb.toString());
            Log.d("地址", location.getAddrStr());
            addr = location.getAddrStr();
        }
    }

    // 初始化小圆点
    void initDots() {
        dots = new ImageView[views.size()];
        for (int i = 0; i < views.size(); i++) {
            dots [i] = (ImageView) findViewById(ids[i]);
        }
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
