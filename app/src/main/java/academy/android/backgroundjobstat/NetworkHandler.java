package academy.android.backgroundjobstat;

import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NetworkHandler extends Handler {

  static final int WHAT_SEND_NAIVE_REPORT = 101;
  static final int WHAT_SEND_SMART_REPORT = 102;
  private final static String TAG = NetworkHandler.class.getSimpleName();

  NetworkHandler(Looper looper) {
    super(looper);
  }

  @Override public void handleMessage(Message msg) {
    switch (msg.what) {
      case WHAT_SEND_NAIVE_REPORT:
        Log.d(TAG, "WHAT_SEND_NAIVE_REPORT: Sending location to server");
        Location location = (Location) msg.obj;
        ServerReport serverReport = new ServerReport(location);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
          DatabaseReference myRef =
                  database.getReference("NaiveReport v" + android.os.Build.VERSION.SDK_INT);
        myRef.push().setValue(serverReport);
        break;
      case WHAT_SEND_SMART_REPORT:
        Log.d(TAG, "WHAT_SEND_SMART_REPORT: Sending location to server");
        location = (Location) msg.obj;
        serverReport = new ServerReport(location);
        database = FirebaseDatabase.getInstance();
          myRef = database.getReference("SmartReport v" + android.os.Build.VERSION.SDK_INT);
        myRef.push().setValue(serverReport);
    }
  }
}
