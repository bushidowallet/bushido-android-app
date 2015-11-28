package com.bitcoin.blockchain.api.android.messaging;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.bitcoin.blockchain.api.Command;
import com.bitcoin.blockchain.api.domain.message.BalanceChangeMessage;
import com.bitcoin.blockchain.api.domain.message.ClientMessage;
import com.bitcoin.blockchain.api.domain.message.ClientMessageBase;
import com.bitcoin.blockchain.api.domain.message.GetAddressMessage;
import com.bitcoin.blockchain.api.domain.message.SpendMessage;
import com.bitcoin.blockchain.api.domain.message.TransactionStatusMessage;
import com.bitcoin.blockchain.api.domain.message.UnspentOutputsMessage;
import com.bitcoin.blockchain.api.domain.message.WalletChangeMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by Jesion on 2015-03-04.
 */
public class WalletAPI {

    private List<MessageListener> observers;
    private Map<String,Class> serializationMap;
    private Map<String,Class> rpcRequestSerializationMap;
    private Map<String,Class> rpcResponseSerializationMap;
    private String serviceHost;
    private int servicePort;
    private String serviceVirtualHost;
    private String serviceUser;
    private String servicePassword;
    private String username;
    private String password;
    private String walletId;
    private String connectCommand;
    private Object connectPayload;
    private String connectHandler;
    private String subscribeDestination;
    private String sendDestination;
    private Object receiver;

    public WalletAPI(String serviceHost,
                     int servicePort,
                     String username,
                     String password,
                     String walletId,
                     Object receiver) {
        this(serviceHost,
            servicePort,
            "/",
            "bushido",
            "bushido",
            username,
            password,
            walletId,
            true,
            null,
            null,
            null,
            "v2e-wallet-updates",
            "v2wallet",
            receiver
        );
    }

    /**
     * WalletAPI - an interface to wallet notifications
     *
     * @param serviceHost - RabbitMQ server IP ( 10.0.2.2 )
     * @param servicePort - RabbitMQ server port ( 5672 )
     * @param serviceVirtualHost - Virtual Host ( / )
     * @param serviceUser - RabbitMQ user ( guest )
     * @param servicePassword - RabbitMQ password ( guest )
     * @param username - Wallet user
     * @param password - Wallet user password
     * @param walletId - Wallet Id you are interacting with
     * @param autoConnect - Tells whether to connect automatically
     * @param connectCommand - Command to execute immediately after connection is established
     * @param connectPayload - Payload that has to be carried with connect command
     * @param connectHandler - Handler that has to be notified when connection command's response is back
     * @param subscribeDestination - Desitnation for wallet server to client messaging. It is an exchange name ( v2e-wallet-updates )
     * @param sendDestination - Destination for client to server messaging. It is a queue name ( v2wallet )
     * @param receiver - Object with handlers
     */
    public WalletAPI(String serviceHost,
                     int servicePort,
                     String serviceVirtualHost,
                     String serviceUser,
                     String servicePassword,
                     String username,
                     String password,
                     String walletId,
                     boolean autoConnect,
                     String connectCommand,
                     Object connectPayload,
                     String connectHandler,
                     String subscribeDestination,
                     String sendDestination,
                     Object receiver) {

        serializationMap = new HashMap<String, Class>();
        serializationMap.put(Command.FC_BALANCE_CHANGE_RECEIVED, BalanceChangeMessage.class);
        serializationMap.put(Command.BALANCE_CHANGE_RECEIVED, WalletChangeMessage.class);
        serializationMap.put(Command.BALANCE_CHANGE_SPENT, WalletChangeMessage.class);
        serializationMap.put(Command.BALANCE_CHANGE_STATUS, BalanceChangeMessage.class);
        serializationMap.put(Command.TRANSACTION_STATUS_CHANGE, TransactionStatusMessage.class);
        serializationMap.put(Command.GET_UNSPENT_OUTPUTS, UnspentOutputsMessage.class);
        serializationMap.put(Command.HEARTBEAT, ClientMessage.class);
        serializationMap.put(Command.SPEND_ALL_UTXO, ClientMessage.class);
        serializationMap.put(Command.SPEND, SpendMessage.class);
        rpcRequestSerializationMap = new HashMap<String, Class>();
        rpcRequestSerializationMap.put(Command.GET_ADDRESS, ClientMessage.class);
        rpcResponseSerializationMap = new HashMap<String, Class>();
        rpcResponseSerializationMap.put(Command.GET_ADDRESS, GetAddressMessage.class);

        this.observers = new ArrayList<MessageListener>();
        this.serviceHost = serviceHost;
        this.servicePort = servicePort;
        this.serviceVirtualHost = serviceVirtualHost;
        this.serviceUser = serviceUser;
        this.servicePassword = servicePassword;
        this.username = username;
        this.password = password;
        this.walletId = walletId;
        this.connectCommand = connectCommand;
        this.connectPayload = connectPayload;
        this.connectHandler = connectHandler;
        this.subscribeDestination = subscribeDestination;
        this.sendDestination = sendDestination;
        this.receiver = receiver;
        if (autoConnect == true) {
            try {
                connect();
            } catch (Exception e) {

            }
        }
    }

