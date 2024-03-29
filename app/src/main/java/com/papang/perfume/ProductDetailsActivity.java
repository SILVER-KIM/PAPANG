package com.papang.perfume;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.papang.perfume.adapter.ExpandablePriceAdapter;
import com.papang.perfume.adapter.HashtagAdapter;
import com.papang.perfume.adapter.PerfumeSizeAdapter;
import com.papang.perfume.adapter.ReviewListAdapter;
import com.papang.perfume.adapter.ReviewTabAdapter;
import com.papang.perfume.custom.WrapHeightViewPager;
import com.papang.perfume.data.DataApi;
import com.papang.perfume.data.DataService;
import com.papang.perfume.data.Flavor;
import com.papang.perfume.data.Hashtag;
import com.papang.perfume.data.Perfume;
import com.papang.perfume.data.PerfumeWish;
import com.papang.perfume.data.Price;
import com.papang.perfume.data.SearchPrice;
import com.papang.perfume.data.Wish;
import com.papang.perfume.main.home.Product_Decoration;
import com.papang.perfume.review.AllReviewActivity;
import com.papang.perfume.review.ReviewWriteActivity;
import com.google.android.material.tabs.TabLayout;
import com.papang.perfume.object.Product;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static java.lang.Thread.sleep;

public class ProductDetailsActivity extends AppCompatActivity {

    public static Context pContext;

    Toolbar toolbar;
    ActionBar actionBar;
    File file;

    Flavor flavor;
    String path;
    String p_name;
    String access;
    String email;
    List<Perfume> r_perfumes;       // 파팡 추천 향수
    Price r_urls;       // 파팡 URLS
    ArrayList<String> urls;
    List<Hashtag> hashtags;         // 해시태그
    DataService dataService;
    DataApi dataApi;

    ArrayList<Product> mData;
    ArrayList<String> shop_name;
    ArrayList<String> price;
    SearchPrice searchPrice;

    ImageView product_image;            // 상품 사진
    ImageView detail_note_1;            // 향수 노트 1
    ImageView detail_note_2;            // 향수 노트 2
    ImageView detail_note_3;            // 향수 노트 3
    ImageView d_text2;

    TextView detail_shop_name;          // 쇼핑몰 이름
    TextView detail_product_name;       // 상품 이름
    TextView detail_size;               // 용량 text
    TextView wish_count;                // 찜 개수

    ImageButton btn_detail_wish;        // 찜 버튼
    ImageButton btn_detail_share;       // 공유 버튼
    ImageButton btn_size;               // 사이즈 변경(업 패널)
    ImageButton btn_shop;               // 웹 뷰 호출(쇼핑몰)
    ImageButton btn_go_review;          // 리뷰쓰러가기
    ImageButton btn_more_review;        // 리뷰 전체 보기
    ImageButton btn_back_detail_page;   // 상세페이지 뒤로가기

    SlidingUpPanelLayout product_slidinglayout;     // 슬라이딩 업 패널
    RecyclerView detail_size_item;                  // 용량 리싸이클러뷰
    RecyclerView detail_product_tag;                // 향수 관련 태그
    RecyclerView detail_similar_product;            // 비슷한 향수

    RecyclerView pd_review_list;                    // 리뷰 목록

    TabLayout review_tab;
    WrapHeightViewPager review_viewpager;
    ReviewTabAdapter rtAdapter;


    ArrayList<String> sizes;
    ArrayList<Integer> ids;
    ArrayList<String> checks;

    LinearLayoutManager mLayoutManager;
    PerfumeSizeAdapter adapter;
    HashtagAdapter adapter2;
    Product_Decoration decoration;
    ReviewListAdapter radapter;
    ExpandablePriceAdapter expandable_adpater;
    ExpandableListView detail_price_item;

    ProgressBar loading_pb;
    ConstraintLayout whole_frame;
    Thread thread;
    Boolean isCheckedData = false;
    Boolean isCheckedPrice = false;
    Boolean isCheckedChangedPrice = false;
    int i  = 0;
    private boolean itemTouch;

