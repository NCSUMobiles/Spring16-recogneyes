package com.recognize.match.bean;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class QuestionBean implements Serializable {
	
	private static final long serialVersionUID = 1L;
    private String theme;
	private int image;
    private String difficulty;
    private byte[] imageByte;
	private List<ImageBean> options;
	private String correctOption;
	private String funFact;

	public QuestionBean(int image, List<ImageBean> options, String theme, String correctOption, String difficulty){
		this.image = image;
		this.options = options;
        this.theme = theme;
		this.correctOption = correctOption;
        this.difficulty = difficulty;
		Collections.shuffle(this.options, new Random(System.nanoTime()));
	}
//
//    public QuestionBean(byte[] imageByte, List<ImageBean> options, String theme, String correctOption, String difficulty){
//        this.imageByte = imageByte;
//        this.options = options;
//        this.theme = theme;
//        this.correctOption = correctOption;
//        this.difficulty = difficulty;
//        Collections.shuffle(this.options, new Random(System.nanoTime()));
//    }
	
	@SuppressWarnings("unchecked")
	public QuestionBean(QuestionBean bean){
        this.image = bean.image;
        this.options = bean.options;
		this.correctOption = bean.correctOption;
        this.difficulty = bean.difficulty;
		Collections.shuffle(this.options, new Random(System.nanoTime()));
	}

    public String getTheme(){
        return theme;
    }

    public void setTheme(String theme){
        this.theme = theme;
    }

	public List<ImageBean> getOptions() {
		return options;
	}

	public void setOptions(List<ImageBean> options) {
		this.options = options;
	}

	public int getImageId() {
        return image;
	}
	public void setImages(int image) {
		this.image = image;
	}

	public String getCorrectOption() {
		return correctOption;
	}

	public void setCorrectOption(String correctOption) {
		this.correctOption = correctOption;
	}

	public String getFunFact() {
		return funFact;
	}

	public void setFunFact(String funFact) {
		this.funFact = funFact;
	}

	public byte[] getImageByte() {
        return imageByte;
    }

    public void setImageByte(byte[] imageByte) {
        this.imageByte = imageByte;
    }
}
