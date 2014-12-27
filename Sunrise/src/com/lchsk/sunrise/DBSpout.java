package com.lchsk.sunrise;

import java.net.UnknownHostException;
import java.util.Map;
import java.util.logging.Logger;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

import com.lchsk.sunrise.db.DBConn;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;

public class DBSpout extends BaseRichSpout
{
    private final static Logger log = Logger.getLogger(DBSpout.class.getName());

    private SpoutOutputCollector collector;
    private DBCollection collection;
    private DBCursor cursor;

    public DBSpout()
    {
    }

    @Override
    public void nextTuple()
    {
        try
        {
            if (cursor.hasNext())
            {
                collector.emit(new Values(cursor.next()));   
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            log.severe("Error when emitting a tweet...");
        }
    }

    @Override
    public void open(Map p_conf, TopologyContext p_context, SpoutOutputCollector p_collector)
    {
        SunriseConfig.getInstance().registerLogger(log);
        collector = p_collector;
        try
        {
            collection = DBConn.getInstance().getSourceTweetsCollection();
            cursor = collection.find();
        }
        catch (UnknownHostException e)
        {
            log.severe("Error connecting to the database");
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer)
    {
        declarer.declare(new Fields("tweet"));
    }
}