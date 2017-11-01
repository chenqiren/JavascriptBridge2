package chenqiren.com.javascriptbridge2;

import butterknife.Bind;
import butterknife.ButterKnife;
import chenqiren.com.bridgewebview.BridgeMethods;
import chenqiren.com.bridgewebview.BridgeWebview;
import chenqiren.com.bridgewebview.BridgeWebview.NativeCallback;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;

public class MainActivity extends Activity {

    private final int[] COLORS = {Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW};

    @Bind(R.id.webView)
    BridgeWebview mBridgeWebview;

    @Bind(R.id.button)
    Button mJsCallButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        WebView.setWebContentsDebuggingEnabled(true);

        mJsCallButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                BridgeMethods.updateJavascriptText(mBridgeWebview, "update JS text successfully", new NativeCallback() {
                    @Override
                    public void onReceiveValue(String value) {
                        int colorNum = COLORS.length;
                        long random = Math.round(Math.random() * 10);
                        int randomColor = COLORS[(int) random % colorNum];
                        mJsCallButton.setBackgroundColor(randomColor);
                    }
                });
            }
        });

        mBridgeWebview.loadUrl("file:///android_asset/demo.html");
    }
}
