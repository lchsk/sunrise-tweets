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

/**
 * FileSpout emits tweets that are stored in a file.
 * (similarly to DBSpout it is mostly used for debugging).
 *
 */
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
            
            // if it's available, emit next line from the file
            // (tweets are stored in seperate lines)
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
            // open file 
            // filename is read from SunriseConfig singleton object
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