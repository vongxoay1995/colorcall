package com.colorcall.callerscreen.main;

import android.content.Context;
import android.content.Intent;
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

import com.colorcall.callerscreen.analystic.FirebaseAnalystic;
import com.colorcall.callerscreen.analystic.ManagerEvent;
import com.google.gson.Gson;
import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.apply.ApplyActivity;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.model.Background;
import com.colorcall.callerscreen.model.Category;
import com.colorcall.callerscreen.utils.AppUtils;
import com.colorcall.callerscreen.utils.CategoryUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.colorcall.callerscreen.constan.Constant.SHOW_IMG_DELETE;

public class MainListCategoryAdapter extends RecyclerView.Adapter<MainListCategoryAdapter.ViewHolder> {
    public ArrayList<Category> arrCategory;
    public Context context;
    public boolean isYourColor;
    public MainListCategoryThumbAdapter.Listener listener;
    private int numberCategory;
    private FirebaseAnalystic firebaseAnalystic;

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
            ButterKnife.bind(this, itemView);
        }

        public void onBind(int i) {
            Category category = arrCategory.get(i);
            CategoryUtils.setInforCategory(context, i, txtContenCategory, imgSymbol);
            loadBackground(i, category.getListFile());
            listener(i, category.getListFile());
        }

        private void listener(int pos, ArrayList<Background> list) {
            this.txtSeeAll.setOnClickListener(v -> {
                if (listenerCategory != null) {
                    listenerCategory.onSeemoreClick(list, pos);
                }
            });
        }

        public void loadBackground(final int position, ArrayList<Background> listBg) {
            if (position == 2) {
                isYourColor = true;
            } else {
                isYourColor = false;
            }
            linearLayoutManager = new LinearLayoutManager(MainListCategoryAdapter.this.context, LinearLayoutManager.HORIZONTAL, false);
            rcvlistDemo.setLayoutManager(this.linearLayoutManager);
            rcvlistDemo.setItemAnimator(new DefaultItemAnimator());
            rcvlistDemo.addItemDecoration(new SimpleDividerItemDecoration(AppUtils.dpToPx(5)));
            adapter = new MainListCategoryThumbAdapter(context, listBg, isYourColor);
            adapter.setListener(new MainListCategoryThumbAdapter.Listener() {
                @Override
                public void onAdd() {
                    if (listenerCategory != null) {
                        listenerCategory.onAddClicked();
                    }
                }

                @Override
                public void onItemClick(ArrayList<Background> backgrounds, int position, boolean delete) {
                    numberCategory = getAdapterPosition();
                    trackingCategoryMain(numberCategory,position);
                    moveApplyTheme(backgrounds, position, delete);
                }
            });
            this.rcvlistDemo.setAdapter(this.adapter);
        }
    }

    private void trackingCategoryMain(int numberCategory, int position) {
        position = position + 1;
        switch (numberCategory) {
            case Constant.CTG_RECOMMEND:
                firebaseAnalystic.trackEvent(ManagerEvent.mainRecoClick(position));
                break;
            case Constant.CTG_POPULAR:
                firebaseAnalystic.trackEvent(ManagerEvent.mainPopuClick(position));
                break;
            case Constant.CTG_YOURTHEME:
                firebaseAnalystic.trackEvent(ManagerEvent.mainYourPictureClick(position));
                break;
            case Constant.CTG_COLOR_EFFECT:
                firebaseAnalystic.trackEvent(ManagerEvent.mainColorEffectClick(position));
                break;
            case Constant.CTG_LOVELY:
                firebaseAnalystic.trackEvent(ManagerEvent.mainLovelyClick(position));
                break;
        }
    }

    private void moveApplyTheme(ArrayList<Background> backgrounds, int position, boolean delete) {
        Background background = backgrounds.get(position);
        Intent intent = new Intent(context, ApplyActivity.class);
        if (delete) {
            intent.putExtra(SHOW_IMG_DELETE, true);
        }
        Gson gson = new Gson();
        intent.putExtra(Constant.BACKGROUND, gson.toJson(background));
        context.startActivity(intent);
    }


    public MainListCategoryAdapter(Context context2, ArrayList<Category> arrCategory2) {
        this.context = context2;
        this.arrCategory = arrCategory2;
        firebaseAnalystic = FirebaseAnalystic.getInstance(context2);
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

    Listener listenerCategory;

    public void setListener(Listener listener) {
        this.listenerCategory = listener;
    }

    public interface Listener {
        void onSeemoreClick(ArrayList<Background> list, int position);

        void onAddClicked();
    }
}
