package cn.edu.pku.jinge.miniweather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
    private EditText mEditText;
    private List<City> mCityList;
    private String SeletedId;
    private ListView mlistView;
    private City citySelected;
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
        mlistView = (ListView) findViewById(R.id.list_view);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(SelectCity.this,android.R.layout.simple_list_item_1,city);
        mlistView.setAdapter(adapter);
        tvCityName = (TextView) findViewById(R.id.title_name);

        mlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                citySelected=mCityList.get(i);
                tvCityName.setText("选择城市:" + citySelected.getCity());
                //SeletedId = cityId.get(i);
            }
        });

        mEditText = (EditText)findViewById(R.id.search_edit);
        mEditText.addTextChangedListener(mTextWatcher);

        mBackBtn = (ImageView) findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);
    }

    TextWatcher mTextWatcher = new TextWatcher() {
        private CharSequence temp;
        private int editStart;
        private int editEnd;
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i1, int i2, int i3) {
            temp = charSequence;
            Log.d("SelectCity", "beforeTextChanged: " + temp);
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i1, int i2, int i3) {
            final List<String> mUpdateList = new ArrayList<String>();
            final List<City> citylist=new ArrayList<City>();
            for (City c : mCityList) {
                // 若匹配，则加入到更新列表中
                if (c.getCity().contains(charSequence)) {
                    mUpdateList.add(c.getCity());
                    citylist.add(c);
                }
            }


            ListView mlistView = (ListView) findViewById(R.id.list_view);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(SelectCity.this,android.R.layout.simple_list_item_1,mUpdateList);
            mlistView.setAdapter(adapter);
            mlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                    citySelected = citylist.get(i);
                    //SeletedId = mUpdateList.get(i);
                    tvCityName.setText("选择城市:" + citySelected.getCity());
                }
            });

        }

        @Override
        public void afterTextChanged(Editable editable) {
            editStart = mEditText.getSelectionStart();
            editEnd = mEditText.getSelectionEnd();
            if (editable.length() > 10) {
                Toast.makeText(SelectCity.this, "输入字数超过限制！", Toast.LENGTH_SHORT).show();
                editable.delete(editStart-1, editEnd);
                int tempSelection = editStart;
                mEditText.setText(editable);
                mEditText.setSelection(tempSelection);
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.title_back:
                Intent i = new Intent();
                if (citySelected != null) {
                    i.putExtra("cityCode", citySelected.getNumber());
                } else {
                    i.putExtra("cityCode", "101010100");
                }
                setResult(RESULT_OK, i);
                finish(); // 退出该Activity
                break;
            default:
                break;
        }
    }
}
