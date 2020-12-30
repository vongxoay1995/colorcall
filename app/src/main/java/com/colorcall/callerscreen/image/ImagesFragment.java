package com.colorcall.callerscreen.image;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.apply.ApplyActivity;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.database.Background;
import com.colorcall.callerscreen.main.SimpleDividerItemDecoration;
import com.colorcall.callerscreen.utils.AppUtils;
import com.colorcall.callerscreen.utils.HawkHelper;
import com.google.gson.Gson;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.colorcall.callerscreen.constan.Constant.SHOW_IMG_DELETE;

public class ImagesFragment extends Fragment implements ImageAdapter.Listener{
    @BindView(R.id.rcvBgImages)
    RecyclerView rcvBgImages;
    ImageAdapter adapter;
    LocalBroadcastManager mLocalBroadcastManager;
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Constant.ACTION_LOAD_COMPLETE_THEME)){
                init();
            }
        }
    };

    public ImagesFragment() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_images, container, false);
        ButterKnife.bind(this,view);
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Constant.ACTION_LOAD_COMPLETE_THEME);
        mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, mIntentFilter);
        return view;
    }
    private void init() {
        ArrayList<Background> listBg;
        listBg = HawkHelper.getListBackground();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false);
        rcvBgImages.setLayoutManager(gridLayoutManager);
        rcvBgImages.setItemAnimator(new DefaultItemAnimator());
        rcvBgImages.addItemDecoration(new SimpleDividerItemDecoration(AppUtils.dpToPx(5)));
        adapter = new ImageAdapter(getContext(), listBg);
        adapter.setListener(this);
        rcvBgImages.setAdapter(adapter);
    }

    @Override
    public void onItemClick(ArrayList<Background> backgrounds, int position, boolean delete) {
        moveApplyTheme(backgrounds,position,delete);
    }
    private void moveApplyTheme(ArrayList<Background> backgrounds, int position, boolean delete) {
        Background background = backgrounds.get(position);
        Intent intent = new Intent(getActivity(), ApplyActivity.class);
        if (delete) {
            intent.putExtra(SHOW_IMG_DELETE, true);
        }
        Gson gson = new Gson();
        intent.putExtra(Constant.BACKGROUND, gson.toJson(background));
        getActivity().startActivity(intent);
    }

    @Override
    public void onDestroy() {
        mLocalBroadcastManager.unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }
}