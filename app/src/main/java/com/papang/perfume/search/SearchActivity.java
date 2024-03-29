package com.papang.perfume.search;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.papang.perfume.ProductDetailsActivity;
import com.papang.perfume.R;
import com.papang.perfume.RecommendationActivity;
import com.papang.perfume.adapter.ResultProductAdpater;
import com.papang.perfume.data.DataApi;
import com.papang.perfume.data.DataService;
import com.papang.perfume.data.PerfumeWish;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxItemDecoration;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static java.lang.Thread.sleep;

public class SearchActivity extends AppCompatActivity {

    DataService dataService;
    DataApi dataApi;

    ArrayList<String> p_name;
    ArrayList<String> p_brand;
    private boolean itemTouch;

    ImageButton search_back_btn;                // 뒤로가기 버튼
    EditText search_input;                      // 검색어 입력 창
    RecyclerView recent_search_item;            // 최근 검색어 리싸이클러뷰
    RecyclerView recommendation_search_item;    // 추천 검색어 리싸이클러뷰
    RecyclerView result_search_item;            // 향수 검색 결과 리싸이클러뷰
    ImageButton search_find_perfume_btn;        // 향수 추천 버튼
    ImageButton btn_all_delete;
    ImageButton search_btn;

    TextView search_txt2;
    TextView count_txt1;

    ConstraintLayout search_frame3;             // 최근 검색어
    ConstraintLayout search_frame4;             // 추천 검색어
    ConstraintLayout search_frame5;             // 나만의 향수
    ConstraintLayout search_frame6;             // 검색된 향수
    ConstraintLayout search_frame7;             // 검색 결과 x

    FlexboxLayoutManager layoutManager;
    RecentSearchAdapter recent_adapter;
    RecommendationSearchAdapter recommendation_adapter;
    FlexboxItemDecoration itemDecoration;

    ResultProductAdpater result_product_adapter;

    ProgressBar loading_pb;
    Thread thread;
    Boolean isCheckedData = false;
    Boolean isCheckedPrice = false;
    int i  = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        dataService = new DataService();
        dataApi =  dataService.getRetrofitClient().create(DataApi.class);

        // 키보드 밀림 방지
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        loading_pb = (ProgressBar)findViewById(R.id.loading_pb);

        search_frame3 = (ConstraintLayout)findViewById(R.id.search_frame3);
        search_frame4 = (ConstraintLayout)findViewById(R.id.search_frame4);
        search_frame5 = (ConstraintLayout)findViewById(R.id.search_frame5);
        search_frame6 = (ConstraintLayout)findViewById(R.id.search_frame6);
        search_frame7 = (ConstraintLayout)findViewById(R.id.search_frame7);

        search_btn = (ImageButton)findViewById(R.id.search_btn);

        search_back_btn = (ImageButton)findViewById(R.id.search_back_btn);
        search_back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        search_input = (EditText)findViewById(R.id.search_input);
        count_txt1 = (TextView)findViewById(R.id.count_txt1);

        recent_search_item = (RecyclerView)findViewById(R.id.recent_search_item);
        layoutManager = new FlexboxLayoutManager(getApplicationContext());
        layoutManager.setFlexDirection (FlexDirection.ROW);
        layoutManager.setJustifyContent (JustifyContent.FLEX_START);
        recent_search_item.setLayoutManager(layoutManager);
        recent_adapter = new RecentSearchAdapter(getApplicationContext());
        recent_search_item.setAdapter(recent_adapter);

        recommendation_search_item = (RecyclerView)findViewById(R.id.recommendation_search_item);
        layoutManager = new FlexboxLayoutManager(getApplicationContext());
        layoutManager.setFlexDirection (FlexDirection.ROW);
        layoutManager.setJustifyContent (JustifyContent.FLEX_START);
        recommendation_search_item.setLayoutManager(layoutManager);
        recommendation_adapter = new RecommendationSearchAdapter(getApplicationContext());
        recommendation_search_item.setAdapter(recommendation_adapter);

        // 향수 검색 결과
        result_search_item = (RecyclerView)findViewById(R.id.result_search_item);
        result_search_item.setLayoutManager(new LinearLayoutManager(this));

