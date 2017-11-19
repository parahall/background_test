package academy.android.backgroundjobstat;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

  private static final int PERMSSION_REQUEST_CODE = 101;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    findViewById(R.id.btn_start_service).setOnClickListener(this);
    findViewById(R.id.btn_stop_service).setOnClickListener(this);
  }

  @Override public void onClick(View v) {
    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(this,
        android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      ArrayList<String> arrayList = new ArrayList<>(2);
      arrayList.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
      arrayList.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
      ActivityCompat.requestPermissions(this, arrayList.toArray(new String[arrayList.size()]),
          PERMSSION_REQUEST_CODE);
      return;
    }
    switch (v.getId()) {
      case R.id.btn_start_service:
        startService(NaiveService.getIntent(this));
        break;
      case R.id.btn_stop_service:
        stopService(NaiveService.getIntent(this));
        break;
    }
  }
}
