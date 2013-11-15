 /* Copyright (C)2013 Pantheon Technologies, s.r.o. All rights reserved. */
package org.opendaylight.openflowjava.protocol.impl.clients;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;

import java.net.InetAddress;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.SettableFuture;

/**
 * Simple client for testing purposes
 *
 * @author michal.polkorab
 */
public class SimpleClient extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleClient.class);
    private final String host;
    private final int port;
    private boolean securedClient = false;
    private EventLoopGroup group;
    private SettableFuture<Boolean> isOnlineFuture;
    private SettableFuture<Boolean> scenarioDone;
    private SimpleClientInitializer clientInitializer;
    private ScenarioHandler scenarioHandler;
    
    /**
     * Constructor of class
     *
     * @param host address of host
     * @param port host listening port
     */
    public SimpleClient(String host, int port) {
        this.host = host;
        this.port = port;
        init();
    }

    private void init() {
        isOnlineFuture = SettableFuture.create();
        scenarioDone = SettableFuture.create();
    }
    
    /**
     * Starting class of {@link SimpleClient}
     */
    @Override
    public void run() {
        group = new NioEventLoopGroup();
        clientInitializer = new SimpleClientInitializer(isOnlineFuture, securedClient);
        clientInitializer.setScenario(scenarioHandler);
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                .channel(NioSocketChannel.class)
                .handler(clientInitializer);

            b.connect(host, port).sync();

            synchronized (scenarioHandler) {
                LOGGER.debug("WAITING FOR SCENARIO");
                scenarioHandler.wait();
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        } finally {
            LOGGER.info("shutting down");
            try {
                group.shutdownGracefully().get();
                LOGGER.info("shutdown succesful");
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        scenarioDone.set(true);
    }

    /**
     * @return close future
     */
    public Future<?> disconnect() {
        LOGGER.debug("disconnecting client");
        return group.shutdownGracefully();
    }

    /**
     * @param securedClient
     */
    public void setSecuredClient(boolean securedClient) {
        this.securedClient = securedClient;
    }

    /**
     * Sets up {@link SimpleClient} and fires run()
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String host;
        int port;
        SimpleClient sc;
        if (args.length != 3) {
            LOGGER.error("Usage: " + SimpleClient.class.getSimpleName()
                    + " <host> <port> <secured>");
            LOGGER.error("Trying to use default setting.");
            InetAddress ia = InetAddress.getLocalHost();
            InetAddress[] all = InetAddress.getAllByName(ia.getHostName());
            host = all[0].getHostAddress();
            port = 6633;
            sc = new SimpleClient(host, port);
            sc.setSecuredClient(true);
        } else {
            host = args[0];
            port = Integer.parseInt(args[1]);
            sc = new SimpleClient(host, port);
            sc.setSecuredClient(Boolean.parseBoolean(args[2]));
        }
        sc.start();
        
    }
    
    /**
     * @return the isOnlineFuture
     */
    public SettableFuture<Boolean> getIsOnlineFuture() {
        return isOnlineFuture;
    }
    
    /**
     * @return the scenarioDone
     */
    public SettableFuture<Boolean> getScenarioDone() {
        return scenarioDone;
    }
    
    /**
     * @param scenario list of wanted actions
     */
    public void setScenarioHandler(ScenarioHandler scenario) {
        this.scenarioHandler = scenario;
    }
}