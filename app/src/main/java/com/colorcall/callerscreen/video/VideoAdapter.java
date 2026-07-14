package com.colorcall.callerscreen.video;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.database.Background;
import com.colorcall.callerscreen.databinding.ItemThemeBinding;
import com.colorcall.callerscreen.utils.AppUtils;
import com.colorcall.callerscreen.utils.HawkHelper;

import java.util.ArrayList;

public class VideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    public ArrayList<Background> listBg;

    public VideoAdapter(Context context, ArrayList<Background> data) {
        this.context = context;
        this.listBg = new ArrayList<>();
        distributeData(data);
    }

    private void distributeData(ArrayList<Background> data) {
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).getType() == 1) {
                data.remove(i);
            } else {
                listBg.add(data.get(i));
            }
        }
    }

    private void resizeItem(Context context, RelativeLayout layout_item) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) layout_item.getLayoutParams();
        layoutParams.width = (int) ((float) width / 2.15f);
        layoutParams.height = (5 * width) / 6;
        layout_item.setLayoutParams(layoutParams);
    }

    public void setNewListBg() {
        this.listBg = new ArrayList<>();
        distributeData(HawkHelper.getListBackground());
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemThemeBinding binding;
        private Background backgroundSelected;
        private int position;
        private int posRandom;

        public ViewHolder(@NonNull ItemThemeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            resizeItem(context, binding.layoutItem);
            listener();
        }

        public void onBind(int i) {
            position = i;
            initInfor();
            backgroundSelected = HawkHelper.getBackgroundSelect();
            Background background = listBg.get(i);
            String pathFile;
            if (!background.getPathThumb().equals("")) {
                if (background.getPathItem().contains("default")) {
                    pathFile = "file:///android_asset/" + background.getPathThumb();
                } else {
                    pathFile = AppUtils.upgradeToHttps(background.getPathThumb());
                }
                Glide.with(context.getApplicationContext())
                        .load(pathFile)
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .thumbnail(0.1f)
                        .into(binding.imgItemThumbTheme);
            }
            if (isPro(background)){
                binding.btnPro.setVisibility(View.VISIBLE);
            }
                Log.e("TAN", "onBind: "+background.getPathThumb()+"##"+backgroundSelected.getPathThumb());
            if (background.getPathThumb().equals(backgroundSelected.getPathThumb()) && HawkHelper.isEnableColorCall()) {
                binding.layoutSelected.setVisibility(View.VISIBLE);
                binding.layoutBorderItemSelect.setVisibility(View.VISIBLE);
                binding.imgItemThumbTheme.setVisibility(View.GONE);
                binding.vdoBackgroundCall.setVisibility(View.VISIBLE);
                processVideo(background);
                startAnimation();
                if (listener != null) {
                    listener.onItemThemeSelected(position);
                }
            } else {
                binding.vdoBackgroundCall.stopPlayback();
                binding.vdoBackgroundCall.setVisibility(View.GONE);
                binding.imgItemThumbTheme.setVisibility(View.VISIBLE);
                binding.layoutSelected.setVisibility(View.GONE);
                binding.layoutBorderItemSelect.setVisibility(View.GONE);
                binding.btnAccept.clearAnimation();
            }
        }

        public void startAnimation() {
            Animation anim8 = AnimationUtils.loadAnimation(context, R.anim.anm_accept_call);
            binding.btnAccept.startAnimation(anim8);
        }

        private void initInfor() {
            posRandom = position % 10;
            String pathAvatar = Constant.avatarRandom[posRandom];
            String name = Constant.nameRandom[posRandom];
            String phone = Constant.phoneRandom[posRandom];
            Glide.with(context.getApplicationContext())
                    .load("file:///android_asset/avatar/" + pathAvatar)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .thumbnail(0.1f)
                    .into(binding.imgAvatar);
            binding.txtName.setText(name);
            binding.txtPhone.setText(phone);
        }

        @SuppressLint("ClickableViewAccessibility")
        private void listener() {
            this.binding.imgItemThumbTheme.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(listBg, position, isPro(listBg.get(position)), listBg.get(position).getDelete(), posRandom);
                }
            });
            this.binding.vdoBackgroundCall.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(listBg, position, isPro(listBg.get(position)), listBg.get(position).getDelete(), posRandom);
                }
            });
        }

        private void processVideo(Background background) {
            String sPath;
            String uriPath = "android.resource://" + context.getPackageName() + background.getPathItem();
            if (background.getPathItem().contains("storage") || background.getPathItem().contains("/data/data") || background.getPathItem().contains("data/user/")) {
                sPath = background.getPathItem();
                if (!sPath.startsWith("http")) {
                    binding.vdoBackgroundCall.setVideoURI(Uri.parse(sPath));
                    playVideo();
                }
            } else {
                binding.vdoBackgroundCall.setVideoURI(Uri.parse(uriPath));
                playVideo();
            }
        }
        private boolean isPro(Background background){
            return !HawkHelper.isPayed() &&( background.getPathItem().contains("video6") ||
                    background.getPathItem().contains("video23") ||
                    background.getPathItem().contains("video48") ||
                    background.getPathItem().contains("video7"));
        }
        private void playVideo() {
            binding.vdoBackgroundCall.setOnPreparedListener(mediaPlayer -> {
                mediaPlayer.setLooping(true);
                binding.vdoBackgroundCall.start();
            });
            binding.vdoBackgroundCall.setOnErrorListener((mp, what, extra) -> {
                binding.vdoBackgroundCall.stopPlayback();
                binding.vdoBackgroundCall.setVisibility(View.GONE);
                binding.imgItemThumbTheme.setVisibility(View.VISIBLE);
                return false;
            });
            binding.vdoBackgroundCall.setOnInfoListener((mp, what, extra) -> {
                if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    new Handler().postDelayed(() -> {
                        binding.vdoBackgroundCall.setAlpha(1.0f);
                        binding.imgItemThumbTheme.setVisibility(View.INVISIBLE);
                    }, 100);
                    return true;
                }
                return false;
            });
        }
    }

    Listener listener;

    public interface Listener {
        void onItemClick(ArrayList<Background> backgrounds, int position,boolean isPro, boolean delete, int posRandom);

        void onItemThemeSelected(int position);
    }

    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        ItemThemeBinding binding = ItemThemeBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false);
        return new ViewHolder(binding);
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

    public void reload() {
        notifyItemRangeChanged(0, getItemCount(), 4);
    }

    public void reloadAll() {
        notifyItemRangeChanged(0, getItemCount(), 2);
    }
}

