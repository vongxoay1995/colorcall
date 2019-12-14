package com.htn.colorcall.database;

import android.content.Context;

import org.greenrobot.greendao.database.Database;

import java.util.InputMismatchException;

public class LocalDatabaseHelper {

    private static LocalDatabaseHelper localDatabaseHelper;

    private DaoSession daoSession;
    private Database database;

    private LocalDatabaseHelper(Context context) {
        DaoMaster.DevOpenHelper helper = new SQLiteOpenHelper(context.getApplicationContext(), "colorcall.db");
        database = helper.getWritableDb();
        daoSession = new DaoMaster(database).newSession();
    }

    public static LocalDatabaseHelper getInstance(Context context) {
        if (localDatabaseHelper == null) {
            throw new InputMismatchException("Please setting LocalDatabaseHelper.init() "
                    + "in onCreate from Application class");
        }
        return localDatabaseHelper;
    }

    public static void init(Context context) {
        if (localDatabaseHelper == null) {
            localDatabaseHelper = new LocalDatabaseHelper(context);
        }
    }

    public static void initNew(Context context) {
        localDatabaseHelper = new LocalDatabaseHelper(context);
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }

    public Database getDatabase() {
        return database;
    }
}
