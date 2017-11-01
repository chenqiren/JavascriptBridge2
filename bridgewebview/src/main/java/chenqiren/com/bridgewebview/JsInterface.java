package chenqiren.com.bridgewebview;

import android.support.annotation.NonNull;
import android.webkit.JavascriptInterface;

/**
 * This class defines native api used by javascript.
 */

public class JsInterface {

    public static final String JAVASCRIPT_INTERFACE_NAME = "Android";

    private final BridgeWebview mWebView;

    public JsInterface(@NonNull BridgeWebview webView) {
        mWebView = webView;
    }

    @JavascriptInterface
    public void callHandler(String jsonMessage) {
        mWebView.callHandler(jsonMessage);
    }
}
