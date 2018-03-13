package academy.android.backgroundjobstat;

import android.content.BroadcastReceiver;

public abstract class LocationBaseBroadcast extends BroadcastReceiver {
    public static final String ACTION_NEW_LOCATION_ARRIVED = "ACTION_NEW_LOCATION_ARRIVED";
    public static final String LOCATION_KEY = "LOCATION_KEY";
}
