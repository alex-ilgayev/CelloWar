package com.alar.cellowar.shared.datatypes;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by Alex on 4/11/2015.
 * This class can represent local/remote client.
 * This client class is unique for the user.
 * if getCurrSession for the client is not null, client is in a middle of a game.
 */
public class Client implements Serializable {
    private static final long serialVersionUID = 1L;

    private int _id;
    private String _name;
    private UUID _currSessionId;
    // if not in active game, then equals null.
    private Integer _playerNumber;

    public Client(int id, String name , UUID currSessionId) {
        this._id = id;
        this._name = name;
        this._currSessionId = currSessionId;
        this._playerNumber = null;
    }

    public Client(int id, String name) {
        this._id = id;
        this._name = name;
        this._currSessionId = null;
        this._playerNumber = null;
    }

    public void setId(int id) {
        this._id = id;
    }

    public int getId() {
        return this._id;
    }

    public void setName(String name) {
        this._name = name;
    }

    public String getName() {
        return _name;
    }

    public void setCurrSessionId(UUID currSessionId){
        this._currSessionId = currSessionId;
    }

    public UUID getCurrSessionId(){
        return _currSessionId;
    }

    public void setPlayerNumber(Integer playerNum) {
        this._playerNumber = playerNum;
    }

    public Integer getPlayerNumber() {
        return this._playerNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Client client = (Client) o;

        return _id == client._id;

    }

    @Override
    public int hashCode() {
        return _id;
    }

    public enum ClientType {
        MAIN_MENU,
        PLAYING_GAME,
    }
}