package com.example.nocompany.noorfmVersion2;



        import java.util.concurrent.TimeUnit;

        import android.app.ProgressDialog;
        import android.content.Context;
        import android.content.Intent;
        import android.media.AudioManager;
        import android.net.wifi.WifiManager;
        import android.os.PowerManager;
        import android.view.MenuItem;
        import android.view.MotionEvent;
        import android.view.View.OnTouchListener;
        import android.media.MediaPlayer.OnBufferingUpdateListener;
        import android.media.MediaPlayer.OnCompletionListener;
        import android.media.MediaPlayer;
        import android.os.Bundle;
        import android.os.Handler;
        import android.app.Activity;
        import android.view.Menu;
        import android.view.View;
        import android.widget.ImageButton;
        import android.widget.ImageView;
        import android.widget.SeekBar;
        import android.widget.TextView;
        import android.widget.Toast;


public class MainActivity extends Activity implements  OnTouchListener, OnCompletionListener, OnBufferingUpdateListener {
    private final String constPath ="https://dl.dropboxusercontent.com/u/96430851/songs/";
    private int mediaFileLengthInMilliseconds; // this value contains the song duration in milliseconds. Look at getDuration() method in MediaPlayer class
    public TextView songName,startTimeField,endTimeField;
    private MediaPlayer mediaPlayer;
    private double startTime = 0;
    private double finalTime = 0;
    private Handler  myHandler = new Handler();
    private SeekBar seekbar;
    private ImageButton playButton,pauseButton;
    private ImageView trackImage;
    private String urlTrack;
    private int trackNumbers;
    private int trackPostion;
    private final String TAG = "";
    private ProgressDialog progress ;
    private WifiManager.WifiLock wifiLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progress = new ProgressDialog(this);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        progress.setMessage("Loading Track..");


        initView();
        Intent intent = getIntent();
        trackNumbers = intent.getIntExtra("trackNumbers",0);
        trackPostion = intent.getIntExtra("trackNumber",0) + 1 ;

        playSong(trackPostion);

    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    /** This method initialise all the views in project*/
    private void initView() {
        trackImage = (ImageView)findViewById(R.id.imageView1);
        songName = (TextView)findViewById(R.id.trackName);
        startTimeField =(TextView)findViewById(R.id.minTime);
        endTimeField =(TextView)findViewById(R.id.maxTime);
        seekbar = (SeekBar)findViewById(R.id.seekBar1);

        playButton = (ImageButton)findViewById(R.id.play);
        playButton.setEnabled(false);
        pauseButton = (ImageButton)findViewById(R.id.pause);
        pauseButton.setEnabled(true);

        seekbar.setMax(99); // It means 100% .0-99
        seekbar.setOnTouchListener(this);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnCompletionListener(this);
        wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");

        wifiLock.acquire();

    }

    private void playSong(int songPos){



        urlTrack = constPath +"Track"+songPos+".mp3";
        songName.setText("Track"+songPos);

        try {
            mediaPlayer.setDataSource(urlTrack);
            mediaPlayer.prepareAsync();
            progress.show();

        } catch (Exception e) {
            e.printStackTrace();
        }

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {

                progress.dismiss();
                playButton.setEnabled(false);
                pauseButton.setEnabled(true);
                trackImage.setImageResource(R.drawable.microphone1_onair);

                mediaPlayer.start();

                finalTime = mediaPlayer.getDuration();
                startTime = mediaPlayer.getCurrentPosition();
                mediaFileLengthInMilliseconds = mediaPlayer.getDuration(); // gets the song length in milliseconds from URL

                endTimeField.setText(String.format("%d min, %d sec",
                                TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                                TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                                toMinutes((long) finalTime)))
                );
                startTimeField.setText(String.format("%d min, %d sec",
                                TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                                TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                                toMinutes((long) startTime)))
                );


                primarySeekBarProgressUpdater();
            }
        });


    }

    private void primarySeekBarProgressUpdater() {
        seekbar.setProgress((int)(((float)mediaPlayer.getCurrentPosition()/mediaFileLengthInMilliseconds)*100)); // This math construction give a percentage of "was playing"/"song length"
        startTime = mediaPlayer.getCurrentPosition();
        startTimeField.setText(String.format("%d min, %d sec",
                        TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                        TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                        toMinutes((long) startTime)))
        );
        if (mediaPlayer != null)
            if (mediaPlayer.isPlaying()) {
                Runnable notification = new Runnable() {
                    public void run() {
                        primarySeekBarProgressUpdater();
                    }
                };
                myHandler.postDelayed(notification,1000);
            }
    }

    public void play(View view){
        Toast.makeText(getApplicationContext(), "Playing sound",
                Toast.LENGTH_SHORT).show();

        trackImage.setImageResource(R.drawable.microphone1_onair);
        mediaPlayer.start();

        playButton.setEnabled(false);
        pauseButton.setEnabled(true);

        primarySeekBarProgressUpdater();

        startTime = mediaPlayer.getCurrentPosition();



    }

    public void pause(View view){
        Toast.makeText(getApplicationContext(), "Pausing sound",
                Toast.LENGTH_SHORT).show();

        mediaPlayer.pause();
        trackImage.setImageResource(R.drawable.microphone1);
        pauseButton.setEnabled(false);
        playButton.setEnabled(true);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_track_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
           Intent intent =new Intent(this,About.class);
            startActivity(intent);

        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(v.getId() == R.id.seekBar1){
/** Seekbar onTouch event handler. Method which seeks MediaPlayer to seekBar primary progress position*/
            if(mediaPlayer.isPlaying()){
                SeekBar sb = (SeekBar)v;
                int playPositionInMillisecconds = (mediaFileLengthInMilliseconds / 100) * sb.getProgress();
                mediaPlayer.seekTo(playPositionInMillisecconds);
            }
        }
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
/** MediaPlayer onCompletion event handler. Method which calls then song playing is complete*/
        trackImage.setImageResource(R.drawable.microphone1);
        mediaPlayer.stop();
        mediaPlayer.reset();
        playNext();
    }
    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
/** Method which updates the SeekBar secondary progress by current song loading from URL position*/
        seekbar.setSecondaryProgress(percent);
    }

    public void nextTrack(View view) {
        trackImage.setImageResource(R.drawable.microphone1);
        mediaPlayer.stop();
        mediaPlayer.reset();
        playNext();
    }

    private void playNext(){
        if(trackPostion == trackNumbers)
        {
            trackPostion = 1;
            playSong(trackPostion);
        }
        else {
            trackPostion++;

            playSong(trackPostion);
        }
    }
    public void prevTrack(View view) {
        trackImage.setImageResource(R.drawable.microphone1);
        mediaPlayer.stop();
        mediaPlayer.reset();
        playPrev();
    }
    private void playPrev(){
        if(trackPostion == 1){
            trackPostion = trackNumbers;
            playSong(trackPostion);
        }else {
            trackPostion--;
            playSong(trackPostion);
        }
    }


    private void releaseMediaPlayer() {
        myHandler.removeCallbacksAndMessages(null);
        wifiLock.release();

        if (mediaPlayer != null) {
            if(mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();

    }

}

