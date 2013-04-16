package com.jumptap.examples.asyncio;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 * An embedded jetty server that answers any request by waiting a configurable
 * amount and then responding with success.
 * 
 * @author rgebhardt
 */
public class FakeServer {
    private Server server;

    /**
     * Create a new fake server with the
     * 
     * @param wait
     */
    public FakeServer(final long wait) {
        server = new Server(0);
        server.setHandler(new AbstractHandler() {
            @Override
            public void handle(String target, Request baseRequest,
                    HttpServletRequest request, HttpServletResponse response)
                    throws IOException, ServletException {
                try {
                    Thread.sleep(wait);
                } catch (Exception e) {
                    response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                    return;
                }
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().append("Great!");
                response.flushBuffer();
            }
        });
        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException("The test is broken.", e);
        }

        // A little simplistic and incorrect.
        while (!server.isStarted()) {
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * Get the address to access this server instance by.
     * 
     * @return The URL of the fake server.
     */
    public String getAddress() {
        return "http://localhost:" + server.getConnectors()[0].getLocalPort() + "/";
    }
}