package com.example.bgmgo;

import android.app.FragmentTransaction;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private ImageButton playBtn;
    private SeekBar positionBar;
    private TextView songNameLabel;
    private MediaPlayer mp;
    private int totalTime;


    //開始時の処理
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //UIのid取得
        playBtn = findViewById(R.id.playBtn);
        songNameLabel = findViewById(R.id.songName);


        //mediaPlayerの初期化
        mp = MediaPlayer.create(this, R.raw.music);
        mp.setLooping(true);
        mp.start();
        mp.seekTo(0);
        mp.setVolume(0.5f, 0.5f);
        totalTime = mp.getDuration();


        // 再生位置
        positionBar = findViewById(R.id.seekBar);
        positionBar.setMax(totalTime);
        positionBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            mp.seekTo(progress);
                            positionBar.setProgress(progress);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                }
        );


        // MapFragmentの生成
        MapFragment mapFragment = MapFragment.newInstance();
        // MapViewをMapFragmentに変更する
        FragmentTransaction fragmentTransaction =
                getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.mapView, mapFragment);
        fragmentTransaction.commit();
        mapFragment.getMapAsync(this);

        /**TimerTask task = new TimerTask(){
            public void run() {
                while (mp != null) {
                    try {
                        Message msg = new Message();
                        msg.what = mp.getCurrentPosition();
                        handler.sendMessage(msg);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {}
                }
            }
        };**/
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }


// Thread (positionBar・経過時間ラベル・残り時間ラベルを更新する)
       new public void Thread(new Runnable() {
            public void run() {
               while (mp != null) {
                   try {
                       Message msg = new Message();
                       msg.what = mp.getCurrentPosition();
                       handler.sendMessage(msg);
                       Thread.sleep(1000);
                   } catch (InterruptedException e) {}
               }
            }
        })
    Thread.start();


    private Handler handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                int currentPosition = msg.what;
                // 再生位置を更新
                positionBar.setProgress(currentPosition);
                return true;
            }
        });


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
    }}
