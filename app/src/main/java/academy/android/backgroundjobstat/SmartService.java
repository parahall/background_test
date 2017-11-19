package academy.android.backgroundjobstat;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

public class SmartService extends JobService {
  public static final String TAG = "LOCATION_SMART_JOB";
  private LocationTracker locationTracker;
  private NetworkHandler networkHandler;
  private SmartBroadcastReceiver receiver;
  private HandlerThread handlerThread;
  private Looper looper;

  @Override public void onCreate() {
    super.onCreate();
    handlerThread = new HandlerThread("MyHandlerThread");
    handlerThread.start();
    looper = handlerThread.getLooper();
    networkHandler = new NetworkHandler(looper);
    locationTracker = new LocationTracker(this, looper);
    receiver = new SmartBroadcastReceiver();
  }

  @Override public boolean onStartJob(JobParameters jobParameters) {
    LocalBroadcastManager.getInstance(this)
        .registerReceiver(receiver,
            new IntentFilter(LocationBaseBroadcast.ACTION_NEW_LOCATION_ARRIVED));
    locationTracker.start();
    return true;
  }

  @Override public boolean onStopJob(JobParameters jobParameters) {
    locationTracker.stop();
    return true;
  }

  @Override public void onDestroy() {
    Log.d(TAG, "Service destroyed");
    locationTracker.stop();
    LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    networkHandler.removeCallbacksAndMessages(null);
    handlerThread.quit();
    looper.quit();
    handlerThread.quit();
  }

  public class SmartBroadcastReceiver extends LocationBaseBroadcast {
    @Override public void onReceive(Context context, Intent intent) {
      Log.d(TAG, "New location received");
      Location location = intent.getExtras().getParcelable(LOCATION_KEY);
      Message message =
          networkHandler.obtainMessage(NetworkHandler.WHAT_SEND_SMART_REPORT, location);
      networkHandler.sendMessage(message);
    }
  }
}
