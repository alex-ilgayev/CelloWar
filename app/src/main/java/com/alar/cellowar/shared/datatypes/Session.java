package com.alar.cellowar.shared.datatypes;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.UUID;

/**
 * Created by Alex on 7/19/2015.
 */
public class Session implements Serializable{
    private static final long serialVersionUID = 1L;

    private UUID _sessionId;
    private LinkedList<Client> _clientList;
    //TODO MAP
    GameData _gameData;

    public Session(UUID sessionId, LinkedList<Client> clientList
            , GameData gameData){
        this._sessionId = sessionId;
        this._clientList = clientList;
        this._gameData = gameData;
    }

    public Session(UUID sessionId, GameData gameData){
        this(sessionId, new LinkedList<Client>(), gameData);
    }

    public Session(GameData gameData){
        this(UUID.randomUUID(), gameData);
    }

    public UUID getSessionId(){
        return _sessionId;
    }

    public LinkedList<Client> getClientList(){
        return _clientList;
    }

    public GameData getGameData() {
        return _gameData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Session session = (Session) o;

        return _sessionId.equals(session._sessionId);
    }

    @Override
    public int hashCode() {
        return _sessionId.hashCode();
    }
}
