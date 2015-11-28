package com.bitcoin.blockchain.api.android.messaging;

import java.lang.reflect.Method;

/**
 * Created by Jesion on 2015-03-04.
 */
public class MessageListener {

    public String correlationId;
    public String command;
    public String handler;
    public String type;

    public MessageListener() {

    }

    public MessageListener(String correlationId,
                           String command,
                           String handler,
                           String type) {
        this.correlationId = correlationId;
        this.command = command;
        this.handler = handler;
        this.type = type;
    }
}
