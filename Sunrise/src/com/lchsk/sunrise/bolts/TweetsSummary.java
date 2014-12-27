package com.lchsk.sunrise.bolts;

import java.net.UnknownHostException;
import java.util.Map;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.lchsk.sunrise.SunriseConfig;
import com.lchsk.sunrise.db.DBConn;
import com.lchsk.sunrise.util.Utils;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class TweetsSummary extends BaseBasicBolt
{
    private static final Logger log = Logger.getLogger(TweetsSummary.class.getName());
    private BasicOutputCollector collector;
    private DBCollection collection;

    @Override
    public void cleanup()
    {
        
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector p_collector)
    {
        collector = p_collector;
        JSONObject json = (JSONObject) input.getValueByField("tweet");
        
        saveTweet(json);
    }
    
    private void saveTweet(JSONObject p_json)
    {
        DBObject o = Utils.parseJSON(p_json.toJSONString());
        
        collection.save(o);
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context)
    {
        try
        {
            collection = DBConn.getInstance().getDestinationTweetsCollection();
        }
        catch (UnknownHostException e)
        {
            log.severe("Error connecting to the databasee...");
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer)
    {
        declarer.declare(new Fields("tweet"));
    }
}
