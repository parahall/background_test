package academy.android.backgroundjobstat;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.concurrent.CountDownLatch;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;

public class LocationWork extends Worker {

    static final String TAG = LocationWork.class.getSimpleName();
    private static CountDownLatch locationWait;
    private HandlerThread handlerThread;
    private Looper looper;
    private NetworkHandler networkHandler;
    private LocationTracker locationTracker;
    private LocationWork.LocationWorkBroadcastReceiver receiver;

    static void reportFinished() {
        if (locationWait != null) {
            Log.d(TAG, "doWork: locationWait down by one");
            locationWait.countDown();
        }
    }

    @NonNull
    @Override
    public WorkerResult doWork() {
        Log.d(TAG, "doWork: Started to work");
        handlerThread = new HandlerThread("MyHandlerThread");
        handlerThread.start();
        looper = handlerThread.getLooper();
        networkHandler = new NetworkHandler(looper, getApplicationContext());
        locationTracker = new LocationTracker(getApplicationContext(), looper);
        receiver = new LocationWork.LocationWorkBroadcastReceiver();
        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(receiver,
                        new IntentFilter(LocationBaseBroadcast.ACTION_NEW_LOCATION_ARRIVED));
        locationTracker.start();
        try {
            locationWait = new CountDownLatch(1);
            locationWait.await();
            Log.d(TAG, "doWork: Countdown released");
        } catch (InterruptedException e) {
            Log.d(TAG, "doWork: CountdownLatch interrupted");
            e.printStackTrace();
        }

        cleanUp();
        return WorkerResult.SUCCESS;
    }

    private void cleanUp() {
        Log.d(TAG, "Work is done");
        locationTracker.stop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(receiver);
        networkHandler.removeCallbacksAndMessages(null);
        handlerThread.quit();
        looper.quit();
        handlerThread.quit();
    }

    public class LocationWorkBroadcastReceiver extends LocationBaseBroadcast {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "New location received");

            if (intent.getExtras() == null) {
                return;
            }

            Location location = intent.getExtras().getParcelable(LOCATION_KEY);
            if (location == null) {
                return;
            }

            Constraints constraints = new Constraints.Builder().setRequiredNetworkType
                    (NetworkType.CONNECTED).build();
            Data inputData = new Data.Builder()
                    .putDouble(LocationUploadWorker.LOCATION_LAT, location.getLatitude())
                    .putDouble(LocationUploadWorker.LOCATION_LONG, location.getLongitude())
                    .putLong(LocationUploadWorker.LOCATION_TIME, location.getTime())
                    .build();
            OneTimeWorkRequest uploadWork = new OneTimeWorkRequest.Builder
                    (LocationUploadWorker.class).setConstraints(constraints).setInputData
                    (inputData).build();
            WorkManager.getInstance().enqueue(uploadWork);
        }
    }
}
