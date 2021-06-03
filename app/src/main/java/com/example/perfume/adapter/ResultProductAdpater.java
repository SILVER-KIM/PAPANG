package com.example.perfume.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
import com.example.perfume.R;
import com.example.perfume.data.Perfume;

public class ResultProductAdpater extends RecyclerView.Adapter<ResultProductAdpater.ViewHolder> {

    Context context;
    ArrayList<Integer> product_drawables;          // 상품 이미지
    ArrayList<String> company_name;                 // 회사 이름
    ArrayList<String> product_name;                 // 상품 이름
    List<Perfume> perfumes;

    String path;
    File file;

    public ResultProductAdpater(Context context, List<Perfume> perfumes) {
        this.context = context;
        this.perfumes = perfumes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_result_product, parent, false);
        ResultProductAdpater.ViewHolder holder = new ResultProductAdpater.ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //holder.product_image.setImageResource(perfumes.get(position).get);
        holder.getImage(perfumes.get(position).getName());
        holder.product_name.setText(perfumes.get(position).getName());
        holder.company_name.setText(perfumes.get(position).getBrand());
        holder.capacity.setText(String.valueOf(perfumes.get(position).getSize()));
        holder.wish_ok.setImageDrawable(context.getResources().getDrawable(R.mipmap.icon_unfull_heart));
    }

    @Override
    public int getItemCount() {
        return perfumes.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView product_image;
        TextView company_name;
        TextView product_name;
        ImageView wish_ok;
        TextView capacity;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            product_image = (ImageView) itemView.findViewById(R.id.product_image);
            company_name = (TextView) itemView.findViewById(R.id.company_name);
            product_name = (TextView) itemView.findViewById(R.id.product_name);
            wish_ok = (ImageView) itemView.findViewById(R.id.unWish_btn);
            capacity = (TextView) itemView.findViewById(R.id.capacity);

            wish_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    wish_ok.setImageResource(R.mipmap.icon_unfull_heart);
                }
            });

        }

        public void getImage(final String p_name){

            // Amazon Cognito 인증 공급자를 초기화합니다
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    context,
                    "us-east-2:7241c5b2-3cf6-4a26-99d2-d08b31b32f8b", // 자격 증명 풀 ID
                    Regions.US_EAST_2 // 리전
            );

            TransferNetworkLossHandler.getInstance(context);
            AmazonS3Client amazonS3Client = new AmazonS3Client(credentialsProvider, Region.getRegion(Regions.AP_NORTHEAST_2));
            TransferUtility transferUtility = TransferUtility.builder()
                    .context(context)
                    .defaultBucket("papang-bucket")
                    .s3Client(amazonS3Client)
                    .build();

            String key = "resources/perfume_de/" + p_name + ".png";
            path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + "/papang_images").toString();
            file = new File(path);
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
                            Glide.with(context)
                                    .load(bitmap)
                                    .fitCenter()
                                    .into(product_image);
                    }
                    file.delete();
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

}
