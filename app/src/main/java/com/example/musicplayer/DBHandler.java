package com.example.musicplayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DBHandler extends SQLiteOpenHelper {

    // creating a constant variables for our database.
    // below variable is for our database name.
    private static final String DB_NAME = "playlists_db";

    // below int is our database version
    private static final int DB_VERSION = 1;

    // below variable is for our table name.
    private static final String PLAYLIST_TABLE_NAME = "playlists";

    // below variable is for our id column.
    private static final String ID_COL = "id";

    // below variable is for our course name column
    private static final String NAME_COL = "name";

   // below variable is for our course tracks column.
    private static final String NUM_OF_TRACKS = "numTrack";

    // creating a constructor for our database handler.
    public DBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // below method is for creating a database by running a sqlite query
    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + PLAYLIST_TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NAME_COL + " TEXT UNIQUE,"
                + NUM_OF_TRACKS + " INTEGER)";

        // at last we are calling a exec sql
        // method to execute above sql query
        db.execSQL(query);
    }

    // this method is use to add new playlist to our sqlite database.
    public void addNewPlaylist(String playlistName) {

        // on below line we are creating a variable for
        // our sqlite database and calling writable method
        // as we are writing data in our database.
        SQLiteDatabase db = this.getWritableDatabase();

        // on below line we are creating a
        // variable for content values.
        ContentValues values = new ContentValues();

        values.put(NAME_COL, playlistName);
        values.put(NUM_OF_TRACKS, 0);

        try {
            db.insert(PLAYLIST_TABLE_NAME, null, values);
        }
        catch(SQLiteConstraintException e){
            e.printStackTrace();
            throw e;
        }
        String query = "CREATE TABLE IF NOT EXISTS" + "\"" + playlistName + "\"" + " ("
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NAME_COL + " TEXT " + "UNIQUE)";

        // at last we are calling a exec sql
        // method to execute above sql query
        try {
            db.execSQL(query);
        }
        catch(SQLiteException e){
            e.printStackTrace();
            throw e;
        }

        db.close();
    }

    public int addNewSong(String playlistName, String songName) throws SQLException {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(NAME_COL, songName);

        try {
            db.insert("\"" + playlistName + "\"", null, values);
        }
        catch(SQLiteConstraintException e){
            e.printStackTrace();
            Log.d("DBErr", e.getMessage());
            return 1;
        }
        values = new ContentValues();

        Cursor cursor = db.rawQuery("SELECT * FROM \"" + playlistName + "\"", null);

        values.put(NUM_OF_TRACKS, cursor.getCount());
        cursor.close();

        db.update("\"" + PLAYLIST_TABLE_NAME + "\"", values, " name = ?", new String[]{playlistName});
        return 0;
    }

    public List<String> getPlaylistNames(){
        List<String> list=new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor =  db.rawQuery( "SELECT * FROM " +
                PLAYLIST_TABLE_NAME, null );
        cursor.moveToFirst();

        if(cursor!=null && cursor.getCount()>0) {
            do {
                list.add(cursor.getString(cursor.getColumnIndexOrThrow(NAME_COL)));
                Log.d("playlists", list.toString());
            } while (cursor.moveToNext());
            cursor.close();
        }

        return list;
    }

    public ArrayList<String> getPlaylistSongs(String playlist_name){
        ArrayList<String> arrayList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " +
                "\"" + playlist_name + "\"",null);
        cursor.moveToFirst();

        if(cursor!=null && cursor.getCount()>0) {
            do {
                arrayList.add(cursor.getString(cursor.getColumnIndexOrThrow(NAME_COL)));
                Log.d("songs", arrayList.toString());
            } while (cursor.moveToNext());
            cursor.close();
        }

        return arrayList;
    }

    public void deletePlaylist(String playlistName){
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(PLAYLIST_TABLE_NAME, NAME_COL + "=?", new String[]{playlistName});
        db.execSQL("DROP TABLE IF EXISTS "+ "\"" + playlistName + "\"");
        db.close();
    }

    public void deleteSongFromPlaylist(String songName, String playlistName){
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete("\"" + playlistName + "\"", NAME_COL + "=?", new String[]{songName});
        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // this method is called to check if the table exists already.
        db.execSQL("DROP TABLE IF EXISTS " + PLAYLIST_TABLE_NAME);
        onCreate(db);
    }
}
