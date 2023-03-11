package com.jamillabltd.websitetoapkfullscreen;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    String webUrl = "https://sites.google.com/view/developer-jamil/";
    private WebView mWebView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private boolean mDoubleBackToExitPressedOnce = false;
    private ProgressBar mProgressBar;
    private FrameLayout webView_container;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint({"MissingInflatedId", "SetJavaScriptEnabled"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the FrameLayout and set it to mLayout
        webView_container = findViewById(R.id.webView_container_id);
        // Find the SwipeRefreshLayout and set its OnRefreshListener
        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        // Find the WebView and configure it
        mWebView = findViewById(R.id.webViewId);
        mWebView.getSettings().setJavaScriptEnabled(true);
        //mWebView.setWebViewClient(new WebViewClient());
        mWebView.setWebChromeClient(new WebChromeClient());


        //internet connection - if no internet
        if (!CheckNetwork.isInternetAvailable(this)) {
            //if there is no internet do this
            setContentView(R.layout.activity_main);

            new AlertDialog.Builder(this) //alert the person knowing they are about to close
                    .setTitle("No internet connection available")
                    .setMessage("Please Check you're Mobile data or Wifi network.")
                    .setPositiveButton("Ok", (dialog, which) -> finish())
                    .show();
        } else { //if connected with internet
            //Web view stuff
            // Find the WebView and configure it
            mWebView = findViewById(R.id.webViewId);
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    // Show the progress bar when the page starts loading
                    mProgressBar.setVisibility(View.VISIBLE);
                    webView_container.setAlpha(0.5f);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    // Hide the progress bar when the page finishes loading
                    mProgressBar.setVisibility(View.GONE);
                    webView_container.setAlpha(1.0f);
                }
            });
            mWebView.setWebChromeClient(new WebChromeClient());
            mWebView.loadUrl(webUrl);
            mProgressBar = findViewById(R.id.progress_bar);

            // Add an OnScrollChangeListener to the WebView to disable the SwipeRefreshLayout
            mWebView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                if (scrollY == 0 && !mWebView.canScrollVertically(-1)) {
                    mSwipeRefreshLayout.setEnabled(true);
                } else if (scrollY == (v.getHeight() - v.getScrollY()) && !mWebView.canScrollVertically(1)) {
                    mSwipeRefreshLayout.setEnabled(false);
                } else {
                    mSwipeRefreshLayout.setEnabled(false);
                }
            });
        }

    }

    //network check
    private static class CheckNetwork {
        static final String TAG = CheckNetwork.class.getSimpleName();

        static boolean isInternetAvailable(Context context) {
            NetworkInfo info = ((ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

            if (info == null) {
                Log.d(TAG, "no internet connection");
                return false;
            } else {
                Log.d(TAG, " internet connection available...");
                return true;
            }
        }
    }

    //scroll manage and refresh the webView
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //swipeRefresh
    @Override
    public void onRefresh() {
        // Reload the web page when the user swipes down to refresh
        mWebView.reload();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    //backPress to webView back - double backPress to exit
    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            if (mDoubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.mDoubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(() -> mDoubleBackToExitPressedOnce = false, 2000);
        }
    }

}