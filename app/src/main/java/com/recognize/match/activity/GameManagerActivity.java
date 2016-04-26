package com.recognize.match.activity;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.nineoldandroids.animation.ArgbEvaluator;
import com.nineoldandroids.animation.ValueAnimator;
import com.recognize.match.R;
import com.recognize.match.bean.QuestionBean;
import com.recognize.match.dataset.DataSet;
import com.recognize.match.fragment.ImageFragment;
import com.recognize.match.helper.ActivityHelper;
import com.recognize.match.helper.Utils;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;

public class GameManagerActivity extends FragmentActivity implements ImageFragment.OnItemClickListener{

	private static final int RESULT_INCOMPLETE = 3;
    private static String effect;

    //Scan line thickness
    private static final int THICK = R.drawable.scan_thick;
    private static final int NORMAL = R.drawable.scan_normal;
    private static final int THIN = R.drawable.scan_thinner;
    private static final int THINNEST = R.drawable.scan_thinnest;

    private static final int THICK_F = R.drawable.scan_thick_flip;
    private static final int NORMAL_F = R.drawable.scan_normal_flip;
    private static final int THIN_F = R.drawable.scan_thinner_flip;
    private static final int THINNEST_F = R.drawable.scan_thinnest_flip;

    //Scan line speed
    private static final long SLOW = 7000;
    private static final long MEDIUM = 4000;
    private static final long FAST = 1000;
    private static final long FASTEST = 800;

    // Gameplay vars
	private int i_time_left;
	private int i_total_score;
	private int i_score;
	private int i_score_diff = 1;
	private int location = 0;
	private int max;
	private static int counter = 0;
	private static int step;
	private boolean option_clicked;
    private boolean started = false; //whether the curent level has started
    private static int expandCount = 0; //# times user has hit expand button
    private int tries = 0;

    // Preferences
    private static SharedPreferences prefs;
	private static MediaPlayer correctVoice;
    private static MediaPlayer incorrectVoice;
    private static MediaPlayer tick;
    private static MediaPlayer fx;

	private Handler handler = new Handler();
    private Runnable runnable;

	// ProgressBarFragment pfrag;
	private static ImageFragment ifrag;
    private static TextView countdown;
    private static TextView topic;
    private static TextView totalScore;
    private static Button start;
    private static TextView tFact;
    private static ImageButton expand;
    private static View trivia;

    //Imageviews and animations
    private ImageView[] dots;
    private View scanView;
    private ImageView scanLine;
    private static TranslateAnimation transScan;
    private static SupportAnimator circleReveal;
    private static Animation animation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//        Log.d("Save", "In onCreate()");
        ActivityHelper.initialize(GameManagerActivity.this);
        if(MainActivity.screenSize.equals("NORMAL")) {
            setContentView(R.layout.activity_game_manager);
        } else{
            setContentView(R.layout.activity_game_manager_large);
        }
		Intent intent = getIntent();
		location = intent.getIntExtra("location", -1);
        step = intent.getIntExtra("step", 0);
        i_total_score = intent.getIntExtra("total_score", 0);

        // Set up views
        totalScore = (TextView) findViewById(R.id.scoreTotal);
        totalScore.setText(String.format("%01d",i_total_score));
        topic = (TextView) findViewById(R.id.topic);
        topic.setText(DataSet.themeTitleArray.get(location));
        max = Utils.masterList.get(location).size();
        dots = new ImageView[max];
        countdown = (TextView) findViewById(R.id.countdown);
        tFact = (TextView) findViewById(R.id.fact);
        expand = (ImageButton) findViewById(R.id.expandText);
        trivia = findViewById(R.id.trivia);

