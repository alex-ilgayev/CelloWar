package com.alar.cellowar.shared.messaging;


import com.alar.cellowar.shared.datatypes.Client;
import com.alar.cellowar.shared.datatypes.Session;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by Alex on 7/19/2015.
 */
public class MessageResponseSession implements IMessage, Serializable{
    private static final long serialVersionUID = 1L;

    public Client responseClient;
    public UUID responseId;

    public Session activeSession;

    @Override
    public MessageType getMessageType() {
        return MessageType.RESPONSE_SESSION;
    }

    @Override
    public Client getClient() {
        return responseClient;
    }

    @Override
    public UUID getId() {
        return responseId;
    }
}
