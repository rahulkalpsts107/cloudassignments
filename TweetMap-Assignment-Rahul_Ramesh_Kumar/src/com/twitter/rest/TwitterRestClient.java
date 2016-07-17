package com.twitter.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.twitter.engine.TwitterBackendStore;
import com.twitter.formatter.TwitterFormatter;
import com.twitter.model.TweetModel;

@Path("/tweetmap")
public class TwitterRestClient {

	@GET
	@Path("/gettweets/{query}/")
	@Produces(MediaType.APPLICATION_JSON )
	public List<TweetModel> getAllTweets(@PathParam ("query") String query)
	{
		System.out.println("Query is "+query);
		List<TweetModel> fetchTweets = new ArrayList<>();
		TwitterBackendStore backendStore = TwitterBackendStore.getInstance();
		backendStore.queryTweets(query, fetchTweets);
		for(TweetModel m: fetchTweets)
			TwitterFormatter.formatTweet(m);
		return fetchTweets;
	}
}
