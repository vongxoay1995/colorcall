package com.colorcall.callerscreen.main;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.model.Background;
import com.colorcall.callerscreen.utils.HawkHelper;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainListCategoryThumbAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public ArrayList<Background> listBg;
    public Context context;
    private boolean isYourColor;
    Listener listener;

    public class AddHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.layoutAdd)
        RelativeLayout layoutAdd;

        public AddHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void onBind() {
            resizeItemAdd(context,layoutAdd);
            this.layoutAdd.setOnClickListener(v->{
                if(listener!=null){
                    listener.onAdd();
                }
            });
        }
    }


    public interface Listener {
        void onAdd();
        void onItemClick(ArrayList<Background> backgrounds, int position, boolean delete);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imgThumb)
        ImageView imgThumb;
        @BindView(R.id.imgVideo)
        ImageView imgVideo;
        @BindView(R.id.layoutSelected)
        RelativeLayout layoutSelected;
        @BindView(R.id.layout_item)
        RelativeLayout layout_item;
        private Background backgroundSelected;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            backgroundSelected = HawkHelper.getBackgroundSelect();
            ButterKnife.bind(this, itemView);
        }

        public void onBind(int i) {
            resizeItem(context,layout_item);
            Background background =  listBg.get(i);
            if(background.getPathThumb().equals(backgroundSelected.getPathThumb())&&HawkHelper.isEnableColorCall()){
                layoutSelected.setVisibility(View.VISIBLE);
            }else {
                layoutSelected.setVisibility(View.GONE);
            }
            if(background.getType()== Constant.TYPE_VIDEO){
                imgVideo.setVisibility(View.VISIBLE);
            }else {
                imgVideo.setVisibility(View.GONE);
            }
            if (!background.getPathThumb().equals("")) {
                if(background.getDelete()){
                    Glide.with(context.getApplicationContext())
                            .load(background.getPathThumb())
                            .thumbnail(0.001f)
                            .apply(RequestOptions.placeholderOf(R.drawable.bg_gradient_green).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).skipMemoryCache(true))
                            .into(imgThumb);
                }else {
                    Glide.with(context.getApplicationContext())
                            .load("file:///android_asset/"+background.getPathThumb())
                            .thumbnail(0.001f)
                            .apply(RequestOptions.placeholderOf(R.drawable.bg_gradient_green).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).skipMemoryCache(true))
                            .into(imgThumb);
                }
            }
            listener();
        }

        private void listener() {
            this.imgThumb.setOnClickListener(v -> {
                 if(listener!=null){
                     listener.onItemClick(listBg,getAdapterPosition(),listBg.get(getAdapterPosition()).getDelete());
                 }
            });
        }
    }

    public MainListCategoryThumbAdapter(Context context2, ArrayList<Background> arrThumb2, boolean isYourColor2) {
        this.context = context2;
        this.listBg = arrThumb2;
        this.isYourColor = isYourColor2;
    }

    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        switch (i) {
            case 0:
                return new AddHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.add_new, viewGroup, false));
            case 1:
                return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list_category_thumb, viewGroup, false));
            default:
                return null;
        }
    }

    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        switch (getItemViewType(i)) {
            case 0:
                ((AddHolder) viewHolder).onBind();
                return;
            case 1:
                ((ViewHolder) viewHolder).onBind(i);
                return;
            default:
                return;
        }
    }

    public int getItemViewType(int position) {
        if (position != 0 || !isYourColor) {
            return 1;
        }
        return 0;
    }

    public int getItemCount() {
        return Math.min(listBg.size(), 3);
    }

    public void setListener(Listener listener2) {
        this.listener = listener2;
    }

    private void resizeItemAdd(Context context, RelativeLayout layout_item) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        GridLayoutManager.LayoutParams layoutParams = (GridLayoutManager.LayoutParams) layout_item.getLayoutParams();
        layoutParams.width = (int) (width/3.2);
        layoutParams.height = width/2;
        layout_item.setLayoutParams(layoutParams);
    }
    private void resizeItem(Context context, RelativeLayout layout_item) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) layout_item.getLayoutParams();
        layoutParams.width = (int) (width/3.2);
        layoutParams.height = width/2;
        layout_item.setLayoutParams(layoutParams);
    }
}
