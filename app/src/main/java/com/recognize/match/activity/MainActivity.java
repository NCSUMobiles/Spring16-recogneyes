package com.recognize.match.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.common.base.Strings;
import com.recognize.match.R;
import com.recognize.match.dao.AlbumDataSource;
import com.recognize.match.helper.ActivityHelper;
import com.recognize.match.helper.AppConstants;
import com.recognize.match.helper.Utils;

import java.io.IOException;
import java.util.Collections;
import java.util.Random;

/**
 * MainActivity presents the main menu and handles all actions on the menu
 */
public class MainActivity extends FragmentActivity {

    // Constants
	private static final int GAME_REQUEST_CODE = 101;
	private static final int ALBUM_REQUEST_CODE = 102;
	private static final int SCORE_REQUEST_CODE = 103;
	private static final int RESULT_COMPLETED = 2;
	private static final int RESULT_INCOMPLETE = 3;
    private static final String LOG_TAG = "MainActivity" ;
    public static final int REQUEST_ACCOUNT_PICKER = 2;
    private static final String PREF_ACCOUNT_NAME = "Account";

    // OAuth vars
    private AuthorizationCheckTask mAuthTask;
    private GoogleAccountCredential credential;

    // Location is the album idx
	private int location;

    // Views
    private ImageView albums;
    private Button match;

    // Storage objects
	private SharedPreferences prefs;
	private AlbumDataSource datasource;
    private String mEmailAccount;

    // Vars for the gameplay
    public static String effect;
    public static String gameplay;
    public static String rotation;
    public static String thickness;
    public static String speed;
    public static String screenSize = "NORMAL";
    private static Random random = new Random(System.nanoTime());
    private Handler handler = new Handler();

