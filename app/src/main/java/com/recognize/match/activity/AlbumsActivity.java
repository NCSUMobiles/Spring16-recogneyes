package com.recognize.match.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.appspot.recognize_1210.recognize.Recognize;
import com.appspot.recognize_1210.recognize.model.RecognizeAlbumCollection;
import com.appspot.recognize_1210.recognize.model.RecognizeAlbumMessage;
import com.appspot.recognize_1210.recognize.model.RecognizeImageMessage;
import com.appspot.recognize_1210.recognize.model.RecognizeQuestionMessage;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.recognize.match.R;
import com.recognize.match.adapter.AlbumsAdapter;
import com.recognize.match.bean.ImageBean;
import com.recognize.match.bean.QuestionBean;
import com.recognize.match.bean.ThemeRowBean;
import com.recognize.match.dataset.DataSet;
import com.recognize.match.helper.AlbumDBHelper;
import com.recognize.match.helper.AppConstants;
import com.recognize.match.helper.QuestionDBHelper;
import com.recognize.match.helper.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * AlbumsActivity presents the user with a GridView of Albums and handles user selection.
 * ViewHolder pattern is used to reduce number of findViewById(...) calls and smoothen the scroll.
 */
public class AlbumsActivity extends AppCompatActivity {

    // Constants
    private static final String LOG_TAG = "AlbumsActivity";
    private static final String PREF_ACCOUNT_NAME = "Account";
    private static final int RESULT_COMPLETED = 2;
    public final static String APP_PATH_SD_CARD = "/images";

    // Views and GridView related vars
	private GridView themegrid;
	private AlbumsAdapter adapter;
	private int location;
    private List<ThemeRowBean> list = new ArrayList<>();
    private View prevView;

    // OAuth vars
    private GoogleAccountCredential credential;
    private SharedPreferences prefs;
    private Context context;
    private ProgressDialog mLoadingProgress;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_albums);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        context = getApplicationContext();
        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

		location = getIntent().getIntExtra("location", -1);


        if(list.isEmpty()) {
            list = Utils.albumThumbnails;
            // TODO Also add the ones in the db
            new InitializeData().execute(0);
        }

        prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

		themegrid = (GridView) findViewById(R.id.grid_layout);
		adapter = new AlbumsAdapter(this, R.layout.albums_list_row, list);
		themegrid.setAdapter(adapter);

        // Listens to what item on grid got selected and sets up an Intent for MainActivity to
        // initiate the chosen Album
        themegrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
