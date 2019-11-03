package com.htn.colorcall.main;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.BaseRequestOptions;
import com.bumptech.glide.request.RequestOptions;
import com.htn.colorcall.R;
import com.htn.colorcall.model.Thumb;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainListCategoryThumbAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public ArrayList<Thumb> arrThumb;
    public Context context;
    private boolean isYourColor;
    Listener listener;

    public class AddHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.layoutAdd)
        RelativeLayout layoutAdd;

        public AddHolder(@NonNull View itemView, Context context) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void onBind() {
            this.layoutAdd.setOnClickListener(new OnClickListener() {
                public final void onClick(View view) {

                }
            });
        }

        private void listener() {
        }
    }


    public interface Listener {
        void onAdd();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imgThumb)
        ImageView imgThumb;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void onBind(int i) {
            Thumb thumb = (Thumb) MainListCategoryThumbAdapter.this.arrThumb.get(i);
            if (!thumb.getPath().equals("")) {
                RequestManager with = Glide.with(MainListCategoryThumbAdapter.this.context);
                StringBuilder sb = new StringBuilder();
                sb.append("file:///android_asset/");
                sb.append(thumb.getPath());
                with.load(Uri.parse(sb.toString())).thumbnail(0.1f).apply((BaseRequestOptions<?>) RequestOptions.placeholderOf((int) R.drawable.bg_gradient_green)).into(this.imgThumb);
            }
            listener();
        }

        private void listener() {
            this.imgThumb.setOnClickListener(v -> {

            });
        }
    }

    public MainListCategoryThumbAdapter(Context context2, ArrayList<Thumb> arrThumb2, boolean isYourColor2) {
        this.context = context2;
        this.arrThumb = arrThumb2;
        this.isYourColor = isYourColor2;
    }

    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        switch (i) {
            case 0:
                return new AddHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.add_new, viewGroup, false), this.context);
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
        if (position != 0 || !this.isYourColor) {
            return 1;
        }
        return 0;
    }

    public int getItemCount() {
        return this.arrThumb.size();
    }

    public void setListener(Listener listener2) {
        this.listener = listener2;
    }
}
