package com.htn.colorcall.categorydetail;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.htn.colorcall.R;
import com.htn.colorcall.model.Background;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CategoryDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    public ArrayList<Background> listBg;
    private boolean isYourColor;

    public CategoryDetailAdapter(Context context, ArrayList<Background> arrThumb, boolean isYourColor) {
        this.context = context;
        this.isYourColor = isYourColor;
        this.listBg = arrThumb;
    }
    private void resizeItem(Context context, RelativeLayout layout_item) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) layout_item.getLayoutParams();
//        layoutParams.width = 5*(width/16);
//        layoutParams.height = 4*layoutParams.width/3;
        layoutParams.width = width/2;
        layoutParams.height = (2*width)/3;
        layout_item.setLayoutParams(layoutParams);
    }
    private void resizeItemAdd(Context context, RelativeLayout layout_item) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        GridLayoutManager.LayoutParams layoutParams = (GridLayoutManager.LayoutParams) layout_item.getLayoutParams();
        //layoutParams.width = 5*(width/16);
      //  layoutParams.height = 4*layoutParams.width/3;
        layoutParams.width = width/2;
        layoutParams.height = (2*width)/3;
        layout_item.setLayoutParams(layoutParams);
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.img_item_thumb_theme)
        ImageView imgThumb;
        @BindView(R.id.layout_item)
        RelativeLayout layout_item;
        @BindView(R.id.imgVideo)
        ImageView imgVideo;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void onBind(int i) {
            Background background = listBg.get(i);
            resizeItem(context,layout_item);
            if(background.getType()==0){
                imgVideo.setVisibility(View.VISIBLE);
            }else {
                imgVideo.setVisibility(View.GONE);
            }
            if (!background.getPathThumb().equals("")) {
                if(background.getDelete()){
                    Glide.with(context.getApplicationContext())
                            .load(background.getPathThumb())
                            .thumbnail(0.001f)
                            .apply(RequestOptions.placeholderOf(R.drawable.bg_gradient_green))
                            .into(imgThumb);
                }else {
                    Glide.with(context.getApplicationContext())
                            .load("file:///android_asset/"+background.getPathThumb())
                            .thumbnail(0.001f)
                            .apply(RequestOptions.placeholderOf(R.drawable.bg_gradient_green))
                            .into(imgThumb);
                }
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

    public class AddHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.layoutAdd)
        RelativeLayout layoutAdd;
        public AddHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void onBind() {
            resizeItemAdd(context,layoutAdd);
            this.layoutAdd.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAdd();
                }
            });
        }
    }


    public interface Listener {
        void onAdd();

        void onItemClick(ArrayList<Background> backgrounds, int position, boolean delete);
    }

    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        switch (i) {
            case 0:
                return new AddHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.add_new, viewGroup, false));
            case 1:
                return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_thumbs_category_detail, viewGroup, false));
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case 0:
                ((AddHolder) holder).onBind();
                return;
            case 1:
                ((ViewHolder) holder).onBind(position);
                return;
            default:
                return;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position != 0 || !this.isYourColor) {
            return 1;
        }
        return 0;
    }

    @Override
    public int getItemCount() {
        return this.listBg.size();
    }

    public void setListener(Listener listener2) {
        this.listener = listener2;
    }
}
