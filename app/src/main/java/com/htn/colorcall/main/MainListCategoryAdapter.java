package com.htn.colorcall.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.htn.colorcall.R;
import com.htn.colorcall.model.Category;
import com.htn.colorcall.model.Thumb;
import com.htn.colorcall.utils.CategoryUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainListCategoryAdapter extends RecyclerView.Adapter<MainListCategoryAdapter.ViewHolder> {
    public ArrayList<Category> arrCategory;
    public Context context;
    public boolean isYourColor;
    public MainListCategoryThumbAdapter.Listener listener;

    public class ViewHolder extends RecyclerView.ViewHolder {
        private MainListCategoryThumbAdapter adapter;
        @BindView(R.id.imgSymbol)
        ImageView imgSymbol;
        private LinearLayoutManager linearLayoutManager;
        @BindView(R.id.rcvlistDemo)
        RecyclerView rcvlistDemo;
        @BindView(R.id.txtContenCategory)
        TextView txtContenCategory;
        @BindView(R.id.txtSeeAll)
        TextView txtSeeAll;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind( this, itemView);
        }

        public void onBind(int i) {
            Category category =  MainListCategoryAdapter.this.arrCategory.get(i);
            if (VERSION.SDK_INT >= 21) {
                this.imgSymbol.setImageResource(category.getSymbol());
            } else {
                this.imgSymbol.setImageDrawable(VectorDrawableCompat.create(MainListCategoryAdapter.this.context.getResources(), category.getSymbol(), MainListCategoryAdapter.this.context.getTheme()));
            }
            this.txtContenCategory.setText(category.getTitle());
            listener();
            loadThumb(i, MainListCategoryAdapter.this.context, category.getAssetsDir());
        }

        private void listener() {
            this.txtSeeAll.setOnClickListener(v -> {

            });
        }

        @SuppressLint({"StaticFieldLeak"})
        public void loadThumb(final int position, final Context context, final String keyCategory) {
            new AsyncTask<Void, Void, ArrayList<Thumb>>() {
                public ArrayList<Thumb> doInBackground(Void... voids) {
                    if (position == 2) {
                        ArrayList<Thumb> thumbs = new ArrayList<>();
                        thumbs.add(null);
                        return thumbs;
                    }
                    return CategoryUtils.getListThumb(context, keyCategory);
                }

                public void onPostExecute(ArrayList<Thumb> thumbs) {
                    super.onPostExecute(thumbs);
                    if (position == 2) {
                        MainListCategoryAdapter.this.isYourColor = true;
                    } else {
                        MainListCategoryAdapter.this.isYourColor = false;
                    }
                    ViewHolder.this.onThumbLoaded(thumbs, MainListCategoryAdapter.this.isYourColor, position);
                }
            }.execute(new Void[0]);
        }

        public void onThumbLoaded(ArrayList<Thumb> thumbs, boolean isYourColor, int position) {
            StringBuilder sb = new StringBuilder();
            sb.append(thumbs.size());
            sb.append("---");
            sb.append(isYourColor);
            sb.append("--");
            sb.append(position);
            Log.e("A", sb.toString());
            this.linearLayoutManager = new LinearLayoutManager(MainListCategoryAdapter.this.context, LinearLayoutManager.HORIZONTAL, false);
            this.rcvlistDemo.setLayoutManager(this.linearLayoutManager);
            this.rcvlistDemo.setItemAnimator(new DefaultItemAnimator());
            this.adapter = new MainListCategoryThumbAdapter(MainListCategoryAdapter.this.context, thumbs, isYourColor);
            this.adapter.setListener(listener);
            this.rcvlistDemo.setAdapter(this.adapter);
        }
    }


    public MainListCategoryAdapter(Context context2, ArrayList<Category> arrCategory2, MainListCategoryThumbAdapter.Listener listener2) {
        this.context = context2;
        this.arrCategory = arrCategory2;
        this.listener = listener2;
    }

    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(this.context).inflate(R.layout.item_list_category, viewGroup, false));
    }

    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.onBind(i);
    }

    public int getItemCount() {
        return this.arrCategory.size();
    }
}
