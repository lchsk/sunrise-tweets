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
 * Topology class describes different ways 
 * that topology can be built.
 * Depending on chosen topology, different 
 * spouts and bolts are present.
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
                
            // Collect tweets and save them in DB
            case COLLECT_TWEETS_DB:
                builder.setSpout("sunrise-tweets", new StreamingSpout(), 1);
                builder.setBolt("tweets-collector-db", new TweetsCollectorDB()).shuffleGrouping("sunrise-tweets");

                break;
            
            // debugging with file data
            case DEBUGGING:

                builder.setSpout("sunrise-tweets", new FileSpout(), 1);
                builder.setBolt("language-identifier", new LanguageIdentifier()).shuffleGrouping("sunrise-tweets");
                builder.setBolt("location-finder", new LocationFinder()).shuffleGrouping("language-identifier");
                builder.setBolt("tweets-summary", new TweetsSummary()).shuffleGrouping("location-finder");
                break;

            // debugging with tweets from DB
            case DEBUGGING_DB:

                builder.setSpout("sunrise-tweets", new DBSpout(), 1);
                builder.setBolt("language-identifier", new LanguageIdentifier()).shuffleGrouping("sunrise-tweets");
                builder.setBolt("location-finder", new LocationFinder()).shuffleGrouping("language-identifier");
                builder.setBolt("tweets-summary", new TweetsSummary()).shuffleGrouping("location-finder");
                break;
            
            // with this setting, the system will work in real-time
            // spout will connect to twitter API and emit tweets
            // that will later be processed, and results will 
            // be saved to the DB
            case LIVE:
                builder.setSpout("sunrise-tweets", new StreamingSpout(), 1);
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