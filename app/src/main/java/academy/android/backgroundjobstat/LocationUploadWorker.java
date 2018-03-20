package academy.android.backgroundjobstat;

import android.support.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.work.Worker;

public class LocationUploadWorker extends Worker {
    static final String LOCATION_LAT = "LOCATION_LAT";
    static final String LOCATION_LONG = "LOCATION_LONG";
    static final String LOCATION_TIME = "LOCATION_TIME";

    @NonNull
    @Override
    public WorkerResult doWork() {
        ServerReport serverReport = new ServerReport(getInputData().getDouble(LOCATION_LONG, 0),
                getInputData().getDouble(LOCATION_LAT, 0), getInputData().getLong(LOCATION_TIME,
                0));
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef =
                database.getReference("WorkerReport v" + android.os.Build.VERSION.SDK_INT);
        myRef.push().setValue(serverReport);
        return WorkerResult.SUCCESS;
    }
}
