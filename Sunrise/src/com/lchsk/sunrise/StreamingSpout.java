package com.lchsk.sunrise;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;

/**
 * StreamingSpout connects to the Twitter Streaming API
 * (using Hosebird Client) and emits tweets.
 * Can be used in real-time (live system) or
 * for gathering tweets and storing them in file/DB.
 *
 */
public class StreamingSpout extends BaseRichSpout
{
    private final static Logger log = Logger.getLogger(StreamingSpout.class.getName());
    
    private BlockingQueue<String> queue;
    private SpoutOutputCollector collector;
    private StatusesFilterEndpoint endpoint;
    
    public StreamingSpout()
    {    
    }
    
    @Override
    public void nextTuple()
    {
        try
        {
            collector.emit(new Values(queue.take()));
        } catch (InterruptedException e)
        {
            log.severe("Error when emitting a tweet...");
            e.printStackTrace();
        } 
    }

    @Override
    public void open(Map p_conf, TopologyContext p_context, SpoutOutputCollector p_collector)
    {
        SunriseConfig.getInstance().registerLogger(log);
        
        queue = new LinkedBlockingQueue<String>(10000);
        collector = p_collector;
        endpoint = new StatusesFilterEndpoint();
        
        // here, keywords to track are specified
        // normally they're list of words, ie. 'sunrise' and its translations
        endpoint.trackTerms(SunriseConfig.getInstance().getTrackedWords());

        // connection to Twitter Streaming API
        // using keys and tokens from Config singleton
        
        Authentication auth = new OAuth1(SunriseConfig.getInstance().consumerKey, SunriseConfig.getInstance().consumerSecret, SunriseConfig.getInstance().token, SunriseConfig.getInstance().secret);

        Client client = new ClientBuilder().hosts(Constants.STREAM_HOST).endpoint(endpoint).authentication(auth)
                .processor(new StringDelimitedProcessor(queue)).build();

        client.connect();
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer)
    {
        declarer.declare(new Fields("tweet"));
    }
}