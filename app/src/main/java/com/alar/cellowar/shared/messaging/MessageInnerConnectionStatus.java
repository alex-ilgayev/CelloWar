package com.alar.cellowar.shared.messaging;

import com.alar.cellowar.shared.datatypes.Client;
import com.alar.cellowar.shared.datatypes.ConnectionStatus;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by Alex on 7/19/2015.
 */
public class MessageInnerConnectionStatus implements IMessage, Serializable{
    private static final long serialVersionUID = 1L;

    public Client responseClient;
    public ConnectionStatus connStatus;

    @Override
    public MessageType getMessageType() {
        return MessageType.INNER_CONNECTION_STATUS;
    }

    @Override
    public Client getClient() {
        return responseClient;
    }

    @Override
    public UUID getId() {
        return null;
    }
}
