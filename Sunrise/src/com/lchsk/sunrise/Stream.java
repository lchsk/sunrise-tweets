package com.lchsk.sunrise;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;

public class Stream
{

    public static void run(String consumerKey, String consumerSecret, String token, String secret) throws InterruptedException
    {
        BlockingQueue<String> queue = new LinkedBlockingQueue<String>(10000);
        StatusesFilterEndpoint endpoint = new StatusesFilterEndpoint();
        // add some track terms
        endpoint.trackTerms(Lists.newArrayList("test1", "test2"));

        Authentication auth = new OAuth1(consumerKey, consumerSecret, token, secret);
        // Authentication auth = new BasicAuth(username, password);

        // Create a new BasicClient. By default gzip is enabled.
        Client client = new ClientBuilder().hosts(Constants.STREAM_HOST).endpoint(endpoint).authentication(auth)
                .processor(new StringDelimitedProcessor(queue)).build();

        // Establish a connection
        client.connect();

        // Do whatever needs to be done with messages
        for (int msgRead = 0; msgRead < 1000; msgRead++)
        {
            String msg = queue.take();
            System.out.println("msg:" + msg);
        }

        client.stop();
    }

    public static void main(String[] args)
    {
        String consumerKey = "rHO8ZBkyTjQT6ppkml17y3Lzo";
        String consumerSecret = "Zs4K6EPGDnSw8kwznE6DEkPJd6dYBrdMNzCNpCwYl7WejZ1b2h";
        String token = "929806328-zwrZbmgijZHjHy97FqaY8RIDJ82qcid1vxX53Nm9";
        String secret = "2o6JEsDN7AfrmCW8Ot2an6hIoqyuPoGVmr0GFThp23Tbo";
        try
        {
            Stream.run(consumerKey, consumerSecret, token, secret);
        } catch (InterruptedException e)
        {
            System.out.println(e);
        }
    }
}
