package com.example.risedemo.monitor;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.risedemo.util.Logger;

public class AppInstallDBHelper extends SQLiteOpenHelper {
    private final static String DB_NAME = "observer.db";
    private final static int DB_VERSION = 1;
    public static final String DB_APP_INSTALL_TABLE_NAME = "test_table";
    public static final String DB_PACKAGE_NAME = "pkg";
    public static final String DB_REPORTED = "reported";
    public static final String DB_DATA = "data";

    AppInstallDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (!db.isOpen()) {
            Logger.d("DownloadApkFileTable database is invalid or not opened.");
            return;
        }
        createDownloadApkFileTable(db);
    }

    /**
     * 当打开数据库时传入的版本号与当前的版本号不同时会调用该方法
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Logger.d("DownloadApkFileTable onUpgrade()");
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            dropDownloadApkFileTable(db);
            createDownloadApkFileTable(db);
        } catch (Exception e) {
            Logger.e("DownloadApkFileTable onDowngrade: ", e);
        }
    }

    private void dropDownloadApkFileTable(SQLiteDatabase db) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + DB_APP_INSTALL_TABLE_NAME);
        } catch (Exception e) {
            Logger.e("dropDownloadApkFileTable: ", e);
        }
    }

    private void createDownloadApkFileTable(SQLiteDatabase db) {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS " + DB_APP_INSTALL_TABLE_NAME
                    + "(" +
                    DB_PACKAGE_NAME + " TEXT PRIMARY KEY NOT NULL, " +
                    DB_REPORTED + " INTEGER NOT NULL DEFAULT 0, " +
                    DB_DATA + " TEXT " +
                    ")";
            db.execSQL(sql);
        } catch (Exception e) {
            Logger.e("createDownloadApkFileTable: ", e);
        }
    }

}
