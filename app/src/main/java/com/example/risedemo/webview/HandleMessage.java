package com.example.risedemo.webview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import org.json.JSONObject;

public class HandleMessage {

    WebView webView;
    Context context;

    public abstract class CallBack {
        /**
         * 接收来自H5应用的请求
         */
        public abstract void postString(String jsonStr);

        /**
         * 向H5发送数据
         */
        public abstract void send(String callbackId);
    }

    @SuppressLint("JavascriptInterface")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public HandleMessage(WebView webView, Context context) {
        this.webView = webView;
        this.context = context;
        webView.addJavascriptInterface(new CallBack() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @SuppressLint("JavascriptInterface")
            @JavascriptInterface
            @Override
            public void postString(String jsonStr) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonStr);
                    Toast.makeText(context, jsonObject.getString("data"), Toast.LENGTH_LONG).show();
                    send(jsonObject.getString("callbackId"));
                } catch (Exception e) {
                    Log.e(WebViewTestActivity.TAG, e.getMessage());
                }
            }


            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void send(String callbackId) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("callbackId", callbackId);
                    jsonObject.put("data", "android version:" + Build.VERSION.SDK_INT);
                    final String script = "javascript:window.JSBridge.postMsg(" + jsonObject.toString() + ")";
                    webView.post(() -> webView.evaluateJavascript(script, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                            //执行的js如果有返回值的话
                        }
                    }));
                } catch (Exception e) {
                    Log.e(WebViewTestActivity.TAG, e.getMessage());
                }
            }
        }, "AndroidInterface"); // 注册命名空间
    }

}
