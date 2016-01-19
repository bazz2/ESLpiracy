package com.bazz2.eslpiracy;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.crypto.ShortBufferException;

public class MainActivity extends Activity
    implements MediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener {
    private final String eslpodWebsite = "http://www.eslpod.com/website/show_all.php";
    private ImageButton btnPlay;
    private ImageButton btnNext;
    private ImageButton btnPrev;
    private SeekBar songProgressBar;
    private TextView songTitleLabel;
    private TextView songCurrentDurationLabel;
    private TextView songTotalDurationLabel;
    private MediaPlayer mp;
    private Handler mHandler = new Handler();
    private SongsManager songManager;
    private Utilities utils;
    private int currentSongIndex = 0;
    private String localAudioPath = null;
    private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
    private boolean isDownloading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            localAudioPath = getExternalFilesDir(null).getAbsolutePath();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        btnPlay = (ImageButton) findViewById(R.id.btnPlay);
        btnNext = (ImageButton) findViewById(R.id.btnNext);
        btnPrev = (ImageButton) findViewById(R.id.btnPrev);
        songProgressBar = (SeekBar) findViewById(R.id.songProgressBar);
        songTitleLabel = (TextView) findViewById(R.id.songTitle);
        songCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);
        songTotalDurationLabel = (TextView) findViewById(R.id.songTotalDurationLabel);

        mp = new MediaPlayer();
        songManager = new SongsManager(localAudioPath);
        utils = new Utilities();

        songProgressBar.setOnSeekBarChangeListener(this);
        mp.setOnCompletionListener(this);

        songsList = songManager.getPlayList();
        if (songsList.size() > 0)
            playSong(0);

        btnPlay.setOnClickListener(mClick);
        btnNext.setOnClickListener(mClick);
        btnPrev.setOnClickListener(mClick);
    }

    private View.OnClickListener mClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnPlay:
                    ArrayList<HashMap<String, String>> currentSongsList = songManager.getPlayList();
                    if (currentSongsList.size() == 0) {
                        // TODO: execute the download thread
                        Thread thread = new downloadAudiosThread(eslpodWebsite);
                        thread.start();
                        return;
                    }
                    if (mp == null) {
                        return;
                    }
                    if (mp.isPlaying()) {
                        mp.pause();
                        btnPlay.setImageResource(R.drawable.play);
                    } else {
                        mp.start();
                        btnPlay.setImageResource(R.drawable.pause);
                    }
                    break;
                case R.id.btnNext:
                    if (currentSongIndex < (songsList.size()-1)) {
                        currentSongIndex++;
                    } else {
                        currentSongIndex = 0;
                    }
                    playSong(currentSongIndex);
                    break;
                case R.id.btnPrev:
                    if (currentSongIndex > 0) {
                        currentSongIndex--;
                    } else {
                        currentSongIndex = songsList.size() -1;
                    }
                    playSong(currentSongIndex);
                    break;
            }
        }
    };

    public void playSong(int songIndex) {
        if (songsList.size() == 0) {
            return;
        }
        try {
            mp.reset();
            mp.setDataSource(songsList.get(songIndex).get("songPath"));
            mp.prepare();
            mp.start();
            String songTitle = songsList.get(songIndex).get("songTitle");
            songTitleLabel.setText(songTitle);
            btnPlay.setImageResource(R.drawable.pause);
            songProgressBar.setProgress(0);
            songProgressBar.setMax(100);
            mHandler.postDelayed(mUpdateTimeTask, 100);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = mp.getDuration();
            long currentDuration = mp.getCurrentPosition();
            songTotalDurationLabel.setText(""+utils.milliSecondsToTimer(totalDuration));
            songCurrentDurationLabel.setText(""+utils.milliSecondsToTimer(currentDuration));

            int progress = (int) (utils.getProgressPercentage(currentDuration, totalDuration));
            songProgressBar.setProgress(progress);
            mHandler.postDelayed(this, 100);// add this Runnable into thread list again
        }
    };

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask); // remove this runnable from thread list
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);// remove runnable from thread list first
        int totalDuration = mp.getDuration();
        int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);
        mp.seekTo(currentPosition);
        mHandler.postDelayed(mUpdateTimeTask, 100);// add runnable into thread list finally
    }

    // On Song Playing completed, play next song
    @Override
    public void onCompletion(MediaPlayer arg0) {
        if (currentSongIndex < (songsList.size()-1)) {
            currentSongIndex++;
        } else {
            currentSongIndex = 0;
        }
        playSong(currentSongIndex);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mp.release();
    }

    class downloadAudiosThread extends Thread {
        private String website = null;

        public downloadAudiosThread(String website) {
            super();
            this.website = website;
        }

        // fetch the link of audios who haven't been downloaded
        public ArrayList<String> getFreshAudioLinks() throws IOException {
            ArrayList<String> allLinks = new ArrayList<String>();

            URL url = new URL(website);
            Document doc  = Jsoup.parse(url, 5000);
            Elements elms = doc.select("[href$=.mp3][href^=http://]");
            if (elms.isEmpty()) {
                return allLinks;
            }
            for (Element elm : elms) {
                String audioUrl = elm.attr("href");
                String audioName = audioUrl.substring(audioUrl.lastIndexOf("/")+1);
                if (songManager.songExist(audioName)) {
                    continue;
                }
                allLinks.add(audioUrl);
            }
            return allLinks;
        }

        private void downloadAudios(ArrayList<String> audioUrls) {
            HttpDownloader dler = new HttpDownloader();
            for (String url : audioUrls) {
                if (!isWifiEnable())
                    break;

                String localName = url.substring(url.lastIndexOf("/")+1);
                dler.download(url, localAudioPath, localName); // maybe keep a long time
            }
        }

        @Override
        public void run() {
            synchronized (this) {
                if (isDownloading)
                    return;

                isDownloading = true;
            }
            try {
                ArrayList<String> availableUrls = getFreshAudioLinks();
                downloadAudios(availableUrls);
            } catch (Exception e) {
                e.printStackTrace();
            }
            synchronized (this) {
                isDownloading = false;
            }
        }
    }

    private boolean isWifiEnable() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }
}
