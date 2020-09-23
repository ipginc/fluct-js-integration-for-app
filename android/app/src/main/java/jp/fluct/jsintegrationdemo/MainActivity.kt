package jp.fluct.jsintegrationdemo

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val webView by lazy { findViewById<WebView>(R.id.webView) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG);
        }

        webView.getSettings().setJavaScriptEnabled(true)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                GlobalScope.launch(Dispatchers.Main) {
                    val adinfo = withContext(Dispatchers.IO) {
                        AdvertisingIdClient.getAdvertisingIdInfo(applicationContext)
                    }
                    val groupId = "1000123868"
                    val unitId = "1000214183"
                    val bundle = "jp.co.ipg.gguide"

                    val js =
                        """
                        var fluctAdScript = fluctAdScript || {};
                        fluctAdScript.cmd = fluctAdScript.cmd || [];
                        fluctAdScript.cmd.push(function (cmd) {
                          cmd.setConfig({dlvParams: {"ifa":"${adinfo.id}","lmt":${adinfo.isLimitAdTrackingEnabled},"bundle":"$bundle"}});
                          cmd.loadByGroup("$groupId");
                          cmd.display(".fluct-unit-$unitId", "$unitId");
                        });
                        """
                    webView.evaluateJavascript(js, null)
                }
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                if (request?.url?.host.equals("voyagegroup.github.io")) {
                    //サイト内の遷移
                    return false;
                }
                startActivity(Intent(Intent.ACTION_VIEW, request?.url))
                return true
            }
        }
        webView.loadUrl("https://firebasestorage.googleapis.com/v0/b/ggmforslack.appspot.com/o/fluctNativeAdHtmlSample.htm?alt=media&token=2e925044-b578-4f2f-af60-a292c389f16b")
    }
}
