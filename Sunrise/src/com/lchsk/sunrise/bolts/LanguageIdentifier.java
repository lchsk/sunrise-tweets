package com.lchsk.sunrise.bolts;

import java.util.Map;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.lchsk.sunrise.SunriseConfig;
import com.lchsk.sunrise.util.Utils;

/**
 * Purpose of this bolt is to identify language that was
 * used to describe sunrise.
 */
public class LanguageIdentifier extends BaseBasicBolt
{
    private static final Logger log = Logger.getLogger(LanguageIdentifier.class.getName());
    private BasicOutputCollector collector;

    @Override
    public void cleanup()
    {
        
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector p_collector)
    {
        collector = p_collector;
        
        String tweet = input.getValueByField("tweet").toString();
        
        // make sure we work only with JSON data
        if (Utils.isTweetAJSON(tweet))
        {
            JSONObject json;
            try
            {
                json = Utils.getJSON(tweet);
                String text = (String) json.get("text");
                
                // run identification
                String lang = identifyLanguage(text);
                json.put("sunrise_language", lang);
                
                try
                {
                    // emit for further processing
                    collector.emit(new Values(json));
                } catch (Exception e)
                {
                    log.severe("Error emitting tweet.");
                } 
                
            } catch (ParseException e)
            {
                log.warning("Error when parsing a json: " + tweet);
            }
            
            
        }
    }
    
    /**
     * Method used for finding out in which language
     * someone tweeted about sunrise
     * 
     * @param p_text
     * @return
     */
    private String identifyLanguage(String p_text)
    {
        // go over all translations
        for (String lang : SunriseConfig.getInstance().getTranslationsMap().keySet())
        {
            for (String word : SunriseConfig.getInstance().getTranslationsMap().get(lang))
            {
                // and find out if the word is in the tweet
                if (p_text.toLowerCase().contains(word))
                    return lang;
            }
        }
        
        return "";
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