        // Set up listener for fun fact expand chevron button
        expand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (expandCount++ == 1) {
                    expand.setImageResource(R.drawable.ic_expand_less_white_18dp);
                    expand.setBackgroundResource(R.drawable.theme_rounded_green_bg);
                    Animation slideDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
                    tFact.startAnimation(slideDown);
                    if (prefs.getBoolean("sounds", true)) {
                        fx = MediaPlayer.create(getApplicationContext(), R.raw.received_sound);
                        fx.start();
                    }
                    expandCount = 0;
                } else {
                    expand.setImageResource(R.drawable.ic_expand_more_white_18dp);
                    expand.setBackgroundResource(R.drawable.theme_rounded_red_bg);
                    Animation slideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
                    slideUp.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            tFact.setVisibility(View.VISIBLE);
                            if (prefs.getBoolean("sounds", true)) {
                                fx = MediaPlayer.create(getApplicationContext(), R.raw.sent_sound);
                                fx.start();
                            }
                        }
                        @Override
                        public void onAnimationEnd(Animation animation) {}
                        @Override
                        public void onAnimationRepeat(Animation animation) {}
                    });
                    slideUp.setInterpolator(new AccelerateInterpolator());
                    tFact.startAnimation(slideUp);
                }
            }
        });

        //Progress dots
        //TODO If every album varies with the number of levels, make these dynamic
        dots[0] = (ImageView) findViewById(R.id.dot1);
        dots[1] = (ImageView) findViewById(R.id.dot2);
        dots[2] = (ImageView) findViewById(R.id.dot3);
        dots[3] = (ImageView) findViewById(R.id.dot4);
        dots[4] = (ImageView) findViewById(R.id.dot5);
        dots[5] = (ImageView) findViewById(R.id.dot6);
        dots[6] = (ImageView) findViewById(R.id.dot7);
        dots[7] = (ImageView) findViewById(R.id.dot8);
        dots[8] = (ImageView) findViewById(R.id.dot9);
        dots[9] = (ImageView) findViewById(R.id.dot10);

		prefs = getSharedPreferences("MyPrefs", Context.MODE_MULTI_PROCESS);
        //Set the animator:
        effect = intent.getExtras().getString("effect");
        animation = getAnimation(effect);
        //Set mediaplayers
        tick = MediaPlayer.create(getApplicationContext(), R.raw.tick);
        correctVoice = MediaPlayer.create(getApplicationContext(), R.raw.correct);
        incorrectVoice = MediaPlayer.create(getApplicationContext(), R.raw.wrong);

        if (savedInstanceState != null) {
//            Log.d("Save", "step: " + savedInstanceState.getInt("step"));
//            Toast.makeText(this, savedInstanceState.getInt("step"), Toast.LENGTH_LONG).show();
            return;
        }
        QuestionBean bean = new QuestionBean(Utils.masterList.get(location).get(step));
        ifrag = ImageFragment.newInstance(Utils.masterList.get(location).get(step));

        getSupportFragmentManager().beginTransaction()
        .add(R.id.fragment1, ifrag).commit();

        start = (Button) findViewById(R.id.start_button_mc);
	}

    @Override
    protected void onStop(){
        super.onStop();
        getIntent().putExtra("step", step);
        getIntent().putExtra("restore", 1);
//        Log.d("Save", "In onStop()");
    }

    /**
     * Saves the current level the user was on when leaving app
     * @param outstate
     */
    @Override
    protected void onSaveInstanceState(Bundle outstate){
//        Log.d("Save","In onSaveInstanceState()");
        outstate.putInt("step", step);
    }

    @Override
	protected void onResume(){
		super.onResume();
        tick = MediaPlayer.create(getApplicationContext(), R.raw.tick);
        correctVoice = MediaPlayer.create(getApplicationContext(), R.raw.correct);
        incorrectVoice = MediaPlayer.create(getApplicationContext(), R.raw.wrong);
        gameManager(counter, false);
    }

    /**
     * Initializes vars required for gameplay and sets the level if user had left the game
     * @param step
     * @param flag
     */
    public void gameManager(int step, boolean flag){
        initialize();
        if(flag){
            fragmentControl(step);
        }
        if(step > 0){
            startGame();
        }
    }

    /**
     * Creates the ImageFragment necessary for displaying the main image and image choices
     * @param step
     */
    public void fragmentControl(int step){
        ifrag = ImageFragment.newInstance(Utils.masterList.get(location).get(step));
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment1, ifrag).commit();
    }

    /**
     * Starts the particular level by setting the timer for counting down on a runnable
     */
    public void startGame(){
        runnable = new Runnable() {
            @Override
            public void run() {
                updateView();
                if(i_time_left >= 0) {
                    handler.postDelayed(this, 1000);
                }
            }
        };
        runnable.run();
    }

    /**
     * Initializes the game/level vars
     */
	public void initialize(){
        i_time_left = 10;
        i_score = 10;
        option_clicked = false;
        //Scan line effect
        if (MainActivity.effect.contains("Scan")){
            setUpScanEffect();
        }
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (start.getText().equals("GO")) {
                    start.setVisibility(View.INVISIBLE);
                    start.setText("NEXT");
                    if (prefs.getBoolean("sounds", true)) {
                        fx = MediaPlayer.create(getApplicationContext(), R.raw.game_start);
                        fx.start();
                    }
                    started = true;
                    startGame();
                } else if (start.getText().equals("NEXT")) {
                    ifrag.tagOverlay.setVisibility(View.INVISIBLE);
                    getSupportFragmentManager().beginTransaction().remove(ifrag).commit();
                    getSupportFragmentManager().executePendingTransactions();
                    if (prefs.getBoolean("sounds", true)) {
                        fx = MediaPlayer.create(getApplicationContext(), R.raw.game_start);
                        fx.start();
                    }
                    // Create a new Fragment to be placed in the activity layout
                    QuestionBean bean = new QuestionBean(Utils.masterList.get(location).get(step));
                    Log.i("Arjun","GameManagerActivity: "+step+" location:"+location+" bean:"+bean.getFunFact());

                    ifrag = ImageFragment.newInstance(bean);
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.fragment1, ifrag).commit();
                    getSupportFragmentManager().executePendingTransactions();
                    start.setVisibility(View.INVISIBLE);
                    started = true;
                    //Hide views, reset expand button to expand
                    tFact.setVisibility(View.GONE);
                    trivia.setVisibility(View.INVISIBLE);
                    expand.setImageResource(R.drawable.ic_expand_less_white_18dp);
                    expand.setBackgroundResource(R.drawable.theme_rounded_green_bg);
                    expandCount = 0;
                    //Game vars
                    i_time_left = 10;
                    i_score = 10;
                    option_clicked = false;
                    tries = 0;
                    //Scan line effect
                    if (MainActivity.effect.contains("Scan")) {
                        setUpScanEffect();
                    }
                    startGame();
                } else if (start.getText().equals("DONE")) {
                    result_ok();
                }
            }
        });
