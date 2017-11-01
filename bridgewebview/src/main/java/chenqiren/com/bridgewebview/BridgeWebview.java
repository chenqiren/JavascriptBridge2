package chenqiren.com.bridgewebview;

import com.google.gson.Gson;

import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BridgeWebview extends WebView {

    private static final String TAG = "BridgeWebview";
    public static final String HANDLE_NATIVE_CALL = "handleNativeCall";

    private Object mBridgeMethods;
    private Map<String, NativeCallback> mNativeCallbackMap;
    private Gson mGson;

    public BridgeWebview(Context context) {
        super(context);
        init();
    }

    public BridgeWebview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BridgeWebview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mBridgeMethods = new BridgeMethods(this);
        mNativeCallbackMap = new HashMap<>();
        mGson = new Gson();

        // init webview
        setWebViewClient(new BridgeWebViewClient());
        setWebChromeClient(new WebChromeClient());
        getSettings().setJavaScriptEnabled(true);
        addJavascriptInterface(new JsInterface(this), JsInterface.JAVASCRIPT_INTERFACE_NAME);
    }

    public void setBridgeMethods(@NonNull Object bridgeMethods) {
        mBridgeMethods = bridgeMethods;
    }

    public void callHandler(final String jsonMessage) {
        ThreadUtils.runOnBackgroundThread(new Runnable() {
            @Override
            public void run() {
                Message message = mGson.fromJson(jsonMessage, Message.class);
                if (message.nativeCallbackId != null) {
                    // handle native callback
                    callNativeCallback(message);
                }
                else {
                    // handle javascript method invocation: reflection
                    invokeMethod(message);
                }
            }
        });
    }

    @Override
    public void setWebViewClient(WebViewClient client) {
        if (!(client instanceof BridgeWebViewClient)) {
            throw new IllegalArgumentException("WebviewClient must be a subclass of BridgeWebviewClient");
        }

        super.setWebViewClient(client);
    }

    @UiThread
    public void evaluateJavascript(
            @NonNull final String function,
            @NonNull final Message message,
            @Nullable final NativeCallback callback) {

        // Store callback into map
        if (callback != null) {
            String callbackId = UUID.randomUUID().toString();
            message.nativeCallbackId = callbackId;
            mNativeCallbackMap.put(callbackId, callback);
        }

        // Construct method call
        String messageString = mGson.toJson(message);
        messageString = messageString.replace("\\", "\\\\");
        messageString = messageString.replace("\"", "\\\"");
        messageString = messageString.replace("\'", "\\\'");
        messageString = messageString.replace("\n", "\\n");
        messageString = messageString.replace("\r", "\\r");
        messageString = messageString.replace("\f", "\\f");
        messageString = messageString.replace("\u2028", "\\u2028");
        messageString = messageString.replace("\u2029", "\\u2029");
        StringBuilder scriptBuilder = new StringBuilder(function).append("(");
        scriptBuilder.append("\"").append(messageString).append("\"");
        scriptBuilder.append(")");

        if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
            evaluateJavascript(scriptBuilder.toString(), new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    // handle native callback. this is used for api above 19.
                    // for api below 19, we use callNativeCallback to handle native callback.
                    if (callback != null) {
                        callback.onReceiveValue(value);
                        mNativeCallbackMap.remove(message.nativeCallbackId);
                    }
                }
            });
        } else {
            loadUrl("javascript:" + scriptBuilder);
        }
    }

    /**
     * Handle native callback for api below 19.
     */
    private void callNativeCallback(@NonNull Message message) {
        String callbackId = message.nativeCallbackId;
        NativeCallback callback = mNativeCallbackMap.remove(callbackId);
        if (callback != null) {
            if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
                callback.onReceiveValue(message.callbackData);
            }
        }
    }

    /**
     * Invoke native method from Javascript.
     */
    private void invokeMethod(@NonNull final Message message) {
        String methodName = message.method;
        Method method;
        Class clazz = mBridgeMethods.getClass();

        try {
            method = clazz.getMethod(methodName, Message.class);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, String.format("Fail to find Method %s, error %s", methodName, e));
            return;
        }

        try {
            method.invoke(mBridgeMethods, message);
        } catch (IllegalAccessException e) {
            Log.e(TAG, String.format("Fail to invoke Method %s, error %s", methodName, e));
        } catch (InvocationTargetException e) {
            Log.e(TAG, String.format("Fail to invoke Method %s, error %s", methodName, e));
        }
    }

    /**
     * Return native method call response to JS through callback.
     */
    public void callJsCallback(@NonNull Message message, @Nullable String result) {
        if (message.jsCallbackId != null) {
            final Message responseMessage = new Message();
            responseMessage.method = "callJsCallback";
            responseMessage.jsCallbackId = message.jsCallbackId;
            responseMessage.callbackData = result;

            post(new Runnable() {
                @Override
                public void run() {
                    evaluateJavascript(responseMessage.method, responseMessage, null /*callback*/);
                }
            });
        }
    }

    public interface NativeCallback {
        void onReceiveValue(String value);
    }
}
