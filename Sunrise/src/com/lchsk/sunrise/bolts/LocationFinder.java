package com.lchsk.sunrise.bolts;

import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.lchsk.sunrise.SunriseConfig;
import com.lchsk.sunrise.db.DBConn;
import com.lchsk.sunrise.db.DBConn.CitySearchMode;
import com.lchsk.sunrise.util.SearchTerms;
import com.lchsk.sunrise.util.Utils;
import com.mongodb.BasicDBList;
import com.mongodb.DBObject;

public class LocationFinder extends BaseBasicBolt
{
    private static final Logger log = Logger.getLogger(LocationFinder.class.getName());
    private BasicOutputCollector collector;

    /**
     * Methods used for finding location of the tweet
     */
    // best option: tweet has coordinates attached
    private final int GEO_COORDINATES = 1;

    // name of some geographic location
    // found in a tweet's content
    private final int GEO_TWEET_CONTENT = 2;

    // location copied from user's profile
    private final int GEO_USER_PROFILE = 3;
    
    private final int GEO_IMAGE_COORDINATES = 4;

    // regardless of effort
    // no location was found
    private final int GEO_NO_LOCATION = 0;

    // minimum length of the search term
    // (for geographic locations)
    private final int SEARCH_TERM_MIN_LENGTH = 3;

    // maximum distance (in meters)
    // for searching closest largest city
    private final int MAX_DISTANCE = 30000;

    @Override
    public void cleanup()
    {

    }

    @Override
    public void execute(Tuple input, BasicOutputCollector p_collector)
    {
        collector = p_collector;
        JSONObject json = (JSONObject) input.getValueByField("tweet");

        try
        {
            if (!getCoordinates(json))
            {
                // left for reference
                //if (!getImageCoordinates(json))
                {
                    if (!lookForLocation(json))
                    {
                        if (!getProfileLocation(json))
                        {
                            saveNoLocation(json);
                        }
                    }
                }
            }

            collector.emit(new Values(json));
        }
        catch (UnknownHostException e)
        {
            log.severe("Problem connecting to the database...");
        }
        catch (Exception e)
        {
            log.severe("Exception: " + e.getMessage());
        }
    }

    private void saveNoLocation(JSONObject p_json)
    {
        p_json.put("sunrise_geo_type", GEO_NO_LOCATION);
    }

    private boolean getCoordinates(JSONObject p_json) throws UnknownHostException
    {
        if (Utils.notEmpty(p_json.get("coordinates")))
        {
            p_json.put("sunrise_geo_type", GEO_COORDINATES);
            
            JSONObject point = (JSONObject) p_json.get("coordinates");
            
            if (point != null)
            {
                JSONArray coords = (JSONArray) point.get("coordinates");
                int id = DBConn.getInstance().findCity((double) coords.get(0), (double) coords.get(1), MAX_DISTANCE);
                addCity(id, p_json, "closest_city");
            }

            if (Utils.notEmpty(p_json.get("place")))
            {
                JSONObject o = (JSONObject) p_json.get("place");
                p_json.put("country_full", (String) Utils.getCountry((String) o.get("country_code")));
                String city = (String) o.get("name");
            }

            return true;
        }

        return false;
    }

    private void addCity(int p_id, JSONObject p_json, String p_key) throws UnknownHostException
    {
        if (p_id > -1)
        {
            DBObject d = DBConn.getInstance().getRow(p_id);

            p_json.put(p_key, fixId(d));
        }
    }

