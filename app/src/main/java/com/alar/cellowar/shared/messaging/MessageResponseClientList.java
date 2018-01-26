package com.alar.cellowar.shared.messaging;

import com.alar.cellowar.shared.datatypes.Client;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by Alex on 4/28/2015.
 */
public class MessageResponseClientList implements IMessage, Serializable {
    private static final long serialVersionUID = 1L;

    public Client responseClient;
    public UUID responseId;

    public Client[] clients;

    @Override
    public MessageType getMessageType() {
        return MessageType.RESPONSE_CLIENT_LIST;
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
