package com.jumptap.examples.asyncio.client;

import java.io.IOException;

import org.eclipse.jetty.client.HttpExchange;

/**
 * Synchronous request and response from the server.
 * 
 * @author rgebhardt
 */
public class SynchronousClient extends HttpFacade {

    public SynchronousClient(String url) {
        super(url);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void get() {
        try {
            count++;
            HttpExchange exchange = new HttpExchange();
            exchange.setURL(url);
            client.send(exchange);
            statusList.add(exchange.waitForDone());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}