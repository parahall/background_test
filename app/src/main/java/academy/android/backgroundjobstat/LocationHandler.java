package academy.android.backgroundjobstat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class LocationHandler extends Handler {
  static final int WHAT_LOCATION_REQUEST = 101;
  static final int TIME_LOCATION_INTERVAL = 2 * 60 * 1000;
  private static final String TAG = LocationHandler.class.getSimpleName();
  private static final int FIVE_MINUTES = 5 * 60 * 1000;

  private final LocationTracker locationTracker;

  LocationHandler(Looper looper, Context context) {
    super(looper);
    locationTracker = new LocationTracker(context, getLooper());
  }

  @SuppressLint("MissingPermission") @Override public void handleMessage(Message msg) {
    switch (msg.what) {
      case WHAT_LOCATION_REQUEST:
        sendEmptyMessageDelayed(WHAT_LOCATION_REQUEST, FIVE_MINUTES);
        Log.d(TAG, "WHAT_LOCATION_REQUEST");
        locationTracker.start();
    }
  }

  void stop() {
    locationTracker.stop();
  }
}
