package com.alar.cellowar.shared.datatypes;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Alex on 4/11/2015.
 * This class represents a transmitted packet between client and server (and vice versa).
 * the packet consist two parameters:
 * <b>payload:</b> base64 encoded serialized message object
 * <b>date:</b> timestamp of the message
 */
public class Packet implements Serializable{
    private static final long serialVersionUID = 1L;

    @Expose
    @SerializedName("Payload")
    public String payload;

    @Expose
    @SerializedName("Date")
    public long date;
}