//                Log.d("AlbumsActivity", "position: " + position);
                if(prevView != null) {
                    prevView.setVisibility(View.INVISIBLE);
                }
                View newView = v.findViewById(R.id.grid_image_start);
                newView.setVisibility(View.VISIBLE);
                if(location == position){
//                    Log.d("AlbumsActivity", "In if position: " + position);
                    Intent data = new Intent();
					data.putExtra("location", location);
                    data.putExtra("effect", getIntent().getStringExtra("effect"));
                    setResult(RESULT_COMPLETED, data);
					finish();
				} else {
					location = position;
//                    Log.d("AlbumsActivity", "In else position: " + position);
				}
                prevView = newView;
            }
        });

	}

    /**
     * This AsyncTask adds Albums that were added from the Server to the list that represents the
     * GridView. If there are more Albums than the local ones, add the new Albums.
     */
    private class InitializeData extends AsyncTask<Integer, Void, String> {
        @Override
        protected String doInBackground(Integer... unused){
            // Query database that holds Albums from the server
            AlbumDBHelper dbHelper = new AlbumDBHelper(context);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            SQLiteDatabase qdb = new QuestionDBHelper(context).getReadableDatabase();
            Cursor cursor = db.query(AlbumDBHelper.TABLE_ALBUM,
                    null,
                    AlbumDBHelper.COLUMN_CATEGORY + " != ?", // COLUMN_CATEGORY is "" for local Albums
                    new String[]{""}, null, null, null);
            // Only refresh list if there are more Albums than the local Albums
            int length = DataSet.themes.size();
            if(unused[0] > 0 || list.size() < length + cursor.getCount()) {
                HashMap<Integer,String> imgPathsMap = new HashMap<>();
                int idOffset = 1234; //random number
                while (cursor.moveToNext()) {
                    List<QuestionBean> newAlbums = new ArrayList<>();
                    String title = cursor.getString(cursor.getColumnIndex(AlbumDBHelper.COLUMN_TITLE));
                    // TODO For image, first make Question table and then query the first image
                    //Grab the incorrect_1's image path (arbitrary choice for image)
                    String[] cols = {QuestionDBHelper.COLUMN_Q_INCORRECT_1};
                    String selection = QuestionDBHelper.COLUMN_Q_ALBUM_ID + "=?";
                    String[] selectionArgs = {cursor.getString(cursor.getColumnIndex(AlbumDBHelper.COLUMN_ALBUM_ID))};
                    Cursor qCursor = qdb.query(QuestionDBHelper.TABLE_QUESTION,
                            null, selection, selectionArgs, null, null, null);
                    String imagePath = "";
                    if (qCursor != null && qCursor.moveToFirst()) {
                        imagePath = qCursor.getString(qCursor.getColumnIndex(QuestionDBHelper.COLUMN_Q_INCORRECT_1));
                        do {
                            //String main = qCursor.getString(qCursor.getColumnIndex(QuestionDBHelper.COLUMN_Q_MAIN));
                            String correct = qCursor.getString(qCursor.getColumnIndex(QuestionDBHelper.COLUMN_Q_CORRECT));
                            String incorrect1 = qCursor.getString(qCursor.getColumnIndex(QuestionDBHelper.COLUMN_Q_INCORRECT_1));
                            String incorrect2 = qCursor.getString(qCursor.getColumnIndex(QuestionDBHelper.COLUMN_Q_INCORRECT_2));
                            String incorrect3 = qCursor.getString(qCursor.getColumnIndex(QuestionDBHelper.COLUMN_Q_INCORRECT_3));
                            String qTitle = qCursor.getString(qCursor.getColumnIndex(QuestionDBHelper.COLUMN_Q_TITLE));
                            String funFact = qCursor.getString(qCursor.getColumnIndex(QuestionDBHelper.COLUMN_Q_FACT));
                            Log.i("Arjun","Funfact: "+funFact);
                            Log.i("Arjun", "InitializeData  correct:" + correct + " incorr1:" + incorrect1 + " incorr2:" + incorrect2 + " incorr3:" + incorrect3);
                            QuestionBean qbean = new QuestionBean(-1,
                                    new ArrayList<>(Arrays.asList(new ImageBean("correct", -1, correct),
                                            new ImageBean("incorrect1", -1, incorrect1),
                                            new ImageBean("incorrect2", -1, incorrect2),
                                            new ImageBean("incorrect3", -1, incorrect3))), qTitle, correct, "easy");
                            qbean.setFunFact(funFact);
                            newAlbums.add(qbean);
                        } while (qCursor.moveToNext());
                        Utils.updateMasterList(title,selectionArgs[0],newAlbums);
                    }
                    // Create the Row bean with the title and image path
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    ThemeRowBean b = new ThemeRowBean(title,
                            BitmapFactory.decodeFile(imagePath, options));

                    Log.i("AlbumsActivity","title: "+title+" imagePath: "+imagePath+" options: "+options+" storage:"+Environment.getExternalStorageState());
                    list.add(b);
                }
                return "New Albums!";
            }
            return "No New Albums!";
        }
        @Override
        protected void onPostExecute(String result) {
//            Log.d("Sql", result);
            if(mLoadingProgress!=null){
                mLoadingProgress.dismiss();
            }
            if(result.equals("New Albums!")){
                adapter.notifyDataSetChanged();
            } else {
                Log.d("Sql", "No new albums!");
            }
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(prefs.getString(PREF_ACCOUNT_NAME, null) == null){
            credential = GoogleAccountCredential.usingAudience(context, AppConstants.AUDIENCE);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.album_menu, menu);
        return true;
    }

    /**
     * This AsyncTask checks with the Server for new Albums
     *
     * Note:
     *
     * This isn't very efficient since it's querying all albums of type match from
     * datastore. Add in more efficient code to handle new/modified/deleted content.
     *
     * It also does not detect changes in Datastore relating to deletion/modification;
     * only new albums are added.
     *
     * Implementing something like GCM could be used to inform clients of these changes.
     */
    private class GetAndDisplayAlbums extends AsyncTask<Void, Void, Integer> {
        @Override
        protected Integer doInBackground(Void... arg) {
            // Retrieve service handle.
            Recognize apiServiceHandle = AppConstants.getApiServiceHandle(credential);
            int count = 0;
            try {
                Recognize.Albums getGreetingCommand = apiServiceHandle.albums();
                RecognizeAlbumCollection albums = getGreetingCommand.get("match").execute();
//                Log.d("AlbumBean", albums.toString());
                List<RecognizeAlbumMessage> allAlbums = albums.getAlbums();
                if(allAlbums == null)
                    return count;
                for(RecognizeAlbumMessage m : allAlbums) {
                    // Query database to ensure it's a new Album
                    AlbumDBHelper dbHelper = new AlbumDBHelper(context);
                    SQLiteDatabase db = dbHelper.getReadableDatabase();
                    QuestionDBHelper qdbHelper = new QuestionDBHelper(context);
                    SQLiteDatabase qdb = qdbHelper.getReadableDatabase();
                    String[] mSelectionArgs = new String[1];
                    mSelectionArgs[0] = Long.toString(m.getAlbumId());
                    Cursor cursor = db.query(AlbumDBHelper.TABLE_ALBUM,
                            null,
                            AlbumDBHelper.COLUMN_ALBUM_ID + "=?",
                            mSelectionArgs,
                            null,
                            null,
                            null);
                    if (cursor.getCount() == 0) { //Add new Album
                        count++;
                        //Create and insert Album, Question, then Image models.
                        Log.d("Sql", m.getTitle() + " not in db");
                        dbHelper.addAlbum(m, db);
                        List<RecognizeQuestionMessage> questions = m.getQuestions();
                        for (RecognizeQuestionMessage q : questions) {
                            List<RecognizeImageMessage> images = q.getImages();
                            List<String> imagePath = new ArrayList<>();
                            for (RecognizeImageMessage i : images) {
                                Log.i("Arjun","Recognize image:"+i.getTitle());
                                Boolean haveSd = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
                                if (haveSd) {
                                    // Work on device-sd
                                    Log.d("Storage", "sd card exists!");
                                    //Save each image
                                    byte[] image = i.decodeImageUrl();
                                    BitmapFactory.Options options = new BitmapFactory.Options();
                                    Log.i("Arjun","image: "+i.getTitle()+" q title:"+q.getTitle());
                                    Boolean result = saveImageToInternalStorage(
                                            imagePath,
                                            i.getTitle()+".png", q.getTitle()+m.getTitle(),
                                            BitmapFactory.decodeByteArray(image, 0, image.length, options));
                                    if (result) {
                                        Log.d("Storage", "image stored!");
                                    }
                                } else {
                                    Log.d("Storage", "sd card does not exist!");
                                    //Work on device
                                }
                            }
                            qdbHelper.addQuestion(q, imagePath, m.getAlbumId(), qdb);
                        }
                    }
                }
            } catch(IllegalArgumentException e) {
                Log.e(LOG_TAG, "Exception during API call", e);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Exception during API call", e);
            }
            return count;
        }
        @Override
        protected void onPostExecute(Integer albums) {
            if (albums > 0) {
                new InitializeData().execute(albums);
                Toast.makeText(AlbumsActivity.this, "New Albums!", Toast.LENGTH_LONG).show();
            } else {
                if(mLoadingProgress!=null){
                    mLoadingProgress.dismiss();
                }
                Toast.makeText(AlbumsActivity.this, "No updates found!", Toast.LENGTH_LONG).show();
                Log.e(LOG_TAG, "No RecognizeAlbumCollections were returned by the API.");
            }
        }
    }

    /**
     * Handles actions on the ActionBar
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
            // Retrieve AlbumBean from the server
            case R.id.action_sync:
                mLoadingProgress = new ProgressDialog(this);
                mLoadingProgress.setMessage("Checking server for new content.");
                mLoadingProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mLoadingProgress.setCancelable(false);
                mLoadingProgress.show();
                new GetAndDisplayAlbums().execute();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This function saves an image file and adds the path name to a list
     * so that the paths can be referred to when loading the image from the disk.
     * @param images A list that needs to be populated with image paths
     * @param filename The name of the particular image file to save
     * @param image The Bitmap representing the image to save
     * @return
     */
    public boolean saveImageToInternalStorage(List<String> images, String filename, String prefix, Bitmap image) {
        try {
            String extDir = context.getExternalCacheDir().toString();  //this is needed as it creates the external directory if not present
            Log.i("AlbumsActivity","extDir:"+extDir);
            // Create Images folder if it doesn't exist
            File direct = new File(Environment.getExternalStorageDirectory().getPath() +
                    "/Android/data/" + getPackageName() + AlbumsActivity.APP_PATH_SD_CARD);
            if (!direct.exists()) {
                File imgDirectory = new File(direct.getAbsolutePath());
                Boolean b = imgDirectory.mkdirs();
            }
            // Create the image file within the images directory
            File file = new File(new File(direct.getAbsolutePath()), prefix+filename);
            if (file.exists()) {
                file.delete();
            }
            images.add(file.getAbsolutePath());
            // Save the compressed image file
            FileOutputStream out = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            return true;
        } catch (Exception e) {
            Log.e("saveToInternalStorage()", e.getMessage());
            return false;
        }
    }

}
