package com.example.isiahlibor.familyapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.sqlitelib.DataBaseHelper;
import com.sqlitelib.SQLite;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    ListView lstview;
    Intent map;
    double latti,longi;
    public Integer cntr = 0;
    public int valueID[];
    public String[] separated;
    ArrayAdapter adapterAccounts;
    LocationManager locationManager;


    private DataBaseHelper dbhelper = new DataBaseHelper(MainActivity.this, "keeperDatabase", 2);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lstview = (ListView)findViewById(R.id.lstview);

        map = new Intent(MainActivity.this, MapsActivity.class);

        Intent sms_intent = getIntent();
        Bundle b = sms_intent.getExtras();


            if (b!=null) {
                try{
                final SQLiteDatabase dbAdd = dbhelper.getWritableDatabase();
                SMSClass smsObj = (SMSClass)b.getSerializable("sms_obj");
                String message = smsObj.getMessage().toString();
                separated = message.split("#");
                if(separated[2] == "VC98C"){
                    String sqlStr = "INSERT INTO tblmessage (emergencyNum, message) VALUES ('" + smsObj.getNumber().toString() + "','"  + message + "')";
                    dbAdd.execSQL(sqlStr);
                    reload();
                }
                }catch(Exception e){
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                }
            }

        reload();
        listview();
        my_location();
    }
    private void  my_location(){
        try{

            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            //check the network provider is enable
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(locationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        //get latitude
                        latti = location.getLatitude();
                        //get longitude
                        longi = location.getLongitude();


                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }

                    @Override
                    public void onProviderEnabled(String provider) {

                    }

                    @Override
                    public void onProviderDisabled(String provider) {

                    }
                });
            } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {

                        //get latitude
                        latti = location.getLatitude();
                        //get longitude
                        longi = location.getLongitude();

                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }

                    @Override
                    public void onProviderEnabled(String provider) {

                    }

                    @Override
                    public void onProviderDisabled(String provider) {

                    }
                });
            }

        }catch(Exception e){
            Toast.makeText(MainActivity.this,""+e,Toast.LENGTH_LONG).show();
        }
    }
    private void listview(){
        final SQLiteDatabase db = dbhelper.getWritableDatabase();
        lstview.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                lstview.setSelector(R.color.colorAccent);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        lstview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                try {
                    cntr=valueID[position];
                } catch (Exception e) {
                    e.printStackTrace();
                }

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle("WARNING!");
                alertDialog.setPositiveButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {
                        dialog.cancel();
                    }
                });
                alertDialog.setNegativeButton("Locate", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {


                        String query = "Select * FROM tblmessage WHERE msgID = '" + cntr + "'";
                        Cursor cursor = db.rawQuery(query, null);

                        if (cursor.moveToFirst()) {
                            cursor.moveToFirst();

                            String[] content = cursor.getString(2).split("#");

                            map.putExtra("latitude", content[0]);
                            map.putExtra("longitude", content[1]);
                            map.putExtra("latti", latti);
                            map.putExtra("longi", longi);

                            startActivity(map);
                            finish();

                        }


                    }
                });
                alertDialog.setNeutralButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        String sqlStr = "DELETE from tblmessage where msgID = '" + cntr + "'";
                        db.execSQL(sqlStr);
                        reload();
                    }
                }); alertDialog.show();

                return false;
            }
        });
    }
    private  void reload(){
        try {

            SQLiteDatabase db = dbhelper.getWritableDatabase();
            Cursor keep = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='tblmessage'", null);
            keep.moveToNext();

            if (keep.getCount() == 0) {
                SQLite.FITCreateTable("keeperDatabase", this, "tblmessage",
                        "msgID INTEGER PRIMARY KEY AUTOINCREMENT," +
                                "emergencyNum VARCHAR(90), message VARCHAR(90)");


            }else{
                keep = db.rawQuery("SELECT *  FROM tblmessage order by msgID desc", null);
                String value[] = new String[keep.getCount()];
                int valueCurrentId[] = new int [keep.getCount()];

                int ctrl = 0;
                while (keep.moveToNext()) {
                    String strFor = "";
                    Integer strId;
                    strFor += "Emergency # : " + keep.getString(keep.getColumnIndex("emergencyNum"));
                    strFor += System.lineSeparator() + "Message : " + keep.getString(keep.getColumnIndex("message"));
                    strId = keep.getInt(keep.getColumnIndex("msgID"));
                    value[ctrl] = strFor;
                    valueCurrentId[ctrl] = strId;

                    ctrl++;
                }
                valueID = Arrays.copyOf(valueCurrentId,keep.getCount());
                adapterAccounts = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, value);
                try {
                    lstview.setAdapter(adapterAccounts);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            Toast.makeText(MainActivity.this,""+e,Toast.LENGTH_LONG).show();
        }
    }
}