//        Log.d("GameManager", "initialize() with effect, "+MainActivity.effect);
	}

    /**
     * Retrieves the proper animation if the effect is between Fade In and Slide In
     * @param effect
     * @return
     */
    public Animation getAnimation(String effect){
        Animation anim;
        switch (effect) {
            case "Fade In":
                anim = AnimationUtils.loadAnimation(this, R.anim.fade_in_accelerate);
                break;
            case "Slide In":
                anim = AnimationUtils.loadAnimation(this, R.anim.slide_out);
                break;
            //Add more as you come up with new stuff
            default:
                anim = null;
        }
        return anim;
    }

    /**
     * If the effect is scan lines, this sets up the views required for the effect to work properly
     */
    public void setUpScanEffect(){
        scanView = ifrag.scanView;
        scanLine = (ImageView) scanView.findViewById(R.id.scanline);
        if(effect.contains("Vertical")){
            scanLine = (ImageView) scanView.findViewById(R.id.scanline_flip);
        }
        //Scan translation vars
        float x = -0.5f;
        float xDelta = 0f;
        long duration;
        switch(MainActivity.thickness){
            case "Thick":
                if(effect.contains("Vertical")){
                    scanLine.setImageResource(THICK_F);
                } else{
                    scanLine.setImageResource(THICK);
                }
                break;
            case "Normal":
                if(effect.contains("Vertical")){
                    scanLine.setImageResource(NORMAL_F);
                } else{
                    scanLine.setImageResource(NORMAL);
                }
                break;
            case "Thin":
                if(effect.contains("Vertical")){
                    scanLine.setImageResource(THIN_F);
                } else{
                    scanLine.setImageResource(THIN);
                }
                break;
            case "Thinnest":
                if(effect.contains("Vertical")){
                    scanLine.setImageResource(THINNEST_F);
                } else{
                    scanLine.setImageResource(THINNEST);
                }
                break;
            default:
                scanLine.setImageResource(NORMAL);
        }
        switch(MainActivity.speed){
            case "Slow":
                duration = SLOW;
                break;
            case "Medium":
                duration = MEDIUM;
                break;
            case "Fast":
                duration = FAST;
                break;
            case "Fastest":
                duration = FASTEST;
                break;
            default:
                duration = 0;
        }
        float y = 0f;
        float yDelta = 0f;
        //Other vars
        int repeatCount = 0;
        int repeatMode = Animation.RESTART;
        if(effect.contains("Vertical")){
            y = x;
            yDelta = 0f;
            x = 0f;
        }
        //Todo make params variables that vary by difficulty
        transScan = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, x,
                Animation.RELATIVE_TO_SELF, xDelta,
                Animation.RELATIVE_TO_SELF, y,
                Animation.RELATIVE_TO_SELF, yDelta);
        transScan.setDuration(duration);
        transScan.setFillAfter(false);
        transScan.setInterpolator(new LinearInterpolator());
        transScan.setRepeatCount(repeatCount);
        transScan.setRepeatMode(repeatMode);
    }

    /**
     * If the effect is circular reveals, this sets up the reveal animation and properties based
     * on the different types of circular reveals (normal, reverse, masked reverse)
     */
    private final Runnable revealAnimationRunnable = new Runnable() {
        @Override
        public void run() {
            int cx = (ifrag.img.getLeft() + ifrag.img.getRight()) / 2;
            int cy = (ifrag.img.getTop() + ifrag.img.getBottom()) / 2;
            float finalRadius = Math.max(ifrag.img.getWidth(), ifrag.img.getHeight());
//            Log.d("x", " " + cx);
//            Log.d("x", " " + cy);
//            Log.d("x", " " + finalRadius);
            ImageView view = ifrag.img;
            float startRadius = 60f;
            if (effect.equals("Reverse Circle")) { // Starts big and shrinks the view of image
                startRadius = finalRadius - 100f;
                finalRadius = 0f;
            } else if (effect.equals("Masked Reverse Circle")) { // Starts dark and big, reveals the view of image
                ifrag.blackLayer.setVisibility(View.VISIBLE);
                view = ifrag.blackLayer;
                startRadius = finalRadius - 150f;
                finalRadius = 0f;
            }
            circleReveal = ViewAnimationUtils.createCircularReveal(view, cx, cy, startRadius, finalRadius);
            circleReveal.setInterpolator(new AccelerateInterpolator());
            circleReveal.setDuration(10000);
            if (effect.equals("Reverse Circle") || effect.equals("Masked Reverse Circle")){
                circleReveal.setInterpolator(new DecelerateInterpolator());
                circleReveal.setDuration(15000);
            }
            // make the black view invisible when the animation is for black reveal
            circleReveal.addListener(new SupportAnimator.AnimatorListener() {
                @Override
                public void onAnimationStart() {}
                @Override
                public void onAnimationEnd() {
                    if(effect.contains("Masked")) {
                        ifrag.blackLayer.setVisibility(View.INVISIBLE);
                    }
                }
                @Override
                public void onAnimationCancel() {}
                @Override
                public void onAnimationRepeat() {}
            });
            circleReveal.start();
        }
    };

    /**
     * This function updates the timer, and the ImageFragment(s) for each intermediate view based
     * on the effect (e.g: intermediate views of blurred images)
     */
	public void updateView(){
        if(i_time_left == 10) {
            i_time_left--;
            updateTimeLeft();
            //View starts the animation
            if(effect.contains("Circle")) {
                ifrag.img.post(revealAnimationRunnable);
            } else if(animation != null){
                ifrag.img.startAnimation(animation);
            } else if(MainActivity.effect.contains("Scan")){
                scanLine.startAnimation(transScan);
            }
        } else if(i_time_left == 0){
//			pfrag.updateTimeLeft();
            i_time_left--;
            updateTimeLeft();
            if(!effect.contains("Circle") && !effect.contains("Scan") && !effect.contains("Slide")) {
                ifrag.updateView();
            }
			timeout_false();
		} else if(i_time_left > 0){
            i_score -= i_score_diff;
            i_time_left--;
            updateTimeLeft();
//			pfrag.updateTimeLeft();
            if(prefs.getBoolean("sounds", true)) {
                tick.start();
            }
            if(!effect.contains("Circle") && !effect.contains("Scan") && !effect.contains("Slide")) {
                ifrag.updateView();
            }
		}
	}

    /**
     * Updates the time left on the counter and changes its color based on the time
     */
    public void updateTimeLeft(){
        switch(i_time_left+1){
            case 10:
                countdown.setTextColor(Color.rgb(50,250,0));
                break;
            case 9:
                countdown.setTextColor(Color.rgb(100,250,0));
                break;
            case 8:
                countdown.setTextColor(Color.rgb(150,250,0));
                break;
            case 7:
                countdown.setTextColor(Color.rgb(200,250,0));
                break;
            case 6:
                countdown.setTextColor(Color.rgb(255,255,0));
                break;
            case 5:
                countdown.setTextColor(Color.rgb(250,200,0));
                break;
            case 4:
                countdown.setTextColor(Color.rgb(250,150,0));
                break;
            case 3:
                countdown.setTextColor(Color.rgb(250,100,0));
                break;
            case 2:
                countdown.setTextColor(Color.rgb(250,50,0));
                break;
            case 1:
                countdown.setTextColor(Color.rgb(250, 0, 0));
        }
        if(i_time_left+1>=0){
            countdown.setText(" "+(i_time_left+1) + " ");
        }
    }

    /**
     * Handles the user selection of images, and performs animations based on their choice. It also
     * sets the score and stops the timer.
     * @param v
     * @param id
     * @param correctOption
     * @param correctView
     */
    @Override
	public void onOptionItemClicked(final View v, final String id, final String correctOption,
                                    final View correctView) {
		if (option_clicked == false && started) {
            handler.removeCallbacksAndMessages(null);
			option_clicked = true;
            final RoundedImageView view = (RoundedImageView) v;
            if (effect.contains("Circle")) {
                circleReveal.end();
            } else if(effect.contains("Scan")){
                transScan.cancel();
                scanView.setVisibility(View.GONE);
            } else if(animation != null){
                ifrag.img.clearAnimation();
            } else if(effect.equals("Blur") || effect.equals("Pixelate")) {
                ifrag.img.setImageResource(ifrag.image); //Blur/Pixelate
            }
            Log.i("Arjun","id: "+id+" correct: "+correctOption);
            if (id.compareTo(correctOption) == 0) {
                i_total_score += i_score;
                ValueAnimator colorGreenAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), Color.parseColor("#F9CB4D"), Color.GREEN);
                colorGreenAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        view.setBorderColor((Integer) animator.getAnimatedValue());
                    }
                });
                colorGreenAnimation.setInterpolator(new LinearInterpolator());
                colorGreenAnimation.setDuration(300);
                colorGreenAnimation.setRepeatMode(ValueAnimator.RESTART);
                colorGreenAnimation.setRepeatCount(3);
                colorGreenAnimation.start();
                //check if sounds are on/off
                if (prefs.getBoolean("sounds", true)) {
//                    correctVoice.start();
                    fx = MediaPlayer.create(getApplicationContext(), R.raw.correct_ding);
                    fx.start();
                }
            } else {
                tries++;
                view.setBorderColor(Color.parseColor("#A30000"));
                //check if sounds are on/off
                if (prefs.getBoolean("sounds", true)) {
//                    incorrectVoice.start();
                    fx = MediaPlayer.create(getApplicationContext(), R.raw.buzzer);
                    fx.start();
                }
            }
            totalScore.setText(String.format("%01d",i_total_score+1));
            timeout_false();
		}
	}

    /**
     * This stops the timer and sets up, displays the fun fact as well as animate the correct choice.
     * This can run when time runs out but also runs when user selects a choice within the time left.
     */
	private void timeout_false() {
//        Log.d("Score", "" + i_total_score);
        handler.removeCallbacksAndMessages(null);
        totalScore.setText(String.format("%01d",i_total_score));
        final TextView tView = (TextView) findViewById(R.id.pictureFact);
        //Witty comments (Superheroes)
        String aName = ifrag.getAlbumName();
        int factResourceId = getResources().getIdentifier(aName, "string", getPackageName());
        int factTextId = getResources().getIdentifier(aName + "_fact", "string", getPackageName());
        if (factResourceId != 0) {
            String label = getResources().getString(factResourceId);
            String content = getResources().getString(factTextId);
            tView.setText(label);
            if(factTextId != 0) {
                tFact.setText("Did you know?\n\n"+content);
                if(MainActivity.screenSize.equals("NORMAL")){
                    tFact.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                }
            }
        } else {
            tView.setText(ifrag.correctOption);
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (effect.contains("Circle")) {
                    circleReveal.end();
                } else if (effect.contains("Scan") && !option_clicked) {
                    transScan.cancel();
                    scanView.setVisibility(View.GONE);
                } else if(animation != null){
                    ifrag.img.clearAnimation();
                } else if(effect.equals("Blur") || effect.equals("Pixelate")) {
                    ifrag.img.setImageResource(ifrag.image); //Blur/Pixelate
                }
                final RoundedImageView v = (RoundedImageView) ifrag.getCorrectView();
                if (!option_clicked) {
                    ValueAnimator colorGreenAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), Color.parseColor("#F9CB4D"), Color.GREEN);
                    colorGreenAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animator) {
                            v.setBorderColor((Integer) animator.getAnimatedValue());
                        }
                    });
                    colorGreenAnimation.setInterpolator(new LinearInterpolator());
                    colorGreenAnimation.setDuration(300);
                    colorGreenAnimation.setRepeatMode(ValueAnimator.RESTART);
                    colorGreenAnimation.setRepeatCount(3);
                    colorGreenAnimation.start();
                    option_clicked = true;
                }
                Animation fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                fadeIn.setAnimationListener(new Animation.AnimationListener() {
                    Animation fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                    @Override
                    public void onAnimationStart(Animation animation) {
                        fadeIn.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                                trivia.setVisibility(View.VISIBLE);
                                ifrag.tagOverlay.setVisibility(View.VISIBLE);
                            }
                            @Override
                            public void onAnimationEnd(Animation animation) {
                                //Fill progress dot
                                if ((tries > 0 && i_time_left > 0) || i_time_left < 0) {
                                    dots[step++].setImageResource(R.drawable.reddot);
                                } else {
                                    dots[step++].setImageResource(R.drawable.greendot);
                                }
                                if (step >= max) {
                                    start.setText("DONE");
                                }
                                start.setVisibility(View.VISIBLE);
                            }
                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }
                        });
                        trivia.startAnimation(fadeIn);
                    }
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        final Animation shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake_afford);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                expand.startAnimation(shake);
                            }
                        }, 200);
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                ifrag.tagOverlay.startAnimation(fadeIn);
            }
        }, 1000);
        //TODO For resetting the slide-in
//        if (i_time_left == 0){
//            ifrag.img.startAnimation(slide_in_anim);
//        }
	}
	
	@Override
	protected void onPause(){
		super.onPause();
        if (tick != null) {
            tick.release();
        }
        if(correctVoice != null){
            correctVoice.release();
        }
        if(incorrectVoice != null){
            incorrectVoice.release();
        }
        if(fx != null){
            fx.release();
        }
		handler.removeCallbacks(runnable);
	}
	
	@Override
	protected void onDestroy() {
        super.onDestroy();
		if (tick != null) {
            tick.release();
        }
        if(correctVoice != null){
            correctVoice.release();
        }
        if(incorrectVoice != null){
            incorrectVoice.release();
        }
        if(fx != null){
            fx.release();
        }
	}

    /**
     * Finishes the Activity for MainActivity to respond with the ScoreActivity
     */
	private void result_ok() {
        Intent intent = new Intent();
        intent.putExtra("just_location", location);
        intent.putExtra("total_score", i_total_score);
        setResult(RESULT_OK, intent);
        finish();
	}

}
