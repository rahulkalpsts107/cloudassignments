package com.twitter.engine;

import java.io.IOException;
import java.util.List;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.elasticsearch.AWSElasticsearchClient;
import com.amazonaws.services.elasticsearch.model.DescribeElasticsearchDomainRequest;
import com.amazonaws.services.elasticsearch.model.DescribeElasticsearchDomainResult;
import com.amazonaws.services.elasticsearch.model.ElasticsearchDomainStatus;
import com.twitter.config.Config;
import com.twitter.model.TweetModel;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;


public class TwitterBackendStore {
	private AWSElasticsearchClient searchClient;
	private boolean isDomainAdded;
	private JestClient client;
	public static TwitterBackendStore instance= null;
	protected TwitterBackendStore()
	{
			isDomainAdded = false;
			searchClient = new AWSElasticsearchClient(new DefaultAWSCredentialsProviderChain().getCredentials());
			searchClient.setRegion(Region.getRegion(Regions.US_WEST_2));
			searchClient.setEndpoint("es.us-west-2.amazonaws.com");
			DescribeElasticsearchDomainRequest request = new DescribeElasticsearchDomainRequest();
			request.setRequestCredentials(new DefaultAWSCredentialsProviderChain().getCredentials());
			request.setDomainName(Config.DOMAIN);
			DescribeElasticsearchDomainResult res = searchClient.describeElasticsearchDomain(request);
			JestClientFactory factory = new JestClientFactory();
			 factory.setHttpClientConfig(new HttpClientConfig
			                        .Builder(Config.ENDPOINT)
			                        .multiThreaded(true)
			                        .build());
			client = factory.getObject();
			if(res.getDomainStatus().getCreated())
			{
				DescribeElasticsearchDomainResult result  = searchClient.describeElasticsearchDomain(request);
				ElasticsearchDomainStatus status =result.getDomainStatus();
				System.out.println(status.getEndpoint());
				isDomainAdded = true;
				System.out.println(Config.DOMAIN+" is running");
			}
	}
	
	public static TwitterBackendStore getInstance()
	{
		if(instance == null)
			instance = new TwitterBackendStore();
		return instance;
	}
	public boolean isDomainConfigured()
	{
		return isDomainAdded;
	}

	public void queryTweets(String keyword ,List<TweetModel> fetchTweets)
	{
		keyword+="\"";
		String query = "{\n" +
            "    \"query\": {\n" +
            "        \"filtered\" : {\n" +
            "            \"query\" : {\n" +
            "                \"query_string\" : {\n" +
            "                    \"query\" : \""+keyword+"\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "    }," +
            "	\"size\": 1000\n" +
            "}";

		Search search = new Search.Builder(query)
		                // multiple index or types can be added.
		                .addIndex("twitter")
		                .build();
		SearchResult searchResult;
		try
		{
			searchResult = client.execute(search);
			System.out.println(searchResult.getErrorMessage());
			List<SearchResult.Hit<TweetModel, Void>> hits = searchResult.getHits(TweetModel.class);
			for(SearchResult.Hit<TweetModel, Void> a : hits)
			{
				//System.out.println(a.source.getGeo());
				fetchTweets.add(a.source);
			}
			System.out.println("Search produced " + hits.size());
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Exception while searching");
		}
	}
	public void addTweetsToES(List<TweetModel> tweets)
	{
		for(TweetModel m: tweets)
		{
			 Index index = new Index.Builder(m).index("twitter").type("tweet").build();
			 try 
			 {
				client.execute(index);
				
			 } 
			 catch (IOException e) 
			 {
				// TODO Auto-generated catch block
				System.out.println("Execute failed to add tweets");
				return;
			 }
		}
		//System.out.println("Added to backend");
		
	}
}