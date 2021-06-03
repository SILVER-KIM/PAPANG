package com.example.perfume.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface DataApi {

    @GET("papang_perfume")
    Call<List<Perfume>> selectAll();

    @GET("perfume/recommendation/{name}")
    Call<List<Perfume>> selectName(@Path("name") String name);

    @GET("flavor_hashtag/{flavor}/{flavor2}")
    Call<List<Hashtag>> getFlavorHashtag(@Path("flavor") int flavor,
                                         @Path("flavor2") int flavor2);

    @GET("perfume_recommend/{concentration}/{size1}/{size2}/{style}/{main}/{first}/{second}")
    Call<ArrayList<Perfume>> getRecommendationResult(@Path("concentration") int concentration,
                                                     @Path("size1") int size1, @Path("size2") int size2,
                                                     @Path("style") int style, @Path("main") int main,
                                                     @Path("first") int first, @Path("second") int second);

    @GET("price_url/{perfumeID}")
    Call<Price> getPerfumeURL(@Path("perfumeID") int perfumeID);

    @GET("join/{email}")
    Call<User> getUser(@Path("email") String email);

    @GET("join/search_email/{phone}")
    Call<User> getEmail(@Path("phone") String phone);

    @POST("join/user")
    Call<User> joinUser(@Body Map<String, String> map);

    @POST("join/reset_pw/{email}")
    Call<User> resetPW(@Path("email") String email, @Body Map<String, String> map);

    @GET("wish/{email}")
    Call<List<Wish>> getWishList(@Path("email") String email);

    @POST("wish/add")
    Call<Wish> addWishList(@Body Map<String, String> map);
}
