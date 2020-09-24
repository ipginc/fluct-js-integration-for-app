package jp.fluct.jsintegrationdemo;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class JavaActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG);
        }

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                GetAdIdAsyncTask task = new GetAdIdAsyncTask(JavaActivity.this, new Callback() {
                    @Override
                    public void onSuccess(AdvertisingIdClient.Info info) {
                        String groupId = "1000123868";
                        String unitId = "1000214183";
//                        String groupId = "1000123948";
//                        String unitId = "1000214357";
                        String bundle = "jp.co.ipg.gguide";

                        String js =  "var fluctAdScript = fluctAdScript || {};" +
                        "fluctAdScript.cmd = fluctAdScript.cmd || [];" +
                                "fluctAdScript.cmd.push(function (cmd) {" +
                            "cmd.setConfig({dlvParams: {\"ifa\":\"${IDFA}\",\"lmt\":\"${LMT}\",\"bundle\":\"${BUNDLE}\"}});" +
                            "cmd.loadByGroup(\"${GROUP_ID}\");" +
                            "cmd.display(\".fluct-unit-${UNIT_ID}\", \"${UNIT_ID}\");" +
                            "});";

                        js = js.replace("${IDFA}", info.getId())
                                .replace("${LMT}", info.isLimitAdTrackingEnabled() ? "1" : "0")
                                .replace("${BUNDLE}", bundle)
                                .replace("${GROUP_ID}", groupId)
                                .replace("${UNIT_ID}", unitId);
                        webView.evaluateJavascript(js, null);
                    }
                });
                task.execute();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (request.getUrl().getHost().equals("voyagegroup.github.io")) {
                    //サイト内の遷移
                    return false;
                }

                startActivity(new Intent(Intent.ACTION_VIEW, request.getUrl()));
                return true;
            }
        });

        webView.loadUrl("http://192.168.0.25:8000/fluctNativeAdHtmlSample/fluctNativeAdHtmlSample.htm");
    }

    interface Callback {
        void onSuccess(AdvertisingIdClient.Info info);
    }
}

class GetAdIdAsyncTask extends AsyncTask<Context, Void, AdvertisingIdClient.Info> {
    private final WeakReference<Context> context;
    private final JavaActivity.Callback callback;

    GetAdIdAsyncTask(Context context, JavaActivity.Callback callback) {
        this.context = new WeakReference<>(context);
        this.callback = callback;
    }

    @Override
    protected AdvertisingIdClient.Info doInBackground(Context... contexts) {
        AdvertisingIdClient.Info adInfo = null;
        try {
            adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context.get());
        } catch (GooglePlayServicesNotAvailableException |
                GooglePlayServicesRepairableException | IOException e) {
            e.printStackTrace();
        }
        return adInfo;
    }

    @Override
    protected void onPostExecute(AdvertisingIdClient.Info info) {
        super.onPostExecute(info);

        callback.onSuccess(info);
    }
}
