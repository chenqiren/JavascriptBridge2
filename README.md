# JavascriptBridge
JavascriptBridge provides an async way for Javascript and Webview to communicate with each other. 
It is inspired by [iOS WebViewJavascriptBridge](https://github.com/marcuswestin/WebViewJavascriptBridge) and 
[JsBridge](https://github.com/lzyzsd/JsBridge).

## How it works
1. Javascript to Android:

    This library targets at android api>=18. There is a security issue of JavascriptInterface on android 
    api<=16. So shouldOverrideUrlLoading is used in [JsBridge](https://github.com/lzyzsd/JsBridge) to avoid this problem. 
    But as that security issue is fixed in api>=18, and JavascriptInterface provides a simpler and more efficient way to
    call native methods, we use JavascriptInterface in this library. 
    
    |        | JavascriptInterface | shouldOverrideUrlLoading  |
    |--------|:-------------------:| -------------------------:|   
    | pros   | simple and efficient |  doesn't have the security issue |
    | cons   | has security issue before Android 4.2 | inefficient to get andorid return value |
    
2. Android to Javascript:
    
    use WebView.loadUrl("Javascript:") to execute JS methods. 

## How to use it
Android:
1. use BridgeWebview to replace WebView in Android code. 
2. Use BridgeWebview.setBridgeMethods to set the bridge methods you define to use between JS and Android. BridgeMethods.java provides
an example of how to define bridge methods. 

```java
    /*************************************************************
     * Methods called from Javascript.
     * These methods param must be a Message Object.
     *************************************************************/
     
     public void initialize() {
        // do whatever you want
     }
     
     public void initializeWithCallback(@NonNull Message message) {
        // do whatever you want

        // Must call callJsCallback after finishing excuting native method with a callback
        mBridgeWebview.callJsCallback(message, null /* return value */);
     }


    /*************************************************************
     * Methods called from client
     *************************************************************/
    public static void updateJavascriptText(
            @NonNull BridgeWebview webview,
            @NonNull String text,
            @Nullable NativeCallback callback) {
        Message message = new Message();

        // Call evaluateJavascript to execute method you want to call in javascript. 
        // put info of js method into message. (e.g js method name, params)
        webview.evaluateJavascript(BridgeWebview.HANDLE_NATIVE_CALL, message, callback);
    }
```

```
mBridgeWebView.setBridgeMethods(new BridgeMethods());
```

Javascript:
Take demo.html as an example. 
1. call native methods
```javascript
    WebViewJavascriptBridge.callHandler(
        'initialize',
        null,
        function responseCallback(responseData) {
            // do whatever you want
        }
    );
```

2. register methods which can be used by native. call setup after javascript finishes loading. 
```javascript
    // Register Javascript methods which can be called from Native.
    // this method should be called when Javascript finishes initializing.
    function setup() {
       WebViewJavascriptBridge.registerHandler("updateJavascriptText", function() {
            // do whatever you want 
       });
    }
```



