package com.colorcall.callerscreen.categorydetail;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.colorcall.callerscreen.BuildConfig;
import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.analystic.FirebaseAnalystic;
import com.colorcall.callerscreen.analystic.ManagerEvent;
import com.colorcall.callerscreen.apply.ApplyActivity;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.database.DataManager;
import com.colorcall.callerscreen.listener.DialogGalleryListener;
import com.colorcall.callerscreen.main.SimpleDividerItemDecoration;
import com.colorcall.callerscreen.model.Background;
import com.colorcall.callerscreen.utils.AdListener;
import com.colorcall.callerscreen.utils.AppUtils;
import com.colorcall.callerscreen.utils.BannerAdsUtils;
import com.colorcall.callerscreen.utils.FileUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.colorcall.callerscreen.constan.Constant.SHOW_IMG_DELETE;

public class CateGoryDetail extends AppCompatActivity implements CategoryDetailAdapter.Listener, DialogGalleryListener, AdListener {
    @BindView(R.id.titleCategory)
    TextView titleCategory;
    @BindView(R.id.rcvThemes)
    RecyclerView rcvThemes;
    @BindView(R.id.layout_ads)
    RelativeLayout layoutAds;
    @BindView(R.id.layout_head)
    RelativeLayout layoutHead;
    private int posTitle;
    private String title;
    private CategoryDetailAdapter adapter;
    private ArrayList<Background> list;
    private boolean isYourColor;
    private int positionSelectBg = -1;
    private FirebaseAnalystic firebaseAnalystic;
    private BannerAdsUtils bannerAdsUtils;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cate_gory_detail);
        ButterKnife.bind(this);
        AppUtils.showFullHeader(this, layoutHead);

        firebaseAnalystic = FirebaseAnalystic.getInstance(this);
        posTitle = getIntent().getIntExtra(Constant.NUMBER_CATEGORY, 0);
        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<Background>>() {
        }.getType();
        ArrayList<Background> list = gson.fromJson(getIntent().getStringExtra(Constant.LIST_BG), listType);
        this.list = list;
        setTitlewithPos(posTitle);
        initData();
        bannerAdsUtils = new BannerAdsUtils(this, layoutAds);
        if(AppUtils.isNetworkConnected(this)){
            loadAds();
        }else {
            layoutAds.setVisibility(View.GONE);
        }
    }

    private void initData() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        rcvThemes.setLayoutManager(gridLayoutManager);
        rcvThemes.setItemAnimator(new DefaultItemAnimator());
        rcvThemes.addItemDecoration(new SimpleDividerItemDecoration(AppUtils.dpToPx(5)));
        adapter = new CategoryDetailAdapter(this, list, isYourColor);
        adapter.setListener(this);
        rcvThemes.setAdapter(adapter);
    }

    private void loadAds() {
        String idGG;
        String ID_ADS_GG = "ca-app-pub-3222539657172474/8602663222";
        if (BuildConfig.DEBUG) {
            idGG = Constant.ID_TEST_BANNER_ADMOD;
        } else {
            idGG = ID_ADS_GG;
        }
        bannerAdsUtils.setIdAds(idGG);
        bannerAdsUtils.setAdListener(this);
        bannerAdsUtils.showMediationBannerAds();
    }

    private void setTitlewithPos(int posTitle) {
        switch (posTitle) {
            case 1:
                isYourColor = false;
                title = getString(R.string.popular);
                break;
            case 2:
                isYourColor = true;
                title = getString(R.string.mytheme);
                break;
            case 3:
                isYourColor = false;
                title = getString(R.string.colorEffect);
                break;
            case 4:
                isYourColor = false;
                title = getString(R.string.lovely);
                break;
            default:
                isYourColor = false;
                title = getString(R.string.recommend);
                break;
        }
        titleCategory.setText(title);
    }

    @OnClick({R.id.btnBack})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                firebaseAnalystic.trackEvent(ManagerEvent.seeMoreBack());
                finish();
                break;
        }
    }

    @Override
    public void onAdd() {
        checkPermissionAccessCamera();
    }

    public void checkPermissionAccessCamera() {
        String[] permistion = {
                READ_EXTERNAL_STORAGE,
                WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
        };
        if (!AppUtils.checkPermission(this, permistion)) {
            ActivityCompat.requestPermissions(this, permistion,
                    Constant.PERMISSION_REQUEST_CODE);
        } else {
            openDialogGallery();
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constant.PERMISSION_REQUEST_CODE && grantResults.length > 0 && AppUtils.checkPermissionGrand(grantResults)) {
            openDialogGallery();
        }
    }

    private void openDialogGallery() {
        AppUtils.showDialogMyGallery(this, this);
    }

    @Override
    public void onItemClick(ArrayList<Background> backgrounds, int position, boolean delete) {
        positionSelectBg = position;
        trackingCategorySeemore(posTitle, position);
        moveApplyTheme(backgrounds, position, delete);
    }

    private void trackingCategorySeemore(int numberCategory, int position) {
        position = position + 1;
        switch (numberCategory) {
            case Constant.CTG_RECOMMEND:
                firebaseAnalystic.trackEvent(ManagerEvent.seemoreRecoClick(position));
                break;
            case Constant.CTG_POPULAR:
                firebaseAnalystic.trackEvent(ManagerEvent.seemorePopuClick(position));
                break;
            case Constant.CTG_YOURTHEME:
                firebaseAnalystic.trackEvent(ManagerEvent.seemoreYourPictureClick(position));
                break;
            case Constant.CTG_COLOR_EFFECT:
                firebaseAnalystic.trackEvent(ManagerEvent.seemoreColorEffectClick(position));
                break;
            case Constant.CTG_LOVELY:
                firebaseAnalystic.trackEvent(ManagerEvent.seemoreLovelyClick(position));
                break;
        }
    }

    private void moveApplyTheme(ArrayList<Background> backgrounds, int position, boolean delete) {
        Background background = backgrounds.get(position);
        Intent intent = new Intent(this, ApplyActivity.class);
        if (delete) {
            intent.putExtra(SHOW_IMG_DELETE, true);
        }
        Gson gson = new Gson();
        intent.putExtra(Constant.BACKGROUND, gson.toJson(background));
        startActivityForResult(intent, Constant.APPLY_REQUEST_CODE);
    }

    @Override
    public void onVideoClicked() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("video/*");
        photoPickerIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
        Intent takePhotoIntent = new Intent("android.media.action.VIDEO_CAPTURE");
        Intent chooserIntent = Intent.createChooser(photoPickerIntent, getResources().getString(R.string.your_video));
        chooserIntent.putExtra("android.intent.extra.INITIAL_INTENTS", new Intent[]{takePhotoIntent});
        startActivityForResult(chooserIntent, Constant.REQUEST_VIDEO);
    }

    @Override
    public void onImagesClicked() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        photoPickerIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        String pickTitle = getResources().getString(R.string.select_picture);
        Intent chooserIntent = Intent.createChooser(photoPickerIntent, pickTitle);
        chooserIntent.putExtra
                (Intent.EXTRA_INITIAL_INTENTS, new Intent[]{takePhotoIntent});
        startActivityForResult(chooserIntent, Constant.REQUEST_CODE_IMAGES);
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
                        + Constant.PATH_THUMB_COLOR_CALL + "thumb_bg_" + listBgDb.size();
            }
            video = new Background(0, imageUrl, path, true);
            FileUtils.saveBitmap(imageUrl, bitmap);
            list.add(video);
            adapter.notifyDataSetChanged();
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
                list.add(picture);
                adapter.notifyDataSetChanged();
                DataManager.query().getBackgroundDao().save(picture);
            } else {
                Toast.makeText(getBaseContext(), "File picture not found", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        firebaseAnalystic.trackEvent(ManagerEvent.seeMoreOpen());
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constant.REQUEST_VIDEO) {
                Uri uriData = data.getData();
                String path = FileUtils.getRealPathFromUri(this, uriData);
                resetListDataVideo(path);
            } else if (requestCode == Constant.REQUEST_CODE_IMAGES) {
                Uri uriData = data.getData();
                String path = FileUtils.getRealPathFromUri(this, uriData);
                resetListDataImage(path);
            } else if (requestCode == Constant.APPLY_REQUEST_CODE) {
                if (data != null) {
                    boolean isDelete = data.getBooleanExtra(Constant.IS_DELETE_BG, false);
                    if (isDelete && positionSelectBg > -1) {
                        list.remove(positionSelectBg);
                        adapter.notifyDataSetChanged();
                    }
                    boolean isUpdateList = data.getBooleanExtra(Constant.IS_UPDATE_LIST,false);
                    if(isUpdateList){
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }
    }

    @Override
    public void onAdloaded() {
        layoutAds.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAdFailed() {
        layoutAds.setVisibility(View.GONE);
    }
}
