package com.recognize.match.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.appspot.recognize_1210.recognize.model.RecognizeQuestionMessage;

import java.util.List;

/**
 * Helper class that allows one to perform operations on the question database
 * Created by nischal on 2/2/16.
 */
public class QuestionDBHelper extends SQLiteOpenHelper {

    // Question table
    public static final String TABLE_QUESTION = "question";

    // Column identifiers
    public static final String COLUMN_Q_ID = "_id";
    public static final String COLUMN_Q_COLUMN_ID = "question_id"; //So we store unique albums
    public static final String COLUMN_Q_ALBUM_ID ="album_id";
    public static final String COLUMN_Q_TITLE = "title";
    public static final String COLUMN_Q_FACT = "fact";
    public static final String COLUMN_Q_MAIN = "main_image";
    public static final String COLUMN_Q_CORRECT = "correct_answer";
    public static final String COLUMN_Q_INCORRECT_1 = "incorrect_answer_1";
    public static final String COLUMN_Q_INCORRECT_2 = "incorrect_answer_2";
    public static final String COLUMN_Q_INCORRECT_3 = "incorrect_answer_3";

    private static final String Q_DATABASE_NAME = "question.db";
    private static final int Q_DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_Q_CREATE = "create table "
            + TABLE_QUESTION + "(" + COLUMN_Q_ID + " integer primary key, "
            + COLUMN_Q_COLUMN_ID + " integer, "
            + COLUMN_Q_ALBUM_ID + " integer, "
            + COLUMN_Q_TITLE + " text_type, "
            + COLUMN_Q_FACT + " text_type, "
            + COLUMN_Q_MAIN + " text_type, " //Image file path
            + COLUMN_Q_CORRECT + " text_type, " //Image file path
            + COLUMN_Q_INCORRECT_1 + " text_type, " //Image file path
            + COLUMN_Q_INCORRECT_2 + " text_type, " //Image file path
            + COLUMN_Q_INCORRECT_3 + " text_type);"; //Image file path

    /**
     * Constructor
     * @param context
     */
    public QuestionDBHelper(Context context) {
        super(context, Q_DATABASE_NAME, null, Q_DATABASE_VERSION);
    }

    /**
     * Creates the question db
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("AlbumDBHelper", "onCreate");
        db.execSQL(DATABASE_Q_CREATE);
    }

    /**
     * Upgrades the old db with the new db
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(QuestionDBHelper.class.getName(),
                "Upgrading question database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUESTION);
        onCreate(db);
    }

    /**
     * Inserts a new Album retrieved from the server
     * @param question
     * @param db
     */
    public void addQuestion(RecognizeQuestionMessage question, List<String> imagePath, long album_id, SQLiteDatabase db){
        ContentValues values = new ContentValues();
        values.put(COLUMN_Q_COLUMN_ID, question.getQuestionId()); // StatesModel Name
        values.put(COLUMN_Q_ALBUM_ID, album_id); // StatesModel Name
        values.put(COLUMN_Q_TITLE, question.getTitle());
        values.put(COLUMN_Q_FACT, question.getFact());

        // Go through imagePath list to store a String that represents
        int length = imagePath.size();
        for(int i = 0; i < length; i++){
            String path = imagePath.get(i);
//            Log.d("Path", "file path: "+path);
            if(path.contains("main_image")){
                Log.d("Question", "main: "+path);
                values.put(COLUMN_Q_MAIN, path);
            } else if(path.contains("incorrect_answer_0")){
                Log.d("Question", "incorrect 0: "+path);
                values.put(QuestionDBHelper.COLUMN_Q_INCORRECT_1, path);
            }  else if(path.contains("incorrect_answer_1")){
                Log.d("Question", "incorrect 1: "+path);
                values.put(QuestionDBHelper.COLUMN_Q_INCORRECT_1, path);
            }  else if(path.contains("incorrect_answer_2")){
                Log.d("Question", "incorrect 2: "+path);
                values.put(QuestionDBHelper.COLUMN_Q_INCORRECT_2, path);
            }  else if(path.contains("incorrect_answer_3")){
                Log.d("Question", "incorrect 3: "+path);
                values.put(QuestionDBHelper.COLUMN_Q_INCORRECT_3, path);
            } else if(path.contains("correct_answer")){
                Log.d("Question", "correct: "+path);
                values.put(COLUMN_Q_CORRECT, path);
            }
        }

        // Inserting Row
        db.insert(TABLE_QUESTION, null, values);
    }

}
