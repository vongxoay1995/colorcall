package com.colorcall.callerscreen.database;

import android.content.Context;

import androidx.annotation.NonNull;

import org.greenrobot.greendao.database.Database;


public class DataManager {
    public DataManager() {

    }

    private static DaoSession mDaoSession;
    private static final boolean ENCRYPTED = true;

    private static class SingletonHolder {
        private static final DataManager INSTANCE = new DataManager();
    }

    public static DataManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void init(Context context) {
        init(context, "colorcall.db");
    }

    private void init(@NonNull Context context, @NonNull String dbName) {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context.getApplicationContext(),dbName);
        Database db =  helper.getWritableDb();
        mDaoSession = new DaoMaster(db).newSession();
    }

    private DaoSession getDaoSession() {
        if (null == mDaoSession) {
            throw new NullPointerException("green db has not been initialized");
        }
        return mDaoSession;
    }

    public static DaoSession query() {
        return DataManager.getInstance().getDaoSession();
    }
}
