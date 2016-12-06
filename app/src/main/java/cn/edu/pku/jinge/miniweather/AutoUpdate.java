package cn.edu.pku.jinge.miniweather;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by jinge on 2016/11/15.
 */
public class AutoUpdate extends Service{
    int count = 0;
    // 每隔60秒更新一次
    static final int UPDATE_INTERVAL = 60 * 1000;
    private Timer timer = new Timer();

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        UpdateRepeatedly();
        return START_STICKY;
    }

    private void UpdateRepeatedly() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Log.d("Update Success!", String.valueOf(++count % 100));
                SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
                String cityCode = sharedPreferences.getString("main_city_code", "101010100");
            }
        }, 0, UPDATE_INTERVAL);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
