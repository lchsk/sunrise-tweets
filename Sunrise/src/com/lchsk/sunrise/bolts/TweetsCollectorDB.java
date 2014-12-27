package com.lchsk.sunrise.bolts;

import java.net.UnknownHostException;
import java.util.Map;
import java.util.logging.Logger;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;

import com.lchsk.sunrise.SunriseConfig;
import com.lchsk.sunrise.db.DBConn;
import com.lchsk.sunrise.util.Utils;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class TweetsCollectorDB extends BaseBasicBolt
{
    private static final Logger log = Logger.getLogger(TweetsCollectorDB.class.getName());

    @Override
    public void cleanup()
    {
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector)
    {
        DBCollection coll;
        try
        {
            coll = DBConn.getInstance().getSourceTweetsCollection();
            DBObject o = Utils.parseJSON(input.getValueByField("tweet").toString());
            
            coll.insert(o);
        }
        catch (UnknownHostException e)
        {
            log.severe("Error connecting to the database...");
        }
        catch(Exception e)
        {
            log.severe("Error inserting a tweet into database. Message: " + e.getMessage());
        }
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context)
    {
        SunriseConfig.getInstance().registerLogger(log);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer)
    {
    }

}
