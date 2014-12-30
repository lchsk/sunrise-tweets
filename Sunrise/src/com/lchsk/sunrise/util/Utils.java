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

    public static JSONObject getJSON(String p_arg) throws ParseException
    {
        return (JSONObject) new JSONParser().parse(p_arg);
    }

    public static String firstLettersUpperCase(String p_str)
    {
        StringBuffer res = new StringBuffer();

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
    
    public static String getCountry(String p_countryCode)
    {
        Locale locale = new Locale("en", p_countryCode, "WIN");
        return locale.getDisplayCountry();
    }
    
    public static Double[] readImageCoordinates(String p_url)
    {
        try
        {
            URL link = new URL(p_url);
            
            BufferedInputStream in = new BufferedInputStream(link.openStream());
            Metadata metadata = ImageMetadataReader.readMetadata(in, true);
            
            GpsDirectory gpsDirectory = metadata.getDirectory(GpsDirectory.class);
            if (gpsDirectory == null)
                return null;
            
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
    
    public static DBObject parseJSON(String p_string)
    {
        return (DBObject) JSON.parse(p_string);
    }
    
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
