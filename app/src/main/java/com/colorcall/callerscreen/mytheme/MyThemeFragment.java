package com.colorcall.callerscreen.mytheme;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.colorcall.callerscreen.constan.Constant.SHOW_IMG_DELETE;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
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
import com.colorcall.callerscreen.utils.FileUtils;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MyThemeFragment extends Fragment implements MyThemeAdapter.Listener, DialogGalleryListener {
    @BindView(R.id.rcvBgYourTheme)
    RecyclerView rcvBgMyTheme;
    MyThemeAdapter adapter;
    private Analystic analystic;
    private String pathUriImage;
    private int positionItemThemeSelected = -1;
    private boolean isResultVideo;
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
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
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
                if(newState==0){
                    adapter.reload();
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
        Objects.requireNonNull(getActivity()).startActivity(intent);
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
                Log.e("TAN", "onActivityResult: video");
                Uri uriData = data.getData();
                final String[] mPath = new String[1];
                if (uriData != null) {
                    isResultVideo = true;
                    new Handler().postDelayed(() -> isResultVideo = false,500);
                    mPath[0] = FileUtils.getRealPathFromUri(getContext(), uriData);
                    Log.e("TAN", "mPath[0]: "+mPath[0]);
                    if(mPath[0].equals("")){
                        createVideoInputPath(requireActivity(), uriData, false,videoInputPath -> {
                            Log.e("TAN", "createVideoInputPath: "+videoInputPath);
                            mPath[0] = videoInputPath;
                            resetListDataVideo(mPath[0]);
                            adapter.setNewListBg();
                            adapter.notifyDataSetChanged();
                        });
                    }else{
                        resetListDataVideo(mPath[0]);
                        adapter.setNewListBg();
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(requireActivity(), "Error! Please try input other video!", Toast.LENGTH_LONG).show();
                }
            } else if (requestCode == Constant.REQUEST_CODE_IMAGES) {
                isResultVideo = true;
                new Handler().postDelayed(() -> isResultVideo = false,500);
                final String[] path = new String[1];
                if (data != null && data.getData() != null) {
                    path[0] = FileUtils.getRealPathFromUri(getContext(), data.getData());
                    if(path[0].equals("")){
                        FileUtils.createImagefromPath(requireActivity(),data.getData(),Constant.IMAGE_INPUT_NAME, new FileUtils.CreateImageInputInterface(){

                            @Override
                            public void onImageCreateSuccess(String imagePath) {
                                path[0] = imagePath;
                                resetListDataImage(path[0]);
                                adapter.setNewListBg();
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onImageCreateFailed() {

                            }
                        });
                    }else{
                        resetListDataVideo(path[0]);
                        adapter.setNewListBg();
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    path[0] = pathUriImage;
                    resetListDataImage(path[0]);
                    adapter.setNewListBg();
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }
    public static void createVideoInputPath(Context context, Uri videoUri, boolean isShowDialog, VideoInputListener listener) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getString(R.string.loading));
        progressDialog.setCancelable(false);
        if (isShowDialog)
            progressDialog.show();
        Handler handler = new Handler();
        Thread thread = new Thread(() -> {
            File inputFile = new File(FileUtils.getInternalFileDir(context), Constant.VIDEO_INPUT_NAME);
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(videoUri);
                OutputStream outputStream = new FileOutputStream(inputFile);
                byte[] buf = new byte[2048];
                int length;
                while ((length = inputStream.read(buf)) > 0) {
                    outputStream.write(buf, 0, length);
                }
                outputStream.close();
                inputStream.close();
                handler.post(() -> {
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    listener.onVideoCreateSuccess(inputFile.getAbsolutePath());
                });
            } catch (IOException e) {
                handler.post(() -> {
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    listener.onVideoCreateSuccess("");
                });
            }
        });
        thread.start();
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
            Log.e("TAN", "resetListDataImage: "+path);
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
        EventBus.getDefault().unregister(this);
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
    public boolean getResultVideoImage(){
        return isResultVideo;
    }
}