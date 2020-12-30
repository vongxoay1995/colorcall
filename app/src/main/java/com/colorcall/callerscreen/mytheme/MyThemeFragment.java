package com.colorcall.callerscreen.mytheme;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.analystic.FirebaseAnalystic;
import com.colorcall.callerscreen.apply.ApplyActivity;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.database.Background;
import com.colorcall.callerscreen.database.DataManager;
import com.colorcall.callerscreen.listener.DialogGalleryListener;
import com.colorcall.callerscreen.main.SimpleDividerItemDecoration;
import com.colorcall.callerscreen.utils.AppUtils;
import com.colorcall.callerscreen.utils.FileUtils;
import com.colorcall.callerscreen.utils.HawkHelper;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.colorcall.callerscreen.constan.Constant.SHOW_IMG_DELETE;

public class MyThemeFragment extends Fragment implements MyThemeAdapter.Listener, DialogGalleryListener {
    @BindView(R.id.rcvBgYourTheme)
    RecyclerView rcvBgMyTheme;
    MyThemeAdapter adapter;
    private FirebaseAnalystic firebaseAnalystic;
    private String pathUriImage;
    private ArrayList<Background> listBg;
    LocalBroadcastManager mLocalBroadcastManager;
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Constant.ACTION_LOAD_COMPLETE_THEME:
                    init();
                    break;
                case Constant.INTENT_DELETE_THEME:
                    Log.e("TAN", "onReceive:INTENT_DELETE_THEME ");
                    adapter.setNewListBg();
                    adapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    public MyThemeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_theme, container, false);
        ButterKnife.bind(this, view);
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Constant.ACTION_LOAD_COMPLETE_THEME);
        mIntentFilter.addAction(Constant.INTENT_DELETE_THEME);
        mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, mIntentFilter);
        return view;
    }

    private void init() {
        firebaseAnalystic = FirebaseAnalystic.getInstance(getContext());
        listBg = HawkHelper.getListBackground();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false);
        rcvBgMyTheme.setLayoutManager(gridLayoutManager);
        rcvBgMyTheme.setItemAnimator(new DefaultItemAnimator());
        rcvBgMyTheme.addItemDecoration(new SimpleDividerItemDecoration(AppUtils.dpToPx(5)));
        adapter = new MyThemeAdapter(getContext(), listBg);
        adapter.setListener(this);
        rcvBgMyTheme.setAdapter(adapter);
    }

    @Override
    public void onAdd() {
        checkPermissionActionCamera();
    }

    @Override
    public void onItemClick(ArrayList<Background> backgrounds, int position, boolean delete) {
        moveApplyTheme(backgrounds, position, delete);
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

    public void checkPermissionActionCamera() {
        String[] permistion = {
                READ_EXTERNAL_STORAGE,
                WRITE_EXTERNAL_STORAGE,
                CAMERA
        };
        if (!AppUtils.checkPermission(getContext(), permistion)) {
            Log.e("TAN", "checkPermissionActionCamera: ");
            requestPermissions(permistion,
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
        AppUtils.showDialogMyGallery(getActivity(), firebaseAnalystic, this);
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
        pathUriImage = AppUtils.openCameraIntent(this, getActivity(), Constant.REQUEST_CODE_IMAGES);
        Log.e("TAN", "onImagesClicked: ");
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constant.REQUEST_VIDEO) {
                Uri uriData = data.getData();
                String path = FileUtils.getRealPathFromUri(getContext(), uriData);
                resetListDataVideo(path);
                adapter.setNewListBg();
                adapter.notifyDataSetChanged();
            } else if (requestCode == Constant.REQUEST_CODE_IMAGES) {
                String path;
                if (data != null && data.getData() != null) {
                    path = FileUtils.getRealPathFromUri(getContext(), data.getData());
                } else {
                    path = pathUriImage;
                }
                resetListDataImage(path);
                adapter.setNewListBg();
                adapter.notifyDataSetChanged();
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
            listBg = HawkHelper.getListBackground();
            video = new Background(0, imageUrl, path, true,"",listBg.size());
            FileUtils.saveBitmap(imageUrl, bitmap);
            DataManager.query().getBackgroundDao().save(video);
            listBg.add(video);
            HawkHelper.setListBackground(listBg);
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
                listBg = HawkHelper.getListBackground();
                Background picture = new Background(1, file.getAbsolutePath(), file.getAbsolutePath(), true,"",listBg.size());
                DataManager.query().getBackgroundDao().save(picture);
                listBg.add(picture);
                HawkHelper.setListBackground(listBg);
            } else {
                Toast.makeText(getContext(), "File picture not found", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        mLocalBroadcastManager.unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }
}