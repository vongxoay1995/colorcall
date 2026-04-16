package com.colorcall.callerscreen.contact;

import static com.colorcall.callerscreen.constan.Constant.REQUEST_CODE_SET_DEFAULT_DIALER;
import static com.colorcall.callerscreen.utils.AppUtils.isDefaultDialer;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.analystic.Analystic;
import com.colorcall.callerscreen.analystic.ManagerEvent;
import com.colorcall.callerscreen.application.ColorCallApplication;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.database.Background;
import com.colorcall.callerscreen.database.Contact;
import com.colorcall.callerscreen.database.DatabaseViewModel;
import com.colorcall.callerscreen.databinding.ActivitySelectContactBinding;
import com.colorcall.callerscreen.utils.AppOpenManager;
import com.colorcall.callerscreen.utils.AppUtils;
import com.colorcall.callerscreen.utils.HawkHelper;
import com.colorcall.callerscreen.utils.PermistionCallListener;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class SelectContactActivity extends AppCompatActivity implements PermistionCallListener, AppOpenManager.AppOpenManagerObserver {
    private boolean isSearchShow;
    private ContactAdapter adapter;
    private Background background;
    private Analystic analystic;
    private AppOpenManager appOpenManager;
    private DatabaseViewModel databaseViewModel;
    private ActivitySelectContactBinding binding;

    @Override
    public void onHasCallPermistion() {
        Log.e("TAN", "onHasCallPermistion: 111");
        setTheme();
    }

    public void setTranslucent() {
        Window w = getWindow();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            AppUtils.showFullHeader(this, binding.layoutHead);
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
            binding.imgClear.setVisibility(TextUtils.isEmpty(charSequence) ? View.GONE : View.VISIBLE);
            if (adapter != null) {
                adapter.search(charSequence);
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView())
                .setAppearanceLightStatusBars(false); // white icons on dark header
        binding = ActivitySelectContactBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        databaseViewModel = new ViewModelProvider(this).get(DatabaseViewModel.class);
        // Set topView height = status bar height để header không bị che
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            android.view.ViewGroup.LayoutParams lp = binding.topView.getLayoutParams();
            lp.height = statusBarHeight;
            binding.topView.setLayoutParams(lp);
            return insets;
        });
        init();
        //setTranslucent();
        analystic = Analystic.getInstance(this);
        analystic.trackEvent(ManagerEvent.contactOpen());
        appOpenManager = ((ColorCallApplication) getApplication()).getAppOpenManager();

    }

    public void showSearch() {
        binding.edtSearch.setFocusable(true);
        binding.edtSearch.setFocusableInTouchMode(true);
        binding.edtSearch.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager.isActive()) {
            inputMethodManager.showSoftInput(binding.edtSearch, 0);
        }
    }

    private void init() {
        Gson gson = new Gson();
        background = gson.fromJson(getIntent().getStringExtra(Constant.BACKGROUND), Background.class);
        String pathFile;
        if (background != null && !background.getPathThumb().isEmpty()) {
            if (background.getPathItem().contains("default")) {
                pathFile = "file:///android_asset/" + background.getPathThumb();
            } else {
                pathFile = background.getPathThumb();
            }
            Glide.with(getApplicationContext())
                    .load(pathFile)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .thumbnail(0.1f)
                    .into(binding.imgBG);
            getAllContact();
        }
        binding.edtSearch.addTextChangedListener(new EditTextListener());
        listener();
    }

    public void listener() {
        binding.btnBack.setOnClickListener(view -> {
            onBackPressed();
        });
        binding.imgSearch.setOnClickListener(view -> {
            binding.header1.setVisibility(View.GONE);
            binding.header2.setVisibility(View.VISIBLE);
            isSearchShow = true;
            showSearch();
            analystic.trackEvent(ManagerEvent.contactSearch());
        });
        binding.layoutSet.setOnClickListener(view -> {
            analystic.trackEvent(ManagerEvent.contactSet());
            Log.e("TAN", "listener: click");
            if (!isDefaultDialer(this)) {
                AppUtils.launchSetDefaultDialerIntent(this);
            } else {
                setTheme();
            }
        });
        binding.imgClear.setOnClickListener(view -> {
            binding.edtSearch.setText("");
            binding.imgClear.setVisibility(View.GONE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SET_DEFAULT_DIALER) {
            if (isDefaultDialer(this)) {
                HawkHelper.setStateColorCall(true);
                setTheme();
            } else {
                Toast.makeText(this, getString(R.string.permistion_not_default_dialer), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (isSearchShow) {
            isSearchShow = false;
            binding.header1.setVisibility(View.VISIBLE);
            binding.header2.setVisibility(View.GONE);
            binding.edtSearch.setText("");
            AppUtils.hideKeyboard(binding.edtSearch);
        } else {
            super.onBackPressed();
        }

    }

    public final void getAllContact() {
     /*   LinkedHashSet linkedHashSet = new LinkedHashSet();
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

        ArrayList arrListContact = new ArrayList(linkedHashSet);*/
        LinkedHashSet<ContactInfor> linkedHashSet = new LinkedHashSet<>();
        try {
            ContentResolver contentResolver = getContentResolver();
            Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            String[] infors = {"contact_id", "display_name", "data1", "photo_uri"};
            Cursor query = contentResolver.query(uri, infors, null, null, "sort_key");

            if (query != null) {
                while (query.moveToNext()) {
                    @SuppressLint("Range")
                    String contact_id = query.getString(query.getColumnIndex(infors[0]));
                    @SuppressLint("Range")
                    String display_name = query.getString(query.getColumnIndex(infors[1]));
                    @SuppressLint("Range")
                    String data1 = query.getString(query.getColumnIndex(infors[2]));
                    @SuppressLint("Range")
                    String photo_uri = query.getString(query.getColumnIndex(infors[3]));

                    if (!linkedHashSet.contains(new ContactInfor(contact_id, display_name, data1, photo_uri))) {
                        Log.e("TAN", "getAllContact: 111");
                        linkedHashSet.add(new ContactInfor(contact_id, display_name, data1, photo_uri));
                    }
                }
                query.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("TAN", "getAllContactaaaa: " + linkedHashSet.size());
        ArrayList<ContactInfor> arrListContact = new ArrayList<>(linkedHashSet);
        // Sử dụng ViewModel để lấy danh sách Contact từ Room
        Log.e("TAN", "getAllContact: " + databaseViewModel + "##" + background);
        databaseViewModel.getContactsByBackgroundPath(background.getPathItem()).observe(this, new Observer<List<Contact>>() {
            @Override
            public void onChanged(List<Contact> listContactDB) {
                // Xử lý để đánh dấu các contact đã tồn tại trong DB
                if (listContactDB != null) {
                    for (Contact contact : listContactDB) {
                        for (ContactInfor contactInfor : arrListContact) {
                            if (contactInfor.getContactId().equals(contact.getContactId())) {
                                contactInfor.setChecked(true);
                                break;
                            }
                        }
                    }
                }

                // Khởi tạo Adapter và thiết lập cho RecyclerView
                adapter = new ContactAdapter(getApplicationContext(), arrListContact);
                binding.rcvContact.setAdapter(adapter);
            }
        });


       /* List<Contact> listContactDB = DataManager.query().getContactDao().queryBuilder()
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
        rcvContact.setAdapter(adapter);*/
    }

    public void setTheme() {
        Log.e("TAN", "setTheme: ");
        //PhoneService.startService(this);
        HawkHelper.setStateColorCall(true);
      /*  if (adapter != null) {
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
        }*/
        if (adapter != null) {
            List<String> listContactIdSelected = adapter.getContactSelected();
            Log.e("TAN", "setTheme: 1");
            // Lấy danh sách Contact từ Room
            List<Contact> listContactDB = databaseViewModel.getContactsByBackgroundPath(background.getPathItem()).getValue();

            // Xóa các Contact không có trong danh sách đã chọn
            if (listContactDB != null) {
                for (Contact contact : listContactDB) {
                    String contactSelect = contact.getContactId();
                    if (!listContactIdSelected.contains(contactSelect)) {
                        // Xóa contact khỏi Room
                        databaseViewModel.deleteContact(contact);
                    }
                }
            }
            Log.e("TAN", "setTheme: 2");
            // Cập nhật hoặc chèn Contact mới
            for (String contactID : listContactIdSelected) {
                Log.e("TAN", "setTheme: 2.5");
                // Truy vấn Contact theo contactID
                Contact existingContact = databaseViewModel.getContactById(contactID);
                if (existingContact != null) {
                    Log.e("TAN", "setTheme: 3");

                    // Cập nhật thông tin của Contact
                    existingContact.setBackgroundPath(background.getPathItem());
                    existingContact.setBackground(new Gson().toJson(background));
                    databaseViewModel.updateContact(existingContact);
                } else {
                    // Chèn Contact mới vào Room
                    Log.e("TAN", "setTheme: 3.5");

                    Contact newContact = new Contact(contactID, background.getPathItem(), new Gson().toJson(background));
                    databaseViewModel.insertContact(newContact);
                }
            }
            Log.e("TAN", "setTheme: 4");
            Toast.makeText(this, getString(R.string.set_theme_success), Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        }
    }

   /* @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.REQUEST_OVERLAY) {
            if (AppUtils.checkDrawOverlayApp2(this)) {
                analystic.trackEvent(new Event("Per_Dlg_DrawOver_Contact_Result_Granted",new Bundle()));
                if (!AppUtils.checkNotificationAccessSettings(this)) {
                    AppUtils.showNotificationAccess(this);
                    isRequestPermission = true;
                }
            }
        } else if (requestCode == Constant.REQUEST_NOTIFICATION_ACCESS) {
            if (AppUtils.checkNotificationAccessSettings(this)) {
                new Handler().postDelayed(() -> isRequestPermission = false,500);
                analystic.trackEvent(new Event("Per_Dlg_Contact_Use_App_Granted",new Bundle()));
                Log.e("TAN", "onActivityResult: 222");
                setTheme();
            }
        }
    }*/

 /*   public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constant.PERMISSION_REQUEST_CODE_CALL_PHONE && grantResults.length > 0 && AppUtils.checkPermissionGrand(grantResults)) {
            if (AppUtils.checkDrawOverlayApp2(this)) {
                analystic.trackEvent(new Event("Permission_Dialog_DrawOver_Contact_Granted",new Bundle()));
                if (!AppUtils.checkNotificationAccessSettings(this)) {
                    isRequestPermission = true;
                    AppUtils.showNotificationAccess(this);
                }
            } else {
                AppUtils.showDrawOverlayApp(this);
            }
        }
    }*/

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        appOpenManager.registerObserver(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        appOpenManager.unregisterObserver();
    }

    @Override
    public void lifecycleStart(@NonNull AppOpenAd appOpenAd, @NonNull AppOpenManager appOpenManager) {
       /* if (hasActive()  && !isRequestPermission && PermistionUtils.checkHasPermissionCall(this)) {
            appOpenAd.show(this);
        }*/
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