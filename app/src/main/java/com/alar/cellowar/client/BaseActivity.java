package com.alar.cellowar.client;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;

import com.alar.cellowar.R;
import com.alar.cellowar.client.controller.NetworkManager;
import com.alar.cellowar.client.controller.Settings;
import com.alar.cellowar.client.service.CelloWarService;
import com.alar.cellowar.shared.datatypes.Client;
import com.alar.cellowar.shared.datatypes.ConnectionStatus;
import com.alar.cellowar.shared.datatypes.ICallback;
import com.alar.cellowar.shared.messaging.IMessage;
import com.alar.cellowar.shared.messaging.MessageInnerConnectionStatus;

/**
 * Created by alexi on 1/26/2018.
 */

public class BaseActivity extends Activity{
    protected CelloWarApp _myApp;
    protected NetworkManager _networkManager = null;
    protected Client _client;

    private Animation _rotateAnim;
    private Animation _zoomInAnim;
    private Animation _fadeOutAnim;
    private ImageButton _btnStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        Settings.getInstance().setContext(this); // first loading from shared prefs, then putting network.
        _client = Settings.getInstance().getThisClient();

        _myApp = (CelloWarApp) this.getApplicationContext();

        setupConnectivityAnimation();
    }

    @Override
    protected void onPause() {
        clearReferences();
        super.onPause();
        if(_networkManager.isPolling())
            _networkManager.disconnectFromServiceAndStopPolling();
    }

    public void setupBaseActivityNetwork(ICallback extendedActivityCallback) {
        _networkManager = new NetworkManager(new EventHandlerToBaseActivity(extendedActivityCallback), this, _client);
    }

    public void setupConnectivityAnimation(){
        _btnStatus = (ImageButton) findViewById(R.id.btnStatus);

        _rotateAnim = AnimationUtils.loadAnimation(_myApp, R.anim.anim_rotate);
        _zoomInAnim = AnimationUtils.loadAnimation(_myApp, R.anim.anim_zoomin);
        _fadeOutAnim = AnimationUtils.loadAnimation(_myApp, R.anim.anim_fadeout);
    }

    @Override
    protected void onDestroy() {
        clearReferences();
        super.onDestroy();
    }

    private void clearReferences(){
        Activity currActivity = _myApp.getCurrentActivity();
        if (this.equals(currActivity))
            _myApp.setCurrentActivity(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        _myApp.setCurrentActivity(this);

        ConnectionStatus state = Settings.getInstance().getConnectivityState();
        if(state == ConnectionStatus.CONNECTION_NOT_TRIED_YET) {
            setConnectivityState(ConnectionStatus.CONNECTION_RETRYING);
        } else if(state == ConnectionStatus.CONNECTION_OK) {
            _btnStatus.clearAnimation();
        }
        else
            setConnectivityState(state);

        _networkManager.resetServiceConnection();
    }

    public void OnClickRetryConnection(View v) {

        if(Settings.getInstance().getConnectivityState() == ConnectionStatus.CONNECTION_TIMED_OUT) {
            setConnectivityState(ConnectionStatus.CONNECTION_RETRYING);
            _networkManager.resetServiceConnection();
        }
    }

    public void setConnectivityState(ConnectionStatus state){
        Settings.getInstance().setConnectivityState(state);
        switch(state){
            case CONNECTION_OK:
                _btnStatus.setBackgroundResource(R.drawable.connected);
                _btnStatus.clearAnimation();
                //_btnStatus.startAnimation(_fadeOutAnim);
                break;
            case CONNECTION_RETRYING:
                _btnStatus.setBackgroundResource(R.drawable.retrying);
                _btnStatus.clearAnimation();
                _btnStatus.startAnimation(_rotateAnim);
                break;
            case CONNECTION_TIMED_OUT:
                _btnStatus.setBackgroundResource(R.drawable.disconnected);
                _btnStatus.clearAnimation();
                _btnStatus.startAnimation(_zoomInAnim);
                break;
        }
    }

    public class EventHandlerToBaseActivity implements ICallback {

        private ICallback _extendedCallback;

        public EventHandlerToBaseActivity(ICallback extendedCallback) {
            _extendedCallback = extendedCallback;
        }

        @Override
        public void receiveMessage(IMessage message) {
            switch(message.getMessageType()) {
                case INNER_CONNECTION_STATUS:
                    MessageInnerConnectionStatus connStatus = (MessageInnerConnectionStatus)message;
                    setConnectivityState(connStatus.connStatus);
                    break;
            }
            _extendedCallback.receiveMessage(message);
        }
    }
}
