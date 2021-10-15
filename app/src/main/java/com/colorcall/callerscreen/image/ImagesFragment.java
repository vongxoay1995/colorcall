package com.colorcall.callerscreen.image;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.apply.ApplyActivity;
import com.colorcall.callerscreen.broadcast.NetworkChangeReceiver;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.database.Background;
import com.colorcall.callerscreen.main.MainActivity;
import com.colorcall.callerscreen.main.SimpleDividerItemDecoration;
import com.colorcall.callerscreen.model.SignApplyImage;
import com.colorcall.callerscreen.model.SignMainImage;
import com.colorcall.callerscreen.utils.AppUtils;
import com.colorcall.callerscreen.utils.Boast;
import com.colorcall.callerscreen.utils.HawkHelper;
import com.colorcall.callerscreen.utils.InterstitialUtil;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.colorcall.callerscreen.constan.Constant.SHOW_IMG_DELETE;

public class ImagesFragment extends Fragment implements ImageAdapter.Listener, NetworkChangeReceiver.Listener {
    @BindView(R.id.rcvBgImages)
    RecyclerView rcvBgImages;
    @BindView(R.id.sw_refesh)
    SwipeRefreshLayout swRefresh;
    @BindView(R.id.layoutLoading)
    LinearLayout layoutLoading;
    @BindView(R.id.layoutNotNetwork)
    LinearLayout layoutNotNetwork;
    ImageAdapter adapter;
    private MainActivity mainActivity;
    private NetworkChangeReceiver networkChangeReceiver;
    private ArrayList<Background> listBg;
    private Background itemThemeSelected;
    private int positionItemThemeSelected = -1;
    private int countAds;
    public ImagesFragment(MainActivity activity) {
        this.mainActivity = activity;
    }
    public ImagesFragment() {
        // doesn't do anything special
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_images, container, false);
        ButterKnife.bind(this, view);
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Constant.ACTION_LOAD_COMPLETE_THEME);
        mIntentFilter.addAction(Constant.INTENT_APPLY_THEME);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        return view;
    }

    private void init() {
        listBg = HawkHelper.getListBackground();
        this.networkChangeReceiver = new NetworkChangeReceiver();
        this.networkChangeReceiver.registerReceiver(this.getContext(), this);
        this.swRefresh.setRefreshing(false);
        this.swRefresh.setOnRefreshListener(this::onRefreshLayout);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false);
        rcvBgImages.setLayoutManager(gridLayoutManager);
        rcvBgImages.setItemAnimator(new DefaultItemAnimator());
        rcvBgImages.addItemDecoration(new SimpleDividerItemDecoration(AppUtils.dpToPx(5)));
        adapter = new ImageAdapter(getContext(), listBg);
        adapter.setListener(this);
        rcvBgImages.setAdapter(adapter);
        rcvBgImages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (!AppUtils.isNetworkConnected(getContext())) {
                        Boast.makeText(getContext(), getString(R.string.err_network)).show();
                    }
                }
            }
        });
    }

    @Override
    public void onItemClick(ArrayList<Background> backgrounds, int position, boolean delete,int posRandom) {
        if (!AppUtils.allowViewClick())
            return;
        if(countAds%3!=0){
            this.countAds++;
            moveApplyTheme(backgrounds, position, delete,posRandom,true);
        }else {
            this.countAds++;
            InterstitialUtil.getInstance().showInterstitialAds(getActivity(), () -> {
                this.countAds = 1;
                moveApplyTheme(backgrounds, position, delete,posRandom,false);
            });
        }
    }

    private void moveApplyTheme(ArrayList<Background> backgrounds, int position, boolean delete,int posRandom,boolean isAllowShowAds) {
        Background background = backgrounds.get(position);
        Intent intent = new Intent(getActivity(), ApplyActivity.class);
        if (delete) {
            intent.putExtra(SHOW_IMG_DELETE, true);
        }
        intent.putExtra(Constant.FROM_SCREEN, Constant.IMAGES_FRAG_MENT);
        intent.putExtra(Constant.POS_RANDOM,posRandom);
        Gson gson = new Gson();
        intent.putExtra(Constant.BACKGROUND, gson.toJson(background));
        getActivity().startActivity(intent);
    }

    private void onRefreshLayout() {
        if (!AppUtils.isNetworkConnected(this.getContext())) {
            if (swRefresh != null) {
                swRefresh.setRefreshing(false);
            }
            return;
        }
        if (swRefresh != null) {
            swRefresh.setRefreshing(true);
        }
        if (mainActivity != null) {
            mainActivity.refreshCalApi();
        }
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        if (networkChangeReceiver != null) {
            networkChangeReceiver.unregisterReceiver(getContext());
        }
        super.onDestroy();
    }

    @Override
    public void netWorkStateChanged(boolean isNetWork) {
        if (!isNetWork && HawkHelper.getListBackground().size() < 10) {
            layoutNotNetwork.setVisibility(View.VISIBLE);
        } else {
            layoutNotNetwork.setVisibility(View.GONE);
            if (HawkHelper.getListBackground().size() < 10 && mainActivity != null) {
                layoutLoading.setVisibility(View.VISIBLE);
                mainActivity.refreshCalApi();
            }
        }
    }
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSignMainApply(SignMainImage signMainImage) {
        if (signMainImage.isSwiped()) {
            init();
        } else {
            swRefresh.setRefreshing(false);
            listBg = HawkHelper.getListBackground();
            adapter.setNewListBg();
            layoutLoading.setVisibility(View.GONE);
            if (adapter != null && listBg.size() > 5) {
                adapter.notifyItemRangeChanged(4, listBg.size() - 4);
            }
        }
        EventBus.getDefault().removeStickyEvent(signMainImage);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSignApplyImage(SignApplyImage signApplyImage) {
        switch (signApplyImage.getAction()) {
            case Constant.INTENT_APPLY_THEME:
                adapter.notifyDataSetChanged();
                break;
        }
        EventBus.getDefault().removeStickyEvent(signApplyImage);
    }

    @Override
    public void onItemThemeSelected(Background background, int position) {
        itemThemeSelected = background;
        positionItemThemeSelected = position;
    }

    @Override
    public void onResume() {
        super.onResume();
            if (itemThemeSelected != null
                    && positionItemThemeSelected != -1
                    & !HawkHelper.getBackgroundSelect().getPathItem().equals(itemThemeSelected.getPathItem())
                    && adapter != null) {
                adapter.notifyItemChanged(positionItemThemeSelected);
                positionItemThemeSelected=-1;
                itemThemeSelected=null;
            }
    }
}