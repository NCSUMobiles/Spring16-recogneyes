package com.recognize.match.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.recognize.match.R;
import com.recognize.match.activity.MainActivity;
import com.recognize.match.bean.ImageBean;
import com.recognize.match.bean.QuestionBean;
import com.recognize.match.helper.Utils;
import com.recognize.match.helper.filters.BitmapFilter;

import java.util.List;
import java.util.Stack;

/**
 * This Fragment represents the current question in the gameplay: main image, and four choices.
 */
public class ImageFragment extends Fragment {

    public ImageView img;
    public int image;
    public ImageView blackLayer;
    public View scanView;
    public ImageView tagOverlay;

    private ImageView[] options = new ImageView[4];
    public String correctOption;
    private ImageView correctView;
    private QuestionBean obj;
    private OnItemClickListener listener;
    public Stack<Bitmap> bitmapStack = new Stack<>();
    BitmapFactory.Options bmpDecodeOpt = new BitmapFactory.Options();


    // Define the events that the fragment will use to communicate
    public interface OnItemClickListener {
        void onOptionItemClicked(View v, String id, String correctOption, View correctView);
    }

	public ImageView getCorrectView() {
		return correctView;
	}

	public static ImageFragment newInstance(QuestionBean GameObj) {
		ImageFragment fragment = new ImageFragment();
        Bundle args = new Bundle();
        args.putSerializable("GameObj", GameObj);
        fragment.setArguments(args);
        return fragment;
    }

	@Override
	  public void onAttach(Activity activity) {
	    super.onAttach(activity);
	      if (activity instanceof OnItemClickListener) {
	        listener = (OnItemClickListener) activity;
	      } else {
	        throw new ClassCastException(activity.toString()
	            + " must implement ImageFragment.OnItemClickListener");
	      }
	  }
	
	@Override
	public void onResume() {
		super.onResume();

	}
	
	@Override
	public void onPause() {
		super.onPause();
	}

    /**
     * Returns the next image in the sequence of filtered images for a Question
     */
	public void updateView(){
		if(bitmapStack != null && bitmapStack.size() > 0){
//			Log.d("ImageFragment", "Update - Size of Stack: "+bitmapStack.size());
            img.setImageBitmap(bitmapStack.pop());
		}
	}

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    /**
     * Creates and returns a View of the current Question in a Match/Correlate
     *
     * Currently, this will only work for local content; Implement code to handle new Server content
     * by querying the database.
     *
     * TODO: Query from database for preparing content retrieved from Server
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
//        Log.d("investigate", "inside onCreateView!: ");
        // Defines the xml file for the fragment
        View view = inflater.inflate(R.layout.image_fragment, container, false);
        if(MainActivity.screenSize.equals("LARGE")){
            view = inflater.inflate(R.layout.image_fragment_large, container, false);
        }
        tagOverlay = (ImageView) view.findViewById(R.id.main_image_overlay);
        img = (ImageView) view.findViewById(R.id.main_image);
        String effect = MainActivity.effect;
        if (effect.contains("Scan")){
            scanView = view.findViewById(R.id.scan_layout);
            scanView.setVisibility(View.VISIBLE);
        }
        if(effect.contains("Circle")){
            view.findViewById(R.id.reveal).setVisibility(View.VISIBLE);
            img = (ImageView) view.findViewById(R.id.circle_main_image);
        }
        // Setup handles to view objects here
        obj = (QuestionBean) getArguments().getSerializable("GameObj");
        List<ImageBean> opts = obj.getOptions();
        correctOption = obj.getCorrectOption();
        Log.i("Arjun","correctopt: "+correctOption);
        options[0] = (ImageView) view.findViewById(R.id.thumbs1);
        options[1] = (ImageView) view.findViewById(R.id.thumbs2);
        options[2] = (ImageView) view.findViewById(R.id.thumbs3);
        options[3] = (ImageView) view.findViewById(R.id.thumbs4);

        //This section sets the correct view
        for (int i = 0; i < options.length; i++) {
            Log.i("Arjun","opt-> "+opts.get(i));
            if(opts.get(i).getOptionId()==-1){
                Log.i("Arjun","opts -1 : "+opts.get(i).getImgPath());
                options[i].setImageBitmap(BitmapFactory.decodeFile(opts.get(i).getImgPath(), bmpDecodeOpt));
                options[i].setTag(opts.get(i).getImgPath());
            }
            else {
                Log.i("Arjun","opts: "+opts.get(i).getOptionId());
                options[i].setImageResource(opts.get(i).getOptionId());
                options[i].setTag(opts.get(i).getOptionName());
            }
            if (((String) options[i].getTag()).compareTo(correctOption) == 0) {
                if (MainActivity.gameplay.equals("Match")) {
                    if(obj.getImageId()!=-1) {
                        options[i].setImageResource(obj.getImageId());
                    }
                    else{
                        options[i].setImageBitmap(BitmapFactory.decodeFile(obj.getCorrectOption(), bmpDecodeOpt));
                    }
                }
                correctView = options[i];
            }
        }

        //Grey overlay over main image when level is complete to highlight NEXT button
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tagOverlay.setVisibility(View.INVISIBLE);
            }
        });

        if (!bitmapStack.isEmpty()){
            bitmapStack.clear();
        }
        image = obj.getImageId();
        Bitmap bmp = null;
        if(image==-1){
            bmp = BitmapFactory.decodeFile(obj.getCorrectOption(), bmpDecodeOpt);
        }
        else{
            bmp = BitmapFactory.decodeResource(getResources(), obj.getImageId());
        }
        bitmapStack.push(bmp);
        if(effect.equals("Blur") || effect.equals("Pixelate")) {
            int blurRadius = 8;
            int pixelSize = 7;
            for (int i = 0; i < 11; i++) {
                bitmapStack.push(effect.equals("Pixelate") ?
                        BitmapFilter.changeStyle(bmp, BitmapFilter.PIXELATE_STYLE, ++pixelSize) :
                        Utils.fastblur(bmp, blurRadius));
                blurRadius += 2;
            }
        } else if(effect.equals("Masked Reverse Circle")){
            blackLayer = (ImageView) view.findViewById(R.id.outer_main_image);
            blackLayer.setVisibility(View.VISIBLE);
        }

        img.setImageBitmap(bitmapStack.pop());
        //Starting to get at the combination effect (some effect + rotation)
        if(MainActivity.rotation.equals("Rotate 90 degrees")){
            img.setRotation(90);
        } else if(MainActivity.rotation.equals("Rotate 180 degrees")){
            img.setRotation(180);
        } else if(MainActivity.rotation.equals("Rotate 270 degrees")){
            img.setRotation(270);
        }

        //OnclickListeners; could apply some kind of animation here on reveal
		for(int i = 0; i < options.length; i++) {
            options[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onOptionItemClicked(v, (String) v.getTag(), correctOption, correctView);
                }
            });
        }

        return view;
    }
	
	public String getAlbumName(){
		String name = this.correctOption.replace("_correct", "");
		return name;
	}


}
