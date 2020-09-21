package com.example.bgmgo;

import android.annotation.SuppressLint;
import android.app.FragmentTransaction;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private ImageButton playBtn;
    private TextView songNameLabel;
    private MediaPlayer mp;
    private int totalTime;
    private static final String TAG = "MyActivity";


    //開始時の処理
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //UIのid取得
        playBtn = findViewById(R.id.playBtn);
        songNameLabel = findViewById(R.id.songName);

        songNameLabel.setSelected(true);

        //mediaPlayerの初期化
        mp = MediaPlayer.create(this, R.raw.music);
        mp.setLooping(true);
        mp.start();
        mp.seekTo(0);
        mp.setVolume(0.5f, 0.5f);


    // MapFragmentの生成
        MapFragment mapFragment = MapFragment.newInstance();
        // MapViewをMapFragmentに変更する
        FragmentTransaction fragmentTransaction =
                getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.mapView, mapFragment);
        fragmentTransaction.commit();
        mapFragment.getMapAsync(this);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
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

}
