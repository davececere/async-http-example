package com.jumptap.examples.asyncio.client;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jetty.client.HttpClient;

/**
 * Abstracts http client and all exchanges into what we need to do for our
 * benchmarks.
 * 
 * @author rgebhardt
 */
public abstract class HttpFacade {

    protected HttpClient client = new HttpClient();
    protected String url;
    protected int count = 0;
    protected List<Integer> statusList = new CopyOnWriteArrayList<Integer>();

    public HttpFacade(String url) {
        this.url = url;
        try {
            client.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
            client.setMaxConnectionsPerAddress(1);
            client.start();

            while (!client.isStarted()) {
                Thread.sleep(100L);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Make a new http exchange.
     */
    public abstract void get();

    /**
     * Check if all of the http exchanges are done.
     * 
     * @return True if all of the http exchanges are done.
     */
    public boolean isDone() {
        return count == statusList.size();
    }
}