    //Loads the C++ library used for image filters
    static {
        System.loadLibrary("AndroidImageFilter");
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityHelper.initialize(MainActivity.this);
        //Determine which layout to use based on screen size
//        Display display = getWindowManager().getDefaultDisplay();
//        int width = display.getWidth();
//        int height = display.getHeight();
//        Log.d("Density", "width: " + width + " height: " + height);
//        // Crashes app if using Android Studio emulator!
//        if(width <= 480 && height <= 800){
//            setContentView(R.layout.activity_main);
//        } else if(width >= 1080 && height >= 1920){
        screenSize = "LARGE";
        setContentView(R.layout.activity_main_large);
//        }
		//Setting up all views
		ImageView settings = (ImageView) findViewById(R.id.settings_view);
        //Different games
        match = (Button) findViewById(R.id.match);
        albums = (ImageView) findViewById(R.id.albums_view);

		prefs = getSharedPreferences(SettingsActivity.MY_PREFERENCES, Context.MODE_MULTI_PROCESS);

		datasource = new AlbumDataSource(this);
	    datasource.open();

		Utils.applicationContext = getApplicationContext();
        //Offloading the database loading so the layout displays quickly
        if(Utils.masterList == null)
            new InitializeData().execute();

		//Listener
		settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
			}
		});


	}

    /**
     * Sets up the listeners for the buttons on the main menu
     */
    private void setupListeners(){
        albums.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AlbumsActivity.class);
                intent.putExtra("location", location);
                intent.putExtra("effect", effect);
                startActivityForResult(intent, ALBUM_REQUEST_CODE);
            }
        });
        // Play button/goodluck audio; start GameManager Activity
        match.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefs = getSharedPreferences("MyPrefs", Context.MODE_MULTI_PROCESS);
                if (prefs.getBoolean("sounds", true)) {
                    final MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.button_select);
                    mp.start();
                }
                gameplay = "Match";

                location = random.nextInt(Utils.INITIAL_CAPACITY);
                while(Utils.masterList.get(location).size() != 10){
                    location = random.nextInt(Utils.INITIAL_CAPACITY);
                }
                final Intent intent = new Intent(MainActivity.this,GameManagerActivity.class);
                intent.putExtra("location", location);
                intent.putExtra("effect", effect);
                intent.putExtra("restore", 0);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivityForResult(intent, GAME_REQUEST_CODE);

                    }
                },1200);
            }
        });
    }

    /**
     * This AsyncTask is used to create the master list holding local content
     */
    private static class InitializeData extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... unused){
            Utils.createMasterList();
            Utils.createAlbumThumbnails();
            return "Done!";
        }
        @Override
        protected void onPostExecute(String result) {
//            Log.d("init", result);
        }
    }

    /**
     * Sets up the vars needed for the gameplay and the listeners for the main menu
     */
	@Override
	protected void onResume(){
		datasource.open();
		super.onResume();
        effect = prefs.getString(SettingsActivity.effectKey, "Horizontal Scan");
        rotation = prefs.getString(SettingsActivity.rotationKey, "None");
        thickness = prefs.getString(SettingsActivity.thicknessKey, "Normal");
        speed = prefs.getString(SettingsActivity.speedKey, "Medium");
        gameplay = "Match";
//        Log.d("effects", "effect in main: "+effect);
        setupListeners();
        // Authorize user to receive new content
        if(prefs.getString(PREF_ACCOUNT_NAME, null) == null){
            credential = GoogleAccountCredential.usingAudience(getApplicationContext(), AppConstants.AUDIENCE);
            onSignIn();
        }
	}

    /**
     * Sets the account name to the SharedPreference and the GoogleAccountCredential object
     * @param accountName
     */
    private void setSelectedAccountName(String accountName) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_ACCOUNT_NAME, accountName);
        editor.commit();
        credential.setSelectedAccountName(accountName);
    }

    /**
     * This function is called to initiate the Google account registration with the app if
     * there is no account already registered
     */
    public void onSignIn() {
        // Check to see how many Google accounts are registered with the device.
        int googleAccounts = AppConstants.countGoogleAccounts(this);
        if (googleAccounts == 0) {
            // No accounts registered, nothing to do.
            Toast.makeText(this, R.string.toast_no_google_accounts_registered,
                    Toast.LENGTH_LONG).show();
        } else if (googleAccounts == 1) {
            // If only one account then select it.
            Toast.makeText(this, R.string.toast_only_one_google_account_registered,
                    Toast.LENGTH_LONG).show();
            AccountManager am = AccountManager.get(this);
            Account[] accounts = am.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
            if (accounts != null && accounts.length > 0) {
                // Select account and perform authorization check.
                mEmailAccount = accounts[0].name;
                performAuthCheck(accounts[0].name);
            }
        } else {
            // More than one Google Account is present, a chooser is necessary.
            Log.d(LOG_TAG, "AuthorizationCheckTask successful");
            // Invoke an {@code Intent} to allow the user to select a Google account.
            Intent accountSelector = AccountPicker.newChooseAccountIntent(null, null,
                    new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, false,
                    "Select the account to access the RecognizeEndpoints API.", null, null, null);
            startActivityForResult(accountSelector,
                    MainActivity.REQUEST_ACCOUNT_PICKER);
        }
    }

    /**
     * Performs an authorization check with the given email account
     * @param emailAccount
     */
    public void performAuthCheck(String emailAccount) {
        // Cancel previously running tasks.
        if (mAuthTask != null) {
            try {
                mAuthTask.cancel(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        new AuthorizationCheckTask().execute(emailAccount);
    }

    /**
     * THis AsyncTask is used to offload the authorization check from the UI thread
     */
    private class AuthorizationCheckTask extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(String... emailAccounts) {
            Log.i(LOG_TAG, "Background task started.");

            if (!AppConstants.checkGooglePlayServicesAvailable(MainActivity.this)) {
                return false;
            }

            String emailAccount = emailAccounts[0];
            // Ensure only one task is running at a time.
            mAuthTask = this;

            // Ensure an email was selected.
            if (Strings.isNullOrEmpty(emailAccount)) {
                publishProgress(R.string.toast_no_google_account_selected);
                // Failure.
                return false;
            }

            Log.d(LOG_TAG, "Attempting to get AuthToken for account: " + mEmailAccount);

            try {
                // If the application has the appropriate access then a token will be retrieved, otherwise
                // an error will be thrown.
                credential = GoogleAccountCredential.usingAudience(
                        MainActivity.this, AppConstants.AUDIENCE);
                credential.setSelectedAccountName(emailAccount);
                String accessToken = credential.getToken();
                Log.d(LOG_TAG, "AccessToken retrieved: "+accessToken);

                // Success.
                return true;
            } catch (GoogleAuthException unrecoverableException) {
                Log.e(LOG_TAG, "Exception checking OAuth2 authentication.", unrecoverableException);
                publishProgress(R.string.toast_exception_checking_authorization);
                // Failure.
                return false;
            } catch (IOException ioException) {
                Log.e(LOG_TAG, "Exception checking OAuth2 authentication.", ioException);
                publishProgress(R.string.toast_exception_checking_authorization);
                // Failure or cancel request.
                return false;
            }
        }


        @Override
        protected void onProgressUpdate(Integer... stringIds) {
            // Toast only the most recent.
            Integer stringId = stringIds[0];
            Toast.makeText(MainActivity.this, stringId, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPreExecute() {
            mAuthTask = this;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                // Authorization check successful
                Log.d(LOG_TAG, "AuthorizationCheckTask successful");
            } else {
                // Authorization check unsuccessful
                Log.d(LOG_TAG, "AuthorizationCheckTask unsuccessful");

            }
            mAuthTask = null;
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }

    /**
     * Responds to the play button, user selection of an Album on AlbumActivity, redirects
     * the user to the ScoreActivity after a game is finished, and sets the Google Auth Credential
     * object with the user account chosen.
     * @param requestCode
     * @param resultCode
     * @param data
     */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Incorporate OMO gameplay
        switch(requestCode){
            case GAME_REQUEST_CODE:
                if(resultCode == RESULT_OK){
                    Intent scoreIntent = new Intent(MainActivity.this, ScoreActivity.class);
                    scoreIntent.putExtra("just_location", data.getIntExtra("just_location", 0));
                    scoreIntent.putExtra("total_score", data.getIntExtra("total_score", 0));
                    startActivityForResult(scoreIntent, SCORE_REQUEST_CODE);
                } else if(resultCode == RESULT_INCOMPLETE){ // Not being called at all currently
//                    Log.d("MainActivity", "RESULT_INCOMPLETE- location: " + location);
                    final Intent intent = new Intent(MainActivity.this, GameManagerActivity.class);
                    intent.putExtra("total_score", data.getIntExtra("total_score", 0));
                    intent.putExtra("location", location);
                    intent.putExtra("step", data.getIntExtra("step", 0));
                    intent.putExtra("effect", effect);
                    Collections.shuffle(Utils.masterList.get(location), new Random(System.nanoTime()));
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivityForResult(intent, GAME_REQUEST_CODE);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                }
                break;
            case ALBUM_REQUEST_CODE:
                if(resultCode == RESULT_COMPLETED){
                    location = data.getIntExtra("location", -1);
//                    Log.d("MainActivity", "RESULT_COMPLETED - location: " + location);
                    final Intent intent = new Intent(MainActivity.this, GameManagerActivity.class);
                    intent.putExtra("location", location);
                    intent.putExtra("step", 0);
                    intent.putExtra("effect", effect);
                    Collections.shuffle(Utils.masterList.get(location), new Random(System.nanoTime()));
                    if (prefs.getBoolean("sounds", true)) {
                        final MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.button_select);
                        mp.start();
                    }
                    startActivityForResult(intent, GAME_REQUEST_CODE);
                }
                break;
            case SCORE_REQUEST_CODE:
                if(resultCode == RESULT_COMPLETED){
//                    Log.d("MainActivity", "Replay: location: " + location);
                    Intent intent = new Intent(MainActivity.this, GameManagerActivity.class);
                    Collections.shuffle(Utils.masterList.get(location), new Random(System.nanoTime()));
                    intent.putExtra("location", location);
                    intent.putExtra("step", 0);
                    intent.putExtra("effect", effect);
                    startActivityForResult(intent, GAME_REQUEST_CODE);
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (data != null && data.getExtras() != null) {
                    String accountName =
                            data.getExtras().getString(
                                    AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        setSelectedAccountName(accountName);
                        // User is authorized.
                        Log.d(LOG_TAG, "In activity result: saving mEmailAccount, "+accountName);
                    }
                }
                break;
        }
	}

	@Override
    protected void onPause() {
        super.onPause();
        datasource.close();
    }

    @Override
    protected  void onDestroy(){
        super.onDestroy();
        datasource.close();
    }

}
