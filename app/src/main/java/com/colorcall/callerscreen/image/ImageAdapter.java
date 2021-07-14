package com.colorcall.callerscreen.image;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.database.Background;
import com.colorcall.callerscreen.utils.HawkHelper;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private Context context;
    public ArrayList<Background> listBg;

    public ImageAdapter(Context context, ArrayList<Background> data) {
        this.context = context;
        this.listBg = new ArrayList<>();
        distributeData(data);
    }

    private void distributeData(ArrayList<Background> data) {
        for (int i=0;i<data.size();i++){
            if (data.get(i).getType()==0){
                data.remove(i);
            }else {
                listBg.add(data.get(i));
            }
        }
    }
    public void setNewListBg() {
        this.listBg = new ArrayList<>();
        distributeData(HawkHelper.getListBackground());
    }
    private void resizeItem(Context context, RelativeLayout layout_item) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) layout_item.getLayoutParams();
        layoutParams.width = (int)((float)width / 2.1f);
        layoutParams.height = (5 * width) / 6;
        layout_item.setLayoutParams(layoutParams);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.img_item_thumb_theme)
        ImageView imgThumb;
        @BindView(R.id.layout_item)
        RelativeLayout layout_item;
        @BindView(R.id.imgAvatar)
        ImageView imgAvatar;
        @BindView(R.id.txtName)
        TextView txtName;
        @BindView(R.id.txtPhone)
        TextView txtPhone;
        @BindView(R.id.layoutSelected)
        ConstraintLayout layoutSelected;
        @BindView(R.id.layoutBorderItemSelect)
        RelativeLayout layoutBorderItemSelect;
        @BindView(R.id.btnAccept)
        ImageView btnAccept;
        private Background backgroundSelected;
        private int position;
        private int posRandom;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            resizeItem(context, layout_item);
            listener();
        }

        public void onBind(int i) {
            position = i;
            initInfor();
            backgroundSelected = HawkHelper.getBackgroundSelect();
            Background background = listBg.get(i);
            if (background.getPathThumb().equals(backgroundSelected.getPathThumb()) && HawkHelper.isEnableColorCall()) {
                layoutSelected.setVisibility(View.VISIBLE);
                layoutBorderItemSelect.setVisibility(View.VISIBLE);
                startAnimation();
                if(listener!=null){
                    listener.onItemThemeSelected(background,position);
                }
            } else {
                layoutSelected.setVisibility(View.GONE);
                layoutBorderItemSelect.setVisibility(View.GONE);
                btnAccept.clearAnimation();
            }

            String pathFile;
            if (!background.getPathThumb().equals("")) {
                if (background.getPathItem().contains("default")) {
                    pathFile = "file:///android_asset/" + background.getPathThumb();
                } else {
                    pathFile = background.getPathThumb();
                }
                Glide.with(context.getApplicationContext())
                        .load(pathFile)
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .thumbnail(0.1f)
                        .into(imgThumb);
            }
        }
        private void initInfor() {
            posRandom = position%10;
            String pathAvatar = Constant.avatarRandom[posRandom];
            String name = Constant.nameRandom[posRandom];
            String phone = Constant.phoneRandom[posRandom];
            Glide.with(context.getApplicationContext())
                    .load("file:///android_asset/avatar/" + pathAvatar)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .thumbnail(0.1f)
                    .into(imgAvatar);
            txtName.setText(name);
            txtPhone.setText(phone);
        }
        private void listener() {
            this.imgThumb.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(listBg, position, listBg.get(position).getDelete(),posRandom);
                }
            });
        }
        public void startAnimation() {
            Animation anim8 = AnimationUtils.loadAnimation(context, R.anim.anm_accept_call);
            btnAccept.startAnimation(anim8);
        }
    }

    Listener listener;

    public interface Listener {
        void onItemClick(ArrayList<Background> backgrounds, int position, boolean delete,int posRandom);
        void onItemThemeSelected(Background background,int position);
    }

    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_theme, viewGroup, false));
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
