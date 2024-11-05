package com.colorcall.callerscreen.mytheme;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.Manifest.permission.READ_MEDIA_VIDEO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.colorcall.callerscreen.constan.Constant.SHOW_IMG_DELETE;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.analystic.Analystic;
import com.colorcall.callerscreen.apply.ApplyActivity;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.database.Background;
import com.colorcall.callerscreen.database.DatabaseViewModel;
import com.colorcall.callerscreen.databinding.FragmentMyThemeBinding;
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class MyThemeFragment extends Fragment implements MyThemeAdapter.Listener, DialogGalleryListener {
    MyThemeAdapter adapter;
    private Analystic analystic;
    private String pathUriImage;
    private int positionItemThemeSelected = -1;
    public boolean isRequestImageVideo;
    private FragmentMyThemeBinding binding;
    private DatabaseViewModel databaseViewModel;
    public ArrayList<Background> listBg;

    public MyThemeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMyThemeBinding.inflate(inflater, container, false);
        databaseViewModel = new ViewModelProvider(this).get(DatabaseViewModel.class);
        databaseViewModel.getAllBackgrounds().observe(requireActivity(), backgrounds -> {
            Log.e("TAN", "setNewListBg0000: " + backgrounds.size());
            listBg = (ArrayList<Background>) backgrounds;
            if (adapter==null){
                adapter = new MyThemeAdapter(getContext(),listBg);
                adapter.setListener(this);
                binding.rcvBgYourTheme.setAdapter(adapter);
            }
            if(actionResetData){
                adapter.setNewListBg(listBg);
                adapter.notifyDataSetChanged();
                actionResetData = false;
            }
        });
        if (savedInstanceState != null) {
            pathUriImage = savedInstanceState.getString(Constant.CAPTURE_IMAGE_PATH);
        }
        init();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        return binding.getRoot();
    }

    private void init() {
        analystic = Analystic.getInstance(getContext());
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false);
        binding.rcvBgYourTheme.setLayoutManager(gridLayoutManager);
        binding.rcvBgYourTheme.setItemAnimator(new DefaultItemAnimator());
        binding.rcvBgYourTheme.addItemDecoration(new SimpleDividerItemDecoration(AppUtils.dpToPx(5)));
        RecyclerView.ItemAnimator animator = binding.rcvBgYourTheme.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        binding.rcvBgYourTheme.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == 0) {
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
    public void onItemClick(ArrayList<Background> backgrounds, int position, boolean delete, int posRandom) {
        moveApplyTheme(backgrounds, position, delete, posRandom);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Constant.CAPTURE_IMAGE_PATH, pathUriImage);
    }

    private void moveApplyTheme(ArrayList<Background> backgrounds, int position, boolean delete, int posRandom) {
        Background background = backgrounds.get(position);
        Intent intent = new Intent(getActivity(), ApplyActivity.class);
        intent.putExtra(Constant.FROM_SCREEN, Constant.MYTHEME_FRAG_MENT);
        if (delete) {
            intent.putExtra(SHOW_IMG_DELETE, true);
        }
        Gson gson = new Gson();
        intent.putExtra(Constant.BACKGROUND, gson.toJson(background));
        intent.putExtra(Constant.POS_RANDOM, posRandom);
        requireActivity().startActivity(intent);
    }

    public void checkPermissionActionCamera() {
        String[] permistion;
        if (Build.VERSION.SDK_INT <= 28) {
            permistion = new String[]{
                    READ_EXTERNAL_STORAGE,
                    WRITE_EXTERNAL_STORAGE,
                    CAMERA
            };
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permistion = new String[]{
                    READ_MEDIA_VIDEO,
                    READ_MEDIA_IMAGES,
                    CAMERA
            };
        } else {
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
        isRequestImageVideo = true;
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
        isRequestImageVideo = true;
        pathUriImage = AppUtils.openCameraIntent(this, getActivity(), Constant.REQUEST_CODE_IMAGES);
    }
    boolean actionResetData = false;
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        isRequestImageVideo = false;
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constant.REQUEST_VIDEO) {
                Log.e("TAN", "onActivityResult: video");
                Uri uriData = data.getData();
                final String[] mPath = new String[1];
                if (uriData != null) {
                    mPath[0] = FileUtils.getRealPathFromUri(getContext(), uriData);
                    Log.e("TAN", "mPath[0]: " + mPath[0]);
                    if (mPath[0].equals("")) {
                        createVideoInputPath(requireActivity(), uriData, false, videoInputPath -> {
                            Log.e("TAN", "createVideoInputPath: " + videoInputPath);
                            mPath[0] = videoInputPath;
                            resetListDataVideo(mPath[0]);
                            //adapter.setNewListBg();
                            //adapter.notifyDataSetChanged();
                        });
                    } else {
                        resetListDataVideo(mPath[0]);
                        //adapter.setNewListBg();
                        //adapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(requireActivity(), "Error! Please try input other video!", Toast.LENGTH_LONG).show();
                }
            } else if (requestCode == Constant.REQUEST_CODE_IMAGES) {
                final String[] path = new String[1];
                if (data != null && data.getData() != null) {
                    path[0] = FileUtils.getRealPathFromUri(getContext(), data.getData());
                    if (path[0].equals("")) {
                        FileUtils.createImagefromPath(requireActivity(), data.getData(), Constant.IMAGE_INPUT_NAME, new FileUtils.CreateImageInputInterface() {

                            @Override
                            public void onImageCreateSuccess(String imagePath) {
                                path[0] = imagePath;
                                resetListDataImage(path[0]);
                               // adapter.setNewListBg();
                               // adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onImageCreateFailed() {

                            }
                        });
                    } else {
                        resetListDataImage(path[0]);
                        //adapter.setNewListBg();
                        //adapter.notifyDataSetChanged();
                    }
                } else {
                    path[0] = pathUriImage;
                    resetListDataImage(path[0]);
                    //adapter.setNewListBg();
                    //adapter.notifyDataSetChanged();
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
    public void saveVideoToDownloads(Context context,String thumb, String videoPath, String fileName) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName); // Tên tệp
        values.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4"); // Loại MIME
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MOVIES+ Constant.PATH_THUMB_COLOR_CALL_VIDEOS); // Thư mục Downloads
        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)+ Constant.PATH_THUMB_COLOR_CALL_VIDEOS+"/"+fileName;
        File file =  new File(filePath);
        Uri uri = context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            try {
                InputStream inputStream = new FileInputStream(new File(videoPath));
                OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
                if (outputStream != null) {
                    byte[] buffer = new byte[4096];
                    int read;
                    while ((read = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, read);
                    }
                    Background video = new Background(0, thumb, file.getAbsolutePath(), true, file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("/") + 1));
                    databaseViewModel.insertBackground(video);
                    actionResetData = true;
                    outputStream.flush();
                    outputStream.close();
                    inputStream.close();
                }
            } catch (FileNotFoundException e) {
                Toast.makeText(context, "File not exist: " + e.getMessage(), Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Toast.makeText(context, "Error when save video: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(context, "Not create Uri", Toast.LENGTH_LONG).show();
        }
    }
    private void resetListDataVideo(String path) {

        //ArrayList<Background> listBgDb = (ArrayList<Background>) DataManager.query().getBackgroundDao().queryBuilder().list();
        if (path != null) {
            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND);
            File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    + Constant.PATH_THUMB_COLOR_CALL);
            if (!folder.exists())
                folder.mkdirs();
            Background video;
            String imageUrl = "";
            if (listBg != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    File folderMovies = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
                            + Constant.PATH_THUMB_COLOR_CALL_VIDEOS);
                    if (!folder.exists())
                        folder.mkdirs();
                    imageUrl = getActivity().getFilesDir()
                            + Constant.PATH_THUMB_COLOR_CALL + "thumb_" + listBg.size();
                    video = new Background(0, imageUrl, path, true, path.substring(path.lastIndexOf("/") + 1));
                    FileUtils.saveBitmap(getActivity().getFilesDir()
                            + Constant.PATH_THUMB_COLOR_CALL,"thumb_" + listBg.size(), bitmap);
                    String filename =  "my_video_" + listBg.size()+".mp4";
                    saveVideoToDownloads(requireActivity(),imageUrl,path,filename);
                    Log.e("TAN", "resetListDataVideo: vvvv"+path);
                }else {
                    imageUrl = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                            + Constant.PATH_THUMB_COLOR_CALL + "thumb_" + listBg.size();
                    video = new Background(0, imageUrl, path, true, path.substring(path.lastIndexOf("/") + 1));
                    FileUtils.saveBitmap(imageUrl, bitmap);
                    databaseViewModel.insertBackground(video);
                    actionResetData = true;
                }

              //  DataManager.query().getBackgroundDao().save(video);
            }
        }
    }

    public Bitmap getBitmapFromPath(String filePath) {
        Bitmap bitmap = null;
        try {
            File imgFile = new File(filePath);
            if (imgFile.exists()) {
                bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            }
        } catch (Exception e) {
            Log.e("Image Loading", "Error in getting image from path: " + e.getMessage());
        }
        return bitmap;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void saveBitmapToDownloads(Context context, String filePath, String fileName) {
        Bitmap bitmap = getBitmapFromPath(filePath);
        if (bitmap != null) {
            saveImageToDownloads(context, bitmap, fileName);
        } else {
            Toast.makeText(context, "Bitmap is null, cannot save the image.", Toast.LENGTH_LONG).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void saveImageToDownloads(Context context, Bitmap bitmap, String fileName) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName); // Tên tệp
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg"); // Loại MIME
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS+ Constant.PATH_THUMB_COLOR_CALL_IMAGES); // Thư mục Downloads
        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+ Constant.PATH_THUMB_COLOR_CALL_IMAGES+"/"+fileName;
        File file =  new File(filePath);
        Uri uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            try {
                OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream); // Ghi Bitmap vào OutputStream
                    Log.e("TAN", "saveImageToDownloads: "+file.getAbsolutePath());
                    if (file.exists()){
                        Background picture = new Background(1, file.getAbsolutePath(), file.getAbsolutePath(), true,
                                file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("/") + 1));
                        actionResetData = true;
                        databaseViewModel.insertBackground(picture);
                    }
                    outputStream.close();
                }
            } catch (IOException e) {
                Toast.makeText(context, "Error when save image: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(context, "Error when save image", Toast.LENGTH_LONG).show();
        }
    }
    private void resetListDataImage(String path) {
        Log.e("TAN", "resetListDataImage: ");

        if (path != null) {
            File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    + Constant.PATH_THUMB_COLOR_CALL_IMAGES);
            Log.e("TAN", "resetListDataImage: "+path);
            if (!folder.exists())
                folder.mkdirs();

            File file = new File(path);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                String filename =  "my_image_" + listBg.size()+".jpg";
                saveBitmapToDownloads(requireActivity(),path, filename);
            }else {
                if (file.exists()) {
                    Background picture = new Background(1, file.getAbsolutePath(), file.getAbsolutePath(), true,
                            file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("/") + 1));
                    actionResetData = true;
                    databaseViewModel.insertBackground(picture);
                } else {
                    Toast.makeText(getContext(), getString(R.string.file_not_found), Toast.LENGTH_LONG).show();
                }
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
            Log.e("TAN", "onSignApply: delete");
            actionResetData = true;
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
    if (adapter != null) {
        adapter.notifyItemChanged(positionItemThemeSelected);
    }
}
}