package com.jumptap.examples.asyncio;

import java.io.IOException;

import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;

public class Main {
    static boolean done = false;
    static int numRequests = 5;
    static int numSeen = 0;
    static long start;
    static HttpClient client;
    static boolean isAsync = true;
    
    private static void setUp() throws Exception{
        //straight from the jetty docs
        //http://wiki.eclipse.org/Jetty/Tutorial/HttpClient
        //we have to use jetty 7 or 8. jetty 9 libs are compiled with jdk 7
        client = new HttpClient();
        client.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
        //just 1 request thread to see if we really are helping the main thread do more work
        client.setMaxConnectionsPerAddress(1); 
        client.start();
    }
    
    /**
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        setUp();
        start = System.nanoTime();
        System.out.println("start="+start);
        for(int i=0; i < numRequests; i++){
            busyWait(); //pretend we're doing work before external request
            ContentExchange exc = sendRequestAsync();
            if(!isAsync) //pretenc we're sync by hanging around until request is over
                exc.waitForDone();
        }
        while(!done){
            Thread.sleep(1000);
        }
    }
    private static void busyWait(){
        //math and stuff
        int j=0;
        for(int i=0;i<1000000;i++)
            j = i*i/2;
        System.out.println(j);
    }
    
    private static ContentExchange sendRequestAsync() throws Exception{
        ContentExchange exchange = new ContentExchange(true)
        {
            @Override
            protected void onResponseComplete() throws IOException
            {
                int status = getResponseStatus();
                //pretend that we have work to do that relies on response
                //so we just pass the length to prove the idea
                if (status == 200)
                    doTheRest(this.getResponseContentBytes().length);
                else
                    error();
            }
        };
        //exchange.setMethod("GET");
        exchange.setURL("http://www.yahoo.com");
        client.send(exchange);
        return exchange;
    }

    //does this synch affect our results? its only hear because we're mutating numSeen
    synchronized private static void doTheRest(int i){
        numSeen++;
        //we consider ourselves done when all requests have been serviced
        if(numSeen == numRequests){
            long end = System.nanoTime();
            System.out.println("finished in "+(end-start) +" nanos");
            done = true;
        }
    }
    private static void error(){
        System.out.println("error");
    }
    
}
