package academy.android.backgroundjobstat;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Trigger;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import static com.firebase.jobdispatcher.Constraint.ON_ANY_NETWORK;
import static com.firebase.jobdispatcher.Lifetime.FOREVER;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int FIVETEEN_MIN = 1000 * 60 * 15;
    private static final int PERMSSION_REQUEST_CODE = 101;
    private static final String TAG = MainActivity.class.getSimpleName();
    private FirebaseJobDispatcher firebaseJobDispatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_start_service).setOnClickListener(this);
        findViewById(R.id.btn_stop_service).setOnClickListener(this);
        findViewById(R.id.btn_schedule_job).setOnClickListener(this);
        findViewById(R.id.btn_cancel_job_schedule).setOnClickListener(this);
        findViewById(R.id.btn_peiodic_work).setOnClickListener(this);
        findViewById(R.id.btn_stop_peiodic_work).setOnClickListener(this);

        firebaseJobDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
    }

    @Override
    public void onClick(View v) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission
                .ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager
                .PERMISSION_GRANTED) {
            ArrayList<String> arrayList = new ArrayList<>(2);
            arrayList.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
            arrayList.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
            ActivityCompat.requestPermissions(this, arrayList.toArray(new String[arrayList.size()]),
                    PERMSSION_REQUEST_CODE);
            return;
        }
        switch (v.getId()) {
            case R.id.btn_start_service:
                Log.d(TAG, "Starting service");
                startService(NaiveService.getIntent(this));
                break;
            case R.id.btn_stop_service:
                Log.d(TAG, "Stopping service");
                stopService(NaiveService.getIntent(this));
                break;
            case R.id.btn_schedule_job:
                Log.d(TAG, "Schedule job");
                Job myJob = firebaseJobDispatcher.newJobBuilder()
                        .setService(SmartService.class)
                        .setTag(SmartService.LOCATION_SMART_JOB)
                        .setRecurring(true)
                        .setLifetime(FOREVER)
                        .setTrigger(Trigger.executionWindow(0, 60 * 5))
                        .setReplaceCurrent(false)
                        .setConstraints(ON_ANY_NETWORK)
                        .build();

                firebaseJobDispatcher.mustSchedule(myJob);
                break;
            case R.id.btn_cancel_job_schedule:
                Log.d(TAG, "Cancel all jobs");
                firebaseJobDispatcher.cancelAll();
                break;
            case R.id.btn_peiodic_work:
                Log.d(TAG, "Schedule periodic Work: ");
                Constraints constraints = new Constraints.Builder().setRequiredNetworkType
                        (NetworkType.CONNECTED).build();
                PeriodicWorkRequest locationWork = new PeriodicWorkRequest.Builder(LocationWork
                        .class, 15, TimeUnit.MINUTES).addTag(LocationWork.TAG)
                        .setConstraints(constraints).build();
                WorkManager.getInstance().enqueue(locationWork);

                break;
            case R.id.btn_stop_peiodic_work:
                Log.d(TAG, "Cancel periodic work");
                WorkManager.getInstance().cancelAllWorkByTag(LocationWork.TAG);
                break;
        }
    }
}
