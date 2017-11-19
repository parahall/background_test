package academy.android.backgroundjobstat;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class NaiveService extends Service {

  public final static String TAG = NaiveService.class.getSimpleName();

  private HandlerThread handlerThread;
  private Looper looper;
  private LocationHandler locationHandler;
  private NaiveBroadcastReceiver mReceiver;
  private NaiveNetworkHandler mNaiveNetworkHandler;

  @Override public void onCreate() {
    super.onCreate();
    handlerThread = new HandlerThread("MyHandlerThread");
    handlerThread.start();
    looper = handlerThread.getLooper();
    locationHandler = new LocationHandler(looper, getApplicationContext());
    mReceiver = new NaiveBroadcastReceiver();
    mNaiveNetworkHandler = new NaiveNetworkHandler(looper);
  }

  @SuppressLint("MissingPermission") @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.d(TAG, "Service started");
    LocalBroadcastManager.getInstance(this)
        .registerReceiver(mReceiver,
            new IntentFilter(NaiveBroadcastReceiver.ACTION_NEW_LOCATION_ARRIVED));
    locationHandler.removeMessages(LocationHandler.WHAT_LOCATION_REQUEST);
    locationHandler.sendEmptyMessage(LocationHandler.WHAT_LOCATION_REQUEST);
    return super.onStartCommand(intent, flags, startId);
  }

  @Override public void onDestroy() {
    Log.d(TAG, "Service destroyed");
    LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    locationHandler.removeCallbacksAndMessages(null);
    mNaiveNetworkHandler.removeCallbacksAndMessages(null);
    locationHandler = null;
    mNaiveNetworkHandler = null;
    looper.quit();
    handlerThread.quit();
    locationHandler = null;
  }

  @Nullable @Override public IBinder onBind(Intent intent) {
    return null;
  }

  public static Intent getIntent(Context context) {
    return new Intent(context, NaiveService.class);
  }

  public class NaiveBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_NEW_LOCATION_ARRIVED = "ACTION_NEW_LOCATION_ARRIVED";
    public static final String LOCATION_KEY = "LOCATION_KEY";

    @Override public void onReceive(Context context, Intent intent) {
      Log.d(TAG, "New location received");
      Location location = intent.getExtras().getParcelable(LOCATION_KEY);
      Message message =
          mNaiveNetworkHandler.obtainMessage(NaiveNetworkHandler.WHAT_SEND_REPORT, location);
      mNaiveNetworkHandler.sendMessage(message);
    }
  }
}
