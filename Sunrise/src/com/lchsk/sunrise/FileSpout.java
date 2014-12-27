package com.lchsk.sunrise;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

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
            String line = file.readLine();
            
            if (line != null)
                collector.emit(new Values(line));
            else
                collector.emit(new Values("null"));
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