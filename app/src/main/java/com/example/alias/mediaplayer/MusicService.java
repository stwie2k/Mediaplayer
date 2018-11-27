package com.example.alias.mediaplayer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import static android.content.ContentValues.TAG;
import static java.security.AccessController.getContext;


public class MusicService extends Service {

    public final IBinder binder = new MyBinder();
    public class MyBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }
    public MediaPlayer mediaPlayer;
    public MusicService() {
        try {

            mediaPlayer = new MediaPlayer();
//          Uri uri = Uri.parse("android.resource://com.example.alias.mediaplayer/"+R.raw.abc);
//          mediaPlayer.setDataSource(this, uri);
//            mediaPlayer.start();
//            mediaPlayer.prepare();
//            mediaPlayer.setLooping(true);
//            mediaPlayer.start();
        } catch (Exception e) {
            Log.e(TAG,Log.getStackTraceString(e));
        }
    }

    public void play() {

        mediaPlayer.start();

    }

    public void pause() {

        mediaPlayer.pause();

    }
    public void stop(){

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource("/data/music.mp3");
                mediaPlayer.prepare();
                mediaPlayer.seekTo(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
