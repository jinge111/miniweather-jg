package cn.edu.pku.jinge.miniweather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import cn.edu.pku.jinge.bean.City;
import cn.edu.pku.jinge.app.MyApplication;

/**
 * Created by jinge on 2016/10/18.
 */
public class SelectCity extends Activity implements View.OnClickListener{
    private ImageView mBackBtn;
    private TextView tvCityName;
    private List<City> mCityList;
    private String SeletedId;
    ArrayList<String> city = new ArrayList<String>();
    ArrayList<String> cityId = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCityList = MyApplication.getInstance().getCityList();

        for (int i=0; i < mCityList.size(); ++i) {
            city.add(mCityList.get(i).getCity().toString());
            cityId.add(mCityList.get(i).getNumber().toString());
        }

        setContentView(R.layout.select_city);
        ListView mlistView = (ListView) findViewById(R.id.list_view);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(SelectCity.this,android.R.layout.simple_list_item_1,city);
        mlistView.setAdapter(adapter);
        tvCityName = (TextView) findViewById(R.id.title_name);

        mlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                tvCityName.setText("选择城市:" + city.get(i));
                SeletedId = cityId.get(i);
            }
        });

        mBackBtn = (ImageView) findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.title_back:
                Intent i = new Intent();
                i.putExtra("cityCode", SeletedId);
                setResult(RESULT_OK, i);
                finish(); // 退出该Activity
                break;
            default:
                break;
        }
    }
}
