package com.example.risedemo.monitor;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import com.example.risedemo.monitor.AppInstallDBManager.AppInstallDBData;
import com.example.risedemo.util.Logger;

/**
 * 子线程处理任务，返回到主线程使用
 */
public final class AppInstallDataSource {
    private Context mContext;
    private ExecutorService mThreadPool;
    private AppInstallDBManager mAppInstallDBManager;
    private static final String FILE_NAME_APPINSTALLED_SWITCH = "cupid_file_name_appinstalled_switch";

    public interface Callback<T> {
        void onResult(final T result);
    }

    public AppInstallDataSource(Context context) {
        mContext = context;
        createAppInstallDBManager(context);
    }

    public AppInstallDBManager getAppInstallDBManager() {
        return createAppInstallDBManager(mContext);
    }

    private synchronized AppInstallDBManager createAppInstallDBManager(Context context) {
        if (mAppInstallDBManager == null) {
            mAppInstallDBManager = new AppInstallDBManager();
            mAppInstallDBManager.initialize(context);
        }
        return mAppInstallDBManager;
    }

    public void replace(final AppInstallDBData appInstallDBData) {
        doInBackground(new BackgroundTask.Call() {
            @Override
            public Object doInBackground(Object... objects) {
                return getAppInstallDBManager().replace((AppInstallDBManager.AppInstallDBData) objects[0]);
            }

            @Override
            public void onPostResult(Object result) {
            }
        }, appInstallDBData);
    }

    public void select(final String packageName, final Callback<AppInstallDBData> callback) {
        doInBackground(new BackgroundTask.Call<AppInstallDBData>() {
            @Override
            public Object doInBackground(Object... objects) {
                return getAppInstallDBManager().select((String) objects[0], false);
            }

            @Override
            public void onPostResult(AppInstallDBData result) {
                callback.onResult(result);
            }
        }, packageName);
    }

    public void query(final boolean reported, final Callback<List<AppInstallDBData>> callback) {
        doInBackground(new BackgroundTask.Call<List<AppInstallDBData>>() {
            @Override
            public Object doInBackground(Object... objects) {
                return getAppInstallDBManager().query((Boolean) objects[0]);
            }

            @Override
            public void onPostResult(List<AppInstallDBData> result) {
                callback.onResult(result);
            }
        }, reported);
    }

    public void updateReportedInstall(final String packageName, final boolean reported) {
        doInBackground(new BackgroundTask.Call() {
            @Override
            public Object doInBackground(Object... objects) {
                return getAppInstallDBManager().updateReportedInstall((String) objects[0], (Boolean) objects[1]);
            }

            @Override
            public void onPostResult(Object result) {
            }
        }, packageName, reported);
    }

    public void isReportedInstallValue(final String packageName, final Callback<Boolean> callback) {
        doInBackground(new BackgroundTask.Call<Boolean>() {
            @Override
            public Object doInBackground(Object... objects) {
                return getAppInstallDBManager().isReportedInstallValue((String) objects[0]);
            }

            @Override
            public void onPostResult(Boolean hadSendTracking) {
                callback.onResult(hadSendTracking);
                if (!hadSendTracking) {
                    getAppInstallDBManager().delete(packageName);
                }
            }
        }, packageName);
    }

    public void saveAppInstalledSwitch(String value) {
        doInBackground(new BackgroundTask.Call() {
            @Override
            public Object doInBackground(Object... objects) {
                return saveReportAppInstalledSwitch((String) objects[0]);
            }

            @Override
            public void onPostResult(Object result) {
            }
        }, value);
    }

    public String getAppInstalledSwitch(final String key) {
        String value = "";
        Future<String> future = doInBackground(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return getReportAppInstalledSwitch(key);
            }
        });
        try {
            //最多同步等待200ms
            value = future.get(200, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            Logger.d("AppInstallDBManager getAppInstalledSwitch e:" + e.getMessage());
        }
        return value;
    }

    private boolean saveReportAppInstalledSwitch(String content) {
        if (null == content || null == mContext) return false;

        FileOutputStream out = null;
        BufferedWriter writer = null;
        try {
            out = mContext.openFileOutput(FILE_NAME_APPINSTALLED_SWITCH, Context.MODE_PRIVATE);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(content);
        } catch (Exception e) {
            Logger.d("AppInstallDBManager saveReportAppInstalledSwitch e:" + e.getMessage());
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
            }
        }
        return true;
    }

    private String getReportAppInstalledSwitch(String key) {
        if (null == mContext) return "";

        String line, installedSwitch = null;
        FileInputStream in = null;
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try {
            in = mContext.openFileInput(FILE_NAME_APPINSTALLED_SWITCH);
            reader = new BufferedReader(new InputStreamReader(in));
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            JSONObject jsonObject = new JSONObject(builder.toString());
            installedSwitch = jsonObject.optString(key, "0");
        } catch (Exception e) {
            Logger.d("AppInstallDBManager getReportAppInstalledSwitch e:" + e.getMessage());
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
            }
        }
        return installedSwitch;
    }

    private ExecutorService getThreadExecutor() {
        if (mThreadPool == null) {
            mThreadPool = Executors.newSingleThreadExecutor();
        }
        return mThreadPool;
    }

    /**
     * 处理带回调的异步任务
     *
     * @param call
     * @param params
     */
    private void doInBackground(BackgroundTask.Call call, Object... params) {
        final ExecutorService executor = getThreadExecutor();
        new BackgroundTask(call).ex(executor, params);
    }

    /**
     * 处理同步获取值的异步任务
     *
     * @param callable
     * @return
     */
    public Future doInBackground(Callable<String> callable) {
        final ExecutorService executor = getThreadExecutor();
        return executor.submit(callable);
    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    public static class BackgroundTask extends AsyncTask<Object, Void, Object> {
        private Call call;

        public BackgroundTask(Call call) {
            this.call = call;
        }

        public void ex(Executor exec, Object... params) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                executeOnExecutor(exec, params);
            } else {
                execute(params);
            }
        }

        @Override
        protected Object doInBackground(Object... objects) {
            return call != null ? call.doInBackground(objects) : null;
        }

        @Override
        protected void onPostExecute(Object o) {
            if (call != null && o != null) {
                call.onPostResult(o);
            }
        }

        public interface Call<T> {
            Object doInBackground(Object... objects);

            void onPostResult(final T result);
        }
    }

}
