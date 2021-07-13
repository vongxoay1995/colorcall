package com.colorcall.callerscreen.mytheme;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.analystic.Analystic;
import com.colorcall.callerscreen.apply.ApplyActivity;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.database.Background;
import com.colorcall.callerscreen.database.DataManager;
import com.colorcall.callerscreen.listener.DialogGalleryListener;
import com.colorcall.callerscreen.main.SimpleDividerItemDecoration;
import com.colorcall.callerscreen.model.SignApplyMyTheme;
import com.colorcall.callerscreen.utils.AppUtils;
import com.colorcall.callerscreen.utils.Boast;
import com.colorcall.callerscreen.utils.FileUtils;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
    private Analystic analystic;
    private String pathUriImage;
    private int positionItemThemeSelected = -1;
    public MyThemeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_theme, container, false);
        ButterKnife.bind(this, view);
        if (savedInstanceState != null) {
          pathUriImage   = savedInstanceState.getString(Constant.CAPTURE_IMAGE_PATH);
        }
        init();
        return view;
    }

    private void init() {
        analystic = Analystic.getInstance(getContext());
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false);
        rcvBgMyTheme.setLayoutManager(gridLayoutManager);
        rcvBgMyTheme.setItemAnimator(new DefaultItemAnimator());
        rcvBgMyTheme.addItemDecoration(new SimpleDividerItemDecoration(AppUtils.dpToPx(5)));
        RecyclerView.ItemAnimator animator = rcvBgMyTheme.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        adapter = new MyThemeAdapter(getContext());
        adapter.setListener(this);
        rcvBgMyTheme.setAdapter(adapter);
        rcvBgMyTheme.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

    @Override
    public void onAdd() {
        checkPermissionActionCamera();
    }

    @Override
    public void onItemClick(ArrayList<Background> backgrounds, int position, boolean delete,int posRandom) {
        moveApplyTheme(backgrounds, position, delete,posRandom);
    }
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Constant.CAPTURE_IMAGE_PATH, pathUriImage);
    }
    private void moveApplyTheme(ArrayList<Background> backgrounds, int position, boolean delete,int posRandom) {
        Background background = backgrounds.get(position);
        Intent intent = new Intent(getActivity(), ApplyActivity.class);
        intent.putExtra(Constant.FROM_SCREEN, Constant.MYTHEME_FRAG_MENT);
        if (delete) {
            intent.putExtra(SHOW_IMG_DELETE, true);
        }
        Gson gson = new Gson();
        intent.putExtra(Constant.BACKGROUND, gson.toJson(background));
        intent.putExtra(Constant.POS_RANDOM, posRandom);
        getActivity().startActivity(intent);
    }

    public void checkPermissionActionCamera() {
        String[] permistion;
        if(Build.VERSION.SDK_INT <=28){
            permistion = new String[]{
                    READ_EXTERNAL_STORAGE,
                    WRITE_EXTERNAL_STORAGE,
                    CAMERA
            };
        }else {
            permistion = new String[]{
                    READ_EXTERNAL_STORAGE,
                    CAMERA
            };
        }

        if (!AppUtils.checkPermission(getContext(), permistion)) {
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
        AppUtils.showDialogMyGallery(getActivity(), analystic, this);
    }

    @Override
    public void onVideoClicked() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        photoPickerIntent.setType("video/*");
        photoPickerIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
        Intent takePhotoIntent = new Intent("android.media.action.VIDEO_CAPTURE");
        Intent chooserIntent = Intent.createChooser(photoPickerIntent, getResources().getString(R.string.your_video));
        chooserIntent.putExtra("android.intent.extra.INITIAL_INTENTS", new Intent[]{takePhotoIntent});
        startActivityForResult(chooserIntent, Constant.REQUEST_VIDEO);
    }

    @Override
    public void onImagesClicked() {
        pathUriImage = AppUtils.openCameraIntent(this, getActivity(), Constant.REQUEST_CODE_IMAGES);
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    imageUrl = getActivity().getFilesDir()
                            + Constant.PATH_THUMB_COLOR_CALL + "thumb_" + listBgDb.size();
                    video = new Background(0, imageUrl, path, true, path.substring(path.lastIndexOf("/") + 1));
                    FileUtils.saveBitmap(getActivity().getFilesDir()
                            + Constant.PATH_THUMB_COLOR_CALL,"thumb_" + listBgDb.size(), bitmap);
                }else {
                    imageUrl = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                            + Constant.PATH_THUMB_COLOR_CALL + "thumb_" + listBgDb.size();
                    video = new Background(0, imageUrl, path, true, path.substring(path.lastIndexOf("/") + 1));
                    FileUtils.saveBitmap(imageUrl, bitmap);
                }
                DataManager.query().getBackgroundDao().save(video);
            }
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
                Background picture = new Background(1, file.getAbsolutePath(), file.getAbsolutePath(), true,
                        file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("/") + 1));
                DataManager.query().getBackgroundDao().save(picture);
            } else {
                Toast.makeText(getContext(), getString(R.string.file_not_found), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSignApply(SignApplyMyTheme signApplyMyTheme) {
        switch (signApplyMyTheme.getAction()) {
            case Constant.INTENT_APPLY_THEME:
                adapter.notifyDataSetChanged();
                break;
            case Constant.INTENT_DELETE_THEME:
                adapter.setNewListBg();
                adapter.notifyDataSetChanged();
                break;
        }
        EventBus.getDefault().removeStickyEvent(signApplyMyTheme);
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
    public void onItemThemeSelected(int position) {
        positionItemThemeSelected = position;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(adapter!=null){
            adapter.notifyItemChanged(positionItemThemeSelected);
        }
    }
}