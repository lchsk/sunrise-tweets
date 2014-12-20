package com.lchsk.sunrise.bolts;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;

import com.lchsk.sunrise.SunriseConfig;

public class TweetsCollectorFile extends BaseBasicBolt
{
    private static final Logger log = Logger.getLogger(TweetsCollectorFile.class.getName());
    
    private BufferedWriter file;

    @Override
    public void cleanup()
    {
        try
        {
            file.close();
        } catch (IOException e)
        {
            log.severe("Error when closing " + SunriseConfig.getInstance().tweetsFile);
            e.printStackTrace();
        }
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector)
    {
        try
        {
            file.append(input.getValueByField("tweet").toString());
            file.flush();
        } catch (IOException e)
        {
            log.severe("Cannot save '" + input + "' to a file.");
            e.printStackTrace();
        }
        finally{
        }
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context)
    {
        SunriseConfig.getInstance().registerLogger(log);
        try
        {
            file = new BufferedWriter(new FileWriter(SunriseConfig.getInstance().tweetsFile, true ));
        } catch (IOException e)
        {
            log.severe("File " + SunriseConfig.getInstance().tweetsFile + " cannot be opened.");
            e.printStackTrace();
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer)
    {
    }

}
