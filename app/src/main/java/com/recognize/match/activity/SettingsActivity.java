package com.recognize.match.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.recognize.match.R;


public class SettingsActivity extends AppCompatActivity implements OnCheckedChangeListener {
	
	private RadioGroup radioGroup; 
	private Switch soundSwitch;

    public final static String MY_PREFERENCES = "MyPrefs";
    public final static String difficultyKey = "difficulty";
    public final static String effectKey = "effect";
    public final static String rotationKey = "rotation";
    public final static String thicknessKey = "thickness";
    public final static String speedKey = "speed";
    public final static String soundKey = "sounds";
	SharedPreferences prefs;
	Editor editor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
		
		prefs = getSharedPreferences(MY_PREFERENCES, Context.MODE_MULTI_PROCESS);
	    editor = prefs.edit();
	    
		radioGroup = (RadioGroup) findViewById(R.id.radioGroup1);
		soundSwitch = (Switch)findViewById(R.id.soundSwitch);
		soundSwitch.setChecked(prefs.getBoolean(soundKey, true));
        final Spinner effectSpinner = (Spinner) findViewById(R.id.effectSpinner);

        // Create an ArrayAdapter using the string array and a default spinner layout
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.effects_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        effectSpinner.setAdapter(adapter);

        //Rotation of image
        final Spinner rotationSpinner = (Spinner) findViewById(R.id.rotationSpinner);
        final ArrayAdapter<CharSequence> rotationAdapter = ArrayAdapter.createFromResource(this,
                R.array.rotation, android.R.layout.simple_spinner_item);
        rotationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rotationSpinner.setAdapter(rotationAdapter);

        //Thickness of scan lines
        final Spinner thicknessSpinner = (Spinner) findViewById(R.id.thicknessSpinner);
        final ArrayAdapter<CharSequence> thicknessAdapter = ArrayAdapter.createFromResource(this,
                R.array.thickness, android.R.layout.simple_spinner_item);
        thicknessAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        thicknessSpinner.setAdapter(thicknessAdapter);

        //Speed of scan lines
        final Spinner speedSpinner = (Spinner) findViewById(R.id.speedSpinner);
        final ArrayAdapter<CharSequence> speedAdapter = ArrayAdapter.createFromResource(this,
                R.array.speed, android.R.layout.simple_spinner_item);
        speedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        speedSpinner.setAdapter(speedAdapter);

		int diff = prefs.getInt(difficultyKey, 1);
		switch (diff) {
			case 1:
                radioGroup.check(R.id.radioEasy);
				break;
			case 2:
                radioGroup.check(R.id.radioMed);
				break;
			case 3:
                radioGroup.check(R.id.radioHard);
                break;
            default:
				break;
		}

        final String effectChoice = prefs.getString(effectKey, "Horizontal Scan");
        switch (effectChoice) {
            case "None":
                effectSpinner.setSelection(adapter.getPosition(effectChoice));
                break;
            case "Fade In":
                effectSpinner.setSelection(adapter.getPosition(effectChoice));
                break;
            case "Slide In":
                effectSpinner.setSelection(adapter.getPosition(effectChoice));
                break;
            case "Blur":
                effectSpinner.setSelection(adapter.getPosition(effectChoice));
                break;
            case "Pixelate":
                effectSpinner.setSelection(adapter.getPosition(effectChoice));
                break;
            case "Circle":
                effectSpinner.setSelection(adapter.getPosition(effectChoice));
                break;
            case "Reverse Circle":
                effectSpinner.setSelection(adapter.getPosition(effectChoice));
                break;
            case "Masked Reverse Circle":
                effectSpinner.setSelection(adapter.getPosition(effectChoice));
                break;
            case "Horizontal Scan":
                effectSpinner.setSelection(adapter.getPosition(effectChoice));
                break;
            case "Vertical Scan":
                effectSpinner.setSelection(adapter.getPosition(effectChoice));
                break;
            default:
                break;
        }

        final String rotationChoice = prefs.getString(rotationKey, "None");
        switch (rotationChoice) {
            case "Rotate 90 degrees":
                rotationSpinner.setSelection(rotationAdapter.getPosition(rotationChoice));
                break;
            case "Rotate 180 degrees":
                rotationSpinner.setSelection(rotationAdapter.getPosition(rotationChoice));
                break;
            case "Rotate 270 degrees":
                rotationSpinner.setSelection(rotationAdapter.getPosition(rotationChoice));
                break;
            default:
                break;
        }

        final String thicknessChoice = prefs.getString(thicknessKey, "Normal");
        switch (thicknessChoice) {
            case "Thick":
                thicknessSpinner.setSelection(thicknessAdapter.getPosition(thicknessChoice));
                break;
            case "Normal":
                thicknessSpinner.setSelection(thicknessAdapter.getPosition(thicknessChoice));
                break;
            case "Thin":
                thicknessSpinner.setSelection(thicknessAdapter.getPosition(thicknessChoice));
                break;
            case "Thinnest":
                thicknessSpinner.setSelection(thicknessAdapter.getPosition(thicknessChoice));
                break;
            default:
                break;
        }

        final String speedChoice = prefs.getString(speedKey, "Medium");
        switch (speedChoice) {
            case "Slow":
                speedSpinner.setSelection(speedAdapter.getPosition(speedChoice));
                break;
            case "Medium":
                speedSpinner.setSelection(speedAdapter.getPosition(speedChoice));
                break;
            case "Fast":
                speedSpinner.setSelection(speedAdapter.getPosition(speedChoice));
                break;
            case "Fastest":
                speedSpinner.setSelection(speedAdapter.getPosition(speedChoice));
                break;
            default:
                break;
        }

        //TODO implement difficulty settings
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // find which radio button is selected
                if (checkedId == R.id.radioEasy) {
                    Toast.makeText(getApplicationContext(), "Easy", Toast.LENGTH_SHORT).show();
                    editor.putInt(difficultyKey, 1);
                } else if (checkedId == R.id.radioMed) {
                    Toast.makeText(getApplicationContext(), "Medium", Toast.LENGTH_SHORT).show();
                    editor.putInt(difficultyKey, 2);
                } else {
                    Toast.makeText(getApplicationContext(), "Hard", Toast.LENGTH_SHORT).show();
                    editor.putInt(difficultyKey, 3);
                }
                editor.commit();
            }
        });

        effectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView v = ((TextView) parent.getChildAt(0));
                v.setTextColor(Color.WHITE);
                editor.putString(effectKey, effectSpinner.getSelectedItem().toString());
                editor.commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        rotationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView v = ((TextView) parent.getChildAt(0));
                v.setTextColor(Color.WHITE);
                editor.putString(rotationKey, rotationSpinner.getSelectedItem().toString());
                editor.commit();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        thicknessSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView v = ((TextView) parent.getChildAt(0));
                v.setTextColor(Color.WHITE);
                editor.putString(thicknessKey, thicknessSpinner.getSelectedItem().toString());
                editor.commit();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        speedSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView v = ((TextView) parent.getChildAt(0));
                v.setTextColor(Color.WHITE);
                editor.putString(speedKey, speedSpinner.getSelectedItem().toString());
                editor.commit();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
		
		soundSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked){
					editor.putBoolean(soundKey, true);
				} else {
                    editor.putBoolean(soundKey, false);
				}
				editor.commit();
			}			
		});

	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                onBackPressed();
            case R.id.action_sync:
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
//        return true;
    }

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		// TODO Auto-generated method stub
		
	}
}
