package com.lchsk.sunrise;

import java.util.logging.Logger;

public class Main
{
    private static final Logger log = Logger.getLogger(Main.class.getName());

    public enum Mode
    {
        // runs storm application and saves tweets to a file
        // for debugging purposes
        COLLECT_TWEETS_FILE,
        
        // in debugging mode, a spout produces tweets
        // read from the file
        DEBUGGING
    }

    public static void main(String[] args)
    {
        SunriseConfig.getInstance().readTranslationsFile();
        SunriseConfig.getInstance().registerLogger(log);
        SunriseConfig.getInstance().setMode(Mode.DEBUGGING);
     
        Topology topology = new Topology();
        topology.build();
    }
}
