package com.colorcall.callerscreen.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.colorcall.callerscreen.BuildConfig;
import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.analystic.FirebaseAnalystic;
import com.colorcall.callerscreen.analystic.ManagerEvent;
import com.colorcall.callerscreen.categorydetail.CateGoryDetail;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.database.DataManager;
import com.colorcall.callerscreen.listener.DialogGalleryListener;
import com.colorcall.callerscreen.model.Background;
import com.colorcall.callerscreen.model.Category;
import com.colorcall.callerscreen.setting.SettingActivity;
import com.colorcall.callerscreen.utils.AdListener;
import com.colorcall.callerscreen.utils.AppUtils;
import com.colorcall.callerscreen.utils.BannerAdsUtils;
import com.colorcall.callerscreen.utils.CategoryUtils;
import com.colorcall.callerscreen.utils.FileUtils;
import com.colorcall.callerscreen.utils.HawkHelper;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity implements MainListCategoryAdapter.Listener, DialogGalleryListener, AdListener {
    private MainListCategoryAdapter adapter;
    private LinearLayoutManager linearLayoutManager;
    @BindView(R.id.rcvCategory)
    RecyclerView rcvCategory;
    @BindView(R.id.layout_ads)
    RelativeLayout layoutAds;
    @BindView(R.id.layout_head)
    RelativeLayout layout_head;
    private ArrayList<Category> listCategory;
    private FirebaseAnalystic firebaseAnalystic;
    private BannerAdsUtils bannerAdsUtils;
    private final int indexYourData = 2;
    private String pathUriImage;

    LocalBroadcastManager mLocalBroadcastManager;
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Constant.INTENT_APPLY_THEME:
                    adapter.notifyDataSetChanged();
                    break;
                case Constant.INTENT_DELETE_THEME:
                    addDatafromDatabase();
                    adapter.setNewData(listCategory);
                    adapter.notifyDataSetChanged();
                    break;
            }
        }
    };


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        AppUtils.showFullHeader(this, layout_head);
        firebaseAnalystic = FirebaseAnalystic.getInstance(this);
        bannerAdsUtils = new BannerAdsUtils(this, layoutAds);
        loadData(Constant.MENU_CATEGORY);
        if (AppUtils.isNetworkConnected(this)) {
            loadAds();
        } else {
            layoutAds.setVisibility(View.GONE);
        }
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Constant.INTENT_APPLY_THEME);
        mIntentFilter.addAction(Constant.INTENT_DELETE_THEME);
        mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, mIntentFilter);
    }

    @SuppressLint("StaticFieldLeak")
    private void loadData(final String assetsDir) {
        if (!HawkHelper.isLoadDataFirst()) {
            new AsyncTask<Void, Void, ArrayList<Category>>() {
                public ArrayList<Category> doInBackground(Void... voids) {
                    ArrayList<Category> list = CategoryUtils.loadData(getApplicationContext(), assetsDir);
                    CategoryUtils.addRecommendCategory(getBaseContext(), list);
                    return list;
                }

                public void onPostExecute(ArrayList<Category> list) {
                    super.onPostExecute(list);
                    HawkHelper.setLoadDataFirst(true);
                    HawkHelper.setListCategory(list);
                    addAllData();
                }
            }.execute();
        } else {
            addAllData();
        }
    }

    public void addAllData() {
        addDatafromDatabase();
        initAdapterCategory();
    }

    @Override
    protected void onResume() {
        firebaseAnalystic.trackEvent(ManagerEvent.mainOpen());
        super.onResume();
    }

    public void addDatafromDatabase() {
        listCategory = HawkHelper.getListCategory();
        ArrayList<Background> listYourTheme;
        listYourTheme = (ArrayList<Background>) DataManager.query().getBackgroundDao().queryBuilder().list();
        if (listYourTheme.size() > 0 && listCategory.size() > 3) {
            listCategory.get(2).getListFile().addAll(listYourTheme);
            listCategory.set(2, listCategory.get(2));
        }
    }


    public void initAdapterCategory() {
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rcvCategory.setLayoutManager(this.linearLayoutManager);
        adapter = new MainListCategoryAdapter(this, listCategory);
        adapter.setListener(this);
        this.rcvCategory.setAdapter(adapter);
    }


    @OnClick({R.id.btnMenu})
    public void onViewClicked() {
        firebaseAnalystic.trackEvent(ManagerEvent.mainSlideClick());
        startActivity(new Intent(this, SettingActivity.class));
    }

    public void checkPermissionActionCamera() {
        String[] permistion = {
                READ_EXTERNAL_STORAGE,
                WRITE_EXTERNAL_STORAGE,
                CAMERA,
        };
        if (!AppUtils.checkPermission(this, permistion)) {
            ActivityCompat.requestPermissions(this, permistion,
                    Constant.PERMISSION_REQUEST_CODE_CAMERA);
        } else {
            openDialogGallery();
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constant.PERMISSION_REQUEST_CODE_CAMERA && grantResults.length > 0 && AppUtils.checkPermissionGrand(grantResults)) {
            openDialogGallery();
        }
    }

    private void openDialogGallery() {
        AppUtils.showDialogMyGallery(this, firebaseAnalystic, this);
    }

    @Override
    public void onSeemoreClick(ArrayList<Background> list, int position) {
        trackingSeemoClick(position);
        Intent intent = new Intent(this, CateGoryDetail.class);
        intent.putExtra(Constant.NUMBER_CATEGORY, position);
        Gson gson = new Gson();
        intent.putExtra(Constant.LIST_BG, gson.toJson(list));
        startActivity(intent);
    }

    private void trackingSeemoClick(int position) {
        switch (position) {
            case Constant.CTG_RECOMMEND:
                firebaseAnalystic.trackEvent(ManagerEvent.mainSeeAllRecommend());
                break;
            case Constant.CTG_POPULAR:
                firebaseAnalystic.trackEvent(ManagerEvent.mainSeeAllPopulated());
                break;
            case Constant.CTG_YOURTHEME:
                firebaseAnalystic.trackEvent(ManagerEvent.mainSeeAllYourTheme());
                break;
            case Constant.CTG_COLOR_EFFECT:
                firebaseAnalystic.trackEvent(ManagerEvent.mainSeeAllColorEffect());
                break;
            case Constant.CTG_LOVELY:
                firebaseAnalystic.trackEvent(ManagerEvent.mainSeeAllLovelly());
                break;
        }
    }

    @Override
    public void onAddClicked() {
        checkPermissionActionCamera();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constant.REQUEST_VIDEO) {
                Uri uriData = data.getData();
                String path = FileUtils.getRealPathFromUri(this, uriData);
                resetListDataVideo(path);
                addDatafromDatabase();
                adapter.setNewData(listCategory);
                adapter.notifyItemChanged(indexYourData);
            } else if (requestCode == Constant.REQUEST_CODE_IMAGES) {
                String path;
                if (data!=null&&data.getData() != null) {
                    path = FileUtils.getRealPathFromUri(this, data.getData());
                } else {
                    path = pathUriImage;
                }
                Log.e("TAN", "onActivityResult: " + path);
                resetListDataImage(path);
                addDatafromDatabase();
                adapter.setNewData(listCategory);
                adapter.notifyItemChanged(indexYourData);
            }
        }
    }

    private void resetListDataVideo(String path) {
        ArrayList<Background> listBgDb = (ArrayList<Background>) DataManager.query().getBackgroundDao().queryBuilder().list();
        if (path != null) {
            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND);
            File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    + Constant.PATH_THUMB_COLOR_CALL);
            if (!folder.exists())
                folder.mkdirs();
            Background video;
            String imageUrl = "";
            if (listBgDb != null) {
                imageUrl = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        + Constant.PATH_THUMB_COLOR_CALL + "thumb_" + listBgDb.size();
            }
            video = new Background(0, imageUrl, path, true);
            FileUtils.saveBitmap(imageUrl, bitmap);
            DataManager.query().getBackgroundDao().save(video);
        }
    }

    private void resetListDataImage(String path) {
        if (path != null) {
            File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    + Constant.PATH_THUMB_COLOR_CALL_IMAGES);
            if (!folder.exists())
                folder.mkdirs();

            File file = new File(path);

            if (file.exists()) {
                Background picture = new Background(1, file.getAbsolutePath(), file.getAbsolutePath(), true);
                DataManager.query().getBackgroundDao().save(picture);
            } else {
                Toast.makeText(getBaseContext(), "File picture not found", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onVideoClicked() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        photoPickerIntent.setType("video/*");
        photoPickerIntent.setAction(Intent.ACTION_GET_CONTENT);
        photoPickerIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
        Intent takePhotoIntent = new Intent("android.media.action.VIDEO_CAPTURE");
        Intent chooserIntent = Intent.createChooser(photoPickerIntent, getResources().getString(R.string.your_video));
        chooserIntent.putExtra("android.intent.extra.INITIAL_INTENTS", new Intent[]{takePhotoIntent});
        startActivityForResult(chooserIntent, Constant.REQUEST_VIDEO);
    }

    @Override
    public void onImagesClicked() {
        pathUriImage = AppUtils.openCameraIntent(this, Constant.REQUEST_CODE_IMAGES);
    }

    @OnClick({R.id.btnGift, R.id.btnCamera, R.id.btnVips})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnGift:
                firebaseAnalystic.trackEvent(ManagerEvent.mainAdsOpen());
                break;
            case R.id.btnCamera:
                firebaseAnalystic.trackEvent(ManagerEvent.mainCameraOpen());
                checkPermissionActionCamera();
                break;
            case R.id.btnVips:
                firebaseAnalystic.trackEvent(ManagerEvent.mainStarClick());
                break;
        }
    }

    private void loadAds() {
        String ID_ADS_GG = "ca-app-pub-3222539657172474/8137142250";
        String idGG;
        if (BuildConfig.DEBUG) {
            idGG = Constant.ID_TEST_BANNER_ADMOD;
        } else {
            idGG = ID_ADS_GG;
        }
        bannerAdsUtils.setIdAds(idGG);
        bannerAdsUtils.setAdListener(this);
        bannerAdsUtils.showMediationBannerAds();
    }

    @Override
    public void onAdloaded() {
        layoutAds.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAdFailed() {
        layoutAds.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        mLocalBroadcastManager.unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }


}
