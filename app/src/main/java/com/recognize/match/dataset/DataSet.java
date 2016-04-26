package com.recognize.match.dataset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class holds all static data related to the local content that ships with the app.
 */
public class DataSet {

    public static void initializeDataset(){
        for(int i=0;i<static_data.length;i++){
            themes.add(new ArrayList<String>(Arrays.asList(static_data[i])));
        }
    }
	//update variables with new library name
	//follow android naming convention
	public static List<String> themeIdArray = new ArrayList<>(Arrays.asList("mustaches","city_skylines","aliens","art","superheroes",
            "usflags","dogs","microscopic","animal_patterns","cartoons","celeb_yearbook","dog_breed",
            "presidents"));
	
	public static List<String> themeTitleArray = new ArrayList<>(Arrays.asList("Mustaches","City SkyLines","Aliens",
            "Art & Artists","Super Heroes","US Flags","Dogs","Microscopic","Animal Patterns",
            "Cartoons","Celebrities","Dog Breed", "US Presidents"));

	// In future they will be updated by web service to reflect trending albums
	// Currently same as themeIdArray
	public static List<String> trendArray = themeIdArray;
	
	// Add a new row for each library
	// Names of actual images (follow android naming convention)
    public static List<List<String>> themes = new ArrayList<>();
	private static String[][] static_data = new String[][]{
		{"chaplin","einstein","dali","frank_zappa","hulk_hogan","jhonny_depp","prince","sasha_baron","john_waters","will_ferrel"},
		{"amsterdam","chicago","dubai","hongkong","london","newyork","pittsburgh","prague","sanfrancisco","seattle","sydney","tokyo","venice"},
		{"fifthelement","alf","alien","et","hgttg","ij","marsattacks","mib","sw","wow"},
		{"bosch","goya","kahlo","klimt","picasso","pollock","titian","vg","warhol","wyeth"},
		{"batman","captain","flash","gambit","hulk","ironman","spiderman","storm","superman","wonder"},
		{"ar","az","ca","fl","la","md","nc","ok","ri","wa"},
		{"aussie","beagle","dalmatian","german","golden","labrador","poodle","rottweiler","sanbernardo","snauzer"},
		{"eyelashes","guitarstring","mascara_brush","needle_thread","saltpepper","toothbrush","tp","used_dental_floss","velcro","vinyllp"},
		{"fish","giraffe","cheetah","peacock","snake","tiger","turkey"},
		{"arnold","bender","cartman","charlie_brown","doug","homer","krumm","rocko","stewie"},
		{"ap","eb","gwb","ow","rdj","rz","sp","zd"},
        {"basset_hound","boxer","chihuahua","cocker_spaniel","dalmation","doverman", "german_shepard","great_dane","irish_wolfhound","poodle_new"},
        {"bush","carter","clinton","eisenhower","grant","lincoln","nixon","obama","reagan", "roosevelt","truman"}
    };

}
