package com.colorcall.callerscreen.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.analystic.Analystic;
import com.colorcall.callerscreen.analystic.ManagerEvent;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.database.Background;
import com.colorcall.callerscreen.listener.DialogDeleteListener;
import com.colorcall.callerscreen.listener.DialogGalleryListener;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static com.colorcall.callerscreen.utils.FileUtils.createImageFile;

public class AppUtils {
    public static boolean checkPermission(Context context, String[] permission) {
        for (String checkSelfPermission : permission) {
            if (ContextCompat.checkSelfPermission(context, checkSelfPermission) != 0) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        return cm.getActiveNetworkInfo() != null;
    }

    public static boolean checkPermissionGrand(int[] grantResults) {
        boolean passed = true;
        for (int i : grantResults) {
            if (i != 0) {
                passed = false;
            }
        }
        return passed;
    }

    public static boolean canDrawOverlays(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;
        else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            return Settings.canDrawOverlays(context);
        } else {
            if (Settings.canDrawOverlays(context)) return true;
            try {
                WindowManager mgr = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                if (mgr == null) return false; //getSystemService might return null
                View viewToAdd = new View(context);
                WindowManager.LayoutParams params = new WindowManager.LayoutParams(0, 0, android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSPARENT);
                viewToAdd.setLayoutParams(params);
                mgr.addView(viewToAdd, params);
                mgr.removeView(viewToAdd);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    public static void showDrawOverlayPermissionDialog(Context context) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(context.getString(R.string.overlay_permision));
        alertDialog.setMessage(context.getString(R.string.overlay_permision_content));
        alertDialog.setCancelable(false);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, context.getString(R.string.ok),
                (dialog, which) -> {
                    Intent intent = null;
                    if (Build.VERSION.SDK_INT >= 23) {
                        intent = new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION", Uri.parse("package:" + context.getPackageName()));
                    }
                    ((Activity) context).startActivityForResult(intent, Constant.REQUEST_OVERLAY);
                    dialog.dismiss();
                });
        alertDialog.show();
    }

    public static boolean checkNotificationAccessSettings(Context context) {
        String string = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
        if (TextUtils.isEmpty(string)) {
            return false;
        }
        for (String unflattenFromString : string.split(":")) {
            ComponentName unflattenFromString2 = ComponentName.unflattenFromString(unflattenFromString);
            if (unflattenFromString2 != null && TextUtils.equals(context.getPackageName(), unflattenFromString2.getPackageName())) {
                return true;
            }
        }
        return false;
    }
    
    public static void showNotificationAccess(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getString(R.string.turn_on_notifi))
                .setNegativeButton(R.string.ok, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    ((Activity) context).startActivityForResult(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS), Constant.REQUEST_NOTIFICATION_ACCESS);
                });
        builder.setCancelable(false);
        AlertDialog getNotifiAcessDialog = builder.create();
        getNotifiAcessDialog.show();
    }

    public static int dpToPx(int dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }


    public static void showDialogMyGallery(Activity activity, Analystic analystic, DialogGalleryListener dialogGalleryListener) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_request_gallery);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        final ImageView imgVideo, imgImages;
        imgVideo = dialog.findViewById(R.id.imgSelectVideo);
        imgImages = dialog.findViewById(R.id.imgSelectImage);
        dialog.show();
        analystic.trackEvent(ManagerEvent.mainDialogOpen());
        imgVideo.setOnClickListener(v -> {
            if (dialogGalleryListener != null) {
                dialogGalleryListener.onVideoClicked();
                analystic.trackEvent(ManagerEvent.mainDialogVideo());
            }
            dialog.dismiss();
        });
        imgImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialogGalleryListener != null) {
                    dialogGalleryListener.onImagesClicked();
                    analystic.trackEvent(ManagerEvent.mainDialogPicture());
                }
                dialog.dismiss();
            }
        });
    }

    public static void showDialogMyGallery(Activity activity, DialogGalleryListener dialogGalleryListener) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_request_gallery);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        final ImageView imgVideo, imgImages;
        imgVideo = dialog.findViewById(R.id.imgSelectVideo);
        imgImages = dialog.findViewById(R.id.imgSelectImage);
        dialog.show();
        imgVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialogGalleryListener != null) {
                    dialogGalleryListener.onVideoClicked();
                }
                dialog.dismiss();
            }
        });
        imgImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialogGalleryListener != null) {
                    dialogGalleryListener.onImagesClicked();
                }
                dialog.dismiss();
            }
        });
    }

    public static void showDialogDelete(Activity activity, DialogDeleteListener dialogDeleteListener) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.dialog_delete, null);
        final TextView txtNo, txtYes;
        txtNo = alertLayout.findViewById(R.id.btnNo);
        txtYes = alertLayout.findViewById(R.id.btnYes);

        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setView(alertLayout);
        alert.setCancelable(false);
        AlertDialog dialog = alert.create();
        dialog.show();
        txtNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        txtYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialogDeleteListener != null) {
                    dialogDeleteListener.onDelete();
                }
                dialog.dismiss();
            }
        });
    }

    public static void checkDrawOverlayApp(Context context) {
        if (Build.VERSION.SDK_INT >= 23 && !AppUtils.canDrawOverlays(context)) {
            AppUtils.showDrawOverlayPermissionDialog(context);
        }
    }

    public static boolean checkDrawOverlay(Context context) {
        return Build.VERSION.SDK_INT < 23 || AppUtils.canDrawOverlays(context);
    }

    public static void populateUnifiedNativeAdView(UnifiedNativeAd nativeAd, UnifiedNativeAdView adView) {
        // Set the media view.
        adView.setMediaView((MediaView) adView.findViewById(R.id.ad_media));

        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        // The headline and mediaContent are guaranteed to be in every UnifiedNativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        adView.getMediaView().setMediaContent(nativeAd.getMediaContent());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd);

        // Get the video controller for the ad. One will always be provided, even if the ad doesn't
        // have a video asset.
    }

    public static Bitmap getContactPhoto(Context context, String number) {
        Bitmap photo = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.user);
        if (!number.equals("")) {
            ContentResolver contentResolver = context.getContentResolver();
            String contactId = null;
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

            String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID};

            Cursor cursor =
                    contentResolver.query(
                            uri,
                            projection,
                            null,
                            null,
                            null);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    contactId = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
                }
                cursor.close();
            }


            try {
                if (contactId != null) {
                    InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(),
                            ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.valueOf(contactId)), true);

                    if (inputStream != null) {
                        photo = BitmapFactory.decodeStream(inputStream);
                    }

                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return photo;
    }

    public static String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = "";
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return contactName;
    }

    public static void showFullHeader(Context context, View toolBar) {
        int statusBarHeight;
        RelativeLayout.LayoutParams layoutParams =
                (RelativeLayout.LayoutParams) toolBar.getLayoutParams();

        if (getStatusBarHeight(context) > 0) {
            statusBarHeight = getStatusBarHeight(context);
        } else {
            statusBarHeight = layoutParams.height / 3;
        }
        layoutParams.height = layoutParams.height + statusBarHeight;
        toolBar.setLayoutParams(layoutParams);
        toolBar.setPadding(toolBar.getPaddingLeft(), statusBarHeight, toolBar.getPaddingRight(), toolBar.getPaddingBottom());
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static String openCameraIntent(Fragment fragment, Activity activity, int requestCode) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        photoPickerIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*"});
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        String pickTitle = activity.getResources().getString(R.string.select_picture);
        Intent chooserIntent = Intent.createChooser(photoPickerIntent, pickTitle);
        chooserIntent.putExtra
                (Intent.EXTRA_INITIAL_INTENTS, new Intent[]{takePhotoIntent});
        if (takePhotoIntent.resolveActivity(activity.getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile(activity);
            } catch (IOException ex)
            {
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(activity, activity.getPackageName() + Constant.PROVIDER, photoFile);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            photoURI);
                }
                fragment.startActivityForResult(chooserIntent, requestCode);
                return photoFile.getAbsolutePath();
            } else return null;
        } else return null;
    }
    public static ArrayList<Background> loadDataDefault(Context context, String path) {
        ArrayList<Background> listBackground = new ArrayList<>();
        String pathThumb, pathFile;
        int type=0;
        String prefixVideo = "/raw/";
        Background background;
        try {
            String[] pathFiles = context.getAssets().list(path);
            for (int i = 0; i < pathFiles.length; i++) {
                pathThumb = path + "/" + pathFiles[i];
                if(i>3){
                    type=1;
                    pathFile =pathThumb;
                }else{
                    type=0;
                    pathFile = prefixVideo + pathFiles[i].substring(0, pathFiles[i].length() - 5);
                }
                background = new Background(type, pathThumb, pathFile, false,"default"+(i+1),i);
                listBackground.add(background);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return listBackground;
    }
    public static void createFolder(String folderApp) {
        File file = new File(folderApp);
        if (!file.exists()) {
            file.mkdir();
        }
    }
    public static void hideSoftKeyboard(Activity activity) {
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (activity.getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }
}