    public void connect() throws Exception {
       setup();
    }

    public void addListener(String command, String handler) {
        this.observers.add(new MessageListener(null, command, handler, MessageListenerType.NOTIFICATION));
    }

    public void removeListener(String command, String handler) {
        int index = -1;
        for (int i = 0; i < observers.size(); i++) {
            MessageListener observer = observers.get(i);
            if (observer.command.equals(command) && observer.handler.equals(handler) && observer.type.equals(MessageListenerType.NOTIFICATION)) {
                index = i;
            }
        }
        if (index > -1) {
            this.observers.remove(index);
        }
    }

    public void invoke(String command, Object payload, String responseHandler) {
        try {
            final String correlationId = UUID.randomUUID().toString();
            Class messageClass;
            if (isRPC(command) == false) {
                messageClass = serializationMap.get(command);
            } else {
                messageClass = rpcRequestSerializationMap.get(command);
            }
            final ClientMessageBase message = (ClientMessageBase) messageClass.newInstance();
            message.setCommand(command);
            message.setRawPayload(payload);
            message.setUsername(this.username);
            message.setPassword(this.password);
            message.setKey(this.walletId);
            message.setCorrelationId(correlationId);
            if (responseHandler != null) {
                observers.add(new MessageListener(correlationId, command, responseHandler, MessageListenerType.RPC));
            }
            publish(message);
        } catch (Exception e) {
            Log.e("", e.toString());
        }
    }

