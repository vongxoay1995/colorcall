package com.colorcall.callerscreen.contact;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.analystic.Analystic;
import com.colorcall.callerscreen.analystic.ManagerEvent;
import com.colorcall.callerscreen.application.ColorCallApplication;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.database.Background;
import com.colorcall.callerscreen.database.Contact;
import com.colorcall.callerscreen.database.ContactDao;
import com.colorcall.callerscreen.database.DataManager;
import com.colorcall.callerscreen.service.PhoneService;
import com.colorcall.callerscreen.utils.AppOpenManager;
import com.colorcall.callerscreen.utils.AppUtils;
import com.colorcall.callerscreen.utils.HawkHelper;
import com.colorcall.callerscreen.utils.PermistionCallListener;
import com.colorcall.callerscreen.utils.PermistionUtils;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.gson.Gson;

import org.greenrobot.greendao.query.DeleteQuery;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SelectContactActivity extends AppCompatActivity implements PermistionCallListener, AppOpenManager.AppOpenManagerObserver {
    @BindView(R.id.layout_head)
    RelativeLayout layoutHead;
    @BindView(R.id.btnBack)
    ImageView btnBack;
    @BindView(R.id.rcvContact)
    RecyclerView rcvContact;
    @BindView(R.id.layoutSet)
    RelativeLayout layoutSet;
    @BindView(R.id.header_1)
    RelativeLayout header_1;
    @BindView(R.id.header_2)
    RelativeLayout header_2;
    @BindView(R.id.edtSearch)
    EditText edtSearch;
    @BindView(R.id.imgClear)
    ImageView imgClear;
    @BindView(R.id.imgBG)
    ImageView imgBG;
    private boolean isSearchShow;
    private ContactAdapter adapter;
    private Background background;
    private Analystic analystic;
    private AppOpenManager appOpenManager;
    private boolean isShowAdsOpen = true;

    @Override
    public void onHasCallPermistion() {
        setTheme();
    }

    public void setTranslucent() {
        Window w = getWindow();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            AppUtils.showFullHeader(this, layoutHead);
        }
    }

    public class EditTextListener implements TextWatcher {
        public EditTextListener() {
        }

        public void afterTextChanged(Editable editable) {

        }

        public void beforeTextChanged(CharSequence charSequence, int start, int before, int after) {
        }

        public void onTextChanged(CharSequence charSequence, int start, int before, int after) {
            imgClear.setVisibility(TextUtils.isEmpty(charSequence) ? View.GONE : View.VISIBLE);
            if (adapter != null) {
                adapter.search(charSequence);
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_contact);
        ButterKnife.bind(this);
        AppUtils.changeStatusBarColor(this, R.color.color_1E1E1E);
        init();
        //setTranslucent();
        analystic = Analystic.getInstance(this);
        analystic.trackEvent(ManagerEvent.contactOpen());
        appOpenManager = ((ColorCallApplication) getApplication()).getAppOpenManager();

    }

    public void showSearch() {
        edtSearch.setFocusable(true);
        edtSearch.setFocusableInTouchMode(true);
        edtSearch.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager.isActive()) {
            inputMethodManager.showSoftInput(edtSearch, 0);
        }
    }

    private void init() {
        Gson gson = new Gson();
        background = gson.fromJson(getIntent().getStringExtra(Constant.BACKGROUND), Background.class);
        String pathFile;
        if (!background.getPathThumb().equals("")) {
            if (background.getPathItem().contains("default")) {
                pathFile = "file:///android_asset/" + background.getPathThumb();
            } else {
                pathFile = background.getPathThumb();
            }
            Glide.with(getApplicationContext())
                    .load(pathFile)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .thumbnail(0.1f)
                    .into(imgBG);
        }
        getAllContact();
        edtSearch.addTextChangedListener(new EditTextListener());
    }

    @OnClick({R.id.btnBack, R.id.imgSearch, R.id.imgClear, R.id.layoutSet})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                onBackPressed();
                break;
            case R.id.imgSearch:
                header_2.setVisibility(View.VISIBLE);
                header_1.setVisibility(View.GONE);
                isSearchShow = true;
                showSearch();
                analystic.trackEvent(ManagerEvent.contactSearch());
                break;
            case R.id.layoutSet:
                analystic.trackEvent(ManagerEvent.contactSet());
                PermistionUtils.checkPermissionCall(this, this);
                break;
            case R.id.imgClear:
                edtSearch.setText("");
                imgClear.setVisibility(View.GONE);
                break;
            default:
                return;
        }
    }

    @Override
    public void onBackPressed() {
        if (isSearchShow) {
            isSearchShow = false;
            header_1.setVisibility(View.VISIBLE);
            header_2.setVisibility(View.GONE);
            edtSearch.setText("");
            AppUtils.hideKeyboard(edtSearch);
        } else {
            super.onBackPressed();
        }

    }

    public final void getAllContact() {
        LinkedHashSet linkedHashSet = new LinkedHashSet();
        try {
            ContentResolver contentResolver = getContentResolver();
            Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            String[] infors = {"contact_id", "display_name", "data1", "photo_uri"};
            Cursor query = contentResolver.query(uri, infors, null, null, "sort_key");
            if (query != null) {
                while (query.moveToNext()) {
                    @SuppressLint("Range") String contact_id = query.getString(query.getColumnIndex(infors[0]));
                    @SuppressLint("Range") String display_name = query.getString(query.getColumnIndex(infors[1]));
                    @SuppressLint("Range") String data1 = query.getString(query.getColumnIndex(infors[2]));
                    @SuppressLint("Range") String photo_uri = query.getString(query.getColumnIndex(infors[3]));
                    if (!linkedHashSet.contains(new ContactInfor(contact_id, display_name, data1, photo_uri))) {
                        linkedHashSet.add(new ContactInfor(contact_id, display_name, data1, photo_uri));
                    }
                }
                query.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ArrayList arrListContact = new ArrayList(linkedHashSet);
        List<Contact> listContactDB = DataManager.query().getContactDao().queryBuilder()
                .where(ContactDao.Properties.Background_path.eq(background.getPathItem()))
                .list();
        Log.e("TAN", "getAllContactDatabase: " + DataManager.query().getContactDao().queryBuilder().list());
        for (int i = 0; i < listContactDB.size(); i++) {
            Iterator it = arrListContact.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                ContactInfor contactInfor = (ContactInfor) it.next();
                if (contactInfor.getContactId().equals(listContactDB.get(i).getContact_id())) {
                    contactInfor.setChecked(true);
                    break;
                }
            }
        }
        adapter = new ContactAdapter(this, arrListContact);
        rcvContact.setAdapter(adapter);
    }

    public void setTheme() {
        PhoneService.startService(this);
        HawkHelper.setStateColorCall(true);
        if (adapter != null) {
            List<String> listContactIdSelected = adapter.getContactSelected();
            List<Contact> listContactDB = DataManager.query().getContactDao().queryBuilder()
                    .where(ContactDao.Properties.Background_path.eq(background.getPathItem()))
                    .list();
            for (int i = 0; i < listContactDB.size(); i++) {
                String contactSelect = listContactDB.get(i).getContact_id();
                if (!listContactIdSelected.contains(contactSelect)) {
                    final DeleteQuery<Contact> tableDeleteQuery = DataManager.query().getContactDao().queryBuilder().where(ContactDao.Properties.Contact_id.eq(contactSelect))
                            .buildDelete();
                    tableDeleteQuery.executeDeleteWithoutDetachingEntities();
                    DataManager.query().getContactDao().detachAll();
                }
            }
            Iterator<String> it = listContactIdSelected.iterator();
            Contact contact;
            while (it.hasNext()) {
                String contactID = it.next();
                List<Contact> listQueryContactID = DataManager.query().getContactDao().queryBuilder()
                        .where(ContactDao.Properties.Contact_id.eq(contactID))
                        .list();
                if (listQueryContactID.size() > 0) {
                    contact = listQueryContactID.get(0);
                    contact.setBackground_path(background.getPathItem());
                    contact.setBackground(new Gson().toJson(background));
                    DataManager.query().getContactDao().update(contact);
                } else {
                    contact = new Contact(contactID, background.getPathItem(), new Gson().toJson(background));
                    DataManager.query().getContactDao().insert(contact);
                }
            }
            Toast.makeText(this, getString(R.string.set_theme_success), Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.REQUEST_OVERLAY) {
            if (AppUtils.checkDrawOverlayApp2(this)) {
                if (!AppUtils.checkNotificationAccessSettings(this)) {
                    AppUtils.showNotificationAccess(this);
                    isShowAdsOpen = false;
                }
            } else {
                isShowAdsOpen = false;
                new Handler().postDelayed(() -> isShowAdsOpen = true, 500);
            }
        } else if (requestCode == Constant.REQUEST_NOTIFICATION_ACCESS) {
            if (AppUtils.checkNotificationAccessSettings(this)) {
                isShowAdsOpen = false;
                setTheme();
            } else {
                isShowAdsOpen = false;
                new Handler().postDelayed(() -> isShowAdsOpen = true, 500);
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constant.PERMISSION_REQUEST_CODE_CALL_PHONE && grantResults.length > 0 && AppUtils.checkPermissionGrand(grantResults)) {
            if (AppUtils.checkDrawOverlayApp2(this)) {
                if (!AppUtils.checkNotificationAccessSettings(this)) {
                    AppUtils.showNotificationAccess(this);
                }
            } else {
                AppUtils.showDrawOverlayApp(this);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        appOpenManager.registerObserver(this);
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        appOpenManager.unregisterObserver();
    }

    @Override
    public void lifecycleStart(@NonNull AppOpenAd appOpenAd, @NonNull AppOpenManager appOpenManager) {
        if (hasActive() && isShowAdsOpen) {
            appOpenAd.show(this);
        }
    }

    @Override
    public void lifecycleShowAd() {

    }

    @Override
    public void lifecycleStop() {

    }

    private boolean hasActive() {
        return !isFinishing() && !isDestroyed();
    }
}