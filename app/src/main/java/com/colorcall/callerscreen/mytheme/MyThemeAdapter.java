package com.colorcall.callerscreen.mytheme;

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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.database.Background;
import com.colorcall.callerscreen.databinding.AddNewBinding;
import com.colorcall.callerscreen.databinding.ItemThemeBinding;
import com.colorcall.callerscreen.utils.HawkHelper;

import java.util.ArrayList;


public class MyThemeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    public ArrayList<Background> listBg;

    public MyThemeAdapter(Context context,ArrayList<Background> backgrounds) {
        this.context = context;
        setNewListBg(backgrounds);
    }

    public void setNewListBg(ArrayList<Background> backgrounds) {
        this.listBg = new ArrayList<>();
        listBg.add(new Background(0, "", "", false));
        this.listBg.addAll(backgrounds);
     /*   databaseViewModel.getAllBackgrounds().observe((LifecycleOwner) context, backgrounds -> {
            Log.e("TAN", "setNewListBg: "+backgrounds.size());
            if (backgrounds != null) {
                this.listBg.addAll(backgrounds);
            }
            notifyDataSetChanged(); // Cập nhật giao diện khi có dữ liệu mới
        });*/
       // listBg.addAll(DataManager.query().getBackgroundDao().queryBuilder().list());
    }

    private void resizeItem(Context context, RelativeLayout layout_item) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) layout_item.getLayoutParams();
        layoutParams.width = (int) ((float) width / 2.1f);
        layoutParams.height = (5 * width) / 6;
        layout_item.setLayoutParams(layoutParams);
    }

    private void resizeItemAdd(Context context, RelativeLayout layout_item) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        GridLayoutManager.LayoutParams layoutParams = (GridLayoutManager.LayoutParams) layout_item.getLayoutParams();
        layoutParams.width = (int) (width / 2.1);
        layoutParams.height = (5 * width) / 6;
        layout_item.setLayoutParams(layoutParams);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private Background backgroundSelected;
        private int position;
        private int posRandom;
        private final ItemThemeBinding binding;

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
                pathFile = background.getPathThumb();
                Log.e("TAN", "onBind: "+pathFile);
                Glide.with(context.getApplicationContext())
                        .load(pathFile)
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .thumbnail(0.1f)
                        .into(binding.imgItemThumbTheme);
            }
            if (background.getPathThumb().equals(backgroundSelected.getPathThumb()) && HawkHelper.isEnableColorCall()) {
                binding.layoutSelected.setVisibility(View.VISIBLE);
                binding.layoutBorderItemSelect.setVisibility(View.VISIBLE);
                if (!checkIsImage(background.getPathItem())) {
                    binding.vdoBackgroundCall.setVisibility(View.VISIBLE);
                    binding.imgItemThumbTheme.setVisibility(View.GONE);
                    processVideo(background);
                }else {
                    binding.imgItemThumbTheme.setVisibility(View.VISIBLE);
                }

                startAnimation();
                if (listener != null) {
                    listener.onItemThemeSelected(position);
                }
            } else {
                if (!checkIsImage(background.getPathItem())) {
                    binding.vdoBackgroundCall.stopPlayback();
                }
                binding.vdoBackgroundCall.setVisibility(View.GONE);
                binding.imgItemThumbTheme.setVisibility(View.VISIBLE);
                binding.layoutSelected.setVisibility(View.GONE);
                binding.layoutBorderItemSelect.setVisibility(View.GONE);
                binding.btnAccept.clearAnimation();
            }
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

        public boolean checkIsImage(String path) {
            if (path.contains(".jpg")
                    || path.contains(".webp")
                    || path.contains(".jpeg")
                    || path.contains(".gif")
                    || path.contains(".tiff")
                    || path.contains(".png")) {
                return true;
            }
            return false;
        }

        @SuppressLint("ClickableViewAccessibility")
        private void listener() {
            this.binding.imgItemThumbTheme.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(listBg, position, listBg.get(position).getDelete(), posRandom);
                }
            });

            binding.vdoBackgroundCall.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(listBg, position, listBg.get(position).getDelete(), posRandom);
                }
            });
        }

        public void startAnimation() {
            Animation anim8 = AnimationUtils.loadAnimation(context, R.anim.anm_accept_call);
            binding.btnAccept.startAnimation(anim8);
        }

        private void processVideo(Background background) {
            String sPath;
            String sPathThumb;
            String uriPath = "android.resource://" + context.getPackageName() + background.getPathItem();
            if (!background.getPathThumb().equals("")) {
                sPathThumb = background.getPathThumb();
                Glide.with(context.getApplicationContext())
                        .load(sPathThumb)
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .thumbnail(0.1f)
                        .into(binding.imgItemThumbTheme);
            }
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

        private void playVideo() {
            binding.vdoBackgroundCall.setOnPreparedListener(mediaPlayer -> {
                mediaPlayer.setLooping(true);
                mediaPlayer.setVolume(0.0f, 0.0f);
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
            binding.vdoBackgroundCall.start();
        }
    }

    Listener listener;

    public class AddHolder extends RecyclerView.ViewHolder {
        private final AddNewBinding binding;
        public AddHolder(@NonNull AddNewBinding mbinding) {
            super(mbinding.getRoot());
            binding = mbinding;
            resizeItemAdd(context, binding.layoutAdd);
            this.binding.layoutAdd.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAdd();
                }
            });
        }
    }


    public interface Listener {
        void onAdd();

        void onItemThemeSelected(int position);

        void onItemClick(ArrayList<Background> backgrounds, int position, boolean delete, int posRandom);
    }

    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        ItemThemeBinding binding = ItemThemeBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false);
        AddNewBinding binding2 = AddNewBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false);
        return switch (i) {
            case 0 -> new AddHolder(binding2);
            case 1 -> new ViewHolder(binding);
            default -> null;
        };
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case 1:
                ((ViewHolder) holder).onBind(position);
                return;
            default:
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position != 0) {
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

    public void reload() {
        notifyItemRangeChanged(0, getItemCount(), 4);
    }
}
