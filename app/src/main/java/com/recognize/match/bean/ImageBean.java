package com.recognize.match.bean;

public class ImageBean {
	private String optionName;
	private int optionId;

	private String imgPath;  //for images from server
	
	public ImageBean(String optionName, int optionId, String imgPath){
		this.optionName = optionName;
		this.optionId = optionId;
        this.imgPath = imgPath;
	}
	public String getImgPath() {
		return imgPath;
	}

	public void setImgPath(String imgPath) {
		this.imgPath = imgPath;
	}
	public int getOptionId() {
		return optionId;
	}
	public void setOptionId(int optionId) {
		this.optionId = optionId;
	}
	public String getOptionName() {
		return optionName;
	}
	public void setOptionName(String optionName) {
		this.optionName = optionName;
	}

}
