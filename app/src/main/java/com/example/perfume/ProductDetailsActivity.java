package com.example.perfume;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.bumptech.glide.Glide;
import com.example.perfume.adapter.ExpandablePriceAdapter;
import com.example.perfume.adapter.PerfumeSizeAdapter;
import com.example.perfume.adapter.ReviewListAdapter;
import com.example.perfume.adapter.ReviewTabAdapter;
import com.example.perfume.custom.WrapHeightViewPager;
import com.example.perfume.data.DataApi;
import com.example.perfume.data.DataService;
import com.example.perfume.data.Flavor;
import com.example.perfume.data.Hashtag;
import com.example.perfume.data.Perfume;
import com.example.perfume.data.Price;
import com.example.perfume.data.SearchPrice;
import com.example.perfume.main.home.Product_Decoration;
import com.example.perfume.main.home.Product_RecyclerView_Adapter;
import com.example.perfume.review.AllReviewActivity;
import com.example.perfume.review.ReviewWriteActivity;
import com.google.android.material.tabs.TabLayout;
import com.example.perfume.object.Product;
import com.example.perfume.review.AllReviewActivity;
import com.example.perfume.review.ReviewWriteActivity;
import com.google.android.material.tabs.TabLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import iammert.com.expandablelib.ExpandableLayout;
import lombok.val;
import lombok.var;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ProductDetailsActivity extends AppCompatActivity {

    public static Context mContext;

    Toolbar toolbar;
    ActionBar actionBar;

    Flavor flavor;
    String path;
    String p_name;
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

    TextView detail_shop_name;          // 쇼핑몰 이름
    TextView detail_product_name;       // 상품 이름
    TextView detail_size;               // 용량 text

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
    ArrayList<String> tags;

    LinearLayoutManager mLayoutManager;
    PerfumeSizeAdapter adapter;
    PerfumeSizeAdapter adapter2;
    Product_Decoration decoration;
    ReviewListAdapter radapter;
    ExpandablePriceAdapter expandable_adpater;
    ExpandableListView detail_price_item;

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

        mContext = this;

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

        mData = new ArrayList<>();
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
        getImage();

        detail_product_tag = (RecyclerView) findViewById(R.id.detail_product_tag);
        mLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        detail_product_tag.setLayoutManager(mLayoutManager);
        detail_product_tag.addItemDecoration(decoration);

        dataApi.selectName(p_name).enqueue(new Callback<List<Perfume>>() {
            @Override
            public void onResponse(Call<List<Perfume>> call, Response<List<Perfume>> response) {
                r_perfumes = response.body();
                detail_shop_name.setText(r_perfumes.get(0).getBrand());         // 브랜드
                detail_product_name.setText(p_name);

                detail_note_1.setImageDrawable(flavor.concentrations.get(r_perfumes.get(0).getConcentration()));
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
                }


                // 사이즈 받아온걸로 세팅
                sizes = new ArrayList<>();
                for(int i = 0; i < r_perfumes.size(); i++){
                    sizes.add(String.valueOf(r_perfumes.get(i).getSize()));
                    Log.e("값", String.valueOf(r_perfumes.get(i).getSize()));
                }

                adapter = new PerfumeSizeAdapter(getApplicationContext(), sizes);
                detail_size_item.setAdapter(adapter);
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
                    if (position >= 0) {
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
                Toast.makeText(getApplicationContext(), "클릭됨", Toast.LENGTH_SHORT).show();
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

        // 뒤로가기
        btn_back_detail_page = (ImageButton) findViewById(R.id.btn_back_detail_page);
        btn_back_detail_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public void getURL(int perfumeID){
        dataApi.getPerfumeURL(perfumeID).enqueue(new Callback<Price>() {
            @Override
            public void onResponse(Call<Price> call, Response<Price> response) {
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
        Product product = new Product(shop_name, price);
        mData.add(product);

        expandable_adpater = new ExpandablePriceAdapter(getApplicationContext(), mData);
        detail_price_item.setAdapter(expandable_adpater);
        detail_price_item.setGroupIndicator(null);

        Log.e("뿅", "들어옴");
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
                adapter2 = new PerfumeSizeAdapter(getApplicationContext(), hashtag);
                detail_product_tag.setAdapter(adapter2);
            }
        }, 1000); // 0.5초후
    }

    public void getImage(){

        // Amazon Cognito 인증 공급자를 초기화합니다
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "us-east-2:7241c5b2-3cf6-4a26-99d2-d08b31b32f8b", // 자격 증명 풀 ID
                Regions.US_EAST_2 // 리전
        );

        TransferNetworkLossHandler.getInstance(getApplicationContext());
        AmazonS3Client amazonS3Client = new AmazonS3Client(credentialsProvider, Region.getRegion(Regions.AP_NORTHEAST_2));
        TransferUtility transferUtility = TransferUtility.builder()
                .context(getApplicationContext())
                .defaultBucket("papang-bucket")
                .s3Client(amazonS3Client)
                .build();

        String key = "resources/perfume_de/" + p_name + ".png";
        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + "/papang_images").toString();
        File file = new File(path);
        if(!file.exists())
            file.mkdirs();

        final File download_file = new File(file.getPath() + "/" + p_name + ".png");
        Log.e("이름", file.getPath());

        TransferObserver downloadObserver = transferUtility.download(key, download_file);
        // 다운로드 과정을 알 수 있도록 Listener를 추가할 수 있다.
        downloadObserver.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    // Handle a completed upload.
                    Bitmap bitmap = BitmapFactory.decodeFile(path + "/" + p_name + ".png");
                    if(bitmap != null)
                        Glide.with(getApplicationContext())
                                .load(bitmap)
                                .fitCenter()
                                .into(product_image);
                    download_file.delete();
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

            }

            @Override
            public void onError(int id, Exception ex) {
                Log.e("실패", "Unable to download the file.", ex);
            }
        });
    }
}