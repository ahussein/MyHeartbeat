package de.ludetis.android.myheartbeat;

import android.app.Activity;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import android.os.Environment;

import java.sql.Timestamp;
import java.util.ArrayList;


public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private TextView textView;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    FileWriter writer;
    File root = Environment.getExternalStorageDirectory();
    ArrayList<String> content = new ArrayList<String>();
    String header = "lat,lng,time,heartbeat\n";

    private void writeCsvData(ArrayList<String> data) throws IOException {
        File gpxfile = new File(root, String.format("labs_heartrate_%s.csv", new Timestamp(new java.util.Date().getTime()).getTime()));
        writer = new FileWriter(gpxfile);
        writer.write(header);
        for (String item : data){
            writer.write(item);
        }
        writer.flush();
        writer.close();

    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // message from API client! message from wear! The contents is the heartbeat.
//            Toast.makeText(MainActivity.this, "handleMessage" + mCurrentLocation, Toast.LENGTH_SHORT).show();
            if(textView!=null && mCurrentLocation!=null) {
                textView.setText(Integer.toString(msg.what));
                String value = String.format("%f,%f,%s,%d\n", mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(),
                        new Timestamp(new java.util.Date().getTime()).getTime(), msg.what);
                if (content.size() == 500){
                    try{
                        writeCsvData(content);
                    }catch(IOException e){
                        e.printStackTrace();
                    }

                    content.clear();
                }
                content.add(value);
//                Toast.makeText(MainActivity.this, "handleMessage: " + value, Toast.LENGTH_SHORT).show();
            }
            else if(textView !=null)
                textView.setText(Integer.toString(msg.what) + "null");
        }

    };

    protected synchronized void buildGoogleApiClient() {
        Toast.makeText(this, "buildGoogleApiClient", Toast.LENGTH_SHORT).show();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
//        Toast.makeText(this, "onConnected", Toast.LENGTH_SHORT).show();
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        startLocationUpdates();
//        Toast.makeText(this, "location" + mCurrentLocation.toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
//        Toast.makeText(this, "onConnectionSuspended", Toast.LENGTH_SHORT).show();

    }

    protected void startLocationUpdates() {
        Toast.makeText(this, "startLocationUpdates", Toast.LENGTH_SHORT).show();

        LocationRequest mLocationRequest = createLocationRequest();
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

    }

    protected LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.heartbeat);
        buildGoogleApiClient();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // register our handler with the DataLayerService. This ensures we get messages whenever the service receives something.
        DataLayerListenerService.setHandler(handler);
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        // unregister our handler so the service does not need to send its messages anywhere.
        DataLayerListenerService.setHandler(null);
        super.onPause();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Toast.makeText(this, "onConnectionFailed", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
//        Toast.makeText(this, "location changed" + mCurrentLocation.toString(), Toast.LENGTH_SHORT).show();
    }
}


