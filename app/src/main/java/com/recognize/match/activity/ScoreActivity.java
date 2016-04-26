package com.recognize.match.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.recognize.match.R;
import com.recognize.match.bean.AlbumBean;
import com.recognize.match.dao.AlbumDataSource;
import com.recognize.match.dataset.DataSet;
import com.recognize.match.helper.ActivityHelper;
import com.recognize.match.helper.Utils;

import java.util.Random;

/**
 * ScoreActivity displays the screen with the high score after a game has been completed.
 */
public class ScoreActivity extends Activity {

    // Vars for setting score
	private static final int RESULT_COMPLETED = 2;
    private MediaPlayer mp;
	private int total_score = 0;
	private int high_score = 0;
	private int just_played = -1;
    private static Random random = new Random(System.nanoTime());

    // Views
	private ImageView replay;
	private ImageView home;
	private ImageView versus;
	private ImageView share_icon;
	private ImageView urscore[] = new ImageView[6];
	private ImageView hscore[] = new ImageView[6];
	private ImageView albumComplete;
	private TextView fact;

    // Storage for highscore
	private AlbumDataSource datasource;
	SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActivityHelper.initialize(ScoreActivity.this);
		setContentView(R.layout.activity_transition_score);
		Intent intent = getIntent();		
		prefs = getSharedPreferences("MyPrefs", Context.MODE_MULTI_PROCESS);
		
		datasource = new AlbumDataSource(this);
	    datasource.open();		
		
		just_played = intent.getIntExtra("just_location", -1);
		total_score = intent.getIntExtra("total_score", 0);
		high_score = getHighScore();
		albumComplete = (ImageView) findViewById(R.id.albumComplete);
		TextView scoreTitle = (TextView) findViewById(R.id.scoreTitle);

		if (total_score == 0) {
            albumComplete.setImageResource(R.drawable.keep_trying);
        } else if (total_score == high_score) {
			albumComplete.setImageResource(R.drawable.high_score_logo);
			scoreTitle.setText("YOU SCORED HIGHEST");
		} else {
            albumComplete.setImageResource(R.drawable.woohoo2);
        }

//		Log.d("ScoreActivity","score: "+total_score);
//		Log.d("ScoreActivity","location: "+just_played);
		
		share_icon = (ImageView) findViewById(R.id.share1);
		replay = (ImageView) findViewById(R.id.replay);
		home = (ImageView) findViewById(R.id.home);
		//versus = (ImageView) findViewById(R.id.vs);
		fact = (TextView) findViewById(R.id.id_pedagog_desc);
		
		for(int i = 0; i < 6; i++){
			urscore[i] = (ImageView) findViewById(Utils.getResId("us_"+i, "id"));
			hscore[i] = (ImageView) findViewById(Utils.getResId("hs_"+i, "id"));
		}
		updatescore();


		int factResourceId = getResources().getIdentifier(DataSet.themeIdArray.get(just_played) + "_str", "string", getPackageName());
        Log.i("Arjun","ScoreActivity: factResourceId:"+factResourceId+" just_played:"+just_played);
		String content = "";

        if(!DataSet.themeIdArray.get(just_played).equals("odd_man_out")) {
            if(factResourceId==0){
                content = Utils.funFacts.get(random.nextInt(Utils.funFacts.size()));
            }
            else{
                content = getResources().getString(factResourceId);
            }
        }
		fact.setText(content);
		
		if(prefs.getBoolean("sounds", true)) {
//			final MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.ooweee);
//			mp.start();
            mp = MediaPlayer.create(getApplicationContext(), R.raw.applause);
            mp.start();
		}
		
		replay.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setResult(RESULT_COMPLETED);
				finish();
			}
		});
		
		home.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setResult(RESULT_OK);
				finish();
			}
		});
		
		/*
		versus.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Toast.makeText(ScoreActivity.this, "Challenge a friend", Toast.LENGTH_SHORT).show();
			}
		});
		*/
		
		share_icon.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String message = "I just scored " + total_score + "on the " + DataSet.themeTitleArray.get(just_played) + " album! Download the game at: https://github.com/chetanpawar0989/Recognize-App";
				Intent share = new Intent(Intent.ACTION_SEND);
				share.setType("text/plain");
				share.putExtra(Intent.EXTRA_TEXT, message);
				startActivity(Intent.createChooser(share, "Share Highscore"));
			}
			
		});
	}
	
	@Override
	protected void onPause(){
        super.onPause();
        datasource.close();
        if(mp != null){
            mp.release();
        }
		//setResult(RESULT_CANCELED);
		//finish();
	}
	
    @Override
    protected void onResume() {
        super.onResume();
        datasource.open();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (mp != null) {
            mp.release();
        }
        datasource.close();
    }


	private void updatescore(){
		int us[] = prepare(total_score);
		int hs[] = prepare(high_score);
		for(int i = 0 ; i < 6; i++){
			urscore[i].setImageResource(Utils.getResId("big" + us[i], "drawable"));
			hscore[i].setImageResource(Utils.getResId("small" + hs[i], "drawable"));
		}
	}
	
	private int getHighScore(){
		AlbumBean al = datasource.getAlbum(just_played);
        int hs = 0;
        Log.d("Score","total score: "+total_score);
        if(al == null){
            al = new AlbumBean();
        } else{
            hs = al.getHighscore();
        }
		if(hs > total_score) {
            return hs;
        } else{
			al.setHighscore(total_score);
			al.setPlayed(1);
			datasource.updateGamePlayRecord(al);
			return total_score;
		}
	}

	private int[] prepare(int score){
		int arr[] = {0, 0, 0, 0, 0, 0};
		String str = Integer.toString(score);
		for(int i = str.length()-1, j = 0; i >= 0 && j < 6; i--, j++){
			arr[j] = str.charAt(i) - '0';
		}
		return arr;		
	}

}
