package com.alar.cellowar.client.controller;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;

import com.alar.cellowar.client.CelloWarApp;
import com.alar.cellowar.client.service.CelloWarService;
import com.alar.cellowar.shared.datatypes.Client;
import com.alar.cellowar.shared.datatypes.ConnectionStatus;
import com.alar.cellowar.shared.datatypes.ICallback;
import com.alar.cellowar.shared.datatypes.Packet;
import com.alar.cellowar.shared.messaging.IMessage;
import com.alar.cellowar.shared.messaging.MessageCompression;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Alex on 4/11/2015.
 * The manager of all the networking operation for all games.
 */
public class NetworkManager {
    private final static String TAG = "NetworkManager";


//    private GameonApi _myApiService = null;
    private CelloWarService _service = null;
    private boolean _isBoundToService = false;
    private ICallback _eventToGameManager = null;
    private Context _ctx = null;
    private Client _client = null;
    private ConnectionStatus _currState = ConnectionStatus.CONNECTION_TIMED_OUT;
    private Retrofit _retrofit;

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection _connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            CelloWarService.LocalBinder binder = (CelloWarService.LocalBinder) service;
            _service = binder.getService();
            _service.startPolling(_eventToGameManager ,_client);
            _isBoundToService = true;

            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            _retrofit = new Retrofit.Builder()
                    .baseUrl(Settings.ROOT_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            _isBoundToService = false;
            _service.stopPolling(_client.getId());
        }
    };

    public NetworkManager(ICallback eventToGameManager, Context ctx, Client client) {
        this._eventToGameManager = eventToGameManager;
        this._ctx = ctx;
        this._client = client;

        if(!isPolling())
            connectServiceAndStartPolling();
    }

    public boolean isPolling(){
        return _isBoundToService;
    }

    public void connectServiceAndStartPolling(){
        Intent intent = new Intent(_ctx, CelloWarService.class);
        _ctx.bindService(intent, _connection, Context.BIND_AUTO_CREATE);
    }

    public void disconnectFromServiceAndStopPolling(){
        if(_isBoundToService) {
            _ctx.unbindService(_connection);
            _isBoundToService = false;
        }
    }

    // will be called the case of polling failure due do network problem.
    public void resetServiceConnection() {
        disconnectFromServiceAndStopPolling();
        connectServiceAndStartPolling();
    }

    public void sendMessage(IMessage message) {
        if(!isPolling())
            connectServiceAndStartPolling();
        new sendMessageAsyncTask().execute(message);
    }

    public ConnectionStatus getCurrentConnectionStatus(){
        if(_service == null)
            return ConnectionStatus.CONNECTION_TIMED_OUT;
        return _service.getCurrentConnectionStatus();
    }

    class sendMessageAsyncTask extends AsyncTask<IMessage, Void, Boolean> {
        private IMessage msg;

        @Override
        protected Boolean doInBackground(IMessage... params) {
            msg = params[0];

            Packet p = new Packet();

            byte[] payload = MessageCompression.getInstance().compress(msg);

            p.payload = Base64.encodeToString(payload, Base64.NO_WRAP);
            p.date = System.currentTimeMillis();

            // making the network call.
            APIEndpoint service = _retrofit.create(APIEndpoint.class);
            Call<Packet[]> messageCall = service.sendMessage(p);
            messageCall.enqueue(new Callback<Packet[]>() {
                @Override
                public void onResponse(Call<Packet[]> call, Response<Packet[]> response) {
                    // server error.
                    if (response != null && !response.isSuccessful() && response.errorBody() != null) {
                        try {
                            Log.e(TAG, "server internal error: " + response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return;
                    }

                    // the response is irrelevant
                    Packet[] packets = response.body();
                }

                @Override
                public void onFailure(Call<Packet[]> call, Throwable t) {
                    // network error.
                    Log.e(TAG, "can't reach server: " + t.toString());
                }
            });
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            //if(aBoolean == false)
        }
    }
}
