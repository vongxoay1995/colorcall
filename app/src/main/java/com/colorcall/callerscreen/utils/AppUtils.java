package com.colorcall.callerscreen.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.analystic.FirebaseAnalystic;
import com.colorcall.callerscreen.analystic.ManagerEvent;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.listener.DialogDeleteListener;
import com.colorcall.callerscreen.listener.DialogGalleryListener;
import com.colorcall.callerscreen.model.Category;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;

import java.util.ArrayList;
import java.util.Locale;

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
                    ((Activity)context).startActivityForResult(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS), Constant.REQUEST_NOTIFICATION_ACCESS);
                });
        builder.setCancelable(false);
        AlertDialog getNotifiAcessDialog = builder.create();
        getNotifiAcessDialog.show();
    }

    public static int dpToPx(int dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    public static ArrayList<Category> loadDataFirst(Context context) {
        ArrayList<Category> list = new ArrayList<>();
        return list;
    }
    public static void showDialogMyGallery(Activity activity, FirebaseAnalystic firebaseAnalystic, DialogGalleryListener dialogGalleryListener){
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_request_gallery);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        final ImageView imgVideo, imgImages;
        imgVideo = dialog.findViewById(R.id.imgSelectVideo);
        imgImages = dialog.findViewById(R.id.imgSelectImage);
        dialog.show();
        firebaseAnalystic.trackEvent(ManagerEvent.mainDialogOpen());
        imgVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dialogGalleryListener!=null){
                    dialogGalleryListener.onVideoClicked();
                    firebaseAnalystic.trackEvent(ManagerEvent.mainDialogVideo());
                }
                dialog.dismiss();
            }
        });
        imgImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dialogGalleryListener!=null){
                    dialogGalleryListener.onImagesClicked();
                    firebaseAnalystic.trackEvent(ManagerEvent.mainDialogPicture());
                }
                dialog.dismiss();
            }
        });
    }
    public static void showDialogMyGallery(Activity activity, DialogGalleryListener dialogGalleryListener){
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
                if(dialogGalleryListener!=null){
                    dialogGalleryListener.onVideoClicked();
                }
                dialog.dismiss();
            }
        });
        imgImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dialogGalleryListener!=null){
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
                if(dialogDeleteListener!=null){
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
    public static String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = "";
        if(cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if(cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return contactName;
    }
    public static void showFullHeader(Context context, View toolBar){
        int statusBarHeight;
        RelativeLayout.LayoutParams layoutParams =
                (RelativeLayout.LayoutParams) toolBar.getLayoutParams();

        if(getStatusBarHeight(context)>0){
            statusBarHeight = getStatusBarHeight(context);
        }
        else {
            statusBarHeight = layoutParams.height/3;
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
}
