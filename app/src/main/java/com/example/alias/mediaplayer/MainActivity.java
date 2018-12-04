package com.example.alias.mediaplayer;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static android.content.ContentValues.TAG;
import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity {

    boolean playing = false;
    boolean anistart=false;
    boolean tag=false;
    private SeekBar seekBar;
    private MusicService musicService;
    private SimpleDateFormat time = new SimpleDateFormat("mm:ss");
    TextView currenttime;
    TextView totaltime;
    TextView title;
    TextView singer;
    CircleImageView cv;
    ImageView play;
    ObjectAnimator animator;

    private IBinder mBinder;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = service;

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBinder = null;
        }
    };

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            currenttime.setText(time.format(musicService.mediaPlayer.getCurrentPosition()));

            seekBar.setProgress(musicService.mediaPlayer.getCurrentPosition());
            seekBar.setMax(musicService.mediaPlayer.getDuration());

            handler.postDelayed(runnable, 200);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currenttime=findViewById(R.id.MusicTime);
        totaltime=findViewById(R.id.MusicTotal);
        title=findViewById(R.id.title);
        singer=findViewById(R.id.singer);

        cv=findViewById(R.id.Image);

        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, sc, BIND_AUTO_CREATE);
        startService(intent);



        animator = ObjectAnimator.ofFloat(cv, "rotation", 0f, 360.0f);
        animator.setDuration(10000);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(-1);

        seekBar = findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser == true) {
                    try{
                        Parcel data = Parcel.obtain();
                        Parcel reply = Parcel.obtain();
                        data.writeInterfaceToken("MusicService");
                        data.writeInt(progress);
                        mBinder.transact(0x006, data, reply, 0);
                    }catch (Exception e){

                    }

//                    musicService.mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        ImageView opnefile = findViewById(R.id.file);
        opnefile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playing)
                {
                    try{
                        Parcel data = Parcel.obtain();
                        Parcel reply = Parcel.obtain();
                        mBinder.transact(0x002, data, reply, 0);
                    }catch (Exception e){

                    }
                    playing = false;

                    animator.pause();
                    anistart=false;
                    play.setImageResource(R.drawable.play);
                }

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                //intent.setType(“image/*”);//选择图片
                intent.setType("audio/*"); //选择音频
                //intent.setType(“video/*”); //选择视频 （mp4 3gp 是android支持的视频格式）
                //intent.setType(“video/*;image/*”);//同时选择视频和图片
//                intent.setType("*/*");//无类型限制
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);

            }
        });


         play = findViewById(R.id.play);
        ImageView stop = findViewById(R.id.stop);
        ImageView quit =findViewById(R.id.quit);


        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!tag){

                    if (playing == true) {
                        try{
                            Parcel data = Parcel.obtain();
                            Parcel reply = Parcel.obtain();
                            mBinder.transact(0x002, data, reply, 0);
                        }catch (Exception e){

                        }
                        playing = false;

                        animator.pause();

                        play.setImageResource(R.drawable.play);

                    } else {

                        try{
                            Parcel data = Parcel.obtain();
                            Parcel reply = Parcel.obtain();
                            mBinder.transact(0x001, data, reply, 0);
                        }catch (Exception e){

                        }
                        playing = true;


                        play.setImageResource(R.drawable.pause);

                        if(anistart==false){
                            animator.start();
                            anistart=true;
//                        totaltime.setText(time.format(musicService.mediaPlayer.getDuration()));
                        }
                        else{
                            animator.resume();
                        }


                    }
                    int total=0;
                    try{
                        Parcel data = Parcel.obtain();
                        Parcel reply = Parcel.obtain();
                        mBinder.transact(0x005, data, reply, 0);
                        total=reply.readInt();
                    }catch (Exception e){

                    }





                    totaltime.setText(time.format(total));
                    seekBar.setMax(total);
                    Observable observable= Observable.create(new ObservableOnSubscribe<Integer>() {
                        @Override
                        public void subscribe(ObservableEmitter<Integer> e) throws Exception {

                            for (int i = 0; i < 1000; i++) {

                                try {
                                    Thread.sleep(500); //模拟下载的操作。
                                } catch (InterruptedException exception) {
                                    if (!e.isDisposed()) {
                                        e.onError(exception);
                                    }
                                }

                                int current=0;
                                try{
                                    Parcel data = Parcel.obtain();
                                    Parcel reply = Parcel.obtain();
                                    mBinder.transact(0x004, data, reply, 0);
                                    current=reply.readInt();
                                }catch (Exception ex){

                                }



                                e.onNext(current);

                            }
                            e.onComplete();


                        }
                    });






                    DisposableObserver<Integer> disposableObserver = new DisposableObserver<Integer>() {

                        @Override
                        public void onNext(Integer value) {
                            Log.d("BackgroundActivity", "onNext=" + value);

                            currenttime.setText(time.format(value));

                            seekBar.setProgress(value);
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d("BackgroundActivity", "onError=" + e);

                        }

                        @Override
                        public void onComplete() {
                            Log.d("BackgroundActivity", "onComplete");

                        }



                    };

                    observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(disposableObserver);
                    mCompositeDisposable.add(disposableObserver);







//                    handler.post(runnable);
//                    tag=true;
                }


            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    Parcel data = Parcel.obtain();
                    Parcel reply = Parcel.obtain();
                    mBinder.transact(0x003, data, reply, 0);
                }catch (Exception e){

                }
                playing = false;

                animator.pause();

                anistart=false;

                play.setImageResource(R.drawable.play);
            }
        });

        quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.removeCallbacks(runnable);
                unbindService(sc);
                Intent intent = new Intent(MainActivity.this, MusicService.class);
                stopService(intent);
                try {
                    MainActivity.this.finish();
                    System.exit(0);
                } catch (Exception e) {

                }
            }
        });

    }

    String path;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            try{



                path = getPath(this, uri);

                try{
                    Parcel data1 = Parcel.obtain();
                    Parcel reply = Parcel.obtain();
                    data1.writeInterfaceToken("MusicService");
                    data1.writeString(path);
                    mBinder.transact(0x007, data1, reply, 0);
                }catch (Exception e){

                }


//                musicService.mediaPlayer.reset();
//                        musicService.mediaPlayer.setDataSource(this,uri);
//                musicService.mediaPlayer.prepare();

                int total=0;
                try{
                    Parcel data2 = Parcel.obtain();
                    Parcel reply = Parcel.obtain();
                    mBinder.transact(0x005, data2, reply, 0);
                    total=reply.readInt();
                }catch (Exception e){

                }
                totaltime.setText(time.format(total));

                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(path);
                String ti=mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

                String art=mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);

                title.setText(ti);
                singer.setText(art);

                byte[] data1 = mmr.getEmbeddedPicture();
                if(data1!=null)
                {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data1, 0, data1.length);
                    cv.setImageBitmap(bitmap);
                }










            }catch (Exception e){
                Log.e(TAG,Log.getStackTraceString(e));
            }




        }

    }


    @SuppressLint("NewApi")
    public String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public String getDataColumn(Context context, Uri uri, String selection,
                                String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }






}
