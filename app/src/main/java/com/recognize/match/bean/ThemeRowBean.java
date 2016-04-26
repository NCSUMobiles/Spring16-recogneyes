package com.recognize.match.bean;

import android.graphics.Bitmap;

public class ThemeRowBean {
	private String title;
	private Bitmap image;
	
	public ThemeRowBean(String title, Bitmap image){
		this.title = title;
		this.image = image;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	public Bitmap getImage() {
		return image;
	}
	public void setImgId(Bitmap image) {
		this.image = image;
	}

}
