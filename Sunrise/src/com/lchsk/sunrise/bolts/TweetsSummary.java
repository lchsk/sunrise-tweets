package com.lchsk.sunrise.bolts;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

import org.json.simple.JSONObject;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;

import com.lchsk.sunrise.db.DBConn;
import com.lchsk.sunrise.util.Utils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * Saves processed tweet in a database.
 *
 */
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
        
        // get emitted object from the previous bolt
        JSONObject json = (JSONObject) input.getValueByField("tweet");

        saveTweet(json);
    }

    /**
     * Saves tweet in a database.
     * @param p_json
     */
    private void saveTweet(JSONObject p_json)
    {
        DBObject o = null;
        try
        {
            // change the class used to store JSON data
            // throughout processing we use Java JSONObject class
            // to save in Mongo, JSON needs to be in DBOject class
            o = Utils.parseJSON(p_json.toJSONString());

            // convert the date from timestamp to the format
            // used by Mongo
            long m = Long.valueOf((String) p_json.get("timestamp_ms"));
            Date d = new Date(m);
            o.put("created_at", d);
            collection.save(o);
        }
        catch (com.mongodb.util.JSONParseException e)
        {
            e.printStackTrace();
            log.warning("JSON is not valid.");
            p_json.put("_id", "123");
            System.out.println(p_json);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println(p_json);
            log.severe("Error parsing json: " + e.getMessage());
        }

    }

    @Override
    public void prepare(Map stormConf, TopologyContext context)
    {
        try
        {
            // obtain collection that is used to store tweets
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
