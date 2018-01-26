package com.alar.cellowar.shared.messaging;

import com.alar.cellowar.shared.datatypes.Client;

import java.util.UUID;

/**
 * Created by Alex on 4/10/2015.
 * Abstraction of all the messages in this program. (inner, and outer)
 */
public interface IMessage {
    MessageType getMessageType();
    Client getClient();
    UUID getId();
}
