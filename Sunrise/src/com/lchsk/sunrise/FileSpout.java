package com.lchsk.sunrise;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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

public class FileSpout extends BaseRichSpout
{
    private final static Logger log = Logger.getLogger(FileSpout.class.getName());
    
    private SpoutOutputCollector collector;
    private BufferedReader file;
    
    public FileSpout()
    {    
    }
    
    @Override
    public void nextTuple()
    {
        try
        {
            collector.emit(new Values(file.readLine()));
            System.out.println(file.readLine());
        } catch (IOException e)
        {
            log.severe("Error when emitting a tweet...");
            e.printStackTrace();
        } 
    }

    @Override
    public void open(Map p_conf, TopologyContext p_context, SpoutOutputCollector p_collector)
    {
        SunriseConfig.getInstance().registerLogger(log);
        collector = p_collector;
    
        try
        {
            file = new BufferedReader(new FileReader(SunriseConfig.getInstance().tweetsFile));
        } catch (FileNotFoundException e)
        {
            log.severe("Cannot open '" + SunriseConfig.getInstance().tweetsFile + "'...");
            e.printStackTrace();
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer)
    {
        declarer.declare(new Fields("tweet"));
    }
}