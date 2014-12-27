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

public class DBConn
{
    private static DBConn instance = null;

    private MongoClient mongoClient;
    private DB db;

    private String dbName;
    private String dbHost;
    private String citiesCollection;
    private String sourceTweetsCollection;
    private String destinationTweetsCollection;

    private final String FIELD_ID = "geonameid";
    
    public enum CitySearchMode
    {
        ORIGINAL_NAME_ONLY,
        ORIGINAL_PLUS_ASCII,
        ALTERNATE_NAMES_GENEROUS,
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

    public int findCity(double p_longitude, double p_latitude, double p_kilometers)
    {
        DBCollection coll = db.getCollection(citiesCollection);
        BasicDBList myLocation = new BasicDBList();
        myLocation.put(0, p_longitude);
        myLocation.put(1, p_latitude);
        DBObject val = coll.findOne(new BasicDBObject("loc", new BasicDBObject("$near", new BasicDBObject("$geometry", new BasicDBObject(
                "type", "Point").append("coordinates", myLocation)).append("$maxDistance", p_kilometers))));

        if (val != null && val.get(FIELD_ID) != null)
            return (int) val.get(FIELD_ID);
        else
            return -1;
    }

    private DBObject buildQuery(String p_city, CitySearchMode p_mode)
    {
        switch (p_mode)
        {
        case ALTERNATE_NAMES_GENEROUS:
        {
            BasicDBObject query1 = new BasicDBObject("name", p_city);
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

    public int findCity(String p_city, CitySearchMode p_mode)
    {
        try
        {
            DBObject query = buildQuery(p_city, p_mode);

            DBCollection coll = db.getCollection(citiesCollection);
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
