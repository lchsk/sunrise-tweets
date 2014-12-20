package com.lchsk.sunrise;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.joda.time.DateTime;

import com.lchsk.sunrise.Main.Mode;

public class SunriseConfig
{
    private static SunriseConfig instance = null;

    private FileHandler logFile;
    private final Logger log = Logger.getLogger(SunriseConfig.class.getName());
    private final String dateSeparator = "-";
    private final String timeSeparator = ":";
    private final String logFileExtension = ".log";
    private final String logDirectory = "log/";
    
    /*
     * File that holds list of languages with translations of the word 'sunrise'
     */
    private final String translationsFile = "translations.txt";
    private HashMap<String, ArrayList<String>> translations;

    // Keys and tokens for Twitter API
    public final String consumerKey = "rHO8ZBkyTjQT6ppkml17y3Lzo";
    public final String consumerSecret = "Zs4K6EPGDnSw8kwznE6DEkPJd6dYBrdMNzCNpCwYl7WejZ1b2h";
    public final String token = "929806328-zwrZbmgijZHjHy97FqaY8RIDJ82qcid1vxX53Nm9";
    public final String secret = "2o6JEsDN7AfrmCW8Ot2an6hIoqyuPoGVmr0GFThp23Tbo";

    // Name of the file that stores tweets for debugging
    public final String tweetsFile = "tweets-debug.txt";

    private Mode mode;

    public static SunriseConfig getInstance()
    {
        if (instance == null)
        {
            synchronized (SunriseConfig.class)
            {
                if (instance == null)
                {
                    instance = new SunriseConfig();
                }
            }
        }

        return instance;
    }

    private SunriseConfig()
    {
        try
        {
            logFile = new FileHandler(logDirectory + getLogFilename(), false);
            logFile.setFormatter(new SimpleFormatter());
            registerLogger(log);
        } catch (SecurityException | IOException e)
        {
            e.printStackTrace();
        }

        translations = new HashMap<String, ArrayList<String>>();
        mode = Mode.COLLECT_TWEETS_FILE;
    }

    /*
     * Opens a file with translations of the word 'sunrise'. Reads that file and
     * stores translations in a hashmap;
     */
    public void readTranslationsFile()
    {
        try
        {
            BufferedReader file = new BufferedReader(new FileReader(translationsFile));

            if (file.ready())
                log.info("File " + translationsFile + " was successfully opened.");

            String line;

            while ((line = file.readLine()) != null)
            {
                String[] data = line.split("=");
                String[] temp = data[1].split(",");

                ArrayList<String> wordList = new ArrayList<String>();
                for (String t : temp)
                    wordList.add(t.trim());

                translations.put(data[0].trim(), wordList);
            }

            file.close();

        } catch (Exception e)
        {
            log.severe("The file " + translationsFile + " does not exist.");
            e.printStackTrace();
        }

    }

    /*
     * Adds logger from another class to the same logfile.
     */
    public void registerLogger(Logger p_logger)
    {
        p_logger.addHandler(logFile);
    }

    /*
     * Generates a new log file filename (based on current date and time).
     */
    public String getLogFilename()
    {
        StringBuilder dateString = new StringBuilder();
        DateTime date = new DateTime();
        dateString.append(date.getYear());
        dateString.append(dateSeparator);
        dateString.append(date.getMonthOfYear());
        dateString.append(dateSeparator);
        dateString.append(date.getDayOfMonth());
        dateString.append(dateSeparator);
        dateString.append(date.getHourOfDay());
        dateString.append(timeSeparator);
        dateString.append(date.getMinuteOfHour());
        dateString.append(timeSeparator);
        dateString.append(date.getSecondOfMinute());

        return dateString.toString() + logFileExtension;
    }

    public void setMode(Mode p_mode)
    {
        mode = p_mode;
    }

    public Mode getMode()
    {
        return mode;
    }

    public ArrayList<String> getTrackedWords()
    {
        ArrayList<String> tracked = new ArrayList<String>();

        for (String lang : translations.keySet())
        {
            tracked.addAll(translations.get(lang));
        }

        // System.out.println(tracked);
        return tracked;
    }
}
