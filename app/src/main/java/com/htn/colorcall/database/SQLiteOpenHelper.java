package com.htn.colorcall.database;

import android.content.Context;
import android.database.SQLException;
import android.util.Log;

import org.greenrobot.greendao.database.Database;

public class SQLiteOpenHelper extends DaoMaster.DevOpenHelper{
    Context context;
    public SQLiteOpenHelper(Context context, String name) {
        super(context, name);
        this.context = context;
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
      onCreate(db);
    }
}
