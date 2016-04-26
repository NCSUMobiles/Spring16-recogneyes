package com.recognize.match.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.appspot.recognize_1210.recognize.model.RecognizeAlbumMessage;
import com.recognize.match.dataset.DataSet;

/**
 * This is a helper class to perform operations on the albums database. It adds both the local
 * content, as well as content retrieved from the Server.
 */
public class AlbumDBHelper extends SQLiteOpenHelper{

    //Album table
	public static final String TABLE_ALBUM = "albums";

    // Column identifiers
	public static final String COLUMN_ID = "_id";
    public static final String COLUMN_ALBUM_ID = "album_id"; //So we store unique albums
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_PLAYED = "played";
    public static final String COLUMN_HIGHSCORE = "highscore";

	private static final String DATABASE_NAME = "recognize.db";
	private static final int DATABASE_VERSION = 3;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_ALBUM + "(" + COLUMN_ID + " integer primary key, "
            + COLUMN_ALBUM_ID + " integer, "
            + COLUMN_TITLE + " text_type, "
            + COLUMN_TYPE + " text_type, "
            + COLUMN_CATEGORY + " text_type, "
            + COLUMN_PLAYED + " integer, "
            + COLUMN_HIGHSCORE + " double);";

    /**
     * Constructor
     * @param context
     */
	public AlbumDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

    /**
     * Creates the album db with the local content (see: DataSet)
     * @param db
     */
	@Override
	public void onCreate(SQLiteDatabase db) {
        Log.d("AlbumDBHelper", "onCreate");
		db.execSQL(DATABASE_CREATE);
        //Retrieving files all from DataSet which refers to resources
		for(int i = 0; i < DataSet.themeIdArray.size(); i++) {
            addLocalData(i, db);
        }
	}

    /**
     * Upgrades the album database with a new one
     * @param db
     * @param oldVersion
     * @param newVersion
     */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(AlbumDBHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALBUM);
		onCreate(db);
	}

    /**
     * Add the local content
     * @param id
     * @param db
     */
	private void addLocalData(long id, SQLiteDatabase db) {
        //TODO Make sure to store the default games (means making them like the models on GAE)
	    ContentValues values = new ContentValues();
        values.put(COLUMN_ID, id);
	    values.put(COLUMN_ALBUM_ID, id); // StatesModel Name
        values.put(COLUMN_TITLE, DataSet.themeTitleArray.get((int)id));
        values.put(COLUMN_TYPE, "match");
        values.put(COLUMN_CATEGORY, "");
        values.put(COLUMN_PLAYED, 0); // StatesModel Phone
        values.put(COLUMN_HIGHSCORE, 0); // StatesModel Phone
	    // Inserting Row
        db.insert(TABLE_ALBUM, null, values);
    }

    /**
     * Inserts a new Album retrieved from the server
     * @param album
     * @param db
     */
    public void addAlbum(RecognizeAlbumMessage album, SQLiteDatabase db){
        ContentValues values = new ContentValues();
        values.put(COLUMN_ALBUM_ID, album.getAlbumId()); // StatesModel Name
        values.put(COLUMN_TITLE, album.getTitle());
        values.put(COLUMN_TYPE, album.getAlbumType());
        values.put(COLUMN_CATEGORY, album.getCategory());
        values.put(COLUMN_PLAYED, 0); // StatesModel Phone
        values.put(COLUMN_HIGHSCORE, 0); // StatesModel Phone
        // Inserting Row
        db.insert(TABLE_ALBUM, null, values);
    }


}
