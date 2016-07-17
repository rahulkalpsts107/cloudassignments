package com.twitter.formatter;

import com.twitter.model.TweetModel;

public final class TwitterFormatter {

	public static void formatTweet(TweetModel tweet)
	{
		String geo = tweet.getGeo();
		String location = tweet.getLocation();
		if(geo.equals("null"))
		{
			//System.out.println("GEO is not present");
			
		}
		else
		{
			//System.out.println("GEO is present");
		}
	}
}
