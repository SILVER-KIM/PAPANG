package com.example.perfume.main.my;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.perfume.AppSatisfactionActivity;
import com.example.perfume.EditMyinfoActivity;
import com.example.perfume.ParticipatedEventActivity;
import com.example.perfume.R;
import com.example.perfume.RecommendationActivity;

import org.w3c.dom.Text;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyPageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyPageFragment extends Fragment {

    ConstraintLayout touch_event_zone;
    ConstraintLayout touch_add_zone;
    ImageButton btn_findperfume;
    ImageButton btn_eidtmyinfo;
    TextView user_name_text;

    Context context;

    private String email;
    private String nickname;

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

        user_name_text = (TextView) view.findViewById(R.id.user_name_text);
        touch_event_zone = (ConstraintLayout) view.findViewById(R.id.touch_event_zone);
        touch_add_zone = (ConstraintLayout) view.findViewById(R.id.touch_add_zone);
        btn_findperfume = (ImageButton) view.findViewById(R.id.btn_findperfume);
        btn_eidtmyinfo = (ImageButton) view.findViewById(R.id.btn_editmyinfo);

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

        return view;
    }

    private void checkLogin(){
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Info", MODE_PRIVATE);    // test 이름의 기본모드 설정, 만약 test key값이 있다면 해당 값을 불러옴.
        email = sharedPreferences.getString("Email","");
        nickname = sharedPreferences.getString("Nickname","");

        user_name_text.setText(nickname + "님!");
    }
}