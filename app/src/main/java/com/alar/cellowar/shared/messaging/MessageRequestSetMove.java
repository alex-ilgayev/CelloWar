package com.alar.cellowar.shared.messaging;

import com.alar.cellowar.shared.datatypes.CelloWarGameData;
import com.alar.cellowar.shared.datatypes.Client;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by Alex on 4/28/2015.
 */
public class MessageRequestSetMove implements IMessage, Serializable{
    private static final long serialVersionUID = 1L;

    public Client client;
    public UUID id;

    public CelloWarGameData move;

    @Override
    public MessageType getMessageType() {
        return MessageType.REQUEST_SET_MOVE;
    }

    @Override
    public Client getClient() {
        return client;
    }

    @Override
    public UUID getId(){
        return id;
    }
}
