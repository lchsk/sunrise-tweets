package com.lchsk.sunrise.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.net.URL;
import java.util.Locale;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.drew.imaging.ImageMetadataReader;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.GpsDirectory;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

/**
 * Utility class with methods used throughout the system.
 */
public class Utils
{
    /**
     * Returns true if a tweet is not null
     * 
     * @param p_tweet
     * @return
     */
    public static boolean notEmpty(Object p_tweet)
    {
        if (p_tweet == null || p_tweet.equals("null"))
            return false;

        return true;
    }

    /**
     * Check is string contains a valid JSON tweet data.
     * @param p_tweet
     * @return
     */
    public static boolean isTweetAJSON(String p_tweet)
    {
        if (!notEmpty(p_tweet))
            return false;

        try
        {
            JSONObject json = (JSONObject) new JSONParser().parse(p_tweet);
            return true;
        } catch (Exception e)
        {
            return false;
        }
    }

    /**
     * Convert JSON from string to Java object.
     * @param p_arg
     * @return
     * @throws ParseException
     */
    public static JSONObject getJSON(String p_arg) throws ParseException
    {
        return (JSONObject) new JSONParser().parse(p_arg);
    }

    /**
     * Change first letters in every word to upper-case
     * @param p_str
     * @return
     */
    public static String firstLettersUpperCase(String p_str)
    {
        StringBuffer res = new StringBuffer();

        // break on whitespaces
        String[] strArr = p_str.split("\\s+");
        
        for (String str : strArr)
        {
            char[] stringArray = str.trim().toCharArray();
            if (stringArray.length > 0)
                stringArray[0] = Character.toUpperCase(stringArray[0]);
            str = new String(stringArray);

            res.append(str).append(" ");
        }

        return res.toString().trim();
    }
    
    /**
     * Given a country code, return a full English name.
     * Eg. GB -> United Kingdom
     * @param p_countryCode
     * @return
     */
    public static String getCountry(String p_countryCode)
    {
        Locale locale = new Locale("en", p_countryCode, "WIN");
        return locale.getDisplayCountry();
    }
    
    /**
     * Search EXIF data for GPS coordinates.
     * @param p_url
     * @return
     */
    public static Double[] readImageCoordinates(String p_url)
    {
        try
        {
            URL link = new URL(p_url);
            
            // obtain a stream based on the URL
            BufferedInputStream in = new BufferedInputStream(link.openStream());
            
            // read image' metadata
            Metadata metadata = ImageMetadataReader.readMetadata(in, true);
            
            // read GPS data
            GpsDirectory gpsDirectory = metadata.getDirectory(GpsDirectory.class);
            
            if (gpsDirectory == null)
                return null;
            
            // read location
            GeoLocation geoLocation = gpsDirectory.getGeoLocation();
            Double[] loc = new Double[2];
            loc[0] = geoLocation.getLongitude();
            loc[1] = geoLocation.getLatitude();

            return loc;
        }
        catch(Exception e)
        {
            return null;
        }
    }
    
    /**
     * Convert JSON from string format to MongoDB JSON object
     * @param p_string
     * @return
     */
    public static DBObject parseJSON(String p_string)
    {
        return (DBObject) JSON.parse(p_string);
    }
    
    /**
     * Gets only first part of a text with a comma
     * eg. "San Francisco, California" returns "San Francisco"
     * (used when searching for a city).
     * @param p_str
     * @return
     */
    public static String extractFirstPart(String p_str)
    {
        String c = ",";
        
        if (p_str.indexOf(c) < 0)
            return p_str;
        else
            return p_str.substring(0, p_str.indexOf(c)).trim();
    }
    
    public static String removeNonAlphanumeric(String p_str)
    {
        return p_str.replaceAll("[^a-zA-Z0-9\\s]", "");
    }
}
