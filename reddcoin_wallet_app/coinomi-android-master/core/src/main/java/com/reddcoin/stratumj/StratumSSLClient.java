package com.reddcoin.stratumj;

import 	java.lang.Object;

import com.reddcoin.stratumj.messages.BaseMessage;
import com.reddcoin.stratumj.messages.CallMessage;
import com.reddcoin.stratumj.messages.MessageException;
import com.reddcoin.stratumj.messages.ResultMessage;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicLong;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;


/**
 * @author John L. Jegutanis
 */
public class StratumSSLClient extends ClientBase {
    private static final Logger log = LoggerFactory.getLogger(StratumSSLClient.class);
    private final int NUM_OF_WORKERS = 1;

    private AtomicLong idCounter = new AtomicLong();
    private ServerAddress serverAddress;
    private SSLSocket socket;
    private String SSLCertPath;
    @VisibleForTesting DataOutputStream toServer;
    BufferedReader fromServer;

    private CATrustManager trustMangaer;

    final private ExecutorService pool = Executors.newFixedThreadPool(NUM_OF_WORKERS);

    final private ConcurrentHashMap<Long, SettableFuture<ResultMessage>> callers =
            new ConcurrentHashMap<Long, SettableFuture<ResultMessage>>();

    final private ConcurrentHashMap<String, List<SubscribeResult>> subscribes =
            new ConcurrentHashMap<String, List<SubscribeResult>>();

    final private BlockingQueue<BaseMessage> queue = new LinkedBlockingDeque<BaseMessage>();

    // public interface SubscribeResult {
    //     public void handle(CallMessage message);
    // }

    private class MessageHandler implements Runnable {
        @Override
        public void run() {
            while (!pool.isShutdown()) {
                BaseMessage message = null;
                try {
                    message = queue.take();
                } catch (InterruptedException ignored) {
                    
                }

                if (message != null) {
                    handle(message);
                }
            }
            log.debug("Shutdown message handler thread: " + Thread.currentThread().getName());
        }

        private void handle(BaseMessage message) {
            if (message instanceof ResultMessage) {
                ResultMessage reply = (ResultMessage) message;
                if (callers.containsKey(reply.getId())) {
                    SettableFuture<ResultMessage> future = callers.get(reply.getId());
                    future.set(reply);
                    callers.remove(reply.getId());
                } else {
                    log.error("Received reply from server, but could not find caller",
                            new MessageException("Orphaned reply", reply.toString()));
                }
            } else if (message instanceof CallMessage) {
                CallMessage reply = (CallMessage) message;
                if (subscribes.containsKey(reply.getMethod())) {
                    List<SubscribeResult> subs;

                    synchronized (subscribes.get(reply.getMethod())) {
                        // Make a defensive copy
                        subs = ImmutableList.copyOf(subscribes.get(reply.getMethod()));
                    }

                    for (SubscribeResult handler : subs) {
                        try {
                            log.debug("Running subscriber handler with result: " + reply);
                            handler.handle(reply);
                        } catch (RuntimeException e) {
                            log.error("Error while executing subscriber handler", e);
                        }
                    }
                } else {
                    log.error("Received call from server, but not could find subscriber",
                            new MessageException("Orphaned call", reply.toString()));
                }

            } else {
                log.error("Unable to handle message",
                        new MessageException("Unhandled message", message.toString()));
            }
        }
    }

    private class CATrustManager{
        String certFile;
        SSLContext context;

        CATrustManager(String file){
            certFile = file;
            readCert();
        }

        private void readCert(){
            try {
                // get file in the Internal Storage
                FileInputStream fis = new FileInputStream(SSLCertPath);

                // Load CAs from an InputStream
                // (could be from a resource or ByteArrayInputStream or ...)
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                // From https://www.washington.edu/itconnect/security/ca/load-der.crt
                InputStream caInput = new BufferedInputStream(fis);
                Certificate ca = null;
                try {
                    ca = cf.generateCertificate(caInput);
                    System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
                } catch (Exception ex) {
                } finally {
                    caInput.close();
                }

                // Create a KeyStore containing our trusted CAs
                String keyStoreType = KeyStore.getDefaultType();
                KeyStore keyStore = KeyStore.getInstance(keyStoreType);
                keyStore.load(null, null);
                keyStore.setCertificateEntry("ca", ca);

                // Create a TrustManager that trusts the CAs in our KeyStore
                String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
                tmf.init(keyStore);

                // Create an SSLContext that uses our TrustManager
                context = SSLContext.getInstance("TLS");
                context.init(null, tmf.getTrustManagers(), null);
            }catch (Exception ex){ex.printStackTrace();log.error(ex.toString());}
        }

        public SSLSocketFactory getSocketFactory(){
            return context.getSocketFactory();
        }
    }

