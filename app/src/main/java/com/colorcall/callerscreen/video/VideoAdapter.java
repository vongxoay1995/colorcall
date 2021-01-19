package com.colorcall.callerscreen.video;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.database.Background;
import com.colorcall.callerscreen.utils.HawkHelper;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    public ArrayList<Background> listBg;

    public VideoAdapter(Context context, ArrayList<Background> data) {
        this.context = context;
        this.listBg = new ArrayList<>();
        distributeData(data);
    }

    private void distributeData(ArrayList<Background> data) {
       for (int i=0;i<data.size();i++){
           if (data.get(i).getType()==1){
               data.remove(i);
           }else {
               listBg.add(data.get(i));
           }
       }
    }

    private void resizeItem(Context context, RelativeLayout layout_item) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) layout_item.getLayoutParams();
        layoutParams.width = width / 2;
        layoutParams.height = (2 * width) / 3;
        layout_item.setLayoutParams(layoutParams);
    }

    public void setNewListBg() {
        this.listBg = new ArrayList<>();
        distributeData(HawkHelper.getListBackground());
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.img_item_thumb_theme)
        ImageView imgThumb;
        @BindView(R.id.layout_item)
        RelativeLayout layout_item;
        @BindView(R.id.layoutSelected)
        RelativeLayout layoutSelected;
        private Background backgroundSelected;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void onBind(int i) {
            backgroundSelected = HawkHelper.getBackgroundSelect();
            Background background = listBg.get(i);
            Log.e("TAN", "onBind: "+background.getPathThumb()+"--"+backgroundSelected.getPathThumb());
            if (background.getPathThumb().equals(backgroundSelected.getPathThumb()) && HawkHelper.isEnableColorCall()) {
                layoutSelected.setVisibility(View.VISIBLE);
                if(listener!=null){
                    listener.onItemThemeSelected(background,getAdapterPosition());
                }
            } else {
                layoutSelected.setVisibility(View.GONE);
            }
            resizeItem(context, layout_item);
            String pathFile;
            if (!background.getPathThumb().equals("")) {
                if (background.getPathItem().contains("default")) {
                    pathFile = "file:///android_asset/" + background.getPathThumb();
                } else {
                    pathFile =  background.getPathThumb();
                }
                Glide.with(context.getApplicationContext())
                        .load(pathFile)
                        .thumbnail(0.001f)
                        .apply(RequestOptions.placeholderOf(R.drawable.bg_gradient_green).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).skipMemoryCache(true))
                        .into(imgThumb);
            }
            listener();
        }

        private void listener() {
            this.imgThumb.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(listBg, getAdapterPosition(), listBg.get(getAdapterPosition()).getDelete());
                }
            });
        }
    }

    Listener listener;

    public interface Listener {
        void onItemClick(ArrayList<Background> backgrounds, int position, boolean delete);
        void onItemThemeSelected(Background background,int position);
    }

    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_thumbs_category_detail, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((ViewHolder) holder).onBind(position);
    }
    @Override
    public int getItemCount() {
        return this.listBg.size();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }
}
