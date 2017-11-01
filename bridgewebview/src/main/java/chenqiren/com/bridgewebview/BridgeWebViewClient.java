package chenqiren.com.bridgewebview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class BridgeWebViewClient extends WebViewClient {

    private static final String TAG = "BridgeWebViewClient";
    private static final String toLoadJs = "WebViewJavascriptBridge.js";

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);

        loadLocalJs(view, toLoadJs);
    }

    @Override
    public void onReceivedError(
            WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);

        Log.d(TAG, String.format("onReceivedError %s", error));
    }

    @Override
    public void onReceivedHttpError(
            WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        super.onReceivedHttpError(view, request, errorResponse);

        Log.d(TAG, String.format("onReceivedHttpError %s", errorResponse));
    }

    public static void loadLocalJs(WebView view, String path){
        String jsContent = assetFile2Str(view.getContext(), path);
        view.loadUrl("javascript:" + jsContent);
    }

    public static String assetFile2Str(@NonNull Context context, @NonNull String urlStr){
        InputStream in = null;
        try{
            in = context.getAssets().open(urlStr);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String line;
            do {
                line = bufferedReader.readLine();
                if (line != null && !line.matches("^\\s*//.*")) {
                    sb.append(line);
                }
            } while (line != null);

            bufferedReader.close();
            in.close();

            return sb.toString();
        } catch (Exception e) {
            Log.e(TAG, "Fail to load WebviewJavascriptBridge.js, exception " + e);
        } finally {
            if(in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // no-op.
                }
            }
        }
        return null;
    }

}