        search_find_perfume_btn = (ImageButton)findViewById(R.id.search_find_perfume_btn);
        search_find_perfume_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent go_recommendation = new Intent(getApplicationContext(), RecommendationActivity.class);
                startActivity(go_recommendation);
                finish();
            }
        });


        btn_all_delete = (ImageButton)findViewById(R.id.btn_all_delete);
        btn_all_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recent_adapter.allDelete();
                search_frame3.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "전체삭제 했습니다", 0).show();
            }
        });

        search_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(search_input.length() == 0) {
                    search_frame3.setVisibility(View.VISIBLE);
                    search_frame4.setVisibility(View.VISIBLE);
                    search_frame5.setVisibility(View.VISIBLE);
                    search_frame7.setVisibility(View.GONE);
                    search_frame6.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(search_input.getText().toString().equals(""))
                    Toast.makeText(getApplicationContext(), "검색어를 입력해주세요.", 0).show();
                else
                {
                    getSearchResult(search_input.getText().toString());

                    loading_pb.setVisibility(View.VISIBLE);
                    // 로딩바 - 화면 터치 막기
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                    // 로딩 중
                    thread = new Thread(new Runnable() {
                        public void run() {
                            while(true){
                                try{
                                    i++;
                                    sleep(1000);
                                    if(isCheckedData == true) { // 향수 정보랑 이미지 불러와졌으면
                                        handler.sendEmptyMessage(1);
                                        break;
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }
                    }); thread.start();

                }
            }
        });

        // 키보드 검색
        search_input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if(search_input.getText().toString().equals(""))
                        Toast.makeText(getApplicationContext(), "검색어를 입력해주세요.", 0).show();
                    else
                    {
                        getSearchResult(search_input.getText().toString());

                        loading_pb.setVisibility(View.VISIBLE);
                        // 로딩바 - 화면 터치 막기
                        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                        // 로딩 중
                        thread = new Thread(new Runnable() {
                            public void run() {
                                while(true){
                                    try{
                                        i++;
                                        sleep(1000);
                                        if(isCheckedData == true) { // 향수 정보랑 이미지 불러와졌으면
                                            handler.sendEmptyMessage(1);
                                            break;
                                        }
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }); thread.start();

                    }

                    return true;
                }
                return false;
            }
        });

        result_search_item.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                if (MotionEvent.ACTION_UP == e.getAction() && itemTouch) {
                    View reV = rv.findChildViewUnder(e.getX(), e.getY());
                    String p_name = result_product_adapter.getName(rv.getChildAdapterPosition(reV));
                    Intent gotoDetail = new Intent(getApplicationContext(), ProductDetailsActivity.class);
                    gotoDetail.putExtra("name", p_name);
                    startActivity(gotoDetail);
                } else if (MotionEvent.ACTION_DOWN == e.getAction()) {
                    itemTouch = true;
                }
                return false;

            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });

        result_search_item.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                itemTouch = false;
            }
        });
    }

    private void getSearchResult(String word){
        recent_adapter.update(word);

        dataApi.getSearchPerfume(word).enqueue(new Callback<List<PerfumeWish>>() {
            @Override
            public void onResponse(Call<List<PerfumeWish>> call, Response<List<PerfumeWish>> response) {
                p_name = new ArrayList<>();
                p_brand = new ArrayList<>();

                List<PerfumeWish> pw = new ArrayList<>();
                pw = response.body();

                for(int i = 0; i < pw.size(); i++){
                    p_name.add(pw.get(i).getName());
                    p_brand.add(pw.get(i).getBrand());
                }
                search_frame3.setVisibility(View.GONE);
                search_frame4.setVisibility(View.GONE);
                search_frame5.setVisibility(View.GONE);
                if(pw.size() == 0){
                    search_frame6.setVisibility(View.GONE);
                    search_frame7.setVisibility(View.VISIBLE);
                }
                else{
                    search_frame6.setVisibility(View.VISIBLE);
                    search_frame7.setVisibility(View.GONE);
                }
                result_product_adapter = new ResultProductAdpater(getApplicationContext(), p_name, p_brand);
                result_search_item.setAdapter(result_product_adapter);
                count_txt1.setText(pw.size() + "건");

                isCheckedData = true;
            }

            @Override
            public void onFailure(Call<List<PerfumeWish>> call, Throwable t) {
            }
        });
    }

    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            if(msg.what == 1)
            {
                loading_pb.setVisibility(View.INVISIBLE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        }
    };
}