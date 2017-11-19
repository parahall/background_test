package academy.android.backgroundjobstat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import java.lang.ref.WeakReference;

import static academy.android.backgroundjobstat.LocationHandler.TIME_LOCATION_INTERVAL;

@SuppressLint("MissingPermission") class LocationTracker {

  private static final String TAG = LocationTracker.class.getSimpleName();
  private static final long LOCATION_SIGNIFICANT_TIME_INTERVAL_MINUTE = 60 * 1000;

  private final LocationManager mLocationManager;
  private final GpsLocationListener mGpsLocationListener;
  private final NetworkLocationListener mNetworkLocationListener;
  private final WeakReference<Context> mWeakReference;
  private final Looper looper;

  LocationTracker(Context context, Looper looper) {
    this.looper = looper;
    mGpsLocationListener = new GpsLocationListener();
    mNetworkLocationListener = new NetworkLocationListener();
    mWeakReference = new WeakReference<>(context);
    mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
  }

  void start() {
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
          looper);
      mLocationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER,
          mNetworkLocationListener, looper);
    }
  }

  private void broadcastLocation(Location location) {
    if (mWeakReference.get() != null) {
      Log.d(TAG, "Broadcasting location");
      Intent intent = new Intent(NaiveService.NaiveBroadcastReceiver.ACTION_NEW_LOCATION_ARRIVED);
      intent.putExtra(NaiveService.NaiveBroadcastReceiver.LOCATION_KEY, location);
      LocalBroadcastManager.getInstance(mWeakReference.get()).sendBroadcast(intent);
    }
  }

  void stop() {
    mLocationManager.removeUpdates(mGpsLocationListener);
    mLocationManager.removeUpdates(mNetworkLocationListener);
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
