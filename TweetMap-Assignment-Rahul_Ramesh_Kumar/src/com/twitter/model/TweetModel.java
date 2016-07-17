package com.twitter.model;

import javax.xml.bind.annotation.XmlRootElement;

import io.searchbox.annotations.JestId;

@XmlRootElement
public class TweetModel {
	
	public TweetModel(String documentId, String tweetContent, String tweetCreatedAt, String tweetId, String userId,
			String userName, String geo, String coordinates, String location) {
		super();
		this.documentId = documentId;
		this.tweetContent = tweetContent;
		this.tweetCreatedAt = tweetCreatedAt;
		this.tweetId = tweetId;
		this.userId = userId;
		this.userName = userName;
		this.geo = geo;
		this.coordinates = coordinates;
		this.location = location;
	}

	public  TweetModel() 
	{

	}
	@JestId
	private String documentId;
	
	private String tweetContent;	
	private String tweetCreatedAt;
	private String tweetId;
	private String userId;
	private String userName;
	private String geo;
	private String coordinates;
	private String location;
	
	public String getTweetContent() {
		return tweetContent;
	}
	public void setTweetContent(String tweetContent) {
		this.tweetContent = tweetContent;
	}
	public String getTweetCreatedAt() {
		return tweetCreatedAt;
	}
	public void setTweetCreatedAt(String tweetCreatedAt) {
		this.tweetCreatedAt = tweetCreatedAt;
	}
	public String getTweetId() {
		return tweetId;
	}
	public void setTweetId(String twitterId) {
		this.tweetId = twitterId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getGeo() {
		return geo;
	}
	public void setGeo(String geo) {
		this.geo = geo;
	}
	public String getCoordinates() {
		return coordinates;
	}
	public void setCoordinates(String coordinates) {
		this.coordinates = coordinates;
	}

	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getDocumentId() {
		return documentId;
	}
	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}
	
	@Override
	public String toString() {
		return "TweetModel [tweetContent=" + tweetContent + ", tweetCreatedAt=" + tweetCreatedAt + ", tweetId="
				+ tweetId + ", userId=" + userId + ", userName=" + userName + ", geo=" + geo + ", coordinates="
				+ coordinates + ", location=" + location + "]";
	}
}
