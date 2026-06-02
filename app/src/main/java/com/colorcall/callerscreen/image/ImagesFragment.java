package com.colorcall.callerscreen.image;

import static com.colorcall.callerscreen.constan.Constant.SHOW_IMG_DELETE;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.apply.ApplyActivity;
import com.colorcall.callerscreen.broadcast.NetworkChangeReceiver;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.database.Background;
import com.colorcall.callerscreen.databinding.FragmentImagesBinding;
import com.colorcall.callerscreen.main.MainActivity;
import com.colorcall.callerscreen.main.SimpleDividerItemDecoration;
import com.colorcall.callerscreen.model.SignApplyImage;
import com.colorcall.callerscreen.model.SignMainImage;
import com.colorcall.callerscreen.utils.AppUtils;
import com.colorcall.callerscreen.utils.Boast;
import com.colorcall.callerscreen.utils.HawkHelper;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class ImagesFragment extends Fragment implements ImageAdapter.Listener, NetworkChangeReceiver.Listener {
    private MainActivity mainActivity;
    private NetworkChangeReceiver networkChangeReceiver;
    private ArrayList<Background> listBg;
    private Background itemThemeSelected;
    private int positionItemThemeSelected = -1;
    private FragmentImagesBinding binding;
    ImageAdapter adapter;

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
        binding = FragmentImagesBinding.inflate(inflater, container, false);

        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Constant.ACTION_LOAD_COMPLETE_THEME);
        mIntentFilter.addAction(Constant.INTENT_APPLY_THEME);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        init(); // Khởi tạo data ngay khi view tạo, không phụ thuộc vào EventBus sticky
        return binding.getRoot();
    }


    private void init() {
        listBg = HawkHelper.getListBackground();
        // Guard: tránh đăng ký NetworkChangeReceiver 2 lần khi init() gọi từ cả
        // onCreateView() lẫn EventBus (onSignMainApply với isSwiped=true)
        if (this.networkChangeReceiver == null) {
            this.networkChangeReceiver = new NetworkChangeReceiver();
            this.networkChangeReceiver.registerReceiver(this.getContext(), this);
        }
        this.binding.swRefesh.setRefreshing(false);
        this.binding.swRefesh.setOnRefreshListener(this::onRefreshLayout);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false);
        binding.rcvBgImages.setLayoutManager(gridLayoutManager);
        binding.rcvBgImages.setItemAnimator(new DefaultItemAnimator());
        binding.rcvBgImages.addItemDecoration(new SimpleDividerItemDecoration(AppUtils.dpToPx(5)));
        adapter = new ImageAdapter(getContext(), listBg);
        adapter.setListener(this);
        binding.rcvBgImages.setAdapter(adapter);
        // Hiển thị loading nếu chưa có data, ẩn đi nếu đã có
        if (listBg.isEmpty()) {
            binding.layoutLoading.setVisibility(View.VISIBLE);
        } else {
            binding.layoutLoading.setVisibility(View.GONE);
        }
        binding.rcvBgImages.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
    public void onItemClick(ArrayList<Background> backgrounds, int position,boolean isPro, boolean delete, int posRandom) {
        if (!AppUtils.allowViewClick())
            return;
        moveApplyTheme(backgrounds, position, delete,posRandom, isPro);
    }

    private void moveApplyTheme(ArrayList<Background> backgrounds, int position, boolean delete, int posRandom, boolean isPro) {
        Background background = backgrounds.get(position);
        Intent intent = new Intent(getActivity(), ApplyActivity.class);
        if (delete) {
            intent.putExtra(SHOW_IMG_DELETE, true);
        }
        intent.putExtra(Constant.FROM_SCREEN, Constant.IMAGES_FRAG_MENT);
        intent.putExtra(Constant.POS_RANDOM,posRandom);
        intent.putExtra("is_pro", isPro);
        Gson gson = new Gson();
        intent.putExtra(Constant.BACKGROUND, gson.toJson(background));
        getActivity().startActivity(intent);
    }

    private void onRefreshLayout() {
        if (!AppUtils.isNetworkConnected(this.getContext())) {
            binding.swRefesh.setRefreshing(false);
            return;
        }
        binding.swRefesh.setRefreshing(true);
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
            binding.layoutNotNetwork.setVisibility(View.VISIBLE);
        } else {
            binding.layoutNotNetwork.setVisibility(View.GONE);
            if (HawkHelper.getListBackground().size() < 10 && mainActivity != null) {
                binding.layoutLoading.setVisibility(View.VISIBLE);
                mainActivity.refreshCalApi();
            }
        }
    }
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSignMainApply(SignMainImage signMainImage) {
        if (signMainImage.isSwiped()) {
            init();
        } else {
            binding.swRefesh.setRefreshing(false);
            listBg = HawkHelper.getListBackground();
            adapter.setNewListBg();
            binding.layoutLoading.setVisibility(View.GONE);
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