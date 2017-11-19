package academy.android.backgroundjobstat;

import academy.android.backgroundjobstat.NaiveService.NaiveBroadcastReceiver;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import java.lang.ref.WeakReference;

public class LocationHandler extends Handler {
  public static final int WHAT_LOCATION_REQUEST = 101;
  private static final String TAG = LocationHandler.class.getSimpleName();
  private static final long LOCATION_SIGNIFICANT_TIME_INTERVAL_MINUTE = 60 * 1000;
  public static final int TIME_LOCATION_INTERVAL = 2 * 60 * 1000;
  public static final int FIVE_MINUTES = 5 * 60 * 1000;

  private final LocationManager mLocationManager;
  private final GpsLocationListener mGpsLocationListener;
  private final NetworkLocationListener mNetworkLocationListener;
  private final WeakReference<Context> mWeakReference;

  public LocationHandler(Looper looper, Context context) {
    super(looper);
    mWeakReference = new WeakReference<>(context);
    mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

    mGpsLocationListener = new GpsLocationListener();
    mNetworkLocationListener = new NetworkLocationListener();
  }

  @SuppressLint("MissingPermission") @Override public void handleMessage(Message msg) {
    switch (msg.what) {
      case WHAT_LOCATION_REQUEST:
        sendEmptyMessageDelayed(WHAT_LOCATION_REQUEST, FIVE_MINUTES);
        Log.d(TAG, "WHAT_LOCATION_REQUEST");
        Location gpsLastKnownLocation =
            mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location networkLastKnownLocation =
            mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        Location bestLocation = null;

        if (gpsLastKnownLocation != null) {
          if (networkLastKnownLocation != null) {
            long time = networkLastKnownLocation.getTime() - gpsLastKnownLocation.getTime();
            if (time > 0) {
              bestLocation = networkLastKnownLocation;
            } else {
              bestLocation = gpsLastKnownLocation;
            }
          } else {
            bestLocation = gpsLastKnownLocation;
          }
        } else if (networkLastKnownLocation != null) {
          bestLocation = networkLastKnownLocation;
        }

        if (bestLocation != null
            && System.currentTimeMillis() - bestLocation.getTime() < TIME_LOCATION_INTERVAL) {
          Log.d(TAG, "Best Location set");
          broadcastLocation(bestLocation);
        } else {
          Log.d(TAG, "Requested for location update");
          mLocationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, mGpsLocationListener,
              getLooper());
          mLocationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER,
              mNetworkLocationListener, getLooper());
        }
    }
  }

  private void broadcastLocation(Location location) {
    if (mWeakReference.get() != null) {
      Log.d(TAG, "Broadcasting location");
      Intent intent = new Intent(NaiveBroadcastReceiver.ACTION_NEW_LOCATION_ARRIVED);
      intent.putExtra(NaiveBroadcastReceiver.LOCATION_KEY, location);
      LocalBroadcastManager.getInstance(mWeakReference.get()).sendBroadcast(intent);
    }
  }

  public class GpsLocationListener implements LocationListener {

    @Override public void onLocationChanged(Location location) {
      Log.d(TAG, String.format("Location track ,onLocationChanged location=%s", location));
      switch (location.getProvider()) {
        case LocationManager.GPS_PROVIDER:
          if (mLocationManager != null) {
            mLocationManager.removeUpdates(mNetworkLocationListener);
          }
          break;
      }
      broadcastLocation(location);
    }

    @Override public void onStatusChanged(String provider, int status, Bundle extras) {
      Log.d(TAG, String.format("onStatusChanged: %s, status: %s", provider, status));
    }

    @Override public void onProviderEnabled(String provider) {
      Log.d(TAG, "onProviderEnabled: " + provider);
    }

    @Override public void onProviderDisabled(String provider) {
      Log.d(TAG, "onProviderDisabled: " + provider);
    }
  }

  public class NetworkLocationListener implements LocationListener {

    @Override public void onLocationChanged(Location location) {
      Log.d(TAG, String.format("Location track ,onLocationChanged location=%s", location));
      switch (location.getProvider()) {
        case LocationManager.NETWORK_PROVIDER:
          broadcastLocation(location);
          break;
      }
    }

    @Override public void onStatusChanged(String provider, int status, Bundle extras) {
      Log.d(TAG, String.format("onStatusChanged: %s, status: %s", provider, status));
    }

    @Override public void onProviderEnabled(String provider) {
      Log.d(TAG, "onProviderEnabled: " + provider);
    }

    @Override public void onProviderDisabled(String provider) {
      Log.d(TAG, "onProviderDisabled: " + provider);
    }
  }
}
