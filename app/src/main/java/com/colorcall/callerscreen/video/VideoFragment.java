package com.colorcall.callerscreen.video;

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
import com.colorcall.callerscreen.response.AppClient;
import com.colorcall.callerscreen.response.AppData;
import com.colorcall.callerscreen.response.AppService;
import com.colorcall.callerscreen.utils.AppUtils;
import com.colorcall.callerscreen.utils.HawkHelper;
import com.google.gson.Gson;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.colorcall.callerscreen.constan.Constant.SHOW_IMG_DELETE;

public class VideoFragment extends Fragment implements VideoAdapter.Listener {
    @BindView(R.id.rcvBgVideo)
    RecyclerView rcvBgVideo;
    VideoAdapter adapter;
    private int positionDownload=-1;
    public VideoFragment() {
    }
    LocalBroadcastManager mLocalBroadcastManager;
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case Constant.ACTION_LOAD_COMPLETE_THEME:
                    Log.e("TAN", "onReceive init video: ");
                    init();
                    break;
                case Constant.INTENT_DOWNLOAD_COMPLETE_THEME:
                    if(positionDownload!=-1){
                        adapter.setNewListBg();
                        adapter.notifyItemChanged(positionDownload);
                    }
                    break;
            }
        }
    };
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video, container, false);
        ButterKnife.bind(this,view);
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Constant.ACTION_LOAD_COMPLETE_THEME);
        mIntentFilter.addAction(Constant.INTENT_DOWNLOAD_COMPLETE_THEME);
        mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, mIntentFilter);
        return view;
    }

    private void init() {
        ArrayList<Background> listBg;
        listBg = HawkHelper.getListBackground();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false);
        rcvBgVideo.setLayoutManager(gridLayoutManager);
        rcvBgVideo.setItemAnimator(new DefaultItemAnimator());
        rcvBgVideo.addItemDecoration(new SimpleDividerItemDecoration(AppUtils.dpToPx(5)));
        adapter = new VideoAdapter(getContext(), listBg);
        adapter.setListener(this);
        rcvBgVideo.setAdapter(adapter);
        rcvBgVideo.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1) && newState==RecyclerView.SCROLL_STATE_IDLE) {
                    Log.e("-----","end");
                }
            }
        });
    }

    @Override
    public void onItemClick(ArrayList<Background> backgrounds, int position, boolean delete) {
        moveApplyTheme(backgrounds,position,delete);
    }

    private void moveApplyTheme(ArrayList<Background> backgrounds, int position, boolean delete) {
        Background background = backgrounds.get(position);
        if(!background.getPathItem().contains("/data/data")){
            positionDownload = position;
        }
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