package com.htn.colorcall.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.htn.colorcall.R;
import com.htn.colorcall.categorydetail.CateGoryDetail;
import com.htn.colorcall.constan.Constant;
import com.htn.colorcall.custom.CustomVideoView;
import com.htn.colorcall.database.DataManager;
import com.htn.colorcall.listener.DialogGalleryListener;
import com.htn.colorcall.model.Background;
import com.htn.colorcall.model.Category;
import com.htn.colorcall.setting.SettingActivity;
import com.htn.colorcall.utils.AppUtils;
import com.htn.colorcall.utils.CategoryUtils;
import com.htn.colorcall.utils.FileUtils;
import com.htn.colorcall.utils.HawkHelper;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ss.com.bannerslider.Slider;
import ss.com.bannerslider.event.OnSlideClickListener;

import static android.Manifest.permission.ANSWER_PHONE_CALLS;
import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity implements MainListCategoryAdapter.Listener, DialogGalleryListener {
    private MainListCategoryAdapter adapter;
    private LinearLayoutManager linearLayoutManager;
    @BindView(R.id.rcvCategory)
    RecyclerView rcvCategory;
    private ArrayList<Category> listCategory;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        checkPermissionAction();

//        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
//        WindowManager mWindowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
//        View mView = inflater.inflate(R.layout.layout_call_color, null);
//        final WindowManager.LayoutParams mLayoutParams = new WindowManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
//                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
//                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
//                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON ,
//                PixelFormat.TRANSLUCENT);
//        mView.setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                        | View.SYSTEM_UI_FLAG_FULLSCREEN
//                        | View.SYSTEM_UI_FLAG_VISIBLE
//                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//        );
//        mView.setVisibility(View.VISIBLE);
//        mWindowManager.addView(mView, mLayoutParams);
    }

    @SuppressLint("StaticFieldLeak")
    private void loadData(final String assetsDir) {
        if (!HawkHelper.isLoadDataFirst()) {
            Log.e("TAN", "loadData: 1");
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
                    addDatafromDatabase();
                    initAdapterCategory();
                }
            }.execute();
        } else {
            Log.e("TAN", "loadData: 2");
            addDatafromDatabase();
            initAdapterCategory();
        }
    }

    @Override
    protected void onResume() {
        loadData(Constant.MENU_CATEGORY);
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
        startActivity(new Intent(this, SettingActivity.class));
    }

    public void checkPermissionAction() {
        String[] permistion;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            permistion = new String[]{
                    READ_PHONE_STATE,
                    CALL_PHONE,
                    READ_EXTERNAL_STORAGE,
                    WRITE_EXTERNAL_STORAGE,
                    CAMERA,
            };
        } else {
            permistion = new String[]{
                    ANSWER_PHONE_CALLS,
                    READ_PHONE_STATE,
                    CALL_PHONE,
                    READ_EXTERNAL_STORAGE,
                    WRITE_EXTERNAL_STORAGE,
                    CAMERA,
            };
        }
        if (!AppUtils.checkPermission(this, permistion)) {
            ActivityCompat.requestPermissions(this, permistion,
                    Constant.PERMISSION_REQUEST_CODE);
        }
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
        AppUtils.showDialogMyGallery(this, this);
    }

    @Override
    public void onSeemoreClick(ArrayList<Background> list, int position) {
        Log.e("Postion see more click", position + "");
        Intent intent = new Intent(this, CateGoryDetail.class);
        intent.putExtra(Constant.NUMBER_CATEGORY, position);
        Gson gson = new Gson();
        intent.putExtra(Constant.LIST_BG, gson.toJson(list));
        startActivity(intent);
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
            } else if (requestCode == Constant.REQUEST_CODE_IMAGES) {
                Uri uriData = data.getData();
                String path = FileUtils.getRealPathFromUri(this, uriData);
                resetListDataImage(path);
            }
        }
    }

    private void resetListDataVideo(String path) {
        Log.e("path", path);
        ArrayList<Background> listBgDb = (ArrayList<Background>) DataManager.query().getBackgroundDao().queryBuilder().list();
        if (path != null) {
            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND);
            File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    + "/ColorCall/Video/thums");
            if (!folder.exists())
                folder.mkdirs();
            Background video;
            String imageUrl = "";
            if (listBgDb != null) {
                imageUrl = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        + "/ColorCall/Video/thums/" + "thumb_" + listBgDb.size();
            }
            video = new Background(0, imageUrl, path, true);
            FileUtils.saveBitmap(imageUrl, bitmap);
            DataManager.query().getBackgroundDao().save(video);
        }
    }

    private void resetListDataImage(String path) {
        Log.e("pathImage", path);
        if (path != null) {
            File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    + "/ColorCall/Images");
            if (!folder.exists())
                folder.mkdirs();

            File file = new File(path);

            if (file.exists()) {
                Background picture = new Background(1, file.getAbsolutePath(), file.getAbsolutePath(), true);
//                if (listCategory.size() > 3) {
//                    listCategory.get(2).getListFile().add(picture);
//                }
                DataManager.query().getBackgroundDao().save(picture);
             //   initAdapterCategory();
                Log.e("List after save picture", DataManager.query().getBackgroundDao().queryBuilder().list() + "");

            } else {
                Toast.makeText(getBaseContext(), "File picture not found", Toast.LENGTH_LONG).show();
            }
        }
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

    @OnClick({R.id.btnGift, R.id.btnCamera})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnGift:
                break;
            case R.id.btnCamera:
                checkPermissionActionCamera();
                break;
        }
    }
}
