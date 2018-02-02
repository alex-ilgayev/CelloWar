package com.alar.cellowar.client.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;

import com.alar.cellowar.client.controller.APIEndpoint;
import com.alar.cellowar.client.controller.Settings;
import com.alar.cellowar.shared.datatypes.Client;
import com.alar.cellowar.shared.datatypes.ConnectionStatus;
import com.alar.cellowar.shared.datatypes.ICallback;
import com.alar.cellowar.shared.datatypes.Packet;
import com.alar.cellowar.shared.messaging.IMessage;
import com.alar.cellowar.shared.messaging.MessageCompression;
import com.alar.cellowar.shared.messaging.MessageInnerConnectionStatus;
import com.alar.cellowar.shared.messaging.MessageRequestPollMessageQueue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.ServiceConfigurationError;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by alexi on 1/26/2018.
 */

public class CelloWarService extends Service {

    private static final String TAG = "CelloWarService";

    private HashMap<Integer, ICallback> _callbackMap = null;
    private HashMap<Integer, pollMessageAsyncTask> _asyncTaskMap = null;
    private ConnectionStatus _currStatus = ConnectionStatus.CONNECTION_TIMED_OUT;

    private final IBinder _binder = new LocalBinder();

    private Retrofit _retrofit;

    @Override
    public void onCreate() {
        super.onCreate();
        _callbackMap = new HashMap<Integer, ICallback>();
        _asyncTaskMap = new HashMap<Integer, pollMessageAsyncTask>();

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        _retrofit = new Retrofit.Builder()
                .baseUrl(Settings.ROOT_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return _binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for(pollMessageAsyncTask task: _asyncTaskMap.values()){
            task.cancel(true);
        }
    }

    public void startPolling(ICallback callback, Client client){
        _callbackMap.put(client.getId(), callback);
        pollMessageAsyncTask task = new pollMessageAsyncTask(client);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        _asyncTaskMap.put(client.getId(), task);
    }

    public void stopPolling(int clientId){
        if(_callbackMap.containsKey(clientId))
            _callbackMap.remove(clientId);
        if(_asyncTaskMap.containsKey(clientId)) {
            pollMessageAsyncTask task = _asyncTaskMap.remove(clientId);
            task.cancel(true);
        }
    }

    public ConnectionStatus getCurrentConnectionStatus() {
        return _currStatus;
    }

    // task which makes continious polling requests for new packets.
    class pollMessageAsyncTask extends AsyncTask<Void, Object, Exception> {
        private Client _client;
        private MessageRequestPollMessageQueue _pollMsg;
        // checking for connection failure.
        private checkConnectionAsyncTask _task = null;

        protected pollMessageAsyncTask(Client client){
            _client = client;
            _pollMsg = new MessageRequestPollMessageQueue();
            _pollMsg.client = client;
            _pollMsg.id = Settings.getInstance().getPollRequestId();
        }

        // if returns null then was cancelled quietly, if not then returns exception.
        @Override
        protected Exception doInBackground(Void... params) {
            try {
                while(true) {
                    if(isCancelled())
                        return null;
                    Log.d(TAG, "Service for client: " + _client.getId() + " polling.");

                    Packet p = new Packet();
                    p.date = System.currentTimeMillis();
                    p.payload = Base64.encodeToString(
                            MessageCompression.getInstance().compress(_pollMsg),
                            Base64.NO_WRAP );

                    // creating new async task, if sending taking too long, sending retrying
                    // message to the activity above.
                    _task = new checkConnectionAsyncTask(_client);
                    publishProgress(_task);

                    // making the network call.

                    APIEndpoint apiService = _retrofit.create(APIEndpoint.class);
                    Call<Packet[]> call = apiService.sendMessage(p);
                    Packet[] packets = call.execute().body();

                    // handling packets

                    // stopping the async task.
                    _task.cancel(true);
                    _task = null;

                    if(packets != null) {
                        for (int i = 0; i < packets.length; i++) {
                            IMessage returnedMsg = MessageCompression.getInstance().decompress
                                    (Base64.decode(packets[i].payload, Base64.NO_WRAP));
                            publishProgress(returnedMsg);
                        }
                    }
                    // creating inner connections status message.
                    MessageInnerConnectionStatus connMsg = new MessageInnerConnectionStatus();
                    connMsg.responseClient = _client;
                    connMsg.connStatus = ConnectionStatus.CONNECTION_OK;
                    _currStatus = ConnectionStatus.CONNECTION_OK;
                    publishProgress(connMsg);
                    if (!isCancelled())
                        Thread.sleep(Settings.POLL_TIMESTAMP);
                    else
                        return null;
                }
            } catch (InterruptedException e) {
                Log.d(TAG, e.getMessage());
                return null;
            } catch (IOException e) {
                Log.d(TAG, e.getMessage());
                if(_task != null)
                    _task.cancel(true);
                return e;
            }
        }

        @Override
        protected void onPostExecute(Exception exc) {
            // sending inner message of connection failure.
            if(exc != null) {
                MessageInnerConnectionStatus msg = new MessageInnerConnectionStatus();
                msg.responseClient = _client;
                msg.connStatus = ConnectionStatus.CONNECTION_TIMED_OUT;
                _currStatus = ConnectionStatus.CONNECTION_TIMED_OUT;
                if (_callbackMap.containsKey(new Integer(msg.getClient().getId())))
                    _callbackMap.get(msg.getClient().getId()).receiveMessage(msg);
            }
        }

        // can receive either async task or a IMessage.
        // if receives task, so executes the task.
        // if receives IMessage, then raising it up to the activity.
        @Override
        protected void onProgressUpdate(Object... values) {
            super.onProgressUpdate(values);
            for(Object obj: values){
                if(obj instanceof checkConnectionAsyncTask) {
                    checkConnectionAsyncTask task = (checkConnectionAsyncTask)obj;
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    IMessage msg = (IMessage)obj;
                    if (_callbackMap.containsKey(new Integer(msg.getClient().getId())))
                        _callbackMap.get(msg.getClient().getId()).receiveMessage(msg);
                }
            }
        }
    }

    class checkConnectionAsyncTask extends AsyncTask<Void, IMessage, Void> {
        private Client _client;

        protected checkConnectionAsyncTask(Client client){
            _client = client;
        }

        // if returns null then was cancelled quietly, if not then returns exception.
        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(Settings.DISCONNECTION_TIMESTAMP);
                if(isCancelled())
                    return null;

                // if it's not cancelled yet, sending RETRYING message to activity.
                MessageInnerConnectionStatus connMsg = new MessageInnerConnectionStatus();
                connMsg.responseClient = _client;
                connMsg.connStatus = ConnectionStatus.CONNECTION_RETRYING;
                _currStatus = ConnectionStatus.CONNECTION_RETRYING;
                publishProgress(connMsg);
                return null;
            } catch(InterruptedException e) {
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(IMessage... values) {
            super.onProgressUpdate(values);
            for(IMessage msg: values){
                if (_callbackMap.containsKey(new Integer(msg.getClient().getId())))
                    _callbackMap.get(msg.getClient().getId()).receiveMessage(msg);
            }
        }
    }

    public class LocalBinder extends Binder {
        public CelloWarService getService(){
            return CelloWarService.this;
        }
    }
}
