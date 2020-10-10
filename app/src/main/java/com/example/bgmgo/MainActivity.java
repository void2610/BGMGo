package com.example.bgmgo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaPlayer;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import java.nio.ByteBuffer;

import wseemann.media.FFmpegMediaMetadataRetriever;

import static android.graphics.PixelFormat.*;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {
    private static final String TAG = "MyActivity";

    //地図
    private GoogleMap mMap;
    LocationManager locationManager;
    private MapView mapView;

    //音楽
    private ImageButton playBtn;
    private TextView songNameLabel;
    private MediaPlayer mp;
    private int totalTime;
    private String locationProvider;
    String userLocation = "road";
    private boolean TrackStartOnce=true;

    //RGB
    int screenCenterX;
    int screenCenterY;
    int lastTimeNumber = 0;
    //ScreenShot
    private MediaProjectionManager mpManager;
    private MediaProjection mProjection;
    private static final int REQUEST_MEDIA_PROJECTION = 1001;

    private int displayWidth, displayHeight;
    private ImageReader imageReader;
    private VirtualDisplay virtualDisplay;
    private int screenDensity;
    private ImageView imageView;
    private boolean canTakeSS = false;

    //開始時の処理
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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


        WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        Display disp = wm.getDefaultDisplay();

        Point realSize = new Point();
        disp.getRealSize(realSize);

        int realScreenWidth = realSize.x;
        int realScreenHeight = realSize.y;
        screenCenterX = realScreenWidth / 2;
        screenCenterY = realScreenHeight / 2 + 500;

        // MapFragmentの生成
        MapFragment mapFragment = MapFragment.newInstance();
        // MapViewをMapFragmentに変更する
        FragmentTransaction fragmentTransaction =
                getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.mapView, mapFragment);
        fragmentTransaction.commit();
        mapFragment.getMapAsync(this);

        //ScreenShot
        // 画面の縦横サイズとdpを取得
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenDensity = displayMetrics.densityDpi;
        displayWidth = displayMetrics.widthPixels;
        displayHeight = displayMetrics.heightPixels;

        mpManager = (MediaProjectionManager)
                getSystemService(MEDIA_PROJECTION_SERVICE);

        // permissionを確認するintentを投げ、ユーザーの許可・不許可を受け取る
        if(mpManager != null){
            startActivityForResult(mpManager.createScreenCaptureIntent(),
                    REQUEST_MEDIA_PROJECTION);
        }

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
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
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

            if (canTakeSS){
                getRGB();
                if (TrackStartOnce){
                    startNextTrack();
                }
                TrackStartOnce=false;
            }
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
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(false);
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
        String songPath = "outdoor" + 0;
        String songTitle = "songtitle";
        int random = 0;
        for (int i = 0; i < 1;){
            random = (int) (Math.random() *4);
            songPath = userLocation + random;
            Log.v("abcd" , "Path is " + songPath);
            if(random != lastTimeNumber){
                i = 2;
            }
        }



            if(userLocation.equals("road") && random == 0){
                path = R.raw.outdoor0;
                songTitle = "Hide-and-seek / Zukisuzuki";
            }
            else if(userLocation.equals("road") && random == 1){
                path = R.raw.outdoor1;
                songTitle = "High Speed Flash / Vegaenduro";
            }
            else if(userLocation.equals("road") && random == 2){
                path = R.raw.outdoor2;
                songTitle = "Blood on the Dance Floor;  / AShamaluevMusic";
            }
            else if(userLocation.equals("road") && random == 3){
                path = R.raw.outdoor3;
                songTitle = "Corporate Motivation / AShamaluevMusic";
            }


            if(userLocation.equals("indoor") && random == 0){
                path = R.raw.indoor0;
                songTitle = "Deal / AShamaluevMusic";
            }
            else if(userLocation.equals("indoor") && random == 1){
                path = R.raw.indoor1;
                songTitle = "Basic drives / Expendable Friend";
            }
            else if(userLocation.equals("indoor") && random == 2){
                path = R.raw.indoor2;
                songTitle = "Stuff / AShamaluevMusic";
            }
            else if(userLocation.equals("indoor") && random == 3){
                path = R.raw.indoor3;
                songTitle = "Proud / AShamaluevMusic";
            }


            if(userLocation.equals("nature") && random == 0){
                path = R.raw.nature0;
                songTitle = "Purpose / AShamaluevMusic";
            }
            else if(userLocation.equals("nature") && random == 1) {
                path = R.raw.nature1;
                songTitle = "Films & Serials / AShamaluevMusic";
            }
            else if(userLocation.equals("nature") && random == 2) {
                path = R.raw.nature2;
                songTitle = "Loveliness / AShamaluevMusic";
            }
            else if(userLocation.equals("nature") && random == 3) {
                path = R.raw.nature3;
                songTitle = "Epic Emotional / AShamaluevMusic";
            }


            lastTimeNumber = random;

            mp = MediaPlayer.create(getApplicationContext(), path);
            mp.setLooping(false);
            mp.seekTo(0);

            FFmpegMediaMetadataRetriever retriever = new  FFmpegMediaMetadataRetriever();
            retriever.release();
            songNameLabel.setText(songTitle);
            Log.v("MediaPlayer", "next isssssssssssssssssssssssssssssssssssssssssssssssss: " + songTitle);
            mp.start();

            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    Log.v("MediaPlayer", "next");
                    startNextTrack();

                }
            });
    }

    // ユーザーの許可を受け取る
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (REQUEST_MEDIA_PROJECTION == requestCode) {
            if (resultCode != RESULT_OK) {
                // 拒否された
                Toast.makeText(this,
                        "User cancelled", Toast.LENGTH_LONG).show();
                return;
            }
            // 許可された結果を受け取る
            setUpMediaProjection(resultCode, data);
            canTakeSS = true;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setUpMediaProjection(int code, Intent intent) {
        mProjection = mpManager.getMediaProjection(code, intent);
        setUpVirtualDisplay();
    }

    @SuppressLint("WrongConstant")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setUpVirtualDisplay() {
        imageReader = ImageReader.newInstance(
                displayWidth, displayHeight, RGBA_8888, 2);

        virtualDisplay = mProjection.createVirtualDisplay("ScreenCapture",
                displayWidth, displayHeight, screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.getSurface(), null, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private Bitmap getScreenshot() {
        // ImageReaderから画面を取り出す
        Log.d("debug", "getScreenshot");

        Image image = imageReader.acquireLatestImage();
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();

        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * displayWidth;

        // バッファからBitmapを生成
        Bitmap bitmap = Bitmap.createBitmap(
                displayWidth + rowPadding / pixelStride, displayHeight,
                Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        image.close();
        return bitmap;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void getRGB() {
        Log.d(TAG, "getRGB: getRGB");
        Bitmap bmp1 = getScreenshot();
        int colour = bmp1.getPixel(screenCenterX, screenCenterY);
        int red = Color.red(colour);
        int blue = Color.blue(colour);
        int green = Color.green(colour);
        Log.d("RGB", "r,g,b = " + red  + "," + green + "," + blue);
        String rgb = Integer.toString(red) + Integer.toString(green) + Integer.toString(blue);
        Log.d(TAG, "getRGB: "+rgb);
        if(rgb.equals("240240240")){
            userLocation = "indoor";
            Log.d("RGB", "getRGB: "+"userLocation:"+userLocation);
        }
        if(rgb.equals("242242242") || rgb.equals("248249250") || rgb.equals("255255255")){
            userLocation = "road";
            Log.d("RGB", "getRGB: "+"userLocation:"+userLocation);
        }
        if(rgb.equals("192236173") || rgb.equals("170218255") || rgb.equals("223223223") || rgb.equals("197232197") || rgb.equals("173219255")){
            userLocation = "nature";
            Log.d("RGB", "getRGB: "+"userLocation:"+userLocation);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onDestroy() {
        if (virtualDisplay != null) {
            Log.d("debug","release VirtualDisplay");
            virtualDisplay.release();
        }
        super.onDestroy();
    }
}



