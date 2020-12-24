package com.example.risedemo.monitor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.example.risedemo.util.Logger;

import java.util.ArrayList;
import java.util.List;

import static com.example.risedemo.monitor.AppInstallDBHelper.DB_APP_INSTALL_TABLE_NAME;
import static com.example.risedemo.monitor.AppInstallDBHelper.DB_PACKAGE_NAME;
import static com.example.risedemo.monitor.AppInstallDBHelper.DB_REPORTED;
import static com.example.risedemo.monitor.AppInstallDBHelper.DB_DATA;

/**
 * 数据库操作类
 */
public class AppInstallDBManager {

    public static class AppInstallDBData {
        public String packageName;
        public String data;
        public boolean reportedInstall;

        @Override
        public String toString() {
            return "AppInstallDBData{" +
                    "packageName='" + packageName + '\'' +
                    ", data='" + data + '\'' +
                    ", reportedInstall=" + reportedInstall +
                    '}';
        }

        public boolean isEffective() {
            return !TextUtils.isEmpty(packageName) && !TextUtils.isEmpty(data);
        }
    }

    private SQLiteDatabase mSQLiteDatabase;
    private AppInstallDBHelper mAppInstallDBHelper;

    public void initialize(Context context) {
        try {
            if (mAppInstallDBHelper == null) {
                mAppInstallDBHelper = new AppInstallDBHelper(context);
            }
        } catch (Exception e) {
            mAppInstallDBHelper = null;
        }
    }

    private SQLiteDatabase getSQLiteDatabase() {
        try {
            mSQLiteDatabase = mAppInstallDBHelper.getWritableDatabase();
        } catch (Exception e) {
            mSQLiteDatabase = null;
        }
        return mSQLiteDatabase;
    }

    private void closeDataBase() {
        try {
            if (null != mSQLiteDatabase && mSQLiteDatabase.isOpen()) {
                mSQLiteDatabase.close();
            }
        } catch (Exception e) {
            mSQLiteDatabase = null;
        }
    }

