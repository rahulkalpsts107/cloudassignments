package com.twitter.engine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Path;


import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import com.amazonaws.util.json.JSONTokener;
import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.core.builder.*;
import com.github.scribejava.core.model.*;
import com.github.scribejava.core.oauth.*;
import com.twitter.config.Config;
import com.twitter.model.TweetModel;

@Path("/tweetmap")
public class TwitterClient {
	List<TweetModel> tweets;
	public TwitterClient()
	{
		tweets = new ArrayList<>();
	}
	
	public class TwitterConsumer extends Thread
	{
		public void run()
		{
			try{
	            System.out.println("Starting Twitter public stream consumer thread.");

	            // Enter your consumer key and secret below
	            OAuthService service = new ServiceBuilder()
	                    .apiKey(Config.CONSUMER_KEY)
	                    .apiSecret(Config.CONSUMER_SECRET)
	                    .build(TwitterApi.instance());
	            
	            // Set your access token
	            OAuth1AccessToken accessToken = new OAuth1AccessToken(Config.ACCESS_TOKEN, Config.ACCESS_TOKEN_SECRET);
	            // Let's generate the request
	            System.out.println("Connecting to Twitter Public Stream");
	            OAuthRequest request = new OAuthRequest(Verb.POST, Config.STREAM_URI, service);
	            request.addHeader("version", "HTTP/1.1");
	            request.addHeader("host", "stream.twitter.com");
	            request.setConnectionKeepAlive(true);
	            request.addHeader("user-agent", "Twitter Stream Reader");
	            request.addBodyParameter("track", Config.HASHTAGS_RACING_SPORT); // Set keywords you'd like to track here
	            ((OAuth10aService) service).signRequest((OAuth1AccessToken) accessToken, request);
	            Response response = request.send();

	            //
	            TwitterBackendStore store = TwitterBackendStore.getInstance();
	            if(store.isDomainConfigured())
	            {
		            // Create a reader to read Twitter's stream
		            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getStream()));
		            String line;
		            while ((line = reader.readLine()) != null) {
		            	parseContent(line);
		            	if(tweets.size() > 5)
		            	{
		            		store.addTweetsToES(tweets);
		            		tweets.clear();
		            	}
		            	else
		            		continue;
		            }
	            }
	            else
	            	System.out.println("Error , unable to configure Elastic Search Domain");
	        }
	        catch (IOException ioe){
	            System.out.println("Error in twitter streaming, exit");
	        } 
		}
		public void parseContent(String tweetLine)
		{
			try
			{
				JSONTokener tokener = new JSONTokener(tweetLine);
		        JSONObject obj = new JSONObject(tokener);
		        String createdAt = obj.get("created_at").toString();
		        String tweetContent = obj.get("text").toString();
		        String tweetId = obj.get("id").toString();
		        String geo = obj.get("geo").toString();
		        String coordinates = obj.get("coordinates").toString();
		        JSONObject usn = obj.getJSONObject("user");
		        String userName = usn.getString("name").toString();
		        String userId = usn.getString("id").toString();
		        String location = usn.getString("location").toString();;
		        if(coordinates .equals("null"))
		        {
		        	String place = obj.get("place").toString();
		        	if(!place.equals("null") )
		        	{
		        		coordinates ="b";//Special identify for bounding box
		        		JSONTokener placeTokener = new JSONTokener(place);
		        		JSONObject placeObj = new JSONObject(placeTokener);
		        		String box = placeObj.get("bounding_box").toString();
		        		placeTokener = new JSONTokener(box);
		        		placeObj = new JSONObject(placeTokener);
		        		coordinates+=placeObj.get("coordinates").toString();
		        	}
		        	else
		        		coordinates = "null";
		        	
		        }
		        TweetModel tweet = new TweetModel();
		        tweet.setCoordinates(coordinates);
		        tweet.setGeo(geo);
		        tweet.setTweetContent(tweetContent);
		        tweet.setTweetId(tweetId);
		        tweet.setUserId(userId);
		        tweet.setTweetCreatedAt(createdAt);
		        tweet.setUserName(userName);
		        tweet.setLocation(location);
		        if(tweet.getCoordinates().equals("null"))
		        	return;
		        tweets.add(tweet);
		        //System.out.println(tweet);
			}
			catch(JSONException e) 
			{
				System.out.println("Error in parsing JSON, proceed "+e.getMessage());
			}
			
		}
	}
}