    // 뒤로가기 버튼
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        // 로딩바 - 화면 터치 막기
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        pContext = this;

        flavor = new Flavor(getApplicationContext());
        decoration = new Product_Decoration();
        Intent intent = getIntent();
        p_name = intent.getExtras().getString("name");

        detail_shop_name = (TextView)findViewById(R.id.detail_shop_name);
        detail_product_name = (TextView)findViewById(R.id.detail_product_name);
        product_image = (ImageView)findViewById(R.id.detail_prouduct_image);
        detail_note_1 = (ImageView)findViewById(R.id.detail_note_1);
        detail_note_2 = (ImageView)findViewById(R.id.detail_note_2);
        detail_note_3 = (ImageView)findViewById(R.id.detail_note_3);
        detail_size = (TextView)findViewById(R.id.detail_size);
        wish_count = (TextView)findViewById(R.id.wish_count);
        btn_detail_wish = (ImageButton)findViewById(R.id.btn_detail_wish);
        btn_shop = (ImageButton)findViewById(R.id.btn_shop);
        d_text2 = (ImageView)findViewById(R.id.d_text2);

        shop_name = new ArrayList<>();
        price = new ArrayList<>();

        dataService = new DataService();
        dataApi =  dataService.getRetrofitClient().create(DataApi.class);
        r_perfumes = new ArrayList<>();     // 파팡 추천 향수
        hashtags = new ArrayList<>();

        product_slidinglayout = (SlidingUpPanelLayout) findViewById(R.id.product_slidinglayout);
        detail_size_item = (RecyclerView) findViewById(R.id.detail_size_item);
        mLayoutManager = new LinearLayoutManager(ProductDetailsActivity.this, LinearLayoutManager.HORIZONTAL, false);
        detail_size_item.setLayoutManager(mLayoutManager);
        checkLogin();
        getImage();

        detail_product_tag = (RecyclerView) findViewById(R.id.detail_product_tag);
        mLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        detail_product_tag.setLayoutManager(mLayoutManager);
        detail_product_tag.addItemDecoration(decoration);

        loading_pb = (ProgressBar)findViewById(R.id.loading_pb);
        whole_frame = (ConstraintLayout)findViewById(R.id.whole_frame);

