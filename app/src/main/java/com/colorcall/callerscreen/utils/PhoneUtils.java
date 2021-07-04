package com.colorcall.callerscreen.utils;

import android.app.Notification;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.colorcall.callerscreen.application.ColorCallApplication;
import com.colorcall.callerscreen.service.NotificationService;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

public class PhoneUtils {
    private static PhoneUtils phoneUtils;
    public Map<String, String> contacts = new HashMap();
    public PhoneListener listener;

    public interface PhoneListener {
        void getNumPhone(String str);
    }

    public static PhoneUtils get() {
        if (phoneUtils == null) {
            phoneUtils = new PhoneUtils();
        }
        return phoneUtils;
    }

    @RequiresApi(api = 28)
    public void getNumberPhoneWhenNull(PhoneListener phoneListener) {
        listener = phoneListener;
        new Thread() {
            public void run() {
                super.run();
                getListNameContact();
                NotificationService notificationReceiverService = NotificationService.get();
                Log.e("TAN", "notificationReceiverService run: "+notificationReceiverService);
                if (notificationReceiverService != null) {
                    StatusBarNotification inCallNotification = notificationReceiverService.getInCallNotification();
                    if (inCallNotification != null) {
                        if (System.currentTimeMillis() - inCallNotification.getPostTime() < 10000) {
                            String phoneNumber = getPhoneFromNotification(inCallNotification, contacts);
                            if (phoneNumber != null) {
                                if (listener != null) {
                                    listener.getNumPhone(phoneNumber);
                                }
                            } else if (listener != null) {
                                listener.getNumPhone("");
                            }

                        } else {
                            notificationReceiverService.startListenColorCall();
                        }
                    } else {
                        notificationReceiverService.startListenColorCall();
                    }
                }
            }
        }.start();
    }

    public void getListNameContact() {
        this.contacts.clear();
        try {
            Cursor query = ColorCallApplication.get().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, "upper(display_name) ASC");
            if (query != null && query.moveToFirst()) {
                do {
                    if (!query.getString(query.getColumnIndex(ContactsContract.PhoneLookup.CONTACT_ID)).equals("-1")) {
                        this.contacts.put(query.getString(query.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)), query.getString(query.getColumnIndex("data1")));
                    }
                } while (query.moveToNext());
                query.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getPhoneFromNotification(StatusBarNotification statusBarNotification, Map<String, String> map) {
        String string = statusBarNotification.getNotification().extras.getString(NotificationCompat.EXTRA_TEXT);
        String string2 = statusBarNotification.getNotification().extras.getString(NotificationCompat.EXTRA_TITLE);
        if (!TextUtils.isEmpty(string)) {
            if (map.containsKey(string)) {
                return map.get(string);
            }
            if (validateNumber(string)) {
                return string;
            }
        }
        if (!TextUtils.isEmpty(string2)) {
            if (map.containsKey(string2)) {
                return map.get(string2);
            }
            if (validateNumber(string2)) {
                return string2;
            }
        }
        Context createContext = createContext(ColorCallApplication.get(), statusBarNotification);
        if (createContext == null) {
            return null;
        }
        Notification notification = statusBarNotification.getNotification();
        RemoteViews remoteViews = notification.bigContentView == null ? notification.contentView : notification.bigContentView;
        if (remoteViews == null) {
            return null;
        }
        try {
            ViewGroup viewGroup = (ViewGroup) ((LayoutInflater) createContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(remoteViews.getLayoutId(), null);
            remoteViews.reapply(createContext, viewGroup);
            Iterator it = new RecursiveFinder(TextView.class).expand(viewGroup).iterator();
            while (it.hasNext()) {
                String charSequence = ((TextView) it.next()).getText().toString();
                if (!TextUtils.isEmpty(charSequence)) {
                    if (map.containsKey(charSequence)) {
                        return map.get(charSequence);
                    }
                    if (validateNumber(charSequence)) {
                        return charSequence;
                    }
                }
            }
            return null;
        } catch (Exception unused) {
            return null;
        }
    }

    private static class RecursiveFinder<T extends View> {
        private final Class<T> clazz;
        private final ArrayList<T> list = new ArrayList<>();

        public RecursiveFinder(@NonNull Class<T> cls) {
            this.clazz = cls;
        }

        public ArrayList<T> expand(@NonNull ViewGroup viewGroup) {
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childAt = viewGroup.getChildAt(i + 0);
                if (childAt != null) {
                    if (this.clazz.isAssignableFrom(childAt.getClass())) {
                        this.list.add((T) childAt);
                    } else if (childAt instanceof ViewGroup) {
                        expand((ViewGroup) childAt);
                    }
                }
            }
            return this.list;
        }
    }

    private boolean validateNumber(String num) {
        PhoneNumberUtil instance = PhoneNumberUtil.getInstance();
        try {
            boolean isValidNumber = instance.isValidNumber(instance.parse(num, "CH"));
            if (Pattern.compile("[0-9*#+() -]*").matcher(num).matches()) {
                return true;
            }
            return isValidNumber;
        } catch (Exception e) {
            PrintStream printStream = System.err;
            printStream.println("NumberParseException was thrown: " + e.toString());
            return false;
        }
    }

    private Context createContext(@NonNull Context context, @NonNull StatusBarNotification statusBarNotification) {
        try {
            return context.createPackageContext(statusBarNotification.getPackageName(), Context.CONTEXT_RESTRICTED);
        } catch (PackageManager.NameNotFoundException unused) {
            return null;
        }
    }
    public void stopFindOutgoingPhone() {
        listener = null;
        NotificationService notificationReceiverService = NotificationService.get();
        if (notificationReceiverService != null) {
            notificationReceiverService.stopListenColorCall();
        }
    }
    public void getPhoneFromNotificationListen(StatusBarNotification statusBarNotification) {
        Log.e("TAN", "getPhoneFromNotificationListen: "+statusBarNotification);
        String phoneFromNotification = getPhoneFromNotification(statusBarNotification, contacts);
        if (phoneFromNotification != null) {
            PhoneListener phoneListener = listener;
            if (phoneListener != null) {
                phoneListener.getNumPhone(phoneFromNotification);
                stopFindOutgoingPhone();
            }
        }
    }
}
