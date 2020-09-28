package com.example.bgmgo;

import android.Manifest;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import wseemann.media.FFmpegMediaMetadataRetriever;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {
    private GoogleMap mMap;
    private ImageButton playBtn;
    private TextView songNameLabel;
    private MediaPlayer mp;
    private int totalTime;
    private static final String TAG = "MyActivity";
    private String locationProvider;
    int lastTimeNumber = 0;
    String userLocation = "outdoor";

    LocationManager locationManager;

    //開始時の処理
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //PermissionCheck
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,},
                    1000);
        } else {
            locationStart();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    1000, 50, this);

        }

        // 位置情報を管理している LocationManager のインスタンスを生成する
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        String locationProvider;

        // GPSが利用可能になっているかどうかをチェック
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationProvider = LocationManager.GPS_PROVIDER;
        } // GPSプロバイダーが有効になっていない場合は基地局情報が利用可能になっているかをチェック
        else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationProvider = LocationManager.NETWORK_PROVIDER;
        }
        // いずれも利用可能でない場合は、GPSを設定する画面に遷移する
        else {
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
            return;
        }
        // 最新の位置情報
        Location location = locationManager.getLastKnownLocation(locationProvider);
        if (location != null) {
            Log.d(TAG, "onCreate:Lat " + location.getLatitude() + ", onCreate:Lon" + location.getLongitude());
        }

        //UIのid取得
        playBtn = findViewById(R.id.playBtn);
        songNameLabel = findViewById(R.id.songName);
        songNameLabel.setSelected(true);

        //mediaPlayerの初期化
        //mp.setOnCompletionListener((MediaPlayer.OnCompletionListener) this);
        startNextTrack();




        // MapFragmentの生成
        MapFragment mapFragment = MapFragment.newInstance();
        // MapViewをMapFragmentに変更する
        FragmentTransaction fragmentTransaction =
                getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.mapView, mapFragment);
        fragmentTransaction.commit();
        mapFragment.getMapAsync(this);

        /**mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                Log.v("MediaPlayer", "next");
                startNextTrack();

            }
        });**/
    }


    private void locationStart() {
        Log.d("debug", "locationStart()");

        // LocationManager インスタンス生成
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d("debug", "location manager Enabled");
        } else {
            // GPSを設定するように促す
            Intent settingsIntent =
                    new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
            Log.d("debug", "not gpsEnable, startActivity");
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);

            Log.d("debug", "checkSelfPermission false");
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1000) {
            // 使用が許可された
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("debug", "checkSelfPermission true");

                locationStart();

            } else {
                // それでも拒否された時の対応
                Toast toast = Toast.makeText(this,
                        "これ以上なにもできません", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    //位置情報変更時
    @Override
    public void onLocationChanged(Location location) {
        if (mMap != null) {
            // 緯度の表示
            String str1 = "Latitude:" + location.getLatitude();
            Log.d(str1, "onLocationChanged: ");
            // 経度の表示
            String str2 = "Longitude:" + location.getLongitude();
            Log.d(str2, "onLocationChanged: ");
            //表示
            LatLng newLocation = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(newLocation));
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(newLocation, 18);
            mMap.moveCamera(cameraUpdate);
        }
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mMap != null) {
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
            mMap.setMyLocationEnabled(true);
        }
    }


    //再生ボタンの処理
    public void playBtnClick(View view) {
        if (!mp.isPlaying()) {
            // 停止中
            mp.start();
            playBtn.setBackgroundResource(R.drawable.stop);

        } else {
            // 再生中
            mp.pause();
            playBtn.setBackgroundResource(R.drawable.play);
        }
    }

    public void startNextTrack() {
        int path = R.raw.outdoor0;
        String songTitle = "songtitle";
        int random = 0;
        for (int i = 0; i < 1;){
            random = (int) (Math.random() *2);
            if(random != lastTimeNumber){
                i = 2;
            }
        }


            if(random == 0 && userLocation == "outdoor"){
                path = R.raw.outdoor0;
                songTitle = "Hide-and-seek / Zukisuzuki";
            }
            else if(random == 1 && userLocation == "outdoor"){
                path = R.raw.outdoor1;
                songTitle = "High Speed Flash / Vegaenduro";
            }


            if(random == 0 && userLocation == "indoor"){
                path = R.raw.indoor0;
                songTitle = "Deal / AShamaluevMusic";
            }
            else if(random == 1 && userLocation == "indoor"){
                path = R.raw.indoor1;
                songTitle = "Basic drives / Expendable Friend";
            }


            if(random == 0 && userLocation == "nature"){
                path = R.raw.nature0;
                songTitle = "Purpose / AShamaluevMusic";
            }
            else if(random == 1 && userLocation == "nature") {
                path = R.raw.nature1;
                songTitle = "Films & Serials / AShamaluevMusic";
            }


            lastTimeNumber = random;

            mp = MediaPlayer.create(getApplicationContext(), path);
            mp.setLooping(false);
            mp.seekTo(0);

            FFmpegMediaMetadataRetriever retriever = new  FFmpegMediaMetadataRetriever();
            retriever.release();
            songNameLabel.setText(songTitle);
            Log.v("MediaPlayer", "next isssssssssssssssssssssssssssssssssssssssssssssssss" + songTitle);
            mp.start();

            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    Log.v("MediaPlayer", "next");
                    startNextTrack();

                }
            });
    }
}



