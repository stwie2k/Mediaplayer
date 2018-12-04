package com.example.alias.mediaplayer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import static android.content.ContentValues.TAG;
import static java.security.AccessController.getContext;


public class MusicService extends Service {

    String path="/storage/emulated/0/山高水长.mp3";
    public final IBinder binder = new MyBinder();
    public class MyBinder extends Binder {
        @Override
        protected boolean onTransact(int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
            switch (code) {
                //service solve
                case 0x001:
                    mediaPlayer.start();
                    return true;

                case 0x002:
                    mediaPlayer.pause();
                    return true;

                case 0x003:
                    if (mediaPlayer != null) {
                        mediaPlayer.stop();
                        try {
                            mediaPlayer.reset();
                            mediaPlayer.setDataSource(path);
                            mediaPlayer.prepare();
                            mediaPlayer.seekTo(0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    return true;

                case 0x004:
                    reply.writeInt(mediaPlayer.getCurrentPosition());
                    return true;
                case 0x005:
                    reply.writeInt(mediaPlayer.getDuration());
                    return true;
                case 0x006:
                    data.enforceInterface("MusicService");
                    int progress = data.readInt();

                    mediaPlayer.seekTo(progress);

                    return true;
                default:
                    break;
            }
            return super.onTransact(code, data, reply, flags);
        }
    }
    public MediaPlayer mediaPlayer;
    public MusicService() {
        try {

            mediaPlayer = new MediaPlayer();

         mediaPlayer.setDataSource(path);

            mediaPlayer.prepare();
           mediaPlayer.setLooping(true);

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
               mediaPlayer.setDataSource(path);
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
