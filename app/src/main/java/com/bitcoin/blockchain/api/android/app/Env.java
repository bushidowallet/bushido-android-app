package com.bitcoin.blockchain.api.android.app;

/**
 * Created by Jesion on 2015-06-09.
 */
public class Env {

    public static String PROD = "Prod";
    public static String DEV = "Dev";
    public String env;
    public boolean useSSL;
    public String restHost;
    public int restPort;
    public String socketHost;
    public int socketPort;

    public Env() {

    }

    public Env(String env,
               boolean useSSL,
               String restHost,
               int restPort,
               String socketHost,
               int socketPort) {
        this.env = env;
        this.useSSL = useSSL;
        this.restHost = restHost;
        this.restPort = restPort;
        this.socketHost = socketHost;
        this.socketPort = socketPort;
    }

    public String getHTTP() {
        if (useSSL) {
            return "https://";
        } else {
            return "http://";
        }
    }
}
