package com.lchsk.sunrise;

import java.util.logging.Logger;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.TopologyBuilder;

import com.lchsk.sunrise.Main.Mode;
import com.lchsk.sunrise.bolts.TweetsCollectorFile;

/**
 * To run this topology you should execute this main as: java -cp
 * theGeneratedJar.jar twitter.streaming.Topology <track> <twitterUser>
 * <twitterPassword>
 *
 * @author StormBook
 *
 */
public class Topology
{
    private static final Logger log = Logger.getLogger(Topology.class.getName());
    
    public Topology()
    {
        
    }
    
    public void build()
    {
        TopologyBuilder builder = new TopologyBuilder();
        
        if (SunriseConfig.getInstance().getMode() == Mode.DEBUGGING)
            builder.setSpout("sunrise-tweets", new FileSpout(), 1);
        else
            builder.setSpout("sunrise-tweets", new StreamingSpout(), 1);
        
        if (SunriseConfig.getInstance().getMode() == Mode.COLLECT_TWEETS_FILE)
            builder.setBolt("tweets-collector-file", new TweetsCollectorFile()).shuffleGrouping("sunrise-tweets");

        LocalCluster cluster = new LocalCluster();
        Config conf = new Config();
        cluster.submitTopology("sunrise", conf, builder.createTopology());
    }
}