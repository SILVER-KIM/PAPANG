package com.papang.perfume.main.my;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.papang.perfume.AllResultProductActivity;
import com.papang.perfume.AppSatisfactionActivity;
import com.papang.perfume.EditMyinfoActivity;
import com.papang.perfume.NoResultActivity;
import com.papang.perfume.ParticipatedEventActivity;
import com.papang.perfume.R;
import com.papang.perfume.RecommendationActivity;
import com.papang.perfume.data.DataApi;
import com.papang.perfume.data.DataService;
import com.papang.perfume.data.UserRecommendation;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyPageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyPageFragment extends Fragment {
    DataService dataService;
    DataApi dataApi;

    ConstraintLayout touch_event_zone;
    ConstraintLayout touch_add_zone;
    ConstraintLayout touch_show_result_zone;
    ImageButton btn_findperfume;
    ImageButton btn_eidtmyinfo;
    TextView user_name_text;

    TextView result_concentration;
    TextView result_size;
    TextView result_flavors;
    TextView result_style;
    ImageView style_img;

    ImageView result_never;

    Context context;

    private String email;
    private String nickname;

    ArrayList<String> perfumeInfos;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MyPageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MyPageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MyPageFragment newInstance(String param1, String param2) {
        MyPageFragment fragment = new MyPageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_page, container, false);
        context = view.getContext();

        dataService = new DataService();
        dataApi =  dataService.getRetrofitClient().create(DataApi.class);

        user_name_text = (TextView) view.findViewById(R.id.user_name_text);
        touch_event_zone = (ConstraintLayout) view.findViewById(R.id.touch_event_zone);
        touch_add_zone = (ConstraintLayout) view.findViewById(R.id.touch_add_zone);
        touch_show_result_zone = (ConstraintLayout) view.findViewById(R.id.touch_show_result_zone);
        btn_findperfume = (ImageButton) view.findViewById(R.id.btn_findperfume);
        btn_eidtmyinfo = (ImageButton) view.findViewById(R.id.btn_editmyinfo);

        result_size = (TextView)view.findViewById(R.id.result_size);
        result_flavors = (TextView)view.findViewById(R.id.result_flavors);
        result_style = (TextView)view.findViewById(R.id.result_style);
        style_img = (ImageView)view.findViewById(R.id.style_img);
        result_never = (ImageView)view.findViewById(R.id.result_never);

        checkLogin();

        touch_event_zone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ParticipatedEventActivity.class);
                startActivity(intent);
            }
        });

        touch_add_zone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AppSatisfactionActivity.class);
                startActivity(intent);
            }
        });

        btn_findperfume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), RecommendationActivity.class);
                startActivity(intent);
            }
        });

        btn_eidtmyinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), EditMyinfoActivity.class);
                startActivity(intent);
            }
        });

        touch_show_result_zone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(perfumeInfos.get(3).equals("16"))
                    getPerfumeExclude(perfumeInfos);
                else
                    getPerfume(perfumeInfos);
            }
        });
        return view;
    }

    public void getPerfumeExclude(final ArrayList<String> perfumeInfos){
        ArrayList<Integer> sizes = new ArrayList<>();
        sizes = changeSize(perfumeInfos.get(4));

        dataApi.getExcludeRecommendationResult(Integer.parseInt(perfumeInfos.get(0)), Integer.parseInt(perfumeInfos.get(1)),
                Integer.parseInt(perfumeInfos.get(2)), sizes.get(0), sizes.get(1)).enqueue(new Callback<ArrayList<String>>() {
            @Override
            public void onResponse(Call<ArrayList<String>> call, Response<ArrayList<String>> response) {
                ArrayList<String> perfumes = response.body();
                if(perfumes.size() > 0) {
                    Intent result = new Intent(context, AllResultProductActivity.class);
                    result.putExtra("결과", perfumes);
                    result.putExtra("정보", perfumeInfos);
                    if(!email.equals(""))
                        setUserRecommend();
                    startActivity(result);
                }
                else{
                    Intent result = new Intent(context, NoResultActivity.class);
                    startActivity(result);
                }
            }

            @Override
            public void onFailure(Call<ArrayList<String>> call, Throwable t) {
                Log.e("연결", t.getMessage());
            }
        });
    }

    private void getPerfume(final ArrayList<String> perfumeInfos) {
        ArrayList<Integer> sizes = new ArrayList<>();
        sizes = changeSize(perfumeInfos.get(4));

        dataApi.getRecommendationResult(Integer.parseInt(perfumeInfos.get(0)), Integer.parseInt(perfumeInfos.get(1)),
                Integer.parseInt(perfumeInfos.get(2)), Integer.parseInt(perfumeInfos.get(3)), sizes.get(0), sizes.get(1)).enqueue(new Callback<ArrayList<String>>() {
            @Override
            public void onResponse(Call<ArrayList<String>> call, Response<ArrayList<String>> response) {
                ArrayList<String> perfumes = response.body();
                if(perfumes.size() > 0) {
                    Intent result = new Intent(getContext(), AllResultProductActivity.class);
                    result.putExtra("결과", perfumes);
                    result.putExtra("정보", perfumeInfos);
                    if(!email.equals(""))
                        setUserRecommend();
                    startActivity(result);
                }
                else{
                    Intent result = new Intent(getContext(), NoResultActivity.class);
                    startActivity(result);
                }
            }

            @Override
            public void onFailure(Call<ArrayList<String>> call, Throwable t) {
                Log.e("연결", t.getMessage());
            }
        });



    }

    private void checkLogin(){
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Info", MODE_PRIVATE);    // test 이름의 기본모드 설정, 만약 test key값이 있다면 해당 값을 불러옴.
        email = sharedPreferences.getString("Email","");
        nickname = sharedPreferences.getString("Nickname","");

        user_name_text.setText(nickname + "님!");

        setUserRecommend();
    }

    private void setUserRecommend(){
        dataApi.getUserRecommend(email).enqueue(new Callback<UserRecommendation>() {
            @Override
            public void onResponse(Call<UserRecommendation> call, Response<UserRecommendation> response) {
                UserRecommendation ur;
                ur = response.body();
                //style_img.setImageDrawable(getResources().getDrawable(R.mipmap.style_2_img));
                touch_show_result_zone.setVisibility(View.VISIBLE);
                result_never.setVisibility(View.INVISIBLE);
                btn_findperfume.setVisibility(View.INVISIBLE);

                style_img.setVisibility(View.VISIBLE);
                result_size.setVisibility(View.VISIBLE);
                result_flavors.setVisibility(View.VISIBLE);
                result_style.setVisibility(View.VISIBLE);

                changeResultText(ur);

                // 향수 정보 가져오기
                perfumeInfos = new ArrayList<>();
                perfumeInfos.add(Integer.toString(ur.getStyle()));
                perfumeInfos.add(Integer.toString(ur.getFlavor1()));
                perfumeInfos.add(Integer.toString(ur.getFlavor2()));
                perfumeInfos.add(Integer.toString(ur.getFlavor3()));
                perfumeInfos.add(ur.getSize());
            }

            @Override
            public void onFailure(Call<UserRecommendation> call, Throwable t) {

            }
        });
    }

    private void changeResultText(UserRecommendation ur) { //가격은 String q_7 추가
        String flavor_1 = "";
        String flavor_2 = "";
        String flavor_3 = "";

        switch (ur.getStyle()) {
            case 1:
                result_style.setText("포근한, 차분한, 따뜻한, 순수한");
                break;
            case 2:
                result_style.setText("발랄한, 귀여운, 사랑스러운");
                break;
            case 3:
                result_style.setText("관능적인, 화려한");
                break;
            case 4:
                result_style.setText("환상적인, 황홀한, 몽환적인, 신비로운");
                break;
            case 5:
                result_style.setText("젠틀한, 클래식한, 깊이있는");
                break;
            case 6:
                result_style.setText("세련된, 우아한, 도시적인, 모던한");
                break;
            case 7:
                result_style.setText("산뜻한, 시원한, 활기찬");
                break;
            case 8:
                result_style.setText("강렬한, 파워풀한, 존재감있는, 대담한");
                break;
        }

        switch (ur.getFlavor1()) {
            case 1:
                flavor_1 = "Aldehyde";
                style_img.setImageDrawable(context.getResources().getDrawable(R.mipmap.flavor_aldehyde_img));
                break;
            case 2:
                flavor_1 = "Animalic";
                style_img.setImageDrawable(context.getResources().getDrawable(R.mipmap.flavor_animal_img));
                break;
            case 3:
                flavor_1 = "Aromatic";
                style_img.setImageDrawable(context.getResources().getDrawable(R.mipmap.flavor_aromatic_img));
                break;
            case 4:
                flavor_1 = "Balsam";
                style_img.setImageDrawable(context.getResources().getDrawable(R.mipmap.flavor_balsam_img));
                break;
            case 5:
                flavor_1 = "Chypre";
                style_img.setImageDrawable(context.getResources().getDrawable(R.mipmap.flavor_chypre_img));
                break;
            case 6:
                flavor_1 = "Citrus";
                style_img.setImageDrawable(context.getResources().getDrawable(R.mipmap.flavor_citrus_img));
                break;
            case 7:
                flavor_1 = "Green";
                style_img.setImageDrawable(context.getResources().getDrawable(R.mipmap.flavor_green_img));
                break;
            case 8:
                flavor_1 = "Floral";
                style_img.setImageDrawable(context.getResources().getDrawable(R.mipmap.flavor_floral_img));
                break;
            case 9:
                flavor_1 = "Fruity";
                style_img.setImageDrawable(context.getResources().getDrawable(R.mipmap.flavor_fruity_img));
                break;
            case 10:
                flavor_1 = "Spicy";
                style_img.setImageDrawable(context.getResources().getDrawable(R.mipmap.flavor_spicy_img));
                break;
            case 11:
                flavor_1 = "Woody";
                style_img.setImageDrawable(context.getResources().getDrawable(R.mipmap.flavor_woody_img));
                break;
            case 12:
                flavor_1 = "Aquatic";
                style_img.setImageDrawable(context.getResources().getDrawable(R.mipmap.flavor_aquatic_img));
                break;
            case 13:
                flavor_1 = "Nutty";
                style_img.setImageDrawable(context.getResources().getDrawable(R.mipmap.flavor_nutty_img));
                break;
            case 14:
                flavor_1 = "Leather";
                style_img.setImageDrawable(context.getResources().getDrawable(R.mipmap.flavor_leather_img));
                break;
            case 15:
                flavor_1 = "None";
                break;
        }

        switch (ur.getFlavor2()) {
            case 1:
                flavor_2 = "Aldehyde";
                break;
            case 2:
                flavor_2 = "Animalic";
                break;
            case 3:
                flavor_2 = "Aromatic";
                break;
            case 4:
                flavor_2 = "Balsam";
                break;
            case 5:
                flavor_2 = "Chypre";
                break;
            case 6:
                flavor_2 = "Citrus";
                break;
            case 7:
                flavor_2 = "Green";
                break;
            case 8:
                flavor_2 = "Floral";
                break;
            case 9:
                flavor_2 = "Fruity";
                break;
            case 10:
                flavor_2 = "Spicy";
                break;
            case 11:
                flavor_2 = "Woody";
                break;
            case 12:
                flavor_2 = "Aquatic";
                break;
            case 13:
                flavor_2 = "Nutty";
                break;
            case 14:
                flavor_2 = "Leather";
                break;
            case 15:
                flavor_2 = "None";
                break;
        }

        switch (ur.getFlavor3()) {
            case 1:
                flavor_3 = "Aldehyde";
                break;
            case 2:
                flavor_3 = "Animalic";
                break;
            case 3:
                flavor_3 = "Aromatic";
                break;
            case 4:
                flavor_3 = "Balsam";
                break;
            case 5:
                flavor_3 = "Chypre";
                break;
            case 6:
                flavor_3 = "Citrus";
                break;
            case 7:
                flavor_3 = "Green";
                break;
            case 8:
                flavor_3 = "Floral";
                break;
            case 9:
                flavor_3 = "Fruity";
                break;
            case 10:
                flavor_3 = "Spicy";
                break;
            case 11:
                flavor_3 = "Woody";
                break;
            case 12:
                flavor_3 = "Aquatic";
                break;
            case 13:
                flavor_3 = "Nutty";
                break;
            case 14:
                flavor_3 = "Leather";
                break;
            case 15:
                flavor_3 = "None";
                break;
        }

        switch (ur.getSize()) {
            case "size1":
                result_size.setText("0ml ~ 50ml");
                break;
            case "size2":
                result_size.setText("0ml ~ 100ml");
                break;
            case "size3":
                result_size.setText("100ml 이상");
                break;
        }

        result_flavors.setText(flavor_1 + ", " + flavor_2 + ", " + flavor_3);

    }


    // 향수 결과 가져오기 위함
    public ArrayList<Integer> changeSize(String size){
        ArrayList<Integer> sizes = new ArrayList<>();

        switch (size) {
            case "size1":
                sizes.add(0);
                sizes.add(50);
                break;
            case "size2":
                sizes.add(0);
                sizes.add(100);
                break;
            case "size3":
                sizes.add(0);
                sizes.add(500);
                break;
        }
        return sizes;
    }
}