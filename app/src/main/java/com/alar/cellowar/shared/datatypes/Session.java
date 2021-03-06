package com.alar.cellowar.shared.datatypes;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Alex on 7/19/2015.
 */
public class Session implements Serializable{
    private static final long serialVersionUID = 1L;

    private UUID _sessionId;
    private List<Client> _clientList;
    private boolean _isSearching = false;

    // a map from client ID to the order number (1,2,3 ..)
    private HashMap<Integer, Integer> _clientOrder;

    //TODO MAP
    CelloWarGameData _gameData;

    public Session(UUID sessionId, List<Client> clientList
            , CelloWarGameData gameData, HashMap<Integer, Integer> clientOrder){
        this._sessionId = sessionId;
        this._clientList = clientList;
        this._gameData = gameData;
        this._clientOrder = clientOrder;
    }

    public Session(UUID sessionId, CelloWarGameData gameData, HashMap<Integer, Integer> clientOrder){
        this(sessionId, new LinkedList<Client>(), gameData, clientOrder);
    }

    public Session(CelloWarGameData gameData, HashMap<Integer, Integer> clientOrder){
        this(UUID.randomUUID(), gameData, clientOrder);
    }

    public UUID getSessionId(){
        return _sessionId;
    }

    public List<Client> getClientList(){
        return _clientList;
    }

    public CelloWarGameData getGameData() {
        return _gameData;
    }

    public void setClientOrder(HashMap<Integer, Integer> clientOrder) {
        this._clientOrder = clientOrder;
    }

    public HashMap<Integer, Integer> getClientOrder() {
        return this._clientOrder;
    }

    public boolean getIsSearching() {
        return this._isSearching;
    }

    public void setIsSearching(boolean isSearching) {
        this._isSearching = isSearching;
    }

    public void setGameData(CelloWarGameData gameData) {
        this._gameData = gameData;
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