    private ConnectionFactory factory = new ConnectionFactory();
    private void setupConnectionFactory() {
        try {
            factory.setAutomaticRecoveryEnabled(false);
            factory.setUsername(serviceUser);
            factory.setPassword(servicePassword);
            factory.setHost(serviceHost);
            factory.setPort(servicePort);
            factory.setVirtualHost(serviceVirtualHost);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Object serialize(String command, String messageJson) {
        Class c = null;
        if (isRPC(command) == true) {
            c = rpcResponseSerializationMap.get(command);
        } else {
            c = serializationMap.get(command);
        }
        Object message = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            message = mapper.readValue(messageJson, c);
        } catch (Exception e) {
            Log.e("", "Error when creating a message instance from json string: " + e.getMessage());
        }
        return message;
    }

    private boolean isRPC(String command) {
        if (command.equals(Command.GET_ADDRESS) == true) {
            return true;
        }
        return false;
    }

    private Handler incomingMessageHandler;
    private void setup() throws Exception {
        setupConnectionFactory();
        incomingMessageHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String messageJson = msg.getData().getString("msg");
                try {
                    JSONObject payload = new JSONObject(messageJson);
                    String command = payload.getString("command");
                    Class payloadClass = null;
                    if (isRPC(command) == true) {
                        payloadClass = rpcResponseSerializationMap.get(command);
                    } else {
                        payloadClass = serializationMap.get(command);
                    }
                    Method handler;
                    int index = -1;
                    if (observers != null) {
                        for (int i = 0; i < observers.size(); i++) {
                            MessageListener observer = observers.get(i);
                            if (observer.type.equals(MessageListenerType.NOTIFICATION)) {
                                if (observer.command.equals(command)) {
                                    handler = receiver.getClass().getMethod(observer.handler, payloadClass);
                                    handler.invoke(receiver, serialize(command, messageJson));
                                }
                            } else if (observer.type.equals(MessageListenerType.RPC)) {
                                if (observer.correlationId.equals(payload.getString("correlationId"))) {
                                    index = i;
                                    handler = receiver.getClass().getMethod(observer.handler, payloadClass);
                                    handler.invoke(receiver, serialize(command, messageJson));
                                }
                            }
                        }
                        if (index > -1) {
                            observers.remove(index);
                        }
                    }
                } catch (Exception e) {
                    Log.e("", "Error when handling message: " + e.getMessage());
                }
            }
        };
        subscribe(incomingMessageHandler);
        runPublishThread();
        if (connectCommand != null) {
            invoke(connectCommand, connectPayload, connectHandler);
        }
    }

    private BlockingDeque queue = new LinkedBlockingDeque();
    private void publish(ClientMessageBase m) {
        ObjectMapper mapper = new ObjectMapper();
        String jsonMessage = "";
        try {
            jsonMessage = mapper.writeValueAsString(m);
        } catch (Exception e) {
            Log.e("", "Failed to convert to json");
        }
        try {
            Log.d("","[q] " + jsonMessage);
            queue.putLast(new QueuedMessage(jsonMessage, m));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Thread publishThread;
    private void runPublishThread()
    {
        publishThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        Connection connection = factory.newConnection();
                        Channel channel = connection.createChannel();
                        channel.confirmSelect();
                        channel.exchangeDeclare(sendDestination, "direct", true);
                        String queueName = sendDestination;
                        channel.queueBind(queueName, sendDestination, walletId);
                        while (true) {
                            QueuedMessage message = (QueuedMessage) queue.takeFirst();
                            try{
                                Map<String, Object> headers = new HashMap<String, Object>();
                                Class messageClass = null;
                                if (isRPC(message.obj.getCommand()) == true) {
                                    messageClass = rpcRequestSerializationMap.get(message.obj.getCommand());
                                } else {
                                    messageClass = serializationMap.get(message.obj.getCommand());
                                }
                                headers.put("__TypeId__", messageClass.getName());
                                AMQP.BasicProperties p = new AMQP.BasicProperties.Builder()
                                        .contentType("application/json")
                                        .headers(headers)
                                        .build();
                                channel.basicPublish(sendDestination, walletId, p, message.json.getBytes());
                                Log.d("", "[s] " + message);
                                channel.waitForConfirmsOrDie();
                            } catch (Exception e){
                                Log.d("","[f] " + message);
                                queue.putFirst(message);
                                throw e;
                            }
                        }
                    } catch (InterruptedException e) {
                        break;
                    } catch (Exception e) {
                        Log.d("", "Connection broken: " + e.getClass().getName());
                        try {
                            Thread.sleep(5000); //sleep and then try again
                        } catch (InterruptedException e1) {
                            break;
                        }
                    }
                }
            }
        });
        publishThread.start();
    }

    private Thread subscribeThread;
    private void subscribe(final Handler handler)
    {
        subscribeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        Connection connection = factory.newConnection();
                        Channel channel = connection.createChannel();
                        channel.basicQos(1);
                        AMQP.Queue.DeclareOk q = channel.queueDeclare();
                        String routingKey = walletId;
                        String exchange = subscribeDestination;
                        channel.queueBind(q.getQueue(), exchange, routingKey);
                        QueueingConsumer consumer = new QueueingConsumer(channel);
                        channel.basicConsume(q.getQueue(), true, consumer);
                        while (true) {
                            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                            String message = new String(delivery.getBody());
                            Log.d("", "[r] " + message);
                            Message msg = handler.obtainMessage();
                            Bundle bundle = new Bundle();
                            bundle.putString("msg", message);
                            msg.setData(bundle);
                            handler.sendMessage(msg);
                        }
                    } catch (InterruptedException e) {
                        break;
                    } catch (Exception e1) {
                        Log.d("", "Connection broken: " + e1.getClass().getName());
                        try {
                            Thread.sleep(4000);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            }
        });
        subscribeThread.start();
    }
}
