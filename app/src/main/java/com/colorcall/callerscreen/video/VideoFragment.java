package com.colorcall.callerscreen.video;

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
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.apply.ApplyActivity;
import com.colorcall.callerscreen.broadcast.NetworkChangeReceiver;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.database.Background;
import com.colorcall.callerscreen.main.MainActivity;
import com.colorcall.callerscreen.main.SimpleDividerItemDecoration;
import com.colorcall.callerscreen.model.SignApplyVideo;
import com.colorcall.callerscreen.model.SignMainVideo;
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

public class VideoFragment extends Fragment implements VideoAdapter.Listener, NetworkChangeReceiver.Listener {
    @BindView(R.id.rcvBgVideo)
    RecyclerView rcvBgVideo;
    VideoAdapter adapter;
    @BindView(R.id.layoutLoading)
    LinearLayout layoutLoading;
    @BindView(R.id.layoutNotNetwork)
    LinearLayout layoutNotNetwork;
    private int positionDownload = -1;
    @BindView(R.id.sw_refesh)
    SwipeRefreshLayout swRefresh;
    private MainActivity mainActivity;
    private NetworkChangeReceiver networkChangeReceiver;
    private ArrayList<Background> listBg;
    private int positionItemThemeSelected = -1;
    public VideoFragment(MainActivity activity) {
        this.mainActivity = activity;
    }
    public VideoFragment() {
        // doesn't do anything special
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video, container, false);
        ButterKnife.bind(this, view);
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Constant.ACTION_LOAD_COMPLETE_THEME);
        mIntentFilter.addAction(Constant.INTENT_DOWNLOAD_COMPLETE_THEME);
        mIntentFilter.addAction(Constant.INTENT_APPLY_THEME);
        return view;
    }

    private void init() {
        this.networkChangeReceiver = new NetworkChangeReceiver();
        this.networkChangeReceiver.registerReceiver(this.getContext(), this);
        this.swRefresh.setRefreshing(false);
        this.swRefresh.setOnRefreshListener(this::onRefreshLayout);
        listBg = HawkHelper.getListBackground();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false);
        rcvBgVideo.setLayoutManager(gridLayoutManager);
        rcvBgVideo.setItemAnimator(new DefaultItemAnimator());
        rcvBgVideo.addItemDecoration(new SimpleDividerItemDecoration(AppUtils.dpToPx(5)));
        adapter = new VideoAdapter(getContext(), listBg);
        adapter.setListener(this);
        RecyclerView.ItemAnimator animator = rcvBgVideo.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        rcvBgVideo.setAdapter(adapter);
        rcvBgVideo.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (!AppUtils.isNetworkConnected(getContext())) {
                        Boast.makeText(getContext(), getString(R.string.err_network)).show();
                    }
                }
                if(newState==0&&positionItemThemeSelected!=-1){
                    adapter.notifyItemChanged(positionItemThemeSelected);
                }
            }
        });
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
    public void onItemClick(ArrayList<Background> backgrounds, int position, boolean delete,int posRandom) {
        InterstitialUtil.getInstance().showInterstitialAds(new InterstitialUtil.AdCloseListener() {
            @Override
            public void onAdClose() {
                moveApplyTheme(backgrounds, position, delete,posRandom);
            }

            @Override
            public void onMove() {
                moveApplyTheme(backgrounds, position, delete,posRandom);
            }
        });
    }

    @Override
    public void onItemThemeSelected(int position) {
        positionItemThemeSelected = position;
    }

    private void moveApplyTheme(ArrayList<Background> backgrounds, int position, boolean delete,int posRandom) {
        Background background = backgrounds.get(position);
        if (!background.getPathItem().contains("/data/data")) {
            positionDownload = position;
        }
        Intent intent = new Intent(getActivity(), ApplyActivity.class);
        intent.putExtra(Constant.FROM_SCREEN, Constant.VIDEO_FRAG_MENT);
        intent.putExtra(Constant.ITEM_POSITION, position);
        intent.putExtra(Constant.POS_RANDOM, posRandom);
        if (delete) {
            intent.putExtra(SHOW_IMG_DELETE, true);
        }
        Gson gson = new Gson();
        intent.putExtra(Constant.BACKGROUND, gson.toJson(background));
        getActivity().startActivity(intent);
    }

    @Override
    public void onDestroy() {
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
    public void onSignMainVideo(SignMainVideo signMainVideo) {
        if (signMainVideo.isRefresh()) {
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
        EventBus.getDefault().removeStickyEvent(signMainVideo);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSignApplyVideo(SignApplyVideo signApplyVideo) {
        switch (signApplyVideo.getAction()) {
            case Constant.INTENT_DOWNLOAD_COMPLETE_THEME:
                if (positionDownload != -1) {
                    adapter.setNewListBg();
                    adapter.notifyItemChanged(positionDownload);
                }
                break;
            case Constant.INTENT_APPLY_THEME:
                adapter.notifyDataSetChanged();
                break;
            case Constant.APPLY_ITEM_DEFAULT:
                adapter.notifyItemChanged(0);
                break;
        }
        EventBus.getDefault().removeStickyEvent(signApplyVideo);
    }
    @Override
    public void onStart() {
        EventBus.getDefault().register(this);
        super.onStart();
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(adapter!=null){
            adapter.notifyItemChanged(positionItemThemeSelected);
        }
    }
}