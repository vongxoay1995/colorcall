package com.htn.colorcall.categorydetail;

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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.htn.colorcall.R;
import com.htn.colorcall.apply.ApplyActivity;
import com.htn.colorcall.constan.Constant;
import com.htn.colorcall.database.DataManager;
import com.htn.colorcall.listener.DialogGalleryListener;
import com.htn.colorcall.main.SimpleDividerItemDecoration;
import com.htn.colorcall.model.Background;
import com.htn.colorcall.utils.AppUtils;
import com.htn.colorcall.utils.FileUtils;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.annotations.NonNull;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission_group.CAMERA;
import static com.htn.colorcall.constan.Constant.SHOW_IMG_DELETE;

public class CateGoryDetail extends AppCompatActivity implements CategoryDetailAdapter.Listener, DialogGalleryListener {
    @BindView(R.id.titleCategory)
    TextView titleCategory;
    @BindView(R.id.rcvThemes)
    RecyclerView rcvThemes;
    private int posTitle;
    private String title;
    private CategoryDetailAdapter adapter;
    private ArrayList<Background> list;
    private boolean isYourColor;
    private int positionSelectBg = -1;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cate_gory_detail);
        ButterKnife.bind(this);
        posTitle = getIntent().getIntExtra(Constant.NUMBER_CATEGORY, 0);
        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<Background>>() {
        }.getType();
        ArrayList<Background> list = gson.fromJson(getIntent().getStringExtra(Constant.LIST_BG), listType);
        this.list = list;
        setTitlewithPos(posTitle);
        initData();
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
        Log.e("AA", "!");
        AppUtils.showDialogMyGallery(this, this);
    }

    @Override
    public void onItemClick(ArrayList<Background> backgrounds, int position, boolean delete) {
        positionSelectBg = position;
        moveApplyTheme(backgrounds, position, delete);
    }

    private void moveApplyTheme(ArrayList<Background> backgrounds, int position, boolean delete) {
        Background background = backgrounds.get(position);
        Intent intent = new Intent(this, ApplyActivity.class);
        if (delete) {
            intent.putExtra(SHOW_IMG_DELETE, true);
        }
        Gson gson = new Gson();
        intent.putExtra(Constant.BACKGROUND, gson.toJson(background));
        startActivityForResult(intent,Constant.APPLY_REQUEST_CODE);
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
        Log.e("pathImage", path);
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
                Log.e("List after save picture", DataManager.query().getBackgroundDao().queryBuilder().list() + "");

            } else {
                Toast.makeText(getBaseContext(), "File picture not found", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        //khi back về mà xóa item chưa xử lý
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
            }
            else if(requestCode == Constant.APPLY_REQUEST_CODE){
                if (data!=null){
                    boolean isDelete = data.getBooleanExtra(Constant.IS_DELETE_BG,false);
                    if(isDelete&&positionSelectBg>-1){
                        list.remove(positionSelectBg);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }
    }

}
