package com.lchsk.sunrise;

import java.util.logging.Logger;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.TopologyBuilder;

import com.lchsk.sunrise.bolts.LanguageIdentifier;
import com.lchsk.sunrise.bolts.LocationFinder;
import com.lchsk.sunrise.bolts.TweetsCollectorDB;
import com.lchsk.sunrise.bolts.TweetsCollectorFile;
import com.lchsk.sunrise.bolts.TweetsSummary;

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

        switch (SunriseConfig.getInstance().getMode())
        {
        // Purpose of this mode it to collect tweets
        // and store them in a file for further processing
        // (in debugging mode)
            case COLLECT_TWEETS_FILE:

                builder.setSpout("sunrise-tweets", new StreamingSpout(), 1);
                builder.setBolt("tweets-collector-file", new TweetsCollectorFile()).shuffleGrouping("sunrise-tweets");
                break;

            case COLLECT_TWEETS_DB:
                builder.setSpout("sunrise-tweets", new StreamingSpout(), 1);
                builder.setBolt("tweets-collector-db", new TweetsCollectorDB()).shuffleGrouping("sunrise-tweets");

                break;

            case DEBUGGING:

                builder.setSpout("sunrise-tweets", new FileSpout(), 1);
                builder.setBolt("language-identifier", new LanguageIdentifier()).shuffleGrouping("sunrise-tweets");
                builder.setBolt("location-finder", new LocationFinder()).shuffleGrouping("language-identifier");
                builder.setBolt("tweets-summary", new TweetsSummary()).shuffleGrouping("location-finder");
                break;

            case DEBUGGING_DB:

                builder.setSpout("sunrise-tweets", new DBSpout(), 1);
                builder.setBolt("language-identifier", new LanguageIdentifier()).shuffleGrouping("sunrise-tweets");
                builder.setBolt("location-finder", new LocationFinder()).shuffleGrouping("language-identifier");
                builder.setBolt("tweets-summary", new TweetsSummary()).shuffleGrouping("location-finder");
                break;
                
            case LIVE:
                builder.setSpout("sunrise-tweets", new StreamingSpout(), 1);
//                builder.setBolt("tweets-collector-db", new TweetsCollectorDB()).shuffleGrouping("sunrise-tweets");
                builder.setBolt("language-identifier", new LanguageIdentifier()).shuffleGrouping("sunrise-tweets");
                builder.setBolt("location-finder", new LocationFinder()).shuffleGrouping("language-identifier");
                builder.setBolt("tweets-summary", new TweetsSummary()).shuffleGrouping("location-finder");
                
                break;
        }

        LocalCluster cluster = new LocalCluster();
        Config conf = new Config();
        cluster.submitTopology("sunrise", conf, builder.createTopology());
    }
}