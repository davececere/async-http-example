package com.jumptap.examples.asyncio.client;

import java.io.IOException;

import org.eclipse.jetty.client.HttpExchange;

/**
 * Asynchronous request and response from a from the server.
 * 
 * @author rgebhardt
 */
public class AsynchronousClient extends HttpFacade {

    public AsynchronousClient(String url) {
        super(url);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void get() {
        try {
            count++;
            HttpExchange exchange = new HttpExchange() {
                @Override
                protected void onResponseComplete() throws IOException {
                    statusList.add(getStatus());
                }
            };
            exchange.setURL(url);
            client.send(exchange);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}