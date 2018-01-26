package com.alar.cellowar.client.controller;


import com.alar.cellowar.shared.datatypes.Packet;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by alexi on 1/24/2018.
 */

public interface APIEndpoint {

    /**
     * Main request handler.
     * the request must be as POST, and contain packet parameter. the packet contains:
     * Payload - serialized message encoded in base64
     * Date - timestamp (epoch) of the message
     *
     * from this both parameters, creates Packet object, and send it the handler function.
     * the request returns json list of packets (same parameters)
     */
@POST("api/")
Call<Packet[]> sendMessage(@Body Packet packet);

}

