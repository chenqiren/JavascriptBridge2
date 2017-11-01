package chenqiren.com.bridgewebview;

import chenqiren.com.bridgewebview.BridgeWebview.NativeCallback;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * This class is an example of how to create Js-Native bridge methods.
 */
public class BridgeMethods {

    private final BridgeWebview mBridgeWebview;

    public BridgeMethods(@NonNull BridgeWebview webview) {
        mBridgeWebview = webview;
    }

    /*************************************************************
     * Methods called from Javascript.
     * These methods param must be an Object.
     *************************************************************/
    public void initialize(@NonNull Message message) {
        // do whatever you want

        mBridgeWebview.callJsCallback(message, null);
    }


    /*************************************************************
     * Methods called from client
     *************************************************************/
    public static void updateJavascriptText(
            @NonNull BridgeWebview webview,
            @NonNull String text,
            @Nullable NativeCallback callback) {
        Message message = new Message();
        message.method = "updateJavascriptText";

        webview.evaluateJavascript(BridgeWebview.HANDLE_NATIVE_CALL, message, callback);
    }
}
