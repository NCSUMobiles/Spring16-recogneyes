package com.recognize.match.helper;


import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.recognize.match.bean.ImageBean;
import com.recognize.match.bean.QuestionBean;
import com.recognize.match.bean.ThemeRowBean;
import com.recognize.match.dataset.DataSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * This class offers a lot of useful functions relevant to creating content and applying the Blur
 * effect on an image.
 */
public class Utils {
	
	public static List<List<QuestionBean>> masterList;
    public static List<ThemeRowBean> albumThumbnails;
	public static Context applicationContext;
	private static Random random;
    public static HashMap<Integer,String> ImagePathsOnServer;
    public static List<String> funFacts;

    public static final int INITIAL_CAPACITY = 13;
    public static int FINAL_CAPACITY = 13;

    /**
     * Creates the master list of the local content from DataSet
     */
	public static void createMasterList(){
        DataSet.initializeDataset();
		masterList = new ArrayList<>(INITIAL_CAPACITY);
        random = new Random(System.nanoTime());
        int length = DataSet.themes.size();
		for(int i = 0; i < length; i++) {
			masterList.add(getThemeList(DataSet.themes.get(i)));
		}
	}

    //updates the masterList with the new data from server which was added in the sqlite db
    public static void updateMasterList(String title, String titleId, List<QuestionBean> newAlbum){
        funFacts = new ArrayList<>();
        masterList.add(FINAL_CAPACITY,newAlbum);
        DataSet.themeTitleArray.add(FINAL_CAPACITY,title);
        DataSet.themeIdArray.add(FINAL_CAPACITY,titleId);

        //TODO HACK! remove this code and retrieve facts in GameManagerActivity directly
        for(QuestionBean qb : newAlbum){
            funFacts.add(qb.getFunFact());
        }

        FINAL_CAPACITY++;
        //Need to add new content into DataSet.themes also
    }

    /**
     * Creates thumbnails of the local content
     */
    public static void createAlbumThumbnails(){
        albumThumbnails = new ArrayList<>();
        int length = DataSet.themeIdArray.size();
        Resources res = applicationContext.getResources();
        for(int i = 0; i < length; i++){
            ThemeRowBean b = new ThemeRowBean(DataSet.themeTitleArray.get(i),
                    BitmapFactory.decodeResource(res, Utils.getResId(DataSet.themeIdArray.get(i) + "_represent", "drawable")));
            albumThumbnails.add(b);
        }
    }

    /**
     * Construct a list of GameImageBeans
     * @param imgGrpNames
     * @return
     */
	private static List<QuestionBean> getThemeList(List<String> imgGrpNames) {
        List<QuestionBean> list = new ArrayList<>();
        int length = imgGrpNames.size();
        for (int i = 0; i < length; i++){
            list.add(getImageObject(imgGrpNames.get(i), getOptionsArray(imgGrpNames.get(i) + "_correct",
                    imgGrpNames.get(i) + "_alt_")));
        }
        if(!imgGrpNames.get(0).contains("omo")) {
            Collections.shuffle(list, random);
        }
		return list;
	}

	/**
	 * Construct an array of the image names shuffled; one correct, 3 others incorrect.
	 **/
	private static List<String> getOptionsArray(String opt, String optionName) {
        List<String> list = new ArrayList<>();
        list.add(0, opt);
        for (int i = 1; i < 4; i++){
            list.add(i, optionName + i);
        }
        Collections.shuffle(list, random);
		return list;
	}

	/**
	 * Creates QuestionBean object
	 **/
	private static QuestionBean getImageObject(String title, List<String> optionsList){
		int image = getResId(title + "_" + 0, "drawable");
        if(title.contains("omo")){
            image = getResId(title+"_correct","drawable");
        }
		List<ImageBean> options = new ArrayList<>();
        int length = optionsList.size();
		for(int i = 0; i < length; i++) {
            options.add(new ImageBean(optionsList.get(i), getResId(optionsList.get(i), "drawable"),""));
        }
		String correctOption = title+"_correct";
        String difficulty = "Easy";
//        Log.d("Title","inside getImageObject: "+title);
		return new QuestionBean(image, options, title, correctOption, difficulty);
	}
	
	public static int getResId(String variableName, String defType) {
	    int identifier = applicationContext
                .getResources()
                .getIdentifier(variableName, defType, applicationContext.getPackageName());
	    return identifier;
	}

    // Stack Blur v1.0 from
    // http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
    // Java Author: Mario Klingemann <mario at quasimondo.com>
    // http://incubator.quasimondo.com
    public static Bitmap fastblur(Bitmap sentBitmap, int radius) {

        // created Feburary 29, 2004
        // Android port : Yahel Bouaziz <yahel at kayenko.com>
        // http://www.kayenko.com
        // ported april 5th, 2012

        // This is a compromise between Gaussian Blur and Box blur
        // It creates much better looking blurs than Box Blur, but is
        // 7x faster than my Gaussian Blur implementation.

        // I called it Stack Blur because this describes best how this
        // filter works internally: it creates a kind of moving stack
        // of colors whilst scanning through the image. Thereby it
        // just has to add one new block of color to the right side
        // of the stack and remove the leftmost color. The remaining
        // colors on the topmost layer of the stack are either added on
        // or reduced by one, depending on if they are on the right or
        // on the left side of the stack.

        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = ( 0xff000000 & pix[yi] ) | ( dv[rsum] << 16 ) | ( dv[gsum] << 8 ) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

//        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }

}
