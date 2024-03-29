package com.papang.perfume.review;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.papang.perfume.R;
import com.papang.perfume.adapter.ReviewListAdapter;
import com.papang.perfume.adapter.ReviewTabAdapter;
import com.papang.perfume.custom.WrapHeightViewPager;
import com.google.android.material.tabs.TabLayout;

public class AllReviewActivity extends AppCompatActivity {
    Toolbar toolbar;
    ActionBar actionBar;

    ImageButton all_review_btn_back;            // 뒤로 가기 버튼
    ImageButton all_review_btn_write;           // 리뷰 작성 버튼

    TextView all_review_count;                  // 리뷰 갯수

    TabLayout all_review_tab;                   // 평점 계절 성별 탭레이아웃
    ReviewTabAdapter allreviewAdapter;          // 탭레이아웃 어댑터
    WrapHeightViewPager all_review_viewpager;   // 탭레이아웃 뷰페이저

    RecyclerView all_review_list;               // 리뷰 목록
    LinearLayoutManager mLayoutManager;
    ReviewListAdapter alladapter;

    Spinner spinner_filter;                     // 필터 스피너

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_review);

        toolbar = findViewById(R.id.all_review_toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);//기본 제목을 없애줍니다.
        actionBar.setDisplayHomeAsUpEnabled(false);

        all_review_btn_back = (ImageButton) findViewById(R.id.all_review_btn_back);
        all_review_btn_write = (ImageButton) findViewById(R.id.all_review_btn_write);
        all_review_btn_write.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goWriteReview = new Intent(getApplicationContext(), ReviewWriteActivity.class);
                startActivity(goWriteReview);
                finish();
            }
        });

        all_review_count = (TextView) findViewById(R.id.all_review_count);
        all_review_tab = (TabLayout) findViewById(R.id.all_review_tab);
        all_review_viewpager = (WrapHeightViewPager) findViewById(R.id.all_review_viewpager);
        all_review_list = (RecyclerView) findViewById(R.id.all_review_list);

        allreviewAdapter = new ReviewTabAdapter(getSupportFragmentManager());
        all_review_viewpager.setAdapter(allreviewAdapter);
        all_review_tab.setupWithViewPager(all_review_viewpager);    // 리뷰 뷰페이저랑 탭 아이템이랑 연동

        mLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        all_review_list.setLayoutManager(mLayoutManager);
        alladapter = new ReviewListAdapter(getApplicationContext(), "김실버");
        all_review_list.setAdapter(alladapter);

        // 필터
        final String[] filter = {"추천순", "최신순", "사진리뷰"};
        spinner_filter = (Spinner) findViewById(R.id.spinner_filter);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.item_spinner_filter, filter);
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_filter.setAdapter(adapter);

        spinner_filter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //Toast.makeText(getApplicationContext(),"선택된 거: " + filter[i] , Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
}