package academy.android.backgroundjobstat;

import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NaiveNetworkHandler extends Handler {

  public final static String TAG = NaiveNetworkHandler.class.getSimpleName();

  public static final int WHAT_SEND_REPORT = 101;

  public NaiveNetworkHandler(Looper looper) {
    super(looper);
  }

  @Override public void handleMessage(Message msg) {
    switch (msg.what) {
      case WHAT_SEND_REPORT:
        Log.d(TAG,"WHAT_SEND_REPORT: Sending location to server");
        Location location = (Location) msg.obj;
        NaiveServerReport naiveServerReport = new NaiveServerReport(location);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("NaiveReport");
        myRef.push().setValue(naiveServerReport);
    }
  }
}
