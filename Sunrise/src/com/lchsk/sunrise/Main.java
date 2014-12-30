package com.lchsk.sunrise;

import java.util.logging.Logger;

import com.lchsk.sunrise.db.DBConn;
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
            //https://raw.githubusercontent.com/drewnoakes/metadata-extractor-images/master/Apple%20iPhone%204.jpg
            
//            Double[] d = Utils.readImageCoordinates("http://lchsk.com/gps.jpg");
//            System.out.println(d[1] + ", " + d[0]);
            
            

//            System.out.println(DBConn.getInstance().findCity(-102.432504, 19.956083, 25000));
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
