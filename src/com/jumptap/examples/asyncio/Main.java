package com.jumptap.examples.asyncio;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Hex;
import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class Main {
    /** How much reps does the busy wait do? */
    private static final int WORK_FACTOR = 10000;

    /**
     * How many times do we run the tests before running tests where we look at
     * the performance. Basically, how long before all the code is no longer
     * getting affected by the JIT.
     */
    private static final int WARMUP_FACTOR = 1000;

    /**
     * How many times do we run the tests.
     */
    private static final int RUN_FACTOR = 1000;

    /**
     * Delay that each server request gets delayed.
     */
    private static final long SERVER_DELAY = 200;

    static boolean done = false;
    static int numRequests = 0;
    static int numSeen = 0;
    static long start;
    static double elapsed = 0.0;
    static HttpClient client;
    static Server server;
    static boolean isAsync = false;
    static BusyWaiter waiter = BusyWaiter.HASH;

    /**
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        setUp();
        warmUp();
        run(RUN_FACTOR, false);
        String firstResults = getStats();
        run(RUN_FACTOR, true);
        String lastResults = getStats();
        System.out.println();
        System.out.println(firstResults);
        System.out.println(lastResults);
        server.stop();
        server.join();
    }

    private static void setUp() throws Exception{
        client = new HttpClient();
        client.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
        client.setMaxConnectionsPerAddress(1); 
        client.start();

        server = new Server(8080);
        server.setHandler(new AbstractHandler() {
            @Override
            public void handle(String target, Request baseRequest,
                    HttpServletRequest request, HttpServletResponse response)
                    throws IOException, ServletException {
                try {
                    Thread.sleep(SERVER_DELAY);
                } catch (Exception e) {
                    response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                    return;
                }
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().append("Great!");
                response.flushBuffer();
            }
        });
        server.start();
    }

    /**
     * Do dry-runs to make sure all classes are loaded and JIT optimizations
     * have been performed.
     */
    public static void warmUp() {
        run(WARMUP_FACTOR, false);
        run(WARMUP_FACTOR, true);
    }

    /**
     * Run through the the async or sync test a number of reps.
     * @param reps The number of reps.
     * @param async True if the test should be asynchronous.
     */
    public static void run(int reps, boolean async) {
        done = false;
        numRequests = reps;
        numSeen = 0;
        isAsync = async;
        start = System.nanoTime();
        MessageDigest sha1 = sha1();
        try {
            for(int i = 0; i < reps; i++){
                // Vary the amount of iterations slightly to not drastically
                // affect benchmark, this could help work around some JIT optimizations.
                String worthIt = waiter.busyWait(WORK_FACTOR + reps % 4);
                sha1.update(worthIt.getBytes());
                ContentExchange exc = sendRequestAsync();
                if(!async)
                    exc.waitForDone();
            }
            while(!done){
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // Required so busy waits aren't eliminated in JIT.
        System.out.println(waiter + " hash " + Hex.encodeHexString(sha1.digest()));
        System.gc();
    }

    private static String getStats() {
        return String.format("Finished async %s benchmark in %.4f s", isAsync, elapsed);
    }

    private static MessageDigest sha1() {
        try {
            return MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } 
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
        exchange.setURL("http://localhost:8080/");
        client.send(exchange);
        return exchange;
    }

    //does this synch affect our results? its only hear because we're mutating numSeen
    synchronized private static void doTheRest(int i){
        numSeen++;
        //we consider ourselves done when all requests have been serviced
        if(numSeen == numRequests){
            long end = System.nanoTime();
            elapsed = (end-start) / 1000000000.0;
            done = true;
        }
    }
    private static void error(){
        System.out.println("error");
    }

    private static enum BusyWaiter {
        ARITHMETIC_LOOP, HASH;
        
        public String busyWait(int reps) {
            switch (this) {
            case ARITHMETIC_LOOP:
                return loopWait(reps);
            case HASH:
                return hashWait(reps);
            default:
                return "";
            }
        }

        /**
         * Modifications to original loop and arithmetic CPU burn to avoid JIT
         * optimizations. Hopefully.
         * 
         * @param max
         *            Max reps to perform.
         * @return Returned so the method isn't optimized away.
         */
        private static String loopWait(int max) {
            int j = 0;
            for(int i = 0; i < max; i++) {
                j += i + i % 2;
            }

            return String.valueOf(j);
        }

        /**
         * A hash based approach to cpu burn.
         * 
         * @param max
         *            Max reps to perform.
         * @return Return value so the method isn't optimized away.
         */
        private static String hashWait(int max) {
            MessageDigest sha1 = sha1();
            MessageDigest reduction = sha1();

            for (int i = 0; i < max; i++) {
                sha1.reset();
                String unhashedString = i + "format" + i;
                byte[] unhashedBytes = unhashedString.getBytes();
                byte[] hashedBytes = sha1.digest(unhashedBytes);
                reduction.update(hashedBytes);
            }

            byte[] hashedBytes = reduction.digest();
            return Hex.encodeHexString(hashedBytes);
        }

    }
}