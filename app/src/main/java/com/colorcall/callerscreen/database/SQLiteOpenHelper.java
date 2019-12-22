package com.colorcall.callerscreen.database;

import android.content.Context;

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
