package com.htn.colorcall.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Path;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.htn.colorcall.R;
import com.htn.colorcall.constan.Constant;
import com.htn.colorcall.model.Category;
import com.htn.colorcall.setting.SettingActivity;
import com.htn.colorcall.utils.AppUtils;
import com.htn.colorcall.utils.CategoryUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import java.util.ArrayList;

import ss.com.bannerslider.Slider;
import ss.com.bannerslider.event.OnSlideClickListener;

public class MainActivity extends AppCompatActivity implements MainListCategoryThumbAdapter.Listener {
    private MainListCategoryAdapter adapter;
    @BindView(R.id.bannerImage)
    Slider bannerImage;
    private LinearLayoutManager linearLayoutManager;
    @BindView(R.id.rcvCategory)
    RecyclerView rcvCategory;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_main);
        ButterKnife.bind( this);
        initView();
        loadCategory(Constant.MENU_CATEGORY);
    }

    @SuppressLint({"StaticFieldLeak"})
    private void loadCategory(final String assetsDir) {
        new AsyncTask<Void, Void, ArrayList<Category>>() {
            public ArrayList<Category> doInBackground(Void... voids) {
                return CategoryUtils.getListCategory(MainActivity.this.getApplicationContext(), assetsDir);
            }

            public void onPostExecute(ArrayList<Category> arrCategory) {
                super.onPostExecute(arrCategory);
                MainActivity.this.initAdapterCategory(arrCategory);
            }
        }.execute(new Void[0]);
    }

    public void initAdapterCategory(ArrayList<Category> arrCategory) {
        this.linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        this.rcvCategory.setLayoutManager(this.linearLayoutManager);
        this.adapter = new MainListCategoryAdapter(this, arrCategory, this);
        this.rcvCategory.setAdapter(adapter);
    }

    private void initView() {
        this.bannerImage.setAdapter(new MainSliderAdapter());
        this.bannerImage.setOnSlideClickListener(new OnSlideClickListener() {
            public void onSlideClick(int position) {
                StringBuilder sb = new StringBuilder();
                sb.append(position);
                sb.append("");
                Log.e("Pos", sb.toString());
            }
        });
    }

    @OnClick({R.id.btnMenu})
    public void onViewClicked() {
        startActivity(new Intent(this, SettingActivity.class));
    }

    @RequiresApi(api = 23)
    public void onAdd() {
        Log.e("Adasd", "11");
        String[] permistion = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};
        if (!AppUtils.checkPermission(this, permistion)) {
            requestPermissions(permistion, 2);
        } else {
            openYourVideo();
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 2 && grantResults.length > 0 && AppUtils.checkPermissionGrand(grantResults)) {
            openYourVideo();
        }
    }

    private void openYourVideo() {
        Intent pickIntent = new Intent();
        pickIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        pickIntent.setType("video/*");
        pickIntent.setAction("android.intent.action.GET_CONTENT");
        Intent takePhotoIntent = new Intent("android.media.action.VIDEO_CAPTURE");
        Intent chooserIntent = Intent.createChooser(pickIntent, getResources().getString(R.string.your_video));
        chooserIntent.putExtra("android.intent.extra.INITIAL_INTENTS", new Intent[]{takePhotoIntent});
        startActivityForResult(chooserIntent, 1);
    }
}
