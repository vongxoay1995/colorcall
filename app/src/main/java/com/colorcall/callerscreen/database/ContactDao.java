package com.colorcall.callerscreen.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "CONTACT".
*/
public class ContactDao extends AbstractDao<Contact, Long> {

    public static final String TABLENAME = "CONTACT";

    /**
     * Properties of entity Contact.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Contact_id = new Property(1, String.class, "contact_id", false, "CONTACT_ID");
        public final static Property Background_path = new Property(2, String.class, "background_path", false, "BACKGROUND_PATH");
        public final static Property Background = new Property(3, String.class, "background", false, "BACKGROUND");
    }


    public ContactDao(DaoConfig config) {
        super(config);
    }
    
    public ContactDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"CONTACT\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"CONTACT_ID\" TEXT NOT NULL ," + // 1: contact_id
                "\"BACKGROUND_PATH\" TEXT NOT NULL ," + // 2: background_path
                "\"BACKGROUND\" TEXT NOT NULL );"); // 3: background
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"CONTACT\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, Contact entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindString(2, entity.getContact_id());
        stmt.bindString(3, entity.getBackground_path());
        stmt.bindString(4, entity.getBackground());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, Contact entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindString(2, entity.getContact_id());
        stmt.bindString(3, entity.getBackground_path());
        stmt.bindString(4, entity.getBackground());
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public Contact readEntity(Cursor cursor, int offset) {
        Contact entity = new Contact( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getString(offset + 1), // contact_id
            cursor.getString(offset + 2), // background_path
            cursor.getString(offset + 3) // background
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, Contact entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setContact_id(cursor.getString(offset + 1));
        entity.setBackground_path(cursor.getString(offset + 2));
        entity.setBackground(cursor.getString(offset + 3));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(Contact entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(Contact entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(Contact entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}