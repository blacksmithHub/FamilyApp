package com.example.isiahlibor.familyapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    double myLat, myLong, lat,longi;
    Intent back;
    private LatLng intruder_location, my_location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        try{
            Bundle extras = getIntent().getExtras();
            if(extras == null){
                myLat = 0.0;
                myLong = 0.0;
                lat = 0.0;
                longi = 0.0;
            }else {
                myLat = Double.parseDouble(extras.getString("latitude"));
                myLong = Double.parseDouble(extras.getString("longitude"));
                lat = Double.parseDouble(extras.getString("latti"));
                longi = Double.parseDouble(extras.getString("longi"));
            }

        }catch (Exception e){
            myLat = 0.0;
            myLong = 0.0;
            lat = 0.0;
            longi = 0.0;
        }

    }

    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {
            String data = "";

            try {
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);

        }
    }
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();
        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        try {
            Geocoder geocoder = new Geocoder(getApplicationContext());

            my_location = new LatLng(myLat,myLong);
            intruder_location = new LatLng(lat,longi);

            List<Address> addressList = geocoder.getFromLocation(myLat,myLong,1);
            String str = addressList.get(0).getLocality()+", ";
            str += addressList.get(0).getCountryName();

            mMap.addMarker(new MarkerOptions().position(my_location).title(str));

            List<Address> Intruder_addressList = geocoder.getFromLocation(lat,longi,1);
            String intruder_str = Intruder_addressList.get(0).getLocality()+", ";
            intruder_str += Intruder_addressList.get(0).getCountryName();

            mMap.addMarker(new MarkerOptions().position(intruder_location).title(intruder_str));

            mMap.moveCamera(CameraUpdateFactory.newLatLng(my_location));

            String str_origin = "origin=" + my_location.latitude + "," + my_location.longitude;
            String str_dest = "destination=" + intruder_location.latitude + "," + intruder_location.longitude;
            String sensor = "sensor=false";
            String parameters = str_origin + "&" + str_dest + "&" + sensor;
            String output = "json";
            String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

            Log.d("onMapClick", url.toString());
            FetchUrl FetchUrl = new FetchUrl();
            FetchUrl.execute(url);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(my_location));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(7));

            Location origin_location = new Location(str);
            origin_location.setLatitude(my_location.latitude);
            origin_location.setLongitude(my_location.longitude);

            Location destination_location = new Location(intruder_str);
            destination_location.setLatitude(intruder_location.latitude);
            destination_location.setLongitude(intruder_location.longitude);

            double distance = (origin_location.distanceTo(destination_location))* 0.000621371 ;

            AlertDialog alertDialog = new AlertDialog.Builder(MapsActivity.this).create();
            alertDialog.setTitle("Info");
            alertDialog.setMessage("Distance between these two location is : "+distance +" miles");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();


        } catch (Exception e) {
            Toast.makeText(MapsActivity.this,""+e,Toast.LENGTH_LONG).show();
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask",jsonData[0].toString());
                JSONParserTask parser = new JSONParserTask();
                Log.d("ParserTask", parser.toString());
                routes = parser.parse(jObject);
                Log.d("ParserTask","Executing routes");
                Log.d("ParserTask",routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask",e.toString());
                e.printStackTrace();
            }
            return routes;
        }
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = result.get(i);
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.RED);

                Log.d("onPostExecute","onPostExecute lineoptions decoded");

            }
            if(lineOptions != null) {
                mMap.addPolyline(lineOptions);
            }
            else {
                Log.d("onPostExecute","without Polylines drawn");
            }
        }
    }

}
