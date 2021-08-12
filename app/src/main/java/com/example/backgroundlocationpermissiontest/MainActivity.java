package com.example.backgroundlocationpermissiontest;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.backgroundlocationpermissiontest.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    //permission and services checks
    private static final int PERMISSION_REQUEST_CODE_COARSE_LOCATION = 2;
    private static final int PERMISSION_REQUEST_CODE_FINE_LOCATION = 3;
    private static final int PERMISSION_REQUEST_BACKGROUND_LOCATION = 4;

    final String TAG = "MainActivityLog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG,"onCreate called");
        super.onCreate(savedInstanceState);
        com.example.backgroundlocationpermissiontest.databinding.ActivityMainBinding x = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(x.getRoot());

        x.backgroundLocationAccessButton.setOnClickListener(v -> handlePermissionRequestForBackgroundNotifications());

        // check for location service enabled
        if (!isLocationServiceEnabled()) showLocationServiceDialog();
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) showLocationPermissionDialog();
    }

    @Override
    protected void onStart() {
        Log.i(TAG,"onStart called");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.i(TAG,"onResume called");
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissions.length == 0 || grantResults.length == 0) return;
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE_COARSE_LOCATION:
            case PERMISSION_REQUEST_CODE_FINE_LOCATION:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
                    showToastAndLog("fine/coarse location permission not granted");
                else
                    showToastAndLog("fine/coarse location permission granted");
                break;
            case PERMISSION_REQUEST_BACKGROUND_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    showToastAndLog("background location permission granted");
                else
                    showToastAndLog("background location permission NOT granted");
        }
    }


    private boolean isBackgroundLocationAccessAlreadyGranted() {
        return checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }


    private void handlePermissionRequestForBackgroundNotifications() {
        // nothing to do if permission was already granted
        if (isBackgroundLocationAccessAlreadyGranted()) {
            showToastAndLog("background location access already granted");
            return;
        };

        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            // ask user for location permission when app is in background
            new AlertDialog.Builder(this)
                    .setTitle("ask for background location permission")
                    .setMessage("I need background location permission because...")
                    .setPositiveButton("okay", (dialog, which) -> {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, PERMISSION_REQUEST_BACKGROUND_LOCATION);
                    })
                    .setNegativeButton("no", null)
                    .show();

        } else {
            // appears when user clicked "do not ask me again" or similar before
            new AlertDialog.Builder(this)
                    .setTitle("declined")
                    .setMessage("you refused to give background location access. You can not use this feature. Go to app settings to change this")
                    .setPositiveButton("app settings", (dialog, which) -> {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    })
                    .setNegativeButton("do not use this", (dialog, which) -> {
                        Log.i(TAG, "usage of background location access not allowed");
                    })
                    .show();
        }
    }


    private boolean isLocationServiceEnabled() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (lm == null) {
            Log.e(TAG, "location manager is null");
            return false;
        }
        return lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }


    private void showLocationServiceDialog() {
        new AlertDialog.Builder(this)
                .setTitle("location service")
                .setMessage("ask for enable location service")
                .setPositiveButton(android.R.string.ok, (paramDialogInterface, paramInt) -> {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                })
                .show();
    }


    private void showLocationPermissionDialog() {

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("fine/coarse location permission request");
        alertDialogBuilder.setMessage("ask for fine/coarse location permission (do this before ask for background location permission)");
        alertDialogBuilder.setPositiveButton(android.R.string.ok, null);

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            alertDialogBuilder.setOnDismissListener(dialog -> requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE_FINE_LOCATION));
        }

        alertDialogBuilder.show();
    }


    private void showToastAndLog(String message) {
        Log.i(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}