    public boolean replace(AppInstallDBData data) {
        SQLiteDatabase database = getSQLiteDatabase();
        if (database == null) {
            return false;
        }
        long rowId = -1;
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DB_PACKAGE_NAME, data.packageName);
            contentValues.put(DB_REPORTED, data.reportedInstall);
            contentValues.put(DB_DATA, data.data);
            rowId = database.replace(DB_APP_INSTALL_TABLE_NAME, null, contentValues);
        } catch (Exception e) {
            Logger.e("AppInstallDBManager replace(): packageName: " + data.packageName + ",e: " + e);
        } finally {
            closeDataBase();
        }
        Logger.d("AppInstallDBManager replace rowId:" + rowId + ",data = " + data.toString());
        return rowId > 0;
    }

    public AppInstallDBData select(String packageName, boolean reported) {
        AppInstallDBData data = new AppInstallDBData();
        SQLiteDatabase database = getSQLiteDatabase();
        if (database == null) {
            return data;
        }
        String[] projection = {DB_PACKAGE_NAME, DB_REPORTED, DB_DATA};
        String selection = DB_PACKAGE_NAME + " = ? AND " + DB_REPORTED + "=?";
        String[] selectionArgs = {packageName, String.valueOf(reported ? 1 : 0)};
        Cursor cursor = null;
        try {
            cursor = database.query(DB_APP_INSTALL_TABLE_NAME, projection, selection, selectionArgs,
                    /*groupBy=*/ null,
                    /*having=*/ null,
                    /*orderBy=*/ null);
            if (cursor.moveToFirst()) {
                data.packageName = cursor.getString(cursor.getColumnIndex(DB_PACKAGE_NAME));
                data.reportedInstall = cursor.getInt(cursor.getColumnIndex(DB_REPORTED)) == 1;
                data.data = cursor.getString(cursor.getColumnIndex(DB_DATA));
            }
            cursor.close();
        } catch (Exception e) {
            Logger.e("AppInstallDBManager select(): packageName: " + packageName + ",e: " + e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            closeDataBase();
        }
        Logger.d("AppInstallDBManager select data:" + data.toString());
        return data;
    }

    public List<AppInstallDBData> query(boolean reported) {
        List<AppInstallDBData> list = new ArrayList<>();
        SQLiteDatabase database = getSQLiteDatabase();
        if (database == null) {
            return list;
        }
        String[] projection = {DB_PACKAGE_NAME, DB_REPORTED, DB_DATA};
        String selection = DB_REPORTED + " = ?";
        String[] selectionArgs = {String.valueOf(reported ? 1 : 0)};
        Cursor cursor = null;
        try {
            cursor = database.query(DB_APP_INSTALL_TABLE_NAME, projection, selection, selectionArgs,
                    /*groupBy=*/ null,
                    /*having=*/ null,
                    /*orderBy=*/ null);

            while (cursor.moveToNext()) {
                AppInstallDBData data = new AppInstallDBData();
                data.packageName = cursor.getString(cursor.getColumnIndex(DB_PACKAGE_NAME));
                data.reportedInstall = cursor.getInt(cursor.getColumnIndex(DB_REPORTED)) == 1;
                data.data = cursor.getString(cursor.getColumnIndex(DB_DATA));
                Logger.d("AppInstallDBManager query reported:" + reported + ",data :" + data.toString());
                list.add(data);
            }
        } catch (Exception e) {
            Logger.e("AppInstallDBManager query() e:" + e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            closeDataBase();
        }
        Logger.d("AppInstallDBManager query reported:" + reported + ",list size:" + list.size());
        return list;
    }

    public boolean isReportedInstallValue(String packageName) {
        return delete(packageName, 1);
    }

    public boolean delete(String packageName) {
        return delete(packageName, -1);
    }

    private boolean delete(String packageName, int reportedInstallValue) {
        SQLiteDatabase database = getSQLiteDatabase();
        if (database == null) {
            return false;
        }
        int affectedRows = 0;
        try {
            String selection = DB_PACKAGE_NAME + "=?";
            ArrayList<String> selectionArgsList = new ArrayList<>();
            selectionArgsList.add(packageName);
            if (reportedInstallValue >= 0) {
                selection += " AND " + DB_REPORTED + "=?";
                selectionArgsList.add(String.valueOf(reportedInstallValue));
            }
            String[] selectionArgs = selectionArgsList.toArray(new String[selectionArgsList.size()]);
            affectedRows = database.delete(DB_APP_INSTALL_TABLE_NAME, selection, selectionArgs);
        } catch (Exception e) {
            Logger.e("AppInstallDBManager delete(): packageName: " + packageName + ",e: " + e);
        } finally {
            closeDataBase();
        }
        Logger.d("AppInstallDBManager delete packageName:" + packageName + ",affectedRows:" + affectedRows + ",reportedInstallValue:" + reportedInstallValue);
        return affectedRows > 0;
    }

    public boolean updateReportedInstall(String packageName, boolean reported) {
        SQLiteDatabase database = getSQLiteDatabase();
        if (database == null) {
            return false;
        }
        int affectedRows = 0;
        try {
            ContentValues values = new ContentValues();
            values.put(DB_REPORTED, reported ? 1 : 0);
            String selection = DB_PACKAGE_NAME + " = ?";
            String[] selectionArgs = new String[]{packageName};
            affectedRows = database.update(DB_APP_INSTALL_TABLE_NAME, values, selection, selectionArgs);
        } catch (Exception e) {
            Logger.e("AppInstallDBManager updateReportFlag(): packageName: " + packageName + ",e: " + e);
        } finally {
            closeDataBase();
        }
        Logger.d("AppInstallDBManager updateReportFlag packageName:" + packageName + ",affectedRows:" + affectedRows);
        return affectedRows > 0;
    }

}
