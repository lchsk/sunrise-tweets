package com.lchsk.sunrise;

import java.util.logging.Logger;

import com.lchsk.sunrise.db.DBConn;
import com.lchsk.sunrise.db.DBConn.CitySearchMode;
import com.lchsk.sunrise.util.Utils;

public class Main
{
    private static final Logger log = Logger.getLogger(Main.class.getName());

    public enum Mode
    {
        // runs storm application and saves tweets to a file
        // for debugging purposes
        COLLECT_TWEETS_FILE,
        
        // same as above, but saves to a database
        COLLECT_TWEETS_DB,

        // in debugging mode, a spout produces tweets
        // read from the file
        DEBUGGING,
        
        // same as debugging
        // but uses database instead of file
        DEBUGGING_DB
    }

    public static void main(String[] args)
    {
        SunriseConfig.getInstance().readTranslationsFile();
        SunriseConfig.getInstance().registerLogger(log);
        SunriseConfig.getInstance().setMode(Mode.DEBUGGING_DB);

        try
        {
            //DBConn.getInstance().findCity(-2.97, 53.41, 1000);
//            System.out.println(DBConn.getInstance().findCity("", CitySearchMode.ALTERNATE_NAMES_FULL));
        } catch (Exception e)
        {
            e.printStackTrace();
            log.severe("Unable to connect to the database...");
        }

         Topology topology = new Topology();
         topology.build();
    }
}