    public StratumSSLClient(ServerAddress address, String certPath) {
        serverAddress = address;
        SSLCertPath = certPath;
    }

    public StratumSSLClient(String host, int port, String certPath) {
        serverAddress = new ServerAddress(host, port);
        log.debug("StratumSSLClient at " + host + ":" + port);
        SSLCertPath = certPath;
    }

    public long getCurrentId() {
        return idCounter.get();
    }

    protected SSLSocket createSocket() throws IOException {
        ServerAddress address = serverAddress;

        SSLSocketFactory factory= trustMangaer.getSocketFactory();
        SSLSocket sslsocket=(SSLSocket) factory.createSocket(address.getHost(),address.getPort());

        log.debug("Opening a SSLsocket to " + address.getHost() + ":" + address.getPort());

        return sslsocket;
    }

    @Override
    protected void startUp() {
        trustMangaer = new CATrustManager("bla");

        for (int i = 0; i < NUM_OF_WORKERS; i++) {
            pool.submit(new MessageHandler());
        }
        try {
            socket = createSocket();
            log.debug("Creating I/O streams to socket: {}", socket);
            toServer = new DataOutputStream(socket.getOutputStream());
            fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (Exception e) {
            log.info("Unable to create socket for {}", serverAddress);
            triggerShutdown();
        }
    }

    @Override
    protected void triggerShutdown() {
        super.triggerShutdown();
        try {
            log.info("Shutting down {}", serverAddress);
            if (isConnected()) socket.close();
        } catch (IOException e) {
            log.error("Unable to close socket", e);
        }
        pool.shutdown();
    }

    @Override
    protected void run() {
        log.debug("Started listening for server replies");

        String serverMessage;
        while (isRunning() && isConnected()) {
            try {
                serverMessage = fromServer.readLine();
            } catch (IOException e) {
                log.info("Error communicating with server: {}", e.getMessage());
                triggerShutdown();
                break;
            }

            if(serverMessage == null) {
                log.info("Server closed communications. Shutting down");
                triggerShutdown();
                break;
            }

            log.debug("Received message from server: " + serverMessage);

            BaseMessage reply;
            try {
                reply = BaseMessage.fromJson(serverMessage);
            } catch (JSONException e) {
                log.error("Server sent malformed data", e);
                continue;
            }

            if (reply.errorOccured()) {
                Exception e = new MessageException(reply.getError(), reply.getFailedRequest());
                log.error("Failed call", e);
                // TODO set exception to the correct future object
               // if (callers.containsKey()) {
               //     SettableFuture<ResultMessage> future = callers.get();
               //     future.setException(e);
               // } else {
               //     log.error("Failed orphaned call", e);
               // }
            } else {
                boolean added = false;

                try {
                    if (reply.isResult()) {
                        reply = ResultMessage.fromJson(serverMessage);
                    } else if (reply.isCall()) {
                        reply = CallMessage.fromJson(serverMessage);
                    }
                } catch (JSONException e) {
                    // Should not happen as we already checked this exception
                    throw new RuntimeException(e);
                }

                while (!added) {
                    try {
                        queue.put(reply);
                        added = true;
                    } catch (InterruptedException e) {
                        log.debug("Interrupted while adding server reply to queue. Retrying...");
                    }
                }

            }
        }
        log.debug("Finished listening for server replies");
    }

    @Override
    public boolean isConnected() {
        return socket != null && socket.isConnected() && isRunning();
    }

    @Override
    public void disconnect() {
        if (isConnected()) {
            try {
                socket.close();
            } catch (IOException ignore) {
                
            }
        }
    }

    @Override
    public ListenableFuture<ResultMessage> call(CallMessage message) {
        SettableFuture<ResultMessage> future = SettableFuture.create();

        message.setId(idCounter.getAndIncrement());

        try {
            toServer.writeBytes(message.toString());
            callers.put(message.getId(), future);
        } catch (Exception e) {
            future.setException(e);
            log.error("Error making a call to the server: {}", e.getMessage());
            triggerShutdown();
        }

        return future;
    }

    @Override
    public ListenableFuture<ResultMessage> subscribe(CallMessage message, SubscribeResult handler) {
        ListenableFuture<ResultMessage> future = call(message);

        if (!subscribes.containsKey(message.getMethod())) {
            List<SubscribeResult> handlers =
                    Collections.synchronizedList(new ArrayList<SubscribeResult>());
            handlers.add(handler);
            subscribes.put(message.getMethod(), handlers);
        } else {
            // Add handler if needed
            if (!subscribes.get(message.getMethod()).contains(handler)) {
                subscribes.get(message.getMethod()).add(handler);
            }
        }

        return future;
    }
}
