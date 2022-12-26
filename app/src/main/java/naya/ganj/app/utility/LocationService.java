package naya.ganj.app.utility;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.List;
import java.util.Locale;

public class LocationService {
    LocationCallback locationCallback;
    FusedLocationProviderClient fusedLocationClient;
    LocationRequest locationRequest;
    String latitude = "";
    String longitude = "";
   static LocationService locationService = null;

    public static LocationService getInstance() {
        if (locationService == null) {
            return locationService = new LocationService();
        } else {
            return locationService;
        }
    }

    public void startLocationUpdate(Context context, TextView tvLocation) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(30000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    latitude = String.valueOf(location.getLatitude());
                    longitude = String.valueOf(location.getLongitude());

                    getCompleteAddressString(context,location.getLatitude(),location.getLongitude(),tvLocation);
                    Log.e("TAG", "onLocationResult: latitude" + latitude + ", Longitude: " + longitude);
                }
            }
        };
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("nayaganj_log", "LocationService: Permission not granted");
            Log.e("nayaganj_log", "LocationService: Requesting Permission");
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_CALL_LOG, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        } else {
            Log.e("nayaganj_log", "LocationService: Permission already granted");
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void stopLocation() {
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void getCompleteAddressString(Context context, double LATITUDE, double LONGITUDE, TextView tvLocation) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");
                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                tvLocation.setText(strAdd);
                tvLocation.setText(strAdd);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
