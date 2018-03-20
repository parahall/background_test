package academy.android.backgroundjobstat;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Build;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Calendar;

public class NaiveService extends Service {

    public final static String TAG = NaiveService.class.getSimpleName();

    private HandlerThread handlerThread;
    private Looper looper;
    private LocationHandler locationHandler;
    private NaiveBroadcastReceiver mReceiver;
    private NetworkHandler mNaiveNetworkHandler;

    public static Intent getIntent(Context context) {
        return new Intent(context, NaiveService.class);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate NaiveService");
        handlerThread = new HandlerThread("MyHandlerThread");
        handlerThread.start();
        looper = handlerThread.getLooper();
        locationHandler = new LocationHandler(looper, getApplicationContext());
        mNaiveNetworkHandler = new NetworkHandler(looper, getApplicationContext());
        mReceiver = new NaiveBroadcastReceiver();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "my_service";
            String channelName = "My Background Service";

            NotificationChannel channel = new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);

            int notifyId = 1;

            Notification notification = new Notification.Builder(this, channelId)
                    .setContentTitle("Some Message")
                    .setContentText("You've received new messages!")
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .build();
            startForeground(notifyId, notification);
        }

    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mReceiver,
                        new IntentFilter(LocationBaseBroadcast.ACTION_NEW_LOCATION_ARRIVED));
        locationHandler.removeMessages(LocationHandler.WHAT_LOCATION_REQUEST);
        locationHandler.sendEmptyMessage(LocationHandler.WHAT_LOCATION_REQUEST);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        scheduleAlarm();

        Log.d(TAG, "Service destroyed");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        locationHandler.removeCallbacksAndMessages(null);
        mNaiveNetworkHandler.removeCallbacksAndMessages(null);
        locationHandler.stop();
        locationHandler = null;
        mNaiveNetworkHandler = null;
        looper.quit();
        handlerThread.quit();
        locationHandler = null;
    }

    private void scheduleAlarm() {
        Log.d(TAG, "scheduleAlarm: repetitive alarm");
        Intent intent = new Intent(this.getApplicationContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 0,
                intent, 0);
        Calendar calendar = Calendar.getInstance();

        AlarmManager alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + AlarmManager
                        .INTERVAL_FIFTEEN_MINUTES,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class NaiveBroadcastReceiver extends LocationBaseBroadcast {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "New location received");
            if (locationHandler != null) {
                locationHandler.stop();
            }
            Location location = intent.getExtras().getParcelable(LOCATION_KEY);
            Message message =
                    mNaiveNetworkHandler.obtainMessage(NetworkHandler.WHAT_SEND_NAIVE_REPORT,
                            location);
            mNaiveNetworkHandler.sendMessage(message);
        }
    }
}
