package com.alar.cellowar.client.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;

import com.alar.cellowar.shared.datatypes.Client;
import com.alar.cellowar.shared.datatypes.ConnectionStatus;

import java.util.Random;
import java.util.UUID;

import com.google.gson.*;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Alex on 4/29/2015.
 *
 * Singleton class which holds various application variables.
 */
public class Settings {
    private static Settings _ins = null;

//    public static final String ROOT_URL = "https://t-monument-94020.appspot.com/_ah/api/";
//    public static final String ROOT_URL = "http://10.0.2.2:8080/_ah/api/";
//    public static final String ROOT_URL = "http://10.25.104.152:8080/";
//    public static final String ROOT_URL = "http://192.168.1.3:8080/";
//    public static final String ROOT_URL = "http://10.0.2.2:8080/";
    public static final String ROOT_URL = "http://cellowar.herokuapp.com/";
//    public static final String ROOT_URL = "http://cellawar-env.eu-central-1.elasticbeanstalk.com/";

    public static final int POLL_TIMESTAMP = 1000;
    public static final int DISCONNECTION_TIMESTAMP = 3000;
    public static final int KEYBOARD_OVERLAY_OFFSET = 90;

    private static final String PREFS_NAME = "com.alar.CelloWar";
    private static final String PREFS_TAG_CLIENT = "Client";

    private Client _client = null;
    // this UUID will be send as poll message id,
    // and activities will check this id for valid response session.
    // (as a response to poll, server returns session message with the same id)
    private UUID _pollRequestId = null;
    private ConnectionStatus _connectivityState = ConnectionStatus.CONNECTION_NOT_TRIED_YET;
    private Typeface _font;
    private Typeface _fontBold;
    private Context _ctx;
    private SharedPreferences _prefs;


    private Settings(){
        Random rand = new Random();
        int randNum = rand.nextInt(1000) + 1;
        _client = new Client(randNum, "Random" + (randNum));
        _pollRequestId = UUID.randomUUID();
    }

    public static Settings getInstance() {
        if (_ins == null)
            _ins = new Settings();
        return _ins;
    }

    // should be called once in MainActivity
    // once context set trying to load a client.
    public void setContext(Context ctx) {
        this._ctx = ctx;
        _prefs = ctx.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        if(!loadClientFromPrefs()) // failed
            storeClientInPrefs();
    }

    public Client getThisClient() {
        return _client;
    }

    public UUID getPollRequestId() {
        return _pollRequestId;
    }

    public ConnectionStatus getConnectivityState(){
        return _connectivityState;
    }

    public void setConnectivityState(ConnectionStatus state) {
        this._connectivityState = state;
    }

    public void setFonts(Typeface font, Typeface fondBold) {
        _font = font;
        _fontBold = fondBold;
    }

    public Typeface getFont() {
        return _font;
    }

    public Typeface getFontBold() {
        return _fontBold;
    }

    public void setClientName(String name) {
        _client.setName(name);
        storeClientInPrefs();
    }

    /**
     * tries to store client property to shared preferenences.
     * @return returns true of succeeded, false otherwise
     */
    private boolean storeClientInPrefs() {
        if(_prefs == null)
            return false;
        String clientStr = new Gson().toJson(_client);
        SharedPreferences.Editor e = _prefs.edit();
        e.putString(PREFS_TAG_CLIENT, clientStr);
        e.commit();
        return true;
    }

    /**
     * tries to load client property from shared preferenences.
     * @return returns true of succeeded, false otherwise
     */
    private boolean loadClientFromPrefs() {
        if(_prefs == null)
            return false;
        String clientStr = _prefs.getString(PREFS_TAG_CLIENT, null);
        if(clientStr == null)
            return false;
        Client c = new Gson().fromJson(clientStr, Client.class);
        if(c == null)
            return false;
        _client = c;
        return true;
    }
}
