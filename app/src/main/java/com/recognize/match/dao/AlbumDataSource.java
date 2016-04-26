package com.recognize.match.dao;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import com.recognize.match.bean.AlbumBean;
import com.recognize.match.helper.AlbumDBHelper;
import com.recognize.match.helper.QuestionDBHelper;

/**
 * AlbumDataSource creates the AlbumDBHelper which is used to communicate with the SQLite db.
 * It maintains constants relate to Column IDs and common SQL methods.
 */
public class AlbumDataSource {

	private SQLiteDatabase database;
    private SQLiteDatabase questionDatabase;


    private AlbumDBHelper dbHelper;
    private QuestionDBHelper questiondbHelper;

	private String[] allColumns = { AlbumDBHelper.COLUMN_ID,
            AlbumDBHelper.COLUMN_ALBUM_ID,
            AlbumDBHelper.COLUMN_TITLE,
            AlbumDBHelper.COLUMN_TYPE,
            AlbumDBHelper.COLUMN_CATEGORY,
            AlbumDBHelper.COLUMN_PLAYED,
            AlbumDBHelper.COLUMN_HIGHSCORE };

	public AlbumDataSource(Context context) {
		dbHelper = new AlbumDBHelper(context);
        questiondbHelper = new QuestionDBHelper(context);
	}

    public SQLiteDatabase db(){
        return database;
    }

    public SQLiteDatabase questionDB() { return questionDatabase; }
	
	public void open() throws SQLiteException	 {
	    database = dbHelper.getWritableDatabase();
        questionDatabase = questiondbHelper.getWritableDatabase();
	}

	public void close() {
	    dbHelper.close();
	}
	
	public AlbumBean getAlbum(int id){
		AlbumBean album = new AlbumBean();
		Cursor cursor = database.query(AlbumDBHelper.TABLE_ALBUM,
		        allColumns, AlbumDBHelper.COLUMN_ALBUM_ID + "=?",new String[] {Integer.toString(id)}, null, null, null, null);
		if(cursor != null) {
            cursor.moveToFirst();
        }
//        Log.d("Cursor", "" + cursor.getCount());
        if(cursor.getCount() == 0){
            return null;
        }
		album.setId(Integer.parseInt(cursor.getString(0)));
        album.setAlbum_id(cursor.getLong(1));
        album.setTitle(cursor.getString(2));
        album.setType(cursor.getString(3));
        album.setCategory(cursor.getString(4));
        album.setPlayed(Integer.parseInt(cursor.getString(5)));
		album.setHighscore(Integer.parseInt(cursor.getString(6)));
		cursor.close();
		return album;
	}
	
	public long updateGamePlayRecord(AlbumBean album){
		ContentValues values = new ContentValues();
        values.put(AlbumDBHelper.COLUMN_ALBUM_ID, album.getAlbum_id());
        values.put(AlbumDBHelper.COLUMN_TITLE, album.getTitle());
        values.put(AlbumDBHelper.COLUMN_TYPE, album.getType());
        values.put(AlbumDBHelper.COLUMN_CATEGORY, album.getCategory());
	    values.put(AlbumDBHelper.COLUMN_PLAYED, album.isPlayed());
	    values.put(AlbumDBHelper.COLUMN_HIGHSCORE, album.getHighscore());
	 
	    // updating row
        int rows = database.update(AlbumDBHelper.TABLE_ALBUM, values, AlbumDBHelper.COLUMN_ALBUM_ID + " = ?",
                new String[] { String.valueOf(album.getId()) });
        Log.d("Data", "rows affected: " + rows);
	    return rows;
	}
}
