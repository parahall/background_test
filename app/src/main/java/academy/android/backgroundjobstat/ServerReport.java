package academy.android.backgroundjobstat;

import android.location.Location;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

class ServerReport {

    private double latitude;
    private double longitude;
    private String locationTime;
    private String reportedAt;

    public ServerReport() {
    }

    ServerReport(Location location) {
        this(location.getLongitude(), location.getLatitude(), location.getTime());
    }

    ServerReport(double longitude, double latitude, long time) {
        this.longitude = longitude;
        this.latitude = latitude;

        DateFormat formatter =
                SimpleDateFormat.getDateTimeInstance();// new SimpleDateFormat("dd/MM/yyyy
        // hh:mm:ss.SSS");

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);

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
