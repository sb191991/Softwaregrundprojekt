package com.notimetospy.controller;

import org.java_websocket.WebSocket;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;


/**
 * Diese Klasse verwaltet die WebSocket Verbindung zum Spielserver.
 */
public class ConnectionHandler {

    private SimpleClient simpleClient;
    private WebSocket connection;

    private UUID clientId;
    public Date date = new Date();
    private String debugMessage;


    public void setSimpleClient(SimpleClient simpleClient) {
        this.simpleClient = simpleClient;
    }

    public SimpleClient getSimpleClient() {

        if (simpleClient == null) {
            reset();
        }
        return simpleClient;
    }

    public void setConnection(WebSocket connection) {
        if (this.connection == null) {
            this.clientId = null;
            this.connection = connection;
            this.connection.setAttachment(null);
        } else {
            closeConnection();
            setConnection(connection);
        }
    }

    public void setClientId(UUID clientId) {
        this.clientId = clientId;
        this.connection.setAttachment(this.clientId);
    }

    public void setDate(Date date)  {
        this.date = date;
    }

    public void setDebugMessage(String debugMessage) {
        this.debugMessage = debugMessage;
        this.connection.setAttachment(this.debugMessage);
    }

    public WebSocket getConnection() {
        if (connection == null) {
            return null;
        }
        return connection;
    }

    public boolean connectionOpen() {
        if (connection == null) {
            return false;
        }

        if (connection.isOpen()) {
            return true;
        } else {
            closeConnection();
            return false;
        }
    }

    public UUID getClientId() {
        return clientId;
    }

    public String getDate() {
        String dateString = date.toString();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        dateString = sdf.format(date);
        return dateString;
    }

    public String getDebugMessage() {
        return debugMessage;
    }

    public ConnectionHandler() {
        reset();
    }

    public void closeConnection() {
        if (connection == null) {
            return;
        }
        connection.close();
        reset();
    }

    private void reset() {
        this.simpleClient = null;
        this.connection = null;
        this.clientId = null;

    }
}
