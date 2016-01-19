package com.bazz2.eslpiracy;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenjt on 2015/11/26.
 */
public class ESLDB {
    public static final int KEEP_IT_NOW = 0;
    public static final int NEED_TO_DELETE = 1;
    public static final int NEED_TO_DOWNLOAD = 2;
    private static final String TBL_ESL_AUDIO = "esl_audio";
    private static final String TBL_ESL_TRIGGER = "esl_trigger";

    public class DBHelper extends SQLiteOpenHelper {
        private static final String DB_NAME = "eslpiracy.db";
        private static final int DB_VERSION = 1;

        public DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TBL_ESL_AUDIO +
                    "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "audio_url VARCHAR, audio_content TEXT, audio_state INT)");
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TBL_ESL_TRIGGER +
                    "(_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "trig_id INT)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("ALTER TABLE" + TBL_ESL_AUDIO + "ADD COLUMN OTHER STRING");
        }
    }

    public class ESLAudio {
        public int _id;
        public String url;
        public String content;
        public int state;

        ESLAudio() {
        }

        ESLAudio(String url, String content, int state) {
            this.url = url;
            this.content = content;
            this.state = state;
        }
    }

    public class ESLTrigger {
        public int _id;
        public int trig_id;

        ESLTrigger() {
        }
        ESLTrigger(int trig_id) {
            this.trig_id = trig_id;
        }
    }

    public class DBManager {
        private DBHelper helper;
        private SQLiteDatabase db;

        public DBManager(Context context) {
            helper = new DBHelper(context);
            try {
                db = helper.getWritableDatabase();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void closeDB() {
            db.close();
        }

        public void addAudio(List<ESLAudio> audios) {
            db.beginTransaction();
            try {
                for (ESLAudio audio : audios) {
                    db.execSQL("INSERT INTO " + TBL_ESL_AUDIO + " VALUES(null, ?, ?, ?)",
                            new Object[]{audio.url, audio.content, audio.state});
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }

        // delete items whose state is $audio_state
        public void trimAudios(int audio_state) {
            db.beginTransaction();
            try {
                db.execSQL("DELETE FROM " + TBL_ESL_AUDIO + " WHERE audio_state=" + audio_state);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }

        // just update audio.state, instead of all the attributions
        public void updateAudio(ESLAudio audio) {
            db.beginTransaction();
            try {
                db.execSQL("UPDATE " + TBL_ESL_AUDIO + " SET audio_state=" + audio.state + " WHERE audio_url=" + audio.url);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }

        /**
         * query all audios
         * @return List<ESLAudio>
         */
        public ArrayList<ESLAudio> queryAudios(String where) {
            ArrayList<ESLAudio> audios = new ArrayList<ESLAudio>();
            Cursor c = db.rawQuery("SELECT * FROM " + TBL_ESL_AUDIO + "WHERE " + where, null);

            while (c.moveToNext())  {
                ESLAudio audio = new ESLAudio();
                try {
                    audio._id = c.getInt(c.getColumnIndex("_id"));
                    audio.url = c.getString(c.getColumnIndex("audio_url"));
                    audio.content = c.getString(c.getColumnIndex("audio_content"));
                    audio.state = c.getInt(c.getColumnIndex("audio_state"));
                    audios.add(audio);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            c.close();
            return audios;
        }

        public ESLAudio queryAudioForPlay(int init_index) {
            Cursor c = null;
            int max_item_id = 0;

            c = db.rawQuery("SELECT * FROM " + TBL_ESL_AUDIO + "WHERE audio_state!=" + NEED_TO_DOWNLOAD + " ORDER BY _id DESC", null);
            if (c.moveToNext()) {
                max_item_id = c.getColumnIndex("_id");
                if (max_item_id < init_index) {
                    init_index = 0; // set to zero to get the first item to play
                }
            }

            c = db.rawQuery("SELECT * FROM " + TBL_ESL_AUDIO + "WHERE audio_state!=2 ORDER BY _id ASC", null);
            ESLAudio audio = null;
            while (c.moveToNext())  {
                audio = new ESLAudio();
                try {
                    audio._id = c.getInt(c.getColumnIndex("_id"));
                    audio.url = c.getString(c.getColumnIndex("audio_url"));
                    audio.content = c.getString(c.getColumnIndex("audio_content"));
                    audio.state = c.getInt(c.getColumnIndex("audio_state"));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (audio._id >= init_index)
                    break;
            }
            c.close();
            return audio;
        }

        public int queryAudiosCount(String where) {
            int count = 0;
            Cursor c = db.rawQuery("SELECT count(*) as count FROM " + TBL_ESL_AUDIO + "WHERE " + where, null);
            if (c.moveToNext()) {
                count = c.getColumnIndex("count");
            }

            return count;
        }

        public void deleteAudio(String where) {
            db.beginTransaction();
            try {
                db.execSQL("DELETE FROM " + TBL_ESL_AUDIO + " WHERE " + where);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }
        /********************************************
         * methods of trigger
         ********************************************/

        public void delTriggerById(int trig_id) {
            db.execSQL("DELETE FROM " +TBL_ESL_TRIGGER+ "WHERE _id=" + trig_id);
        }

        public void addTrigger(List<ESLTrigger> triggers) {
            db.beginTransaction();
            try {
                for (ESLTrigger trigger : triggers) {
                    db.execSQL("INSERT INTO " + TBL_ESL_TRIGGER + " VALUES(null, ?)",
                            new Object[]{trigger.trig_id});
                }
                db.setTransactionSuccessful();
            }finally {
                db.endTransaction();
            }
        }
        /**
         * query all triggers
         * @return List<ESLTrigger>
         */
        public ArrayList<ESLTrigger> queryTriggers(int count) {
            int i = 0;
            ArrayList<ESLTrigger> triggers = new ArrayList<ESLTrigger>();
            Cursor c = queryTriggerCursor();
            while (c.moveToNext())  {
                if (i >= count)
                    break;
                ESLTrigger trigger = new ESLTrigger();
                trigger._id = c.getInt(c.getColumnIndex("_id"));
                trigger.trig_id = c.getInt(c.getColumnIndex("trig_id"));
                triggers.add(trigger);
                i++;
            }
            c.close();
            return triggers;
        }

        public Cursor queryTriggerCursor() {
            Cursor c = db.rawQuery("SELECT * FROM " + TBL_ESL_TRIGGER, null);
            return c;
        }
    }
}