    private boolean getImageCoordinates(JSONObject p_json) throws UnknownHostException
    {
        if (p_json.containsKey("entities"))
        {
            JSONObject entities = (JSONObject) p_json.get("entities");

            if (entities.containsKey("media"))
            {
                JSONArray media = (JSONArray) entities.get("media");

                if (media.size() > 0)
                {
                    JSONObject image = (JSONObject) media.get(0);

                    if (image.containsKey("media_url"))
                    {
                        String url = (String) image.get("media_url");
                        Double[] coords = Utils.readImageCoordinates(url);

                        if (coords != null)
                        {
                            int id = DBConn.getInstance().findCity(coords[0], coords[1], MAX_DISTANCE);

                            JSONArray tmp = new JSONArray();
                            tmp.add(coords[0]);
                            tmp.add(coords[1]);

                            p_json.put("image_coordinates", coords);
                            p_json.put("sunrise_geo_type", GEO_IMAGE_COORDINATES);
                            
                            addCity(id, p_json, "closest_city");

                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private boolean getProfileLocation(JSONObject p_json) throws UnknownHostException
    {
        if (p_json.containsKey("user"))
        {
            JSONObject user = (JSONObject) p_json.get("user");

            if (user.containsKey("location"))
            {
                String location = (String) user.get("location");

                if (location.length() > 0)
                {
                    location = Utils.extractFirstPart(location);
                    int id = DBConn.getInstance().findCity(location, CitySearchMode.ORIGINAL_PLUS_ASCII);
                    if (id > -1)
                    {
                        DBObject o = DBConn.getInstance().getRow(id);
                        o.put("country_full", (String) Utils.getCountry((String) o.get("country code")));
                        p_json.put("country_full", (String) Utils.getCountry((String) o.get("country code")));
                        p_json.put("sunrise_geo_type", GEO_USER_PROFILE);
                        p_json.put("sunrise_geo_identified", fixId(o));
                    }

                    return true;
                }
            }
        }

        return false;
    }

    private DBObject fixId(DBObject o)
    {
        //        String tmp = (String) o.get("_id");
        //        tmp = "x" + tmp;
        o.put("_id", "123");
        return o;
    }

    private boolean lookForLocation(JSONObject p_json) throws UnknownHostException
    {
        String text = (String) p_json.get("text");

        if (text != null)
        {
            DBObject o = search(text, 2);

            if (o != null)
            {
                p_json.put("sunrise_geo_type", GEO_TWEET_CONTENT);
                o.put("country_full", (String) Utils.getCountry((String) o.get("country code")));
                p_json.put("country_full", (String) Utils.getCountry((String) o.get("country code")));
                p_json.put("sunrise_geo_identified", fixId(o));

                return true;
            }
            else
            {
                o = search(text, 1);

                if (o != null)
                {
                    p_json.put("sunrise_geo_type", GEO_TWEET_CONTENT);
                    o.put("country_full", (String) Utils.getCountry((String) o.get("country code")));
                    p_json.put("country_full", (String) Utils.getCountry((String) o.get("country code")));
                    p_json.put("sunrise_geo_identified", fixId(o));

                    return true;
                }
            }
        }

        return false;
    }

    private DBObject search(String p_text, int p_words) throws UnknownHostException
    {
        String[] words = p_text.split("\\s+");

        SearchTerms st = new SearchTerms(words, p_words);

        String t;
        while ((t = st.next()) != null)
        {
            t = removeKeywords(t);

            String term = prepareSearchTerms(t);

            if (term.length() < SEARCH_TERM_MIN_LENGTH)
                continue;

            int id = DBConn.getInstance().findCity(term, CitySearchMode.ALTERNATE_NAMES_FULL);
            if (id > -1)
            {
                DBObject o = DBConn.getInstance().getRow(id);
                return o;
            }
        }

        return null;
    }

    /**
     * Removes word 'sunrise' and its translations from the string
     * 
     * @param p_str
     * @return
     */
    private String removeKeywords(String str)
    {
        for (String lang : SunriseConfig.getInstance().getTranslationsMap().keySet())
        {
            for (String word : SunriseConfig.getInstance().getTranslationsMap().get(lang))
            {
                // case insensitive replacing 
                str = str.replaceAll("(?i)" + word, "");
            }
        }

        return str;
    }

    private String prepareSearchTerms(String p_str)
    {
        return Utils.firstLettersUpperCase(Utils.removeNonAlphanumeric(p_str.toLowerCase()));
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context)
    {
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer)
    {
        declarer.declare(new Fields("tweet"));
    }
}
