package com.bitcoin.blockchain.api.android.messaging;

import com.bitcoin.blockchain.api.domain.message.ClientMessageBase;

/**
 * Created by Jesion on 2015-03-25.
 */
public class QueuedMessage {

    public String json;

    public ClientMessageBase obj;

    public QueuedMessage(String json, ClientMessageBase obj) {
        this.json = json;
        this.obj = obj;
    }
}
