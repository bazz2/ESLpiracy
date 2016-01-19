package com.bazz2.eslpiracy;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import android.content.Context;

/**
 * Created by chenjt on 2015/12/8.
 */
public class SongsManager {

    String MEDIA_PATH = null;
    private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();

    // Constructor
    public SongsManager(String media_path) {
        MEDIA_PATH = media_path;
    }

    public ArrayList<HashMap<String, String>> getPlayList() {
        File home = new File(MEDIA_PATH);

        if (home.listFiles() == null)
            return songsList;

        if (home.listFiles(new FileExtensionFilter()).length > 0) {
            for (File file : home.listFiles(new FileExtensionFilter())) {
                HashMap<String, String> song = new HashMap<String, String>();
                song.put("songTitle", file.getName().substring(0, file.getName().length() - 4));
                song.put("songPath", file.getPath());

                songsList.add(song);
            }
        }
        return songsList;
    }

    class FileExtensionFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            return (name.endsWith(".mp3") || name.endsWith(".MP3"));
        }
    }

    public boolean songExist(String name) {
        ArrayList<HashMap<String, String>> songs = getPlayList();
        for (HashMap<String, String> song : songs) {
            if (song.get("songTitle").equals(name))
                return true;
        }
        return false;
    }
}
