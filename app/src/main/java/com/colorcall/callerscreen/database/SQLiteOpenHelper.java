package com.colorcall.callerscreen.database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.greenrobot.greendao.database.Database;

import static android.util.Log.i;

public class SQLiteOpenHelper extends DaoMaster.DevOpenHelper{
    Context context;
    public SQLiteOpenHelper(Context context, String name) {
        super(context, name);
        this.context = context;
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        try {
            if (oldVersion < 2) {
                Log.e("TAN", "onUpgrade: aaa");
                db.execSQL("CREATE TABLE CONTACT (`ID` INTEGER PRIMARY KEY AUTOINCREMENT," +
                        " `CONTACT_ID` TEXT ," +
                        "`BACKGROUND_PATH` TEXT )");
            }
        } catch (SQLException e) {
            Log.e("TBB", "Error " + e.getLocalizedMessage());
        }
    }
}
