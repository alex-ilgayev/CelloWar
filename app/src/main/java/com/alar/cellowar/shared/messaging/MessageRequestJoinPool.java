package com.alar.cellowar.shared.messaging;

import com.alar.cellowar.shared.datatypes.Client;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by alexi on 1/26/2018.
 */

public class MessageRequestJoinPool implements IMessage, Serializable {
    private static final long serialVersionUID = 1L;

    public Client client;
    public UUID id;

    @Override
    public MessageType getMessageType() {
        return MessageType.REQUEST_JOIN_POOL;
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
