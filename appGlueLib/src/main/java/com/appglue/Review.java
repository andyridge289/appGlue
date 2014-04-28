package com.appglue;

public class Review 
{
	private int rating;
	private String reviewer;
	private String review;
	private String author;
	private String device;
	
	public Review(String reviewer, int rating, String review, String author, String device)
	{
		this.reviewer = reviewer;
		this.rating = rating;
		this.review = review;
		this.author = author;
		this.device = device;
	}

	public String getReviewer() {
		return reviewer;
	}

	public void setReviewer(String reviewer) {
		this.reviewer = reviewer;
	}

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

	public String getReview() {
		return review;
	}

	public void setReview(String review) {
		this.review = review;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}
	
	
	
	
}
