package cn.edu.pku.jinge.miniweather;

import android.app.Activity;
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
import android.util.Log;
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

import cn.edu.pku.jinge.bean.TodayWeather;
import cn.edu.pku.jinge.util.NetUtil;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

/**
 * Created by jinge on 2016/9/27.
 */
public class MainActivity extends Activity implements View.OnClickListener {
    private static final int UPDATE_TODAY_WEATHER = 1;
    private ImageView mUpdateBtn;
    private ProgressBar mUpdateProg;
    private ImageView mLocateBtn;
    private ImageView mCitySelect;
    private ImageView mShareBtn;
    private TextView cityTv, timeTv, humidityTv, weekTv, pmDataTv, pmQualityTv,
            temperatureTv, climateTv, windTv, city_name_Tv;
    private ImageView weatherImg, pmImg;

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
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_info);

        // 启动service
        startService(new Intent(getBaseContext(), AutoUpdate.class));

        mUpdateBtn = (ImageView) findViewById(R.id.title_update_btn);
        mUpdateBtn.setOnClickListener(this);
        mLocateBtn = (ImageView) findViewById(R.id.title_location);
        mLocateBtn.setOnClickListener(this);
        mUpdateProg = (ProgressBar) findViewById(R.id.title_update_progress);

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
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.title_update_btn) {
            view.setVisibility(View.GONE);
            mShareBtn = (ImageView) findViewById(R.id.title_share);
            mShareBtn.setOnClickListener(this);
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
        int fengxiangCount = 0;
        int fengliCount = 0;
        int dateCount = 0;
        int highCount = 0;
        int lowCount = 0;
        int typeCount = 0;
        try {
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldata));
            int eventType = xmlPullParser.getEventType();
            Log.d("myWeather", "parseXML");
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    // 判断当前事件是否为文档开始事件
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    // 判断当前事件是否为标签元素开始事件
                    case XmlPullParser.START_TAG:
                        if (xmlPullParser.getName().equals("resp")) {
                            todayWeather = new TodayWeather();
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
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli(xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("date") && dateCount == 0) {
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
                                typeCount++;
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
                                //if (todayWeather.getCity() == null || todayWeather.getUpdatetime() == null) {
                                    // 一种情况是所有数据都没有，则都取上级城市的数据
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
}
