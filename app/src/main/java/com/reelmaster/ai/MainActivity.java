package com.reelmaster.ai;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import androidx.webkit.WebViewAssetLoader;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;

public class MainActivity extends Activity {

    private WebView webView;

    private static final String REMOTE_URL =
        "https://cdn.jsdelivr.net/gh/prasadchamod26-coder/ReelMasterAI@main/app/src/main/assets/index.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        WebViewAssetLoader loader =
            new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/",
                    new WebViewAssetLoader.AssetsPathHandler(this))
                .build();

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(
                WebView view, WebResourceRequest request) {
                return loader.shouldInterceptRequest(request.getUrl());
            }
        });

        setContentView(webView);
        loadRemotePage();
    }

    private void loadRemotePage() {
        new Thread(() -> {
            try {
                URL url = new URL(REMOTE_URL);
                HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();

                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                connection.setRequestProperty("Cache-Control", "no-cache");

                InputStream input = connection.getInputStream();
                ByteArrayOutputStream output = new ByteArrayOutputStream();

                byte[] buffer = new byte[4096];
                int count;

                while ((count = input.read(buffer)) != -1) {
                    output.write(buffer, 0, count);
                }

                input.close();
                connection.disconnect();

                String html = output.toString("UTF-8");

                new Handler(Looper.getMainLooper()).post(() ->
                    webView.loadDataWithBaseURL(
                        "https://appassets.androidplatform.net/",
                        html,
                        "text/html",
                        "UTF-8",
                        null
                    )
                );

            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() ->
                    webView.loadUrl(
                        "https://appassets.androidplatform.net/assets/index.html"
                    )
                );
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
