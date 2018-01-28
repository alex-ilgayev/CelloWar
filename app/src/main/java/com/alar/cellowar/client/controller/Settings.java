package com.alar.cellowar.client.controller;

import android.graphics.Typeface;

import com.alar.cellowar.shared.datatypes.Client;
import com.alar.cellowar.shared.datatypes.ConnectionStatus;

import java.util.Random;
import java.util.UUID;

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
//    public static final String ROOT_URL = "http://10.0.2.2:8080/";
    public static final String ROOT_URL = "http://default-environment.y2ruc2btia.eu-central-1.elasticbeanstalk.com/ ";
//    public static final String ROOT_URL = "http://cellawar-env.eu-central-1.elasticbeanstalk.com/";

    public static final int POLL_TIMESTAMP = 1000;
    public static final int DISCONNECTION_TIMESTAMP = 3000;
    public static final int KEYBOARD_OVERLAY_OFFSET = 90;

    private Client _client = null;
    // this UUID will be send as poll message id,
    // and activities will check this id for valid response session.
    // (as a response to poll, server returns session message with the same id)
    private UUID _pollRequestId = null;
    private ConnectionStatus _connectivityState = ConnectionStatus.CONNECTION_NOT_TRIED_YET;
    private Typeface _font;
    private Typeface _fontBold;

    private Settings(){
        Random rand = new Random();
        int randNum = rand.nextInt(1000) + 1;
        _client = new Client(randNum, "Alex" + (randNum));
        _pollRequestId = UUID.randomUUID();
    }

    public static Settings getInstance() {
        if (_ins == null)
            _ins = new Settings();
        return _ins;
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
}
