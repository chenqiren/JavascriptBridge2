package chenqiren.com.bridgewebview;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

public class ThreadUtils {

    private static final Looper sMainThreadLooper = Looper.getMainLooper();
    private static Handler mHandler;

    static {
        HandlerThread handlerThread = new HandlerThread("");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
    }


    public static boolean isOnUiThread() {
        return sMainThreadLooper != null && Thread.currentThread() == sMainThreadLooper.getThread();
    }

    public static void runOnBackgroundThread(Runnable runnable) {
        mHandler.post(runnable);
    }
}
