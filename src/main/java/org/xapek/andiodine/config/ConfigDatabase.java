package org.xapek.andiodine.config;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ConfigDatabase extends SQLiteOpenHelper {
    public static final String TAG = "ConfigDatabase";

    private static final String DATABASE_NAME = "andiodine.db";
    private static final int DATABASE_VERSION = 1;
    static public final String TABLE_NAME_CONF = "configuration";
    static public final String COLUMN_CONF_ID = "id";
    static public final String COLUMN_CONF_NAME = "name";
    static public final String COLUMN_CONF_LAST_USED = "last_used";
    static public final String COLUMN_CONF_TUNNEL_NAMESERVER = "tunnel_nameserver";
    static public final String COLUMN_CONF_TOP_DOMAIN = "top_domain";
    static public final String COLUMN_CONF_PASSWORD = "password";
    static public final String COLUMN_CONF_NAMESERVER_MODE = "nameserver_mode";
    static public final String COLUMN_CONF_NAMESERVER = "nameserver";
    static public final String COLUMN_CONF_RAW_MODE = "raw_mode";
    static public final String COLUMN_CONF_LAZY_MODE = "lazy_mode";
    static public final String COLUMN_CONF_DEFAULT_ROUTE = "default_route";
    static public final String COLUMN_CONF_REQUEST_TYPE = "request_type";

    private static final String createStmt = "CREATE TABLE " + TABLE_NAME_CONF + " (" + //
            COLUMN_CONF_ID + " INTEGER PRIMARY KEY," + //
            COLUMN_CONF_NAME + " TEXT," + //
            COLUMN_CONF_LAST_USED + " INTEGER," + //
            COLUMN_CONF_TUNNEL_NAMESERVER + " TEXT," + //
            COLUMN_CONF_TOP_DOMAIN + " TEXT," + //
            COLUMN_CONF_PASSWORD + " TEXT," + //
            COLUMN_CONF_NAMESERVER_MODE + " TEXT," + //
            COLUMN_CONF_NAMESERVER + " TEXT," + //
            COLUMN_CONF_RAW_MODE + " INTEGER," + // Boolean stored as 1=true / 0=false
            COLUMN_CONF_LAZY_MODE + " INTEGER," + //
            COLUMN_CONF_DEFAULT_ROUTE + " INTEGER," + //
            COLUMN_CONF_REQUEST_TYPE + " TEXT" + //
            ");";

    public ConfigDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createStmt);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void insert(ContentValues config) throws SQLException {
        if (config.getAsLong(COLUMN_CONF_ID) != null)
            throw new SQLException("id must be null for update");
        SQLiteDatabase writableDatabase = getWritableDatabase();
        long id = writableDatabase.insertOrThrow(TABLE_NAME_CONF, null, config);
        writableDatabase.close();
        config.put(COLUMN_CONF_ID, id);
        Log.d(TAG, "Insert id=" + id);
    }

    public int update(ContentValues config) throws SQLException {
        if (config.getAsLong(COLUMN_CONF_ID) == null)
            throw new SQLException("id must NOT be null for update");
        SQLiteDatabase writableDatabase = getWritableDatabase();
        int rows = writableDatabase.update(TABLE_NAME_CONF, config, COLUMN_CONF_ID + " = ?",
                new String[]{config.getAsString(COLUMN_CONF_ID)});
        writableDatabase.close();
        Log.d(TAG, "Update rows=" + rows);
        return rows;
    }

    public void delete(ContentValues config) throws SQLException {
        if (config.getAsLong(COLUMN_CONF_ID) == null)
            throw new SQLException("id must NOT be null for delete");
        SQLiteDatabase writableDatabase = getWritableDatabase();
        writableDatabase.delete(TABLE_NAME_CONF, COLUMN_CONF_ID + " = ?",
                new String[]{config.getAsString(COLUMN_CONF_ID)});
        writableDatabase.close();
    }

    public IodineConfiguration selectById(Long id) throws SQLException {
        ContentValues v = new ContentValues();
        SQLiteDatabase readableDatabase = getReadableDatabase();
        Cursor query = readableDatabase.query(TABLE_NAME_CONF, null, COLUMN_CONF_ID + " = ?",
                new String[]{id.toString()}, null, null, null);
        query.moveToFirst();
        DatabaseUtils.cursorRowToContentValues(query, v);
        IodineConfiguration iodineConfiguration = new IodineConfiguration(v);
        Log.d(TAG, "Selected: " + iodineConfiguration);
		return iodineConfiguration;
    }

    public List<IodineConfiguration> selectAll() throws SQLException {
        List<IodineConfiguration> configurations = new ArrayList<IodineConfiguration>();
        SQLiteDatabase readableDatabase = getReadableDatabase();
        Cursor query = readableDatabase.query(TABLE_NAME_CONF, null, null, null, null, null, null);

        while (query.moveToNext()) {
            ContentValues v = new ContentValues();
            DatabaseUtils.cursorRowToContentValues(query, v);
            configurations.add(new IodineConfiguration(v));
        }
        return configurations;
    }

    public void insertOrUpdate(ContentValues config) {
        try {
            update(config);
        } catch (SQLException e) {
            insert(config);
        }
    }
}
