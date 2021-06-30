package com.colorcall.callerscreen.contact;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentResolver;
import android.content.ContentValues;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.analystic.ManagerEvent;
import com.colorcall.callerscreen.constan.Constant;
import com.colorcall.callerscreen.database.Background;
import com.colorcall.callerscreen.database.Contact;
import com.colorcall.callerscreen.database.ContactDao;
import com.colorcall.callerscreen.database.DataManager;
import com.colorcall.callerscreen.utils.AppUtils;
import com.google.common.collect.Table;
import com.google.gson.Gson;

import org.greenrobot.greendao.query.DeleteQuery;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SelectContactActivity extends AppCompatActivity {
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
    public class EditTextListener implements TextWatcher {
        public EditTextListener() {
        }

        public void afterTextChanged(Editable editable) {

        }

        public void beforeTextChanged(CharSequence charSequence, int start, int before, int after) {
        }

        public void onTextChanged(CharSequence charSequence, int start, int before, int after) {
            imgClear.setVisibility(TextUtils.isEmpty(charSequence) ? View.GONE : View.VISIBLE);
            if(adapter!=null){
                adapter.search(charSequence);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_contact);
        ButterKnife.bind(this);
        init();
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
        background = new Background(null, 0, "thumbDefault/default1.webp", "/raw/default1", false, "default1");
        // background = gson.fromJson(getIntent().getStringExtra(Constant.BACKGROUND), Background.class);
        Log.e("TAN", "init: " + background);
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

    @OnClick({R.id.btnBack, R.id.imgSearch, R.id.imgClear,R.id.layoutSet})
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
                break;
            case R.id.layoutSet:
                setTheme();
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
            AppUtils.hideSoftKeyboard(this);
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
                    String contact_id = query.getString(query.getColumnIndex(infors[0]));
                    String display_name = query.getString(query.getColumnIndex(infors[1]));
                    String data1 = query.getString(query.getColumnIndex(infors[2]));
                    String photo_uri = query.getString(query.getColumnIndex(infors[3]));
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
        Log.e("TAN", "setTheme: ");
        if (adapter != null) {
            //iv ivVar = this.b;
            List<String> listContactIdSelected = adapter.getContactSelected();
            List<Contact> listContactDB = DataManager.query().getContactDao().queryBuilder()
                    .where(ContactDao.Properties.Background_path.eq(background.getPathItem()))
                    .list();
            for(int i=0;i<listContactDB.size();i++){
                String contactSelect = listContactDB.get(i).getContact_id();
                if(!listContactIdSelected.contains(contactSelect)){
                    final DeleteQuery<Contact> tableDeleteQuery  = DataManager.query().getContactDao().queryBuilder().where(ContactDao.Properties.Contact_id.eq(contactSelect))
                            .buildDelete();
                    tableDeleteQuery.executeDeleteWithoutDetachingEntities();
                    DataManager.query().getContactDao().detachAll();
                }
            }
           /* Background themeBean = this.a;
            SQLiteDatabase writableDatabase = ivVar.getWritableDatabase();
            int i = 1;
            char c2 = 0;
            Cursor query = writableDatabase.query("CONTACT_TABLE", null, "theme=?", new String[]{themeBean.folder}, null, null, null);
            while (query.moveToNext()) {
                String string = query.getString(query.getColumnIndex("contact_id"));
                if (!a2.contains(string)) {
                    writableDatabase.delete("CONTACT_TABLE", "contact_id=?", new String[]{string});
                }
            }*/
           // query.close();
            Iterator<String> it = listContactIdSelected.iterator();
            while (it.hasNext()) {
                String contactID = it.next();
               /* List<Contact> listQueryContactID = DataManager.query().getContactDao().queryBuilder()
                        .where(ContactDao.Properties.Contact_id.eq(contactID))
                        .list();*/
                Contact contact = new Contact(contactID,background.getPathItem());
                DataManager.query().getContactDao().insertOrReplace(contact);
               /* if(listQueryContactID.size()>0){
                    Contact contact = listQueryContactID.get(0);
                    contact.setBackground_path(background.getPathItem());
                    DataManager.query().getContactDao().update(contact);
                }else{
                    DataManager.query().getContactDao().insertOrReplace(contact);
                }
                Cursor query2 = writableDatabase.query("CONTACT_TABLE", null, "contact_id=?", strArr, null, null, null);
                if (query2.getCount() > 0) {
                    writableDatabase.update("CONTACT_TABLE", contentValues, "contact_id=?", strArr);
                } else {
                    contentValues.put("contact_id", next);
                    writableDatabase.insert("CONTACT_TABLE", null, contentValues);
                }
                query2.close();
                it = it;
                i = 1;
                c2 = 0;*/
            }
            Toast.makeText(this, "set theme success", Toast.LENGTH_SHORT).show();
            finish();
        }
    }


}