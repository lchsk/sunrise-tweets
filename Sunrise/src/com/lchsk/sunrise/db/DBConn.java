package com.lchsk.sunrise.db;

import java.net.UnknownHostException;
import java.util.regex.Pattern;

import com.lchsk.sunrise.SunriseConfig;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

/**
 * Class different operations on MongoDB
 * @author root
 *
 */
public class DBConn
{
    // we're going to use singleton again :(
    private static DBConn instance = null;

    // necessary mongo objects
    private MongoClient mongoClient;
    private DB db;

    // connection data (read from the config file)
    private String dbName;
    private String dbHost;
    private String citiesCollection;
    private String sourceTweetsCollection;
    private String destinationTweetsCollection;

    private final String FIELD_ID = "geonameid";
    
    /**
     * Methods for searching city
     * in a DB collection that holds cities data.
     */
    public enum CitySearchMode
    {
        // use only original name (not necessarily in English)
        ORIGINAL_NAME_ONLY,
        
        // use original name + name that uses only ASCII characters
        ORIGINAL_PLUS_ASCII,
        
        // after some tests, turns out that this mode is not very practical
        // performs case-insensitive search
        // can find any part of the city name
        ALTERNATE_NAMES_GENEROUS,
        
        // searches among original and ascii name
        // as well as alternate names (in various languages)
        // but only finds full names (not parts)
        ALTERNATE_NAMES_FULL
    }

    private DBConn() throws UnknownHostException
    {
        dbHost = SunriseConfig.getInstance().getSetting("db_host");
        dbName = SunriseConfig.getInstance().getSetting("db_name");
        citiesCollection = SunriseConfig.getInstance().getSetting("cities_coll");
        sourceTweetsCollection = SunriseConfig.getInstance().getSetting("deb_tweets_coll");
        destinationTweetsCollection = SunriseConfig.getInstance().getSetting("tweets_coll");

        mongoClient = new MongoClient(dbHost);
        db = mongoClient.getDB(dbName);
    }

    public static DBConn getInstance() throws UnknownHostException
    {
        if (instance == null)
            instance = new DBConn();

        return instance;
    }

    /**
     * Finds largest city thats within p_kilometers from given coordinates 
     * @param p_longitude
     * @param p_latitude
     * @param p_kilometers
     * @return -1 if no city was found, otherwise, a DB id
     */
    public int findCity(double p_longitude, double p_latitude, double p_kilometers)
    {
        // cities are stored in Mongo collection 'cities'
        DBCollection coll = db.getCollection(citiesCollection);
        BasicDBList myLocation = new BasicDBList();
        myLocation.put(0, p_longitude);
        myLocation.put(1, p_latitude);
        
        // critical operation is performed by MongoDB
        // which offers operation on coordinates
        // thanks it its 2dsphere indexing
        DBObject val = coll.findOne(new BasicDBObject("loc", new BasicDBObject("$near", new BasicDBObject("$geometry", new BasicDBObject(
                "type", "Point").append("coordinates", myLocation)).append("$maxDistance", p_kilometers))));

        if (val != null && val.get(FIELD_ID) != null)
            return (int) val.get(FIELD_ID);
        else
            return -1;
    }

    /**
     * Returns query that is used for searching for cities 
     * in different modes.
     * 
     * @param p_city
     * @param p_mode
     * @return
     */
    private DBObject buildQuery(String p_city, CitySearchMode p_mode)
    {
        switch (p_mode)
        {
            case ALTERNATE_NAMES_GENEROUS:
            {
                BasicDBObject query1 = new BasicDBObject("name", p_city);
                
                // because of this regex, any part of the city name can be found
                Pattern regex = Pattern.compile(".*" + p_city + ".*", Pattern.CASE_INSENSITIVE);
                BasicDBObject query2 = new BasicDBObject("alternatenames", regex);
                BasicDBList or = new BasicDBList();
                or.add(query1);
                or.add(query2);
                return new BasicDBObject("$or", or);
            }
            
            case ALTERNATE_NAMES_FULL:
            {
                BasicDBObject query1 = new BasicDBObject("name", p_city);
                
                // make sure we can find names that are at the beginning, in the middle and at the end
                Pattern regex1 = Pattern.compile(".*," + p_city + ",.*");
                Pattern regex2 = Pattern.compile("^" + p_city + ",.*");
                Pattern regex3 = Pattern.compile(".*," + p_city + "$");
                
                BasicDBObject query2 = new BasicDBObject("asciiname", p_city);
                
                BasicDBObject query3 = new BasicDBObject("alternatenames", regex1);
                BasicDBObject query4 = new BasicDBObject("alternatenames", regex2);
                BasicDBObject query5 = new BasicDBObject("alternatenames", regex3);
                
                BasicDBList or = new BasicDBList();
                or.add(query1);
                or.add(query2);
                or.add(query3);
                or.add(query4);
                or.add(query5);
                return new BasicDBObject("$or", or);
            }
            
            case ORIGINAL_NAME_ONLY:
                return new BasicDBObject("name", p_city);
            
            case ORIGINAL_PLUS_ASCII:
            {
                BasicDBObject query1 = new BasicDBObject("name", p_city);
                BasicDBObject query2 = new BasicDBObject("asciiname", p_city);
                
                BasicDBList or = new BasicDBList();
                or.add(query1);
                or.add(query2);
                return new BasicDBObject("$or", or);
            }
        }
        
        return null;
    }

    /**
     * Run a query for searching a city and collect results.
     * 
     * @param p_city
     * @param p_mode
     * @return
     */
    public int findCity(String p_city, CitySearchMode p_mode)
    {
        try
        {
            // obtain a query
            DBObject query = buildQuery(p_city, p_mode);

            DBCollection coll = db.getCollection(citiesCollection);
            
            // city with a largest population is selected
            // -1 means sorting in decreasing order
            DBCursor cursor = coll.find(query).sort(new BasicDBObject("population", -1)).limit(1);

            DBObject val = cursor.next();
            cursor.close();

            if (val != null && val.get(FIELD_ID) != null)
                return (int) val.get(FIELD_ID);
            else
                return -1;
        } catch (Exception e)
        {
            return -1;
        }
    }

    /**
     * Return a single document (in Mongo speak) about a city, based on its ID.
     * @param p_id
     * @return
     */
    public DBObject getRow(int p_id)
    {
        try
        {
            BasicDBObject query = new BasicDBObject(FIELD_ID, p_id);
            DBCollection coll = db.getCollection(citiesCollection);
            DBCursor cursor = coll.find(query).limit(1);

            DBObject val = cursor.next();
            cursor.close();

            return val;
        } catch (Exception e)
        {
            return null;
        }
    }
    
    public DBCollection getCollection(String p_collection)
    {
        return db.getCollection(p_collection);
    }
    
    public DBCollection getCitiesCollection()
    {
        return getCollection(citiesCollection);
    }
    
    public DBCollection getSourceTweetsCollection()
    {
        return getCollection(sourceTweetsCollection);
    }
    
    public DBCollection getDestinationTweetsCollection()
    {
        return getCollection(destinationTweetsCollection);
    }
}
