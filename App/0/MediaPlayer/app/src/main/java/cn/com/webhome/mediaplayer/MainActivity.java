package cn.com.webhome.mediaplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private SurfaceView m_SurfaceView = null;
    private SurfaceHolder m_SurfaceHolder = null;
    private MediaPlayer m_MediaPlayer = null;

    private static final int PERMISSION_REQUEST_CODE = 200;
    private boolean m_PermissionGranted = false;
    private boolean m_PlayerInitialized = false;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    m_PermissionGranted = true;
                } else {
                    Toast.makeText(this, "未授权访问存储器!", Toast.LENGTH_LONG).show();
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public boolean requestPermissions() {
        int storagePermission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (storagePermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            return false;
        }
        m_PermissionGranted = true;
        return true;
    }

    private void startPlayer() {
        if (!m_PermissionGranted) {
            requestPermissions();
            return;
        }

        try {
            m_MediaPlayer.setDataSource("/storage/emulated/0/Movies/Test.ts");
            m_MediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);//缩放模式
            m_MediaPlayer.setLooping(true);//设置循环播放
            m_MediaPlayer.prepareAsync();//异步准备
            m_MediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                    m_PlayerInitialized = true;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "MediaPlayer.setDataSource() failed.");
        }
    }

    private void stopPlayer() {
        m_MediaPlayer.stop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions();

        m_MediaPlayer = new MediaPlayer();
        m_SurfaceView = findViewById(R.id.media_player_surface);
        m_SurfaceView.setKeepScreenOn(true);

        m_SurfaceHolder = m_SurfaceView.getHolder();
        m_SurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                m_MediaPlayer.setDisplay(m_SurfaceHolder);
                startPlayer();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                m_MediaPlayer.setDisplay(null);
                stopPlayer();
            }
        });


        m_SurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (m_PlayerInitialized) {
                    if (m_MediaPlayer.isPlaying()) {
                        m_MediaPlayer.pause();
                    } else {
                        m_MediaPlayer.start();
                    }
                } else {
                    if (!m_SurfaceHolder.isCreating()) {
                        startPlayer();
                    }
                }
                return false;
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        m_MediaPlayer.release();
        m_MediaPlayer = null;
    }
}