        // 로딩 중
        thread = new Thread(new Runnable() {
            public void run() {
                while(true){
                    try{
                        i++;
                        sleep(1000);
                        if(isCheckedData == true && isCheckedPrice == true) { // 향수 정보랑 가격 불러와졌으면
                            handler.sendEmptyMessage(1);
                            break;
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }); thread.start();

        dataApi.selectName(p_name).enqueue(new Callback<List<Perfume>>() {
            @Override
            public void onResponse(Call<List<Perfume>> call, Response<List<Perfume>> response) {
                r_perfumes = response.body();
                isCheckedData = true;
                if(r_perfumes.size() != 0)
                    getInfo();
            }

            @Override
            public void onFailure(Call<List<Perfume>> call, Throwable t) {
                Log.e("연결", t.getMessage());
            }
        });

        detail_size_item.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    View reV = rv.findChildViewUnder(e.getX(), e.getY());
                    int position = rv.getChildAdapterPosition(reV);
                    // 로딩바 - 화면 터치 막기
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    isCheckedChangedPrice = false;
                    if (position >= 0) {
                        loading_pb.setVisibility(View.VISIBLE);
                        newThread();

                        int id = adapter.getID(position);
                        if(adapter.getCheck(position).equals("TRUE")){
                            detail_price_item.setVisibility(View.VISIBLE);
                            getURL(id);
                        }
                        else if(adapter.getCheck(position).equals("FALSE"))
                        {
                            detail_price_item.setVisibility(View.GONE);
                            isCheckedChangedPrice = true;
                        }
                        String num = String.valueOf(adapter.getSize(position));
                        detail_size.setText(num + "ml");
                        adapter.notifyDataSetChanged();
                        product_slidinglayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        }
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


        pd_review_list = (RecyclerView) findViewById(R.id.pd_review_list);
        mLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        pd_review_list.setLayoutManager(mLayoutManager);
        radapter = new ReviewListAdapter(getApplicationContext(), "김실버");
        pd_review_list.setAdapter(radapter);

        btn_size = (ImageButton) findViewById(R.id.btn_size);
        btn_size.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                product_slidinglayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            }
        });

        detail_price_item = (ExpandableListView) findViewById(R.id.detail_price_item);

        detail_price_item.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View view, int groupPosition, long l) {
                expandable_adpater.setListViewHeight(parent, groupPosition);
                return false;
            }
        });

        btn_go_review = (ImageButton) findViewById(R.id.btn_go_review);
        btn_go_review.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent review = new Intent(getApplicationContext(), ReviewWriteActivity.class);
                startActivity(review);
            }
        });

        review_tab = (TabLayout) findViewById(R.id.review_tab);
        review_viewpager = (WrapHeightViewPager) findViewById(R.id.review_viewpager);
        rtAdapter = new ReviewTabAdapter(getSupportFragmentManager());
        review_viewpager.setAdapter(rtAdapter);

        review_tab.setupWithViewPager(review_viewpager);    // 리뷰 뷰페이저랑 탭 아이템이랑 연동

        btn_more_review = (ImageButton) findViewById(R.id.btn_more_review);
        btn_more_review.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent allReview = new Intent(getApplicationContext(), AllReviewActivity.class);
                startActivity(allReview);
            }
        });

        btn_detail_wish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!access.equals("Login"))
                    Toast.makeText(getApplicationContext(), "로그인 후 이용해 주세요!", 0).show();
                else if(access.equals("Login") && btn_detail_wish.getDrawable().getConstantState().equals(getResources().getDrawable(R.mipmap.heart_icon).getConstantState())){         // 찜하는 함수
                    addWishCount();
                    addWishList();
                }
                else if(access.equals("Login") && btn_detail_wish.getDrawable().getConstantState().equals(getResources().getDrawable(R.mipmap.heart_full_icon).getConstantState())){        // 찜 삭제하는 함수
                    deleteWishCount();
                    deleteWishList(detail_shop_name.getText().toString());
                }
            }
        });

        // 뒤로가기
        btn_back_detail_page = (ImageButton) findViewById(R.id.btn_back_detail_page);
        btn_back_detail_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btn_shop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(r_perfumes.size() > 0) {
                    Intent shop = new Intent(getApplicationContext(), WebViewActivity.class);
                    shop.putExtra("url", r_perfumes.get(0).getUrl());
                    startActivity(shop);
                }
                else
                    Toast.makeText(getApplicationContext(), "잠시만 기다려 주세요.", 0).show();
            }
        });

        detail_price_item.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                String url = expandable_adpater.getURL(groupPosition, childPosition);
                Intent shop = new Intent(getApplicationContext(), WebViewActivity.class);
                shop.putExtra("url", url);
                startActivity(shop);
                return false;
            }
        });
    }

    private void newThread() {
        thread = new Thread(new Runnable() {
        public void run() {
            while(true){
                try{
                    i++;
                    sleep(1000);
                    if(isCheckedChangedPrice == true)          // 향수 용량 변경하면
                    {
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

    private void getInfo() {
        detail_shop_name.setText(r_perfumes.get(0).getBrand());         // 브랜드
        detail_product_name.setText(p_name);

        if(TextUtils.isEmpty(r_perfumes.get(0).getUrl())) {
            d_text2.setImageDrawable(getResources().getDrawable(R.mipmap.detail_no_txt));
            btn_shop.setVisibility(View.INVISIBLE);
        }

        getWishCount();
        checkWishList(detail_shop_name.getText().toString());

        detail_note_1.setImageDrawable(flavor.concentrations.get(r_perfumes.get(0).getConcentration()-1));
        detail_note_2.setImageDrawable(flavor.flavors.get(r_perfumes.get(0).getMain()));
        detail_note_3.setImageDrawable(flavor.flavors.get(r_perfumes.get(0).getFirst()));
        detail_size.setText(r_perfumes.get(0).getSize() + "ml");

        ArrayList<Integer> flavors = new ArrayList<>();
        getHashtag(r_perfumes.get(0).getMain(), r_perfumes.get(0).getFirst());
        urls = new ArrayList<>();

        for(int i = 0; i < r_perfumes.size(); i++){
            if(r_perfumes.get(0).getCheck().equals("TRUE"))
            {
                getURL((r_perfumes.get(0).getPerfumeID()));
                break;
            }
            else {
                // 가격이 없을 경우에도 가격을 불러왔다고 체크해야함
                isCheckedPrice = true;
            }
        }
        // 사이즈 받아온걸로 세팅
        sizes = new ArrayList<>();
        ids = new ArrayList<>();
        checks = new ArrayList<>();

        for(int i = 0; i < r_perfumes.size(); i++){
            checks.add(r_perfumes.get(i).getCheck());
            ids.add(r_perfumes.get(i).getPerfumeID());
            sizes.add(String.valueOf(r_perfumes.get(i).getSize()));
            Log.e("값", String.valueOf(r_perfumes.get(i).getSize()));
        }

        adapter = new PerfumeSizeAdapter(getApplicationContext(), sizes, ids, checks);
        detail_size_item.setAdapter(adapter);
    }

    public void setSizeUrl(int perfumeID){

    }

    private void addWishCount(){
        Map<String, String> map = new HashMap();
        final int count = Integer.parseInt(wish_count.getText().toString()) + 1;
        map.put("wish_count", String.valueOf(count));
        dataApi.addWishCount(p_name, detail_shop_name.getText().toString(), map).enqueue(new Callback<PerfumeWish>() {
            @Override
            public void onResponse(Call<PerfumeWish> call, Response<PerfumeWish> response) {
                wish_count.setText(String.valueOf(count));
            }

            @Override
            public void onFailure(Call<PerfumeWish> call, Throwable t) {

            }
        });
    }

    private void deleteWishCount(){
        Map<String, String> map = new HashMap();
        final int count = Integer.parseInt(wish_count.getText().toString()) - 1;
        map.put("wish_count", String.valueOf(count));
        dataApi.deleteWishCount(p_name, detail_shop_name.getText().toString(), map).enqueue(new Callback<PerfumeWish>() {
            @Override
            public void onResponse(Call<PerfumeWish> call, Response<PerfumeWish> response) {
                wish_count.setText(String.valueOf(count));
            }

            @Override
            public void onFailure(Call<PerfumeWish> call, Throwable t) {

            }
        });
    }


    private void checkWishList(String p_brand){
        if(access.equals("Login")){
            dataApi.getWisPerfume(email, p_brand, p_name).enqueue(new Callback<Wish>() {
                @Override
                public void onResponse(Call<Wish> call, Response<Wish> response) {
                    btn_detail_wish.setImageDrawable(getResources().getDrawable(R.mipmap.heart_full_icon));
                }

                @Override
                public void onFailure(Call<Wish> call, Throwable t) {
                    Log.e("오류", t.getMessage());
                }
            });
        }
    }

    private void addWishList(){
        Map<String, String> map = new HashMap();
        map.put("email", email);
        map.put("perfumeID", String.valueOf(r_perfumes.get(0).getPerfumeID()));
        map.put("brand", detail_shop_name.getText().toString());
        map.put("name", p_name);
        dataApi.addWishList(map).enqueue(new Callback<Wish>() {
            @Override
            public void onResponse(Call<Wish> call, Response<Wish> response) {
                btn_detail_wish.setImageDrawable(getResources().getDrawable(R.mipmap.heart_full_icon));
                Toast.makeText(ProductDetailsActivity.this, "찜목록 추가!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Wish> call, Throwable t) {
            }
        });
    }

    private void deleteWishList(String p_brand){
        dataApi.deleteWish(email, p_brand, p_name).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                btn_detail_wish.setImageDrawable(getResources().getDrawable(R.mipmap.heart_icon));
                Toast.makeText(ProductDetailsActivity.this, "찜목록 삭제!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {

            }
        });
    }

    private void checkLogin(){
        SharedPreferences sharedPreferences = getSharedPreferences("Info", MODE_PRIVATE);    // test 이름의 기본모드 설정, 만약 test key값이 있다면 해당 값을 불러옴.
        access = sharedPreferences.getString("Access","");
        email = sharedPreferences.getString("Email","");
    }

    public void getWishCount(){
        dataApi.getPerfumeWish(p_name, detail_shop_name.getText().toString()).enqueue(new Callback<PerfumeWish>() {
            @Override
            public void onResponse(Call<PerfumeWish> call, Response<PerfumeWish> response) {
                PerfumeWish pw = response.body();
                wish_count.setText(String.valueOf(pw.getWish_count()));
            }

            @Override
            public void onFailure(Call<PerfumeWish> call, Throwable t) {

            }
        });
    }

    public void getURL(int perfumeID){
        dataApi.getPerfumeURL(perfumeID).enqueue(new Callback<Price>() {
            @Override
            public void onResponse(Call<Price> call, Response<Price> response) {
                urls = new ArrayList<>();
                r_urls = response.body();
                if(!r_urls.getUrl1().equals(""))
                    urls.add(r_urls.getUrl1());
                if(!r_urls.getUrl2().equals(""))
                    urls.add(r_urls.getUrl2());
                if(!r_urls.getUrl3().equals(""))
                    urls.add(r_urls.getUrl3());

                searchPrice = new SearchPrice(ProductDetailsActivity.this, urls);
                searchPrice.execute();
            }

            @Override
            public void onFailure(Call<Price> call, Throwable t) {

            }
        });
    }

    public void setURl(){
        shop_name = searchPrice.shops;
        price = searchPrice.prices;
        urls = searchPrice.urls;
        if(price.size() > 0) {
            Product product = new Product(shop_name, price, urls);
            mData = new ArrayList<>();
            mData.add(product);
            //expandable_adpater.clear();
            expandable_adpater = new ExpandablePriceAdapter(getApplicationContext(), mData);
            detail_price_item.setAdapter(expandable_adpater);
            detail_price_item.setGroupIndicator(null);
            expandable_adpater.notifyDataSetChanged();

            //로딩
            isCheckedPrice = true;
            isCheckedChangedPrice = true;
            if(isCheckedChangedPrice == true)
                //가격 불러오는 동안 리스트 접어버리기
                expandable_adpater.setListViewHeight(detail_price_item, -1);
        }
    }

    public void getHashtag(int main, int first) {
        final ArrayList<String> hashtag = new ArrayList<>();
        dataApi.getFlavorHashtag(main, first).enqueue(new Callback<List<Hashtag>>() {
            @Override
            public void onResponse(Call<List<Hashtag>> call, Response<List<Hashtag>> response) {
                hashtags = response.body();
                for (int j = 0; j < hashtags.size(); j++) {
                    hashtag.add(hashtags.get(j).getHashtag());
                }
            }

            @Override
            public void onFailure(Call<List<Hashtag>> call, Throwable t) {
                Log.e("연결", t.getMessage());
            }
        });
        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            public void run() {
                adapter2 = new HashtagAdapter(getApplicationContext(), hashtag);
                detail_product_tag.setAdapter(adapter2);
            }
        }, 1000); // 0.5초후
    }

    public void getImage(){
        String url = "https://papang-bucket.s3.ap-northeast-2.amazonaws.com/resources/perfume_de/" + p_name.trim() + ".png";
        Glide.with(this).load(url).into(product_image);
    }

    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            if(msg.what == 1)
            {
                loading_pb.setVisibility(View.INVISIBLE);
                //whole_frame.setVisibility(View.VISIBLE);
                //whole_frame.setBackground(null);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        }
    };
}