package academy.android.backgroundjobstat;

import android.location.Location;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

class NaiveServerReport {

  private double latitude;
  private double longitude;
  private String locationTime;
  private String reportedAt;

  public NaiveServerReport() {
  }

  public NaiveServerReport(Location location) {
    latitude = location.getLatitude();
    longitude = location.getLongitude();

    DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");

    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(location.getTime());

    locationTime = formatter.format(calendar.getTime());

    calendar.setTimeInMillis(System.currentTimeMillis());
    reportedAt = formatter.format(calendar.getTime());
  }

  public double getLatitude() {
    return latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public String getLocationTime() {
    return locationTime;
  }

  public String getReportedAt() {
    return reportedAt;
  }
}
