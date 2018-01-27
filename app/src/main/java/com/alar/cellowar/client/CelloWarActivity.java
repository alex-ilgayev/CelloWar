package com.alar.cellowar.client;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alar.cellowar.R;
import com.alar.cellowar.client.controller.Settings;
import com.alar.cellowar.shared.datatypes.Antenna;
import com.alar.cellowar.shared.datatypes.CelloWarGameData;
import com.alar.cellowar.shared.datatypes.ConnectionStatus;
import com.alar.cellowar.shared.datatypes.ICallback;
import com.alar.cellowar.shared.datatypes.Obstacle;
import com.alar.cellowar.shared.datatypes.Session;
import com.alar.cellowar.shared.messaging.IMessage;
import com.alar.cellowar.shared.messaging.MessageRequestSetMove;
import com.alar.cellowar.shared.messaging.MessageResponseSession;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by alexi on 1/26/2018.
 */

public class CelloWarActivity extends BaseActivity {

    public static final String INTENT_TAG_CELLOWAR_GAME_DATA = "gameData";
    public static final String INTENT_TAG_SESSION_ID_TO_JOIN = "sessionToJoin";
    public static final String INTENT_TAG_PLAYER_NUMBER = "playerNumber";

    private CelloWarGameData _gameData;
    private Button _btnFinishMove;
    private TextView _tvGameInfo;
    private com.wang.avi.AVLoadingIndicatorView _pbWaiting;
    private GameView _gameView;

    private boolean isPlayerFinishedTurn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setupBaseActivityNetwork(new EventHandlerToSudokuActivity());

        RelativeLayout rlRootView = (RelativeLayout) findViewById( R.id.rlRootView);
        View view_child = getLayoutInflater().inflate(R.layout.activity_cellowar_game, null);
        rlRootView.addView(view_child, 0);
        for(int i=1; i<rlRootView.getChildCount(); i++){
            rlRootView.getChildAt(i).bringToFront();
        }


        Serializable gameDataSerializable = getIntent().getSerializableExtra(INTENT_TAG_CELLOWAR_GAME_DATA);
        Serializable sessionIdToJoin = getIntent().getSerializableExtra(INTENT_TAG_SESSION_ID_TO_JOIN);
        Serializable playerNumber = getIntent().getSerializableExtra(INTENT_TAG_PLAYER_NUMBER);

        if(gameDataSerializable != null)
            _gameData = (CelloWarGameData) gameDataSerializable;
        if(sessionIdToJoin != null)
            _client.setCurrSessionId((UUID) sessionIdToJoin);
        if(playerNumber!= null)
            _client.setPlayerNumber((Integer)playerNumber);

        _btnFinishMove = findViewById(R.id.btnFinishMove);
        _tvGameInfo = findViewById(R.id.tvGameInfo);
        _pbWaiting = findViewById(R.id.pbWaiting);
        _gameView = findViewById(R.id.vGameView);

        _btnFinishMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Settings.getInstance().getConnectivityState() == ConnectionStatus.CONNECTION_TIMED_OUT) {
                    _networkManager.resetServiceConnection();
                }

                MessageRequestSetMove msg = new MessageRequestSetMove();
                msg.client = _client;
                msg.id = UUID.randomUUID();
                msg.move = _gameData;
                _networkManager.sendMessage(msg);

                isPlayerFinishedTurn = true;
            }
        });

        postInfo(getString(R.string.info_place_ant));

//        //TODO shouldn't be, for debugging.
//        _gameData.ants.add(new Antenna(10, 5, 6, Antenna.AntennaType.TRANSMISSION));
//        _gameData.ants.add(new Antenna(1, 2, 3, Antenna.AntennaType.ELECTONIC_WARFARE));

        _gameView.setMap(_gameData);
    }

    public void postInfo(String text) {
        _tvGameInfo.setText(text);
    }

    public class EventHandlerToSudokuActivity implements ICallback {
        @Override
        public void receiveMessage(IMessage message) {
            switch(message.getMessageType()) {
                case RESPONSE_SESSION:
                    if(message.getId() == null ||
                            !message.getId().equals(Settings.getInstance().getPollRequestId()))
                        break;
                    MessageResponseSession sessionInfo = (MessageResponseSession)message;
                    Session session = sessionInfo.activeSession;

                    CelloWarGameData data = session.getGameData();

                    switch (data.state) {
                        case ANT_PLACEMENT:
                            //to nothing.
                            break;
                        case WAIT_FOR_OTHER:
                            // check if state changed.
                            if(isPlayerFinishedTurn) {
                                postInfo(getString(R.string.info_wait));
                                _btnFinishMove.setVisibility(View.INVISIBLE);
                                _pbWaiting.setVisibility(View.VISIBLE);
                            } else {
                                _tvGameInfo.setText(getString(R.string.info_enemy_finished));
                                break;
                            }
                            _gameData = data;
                            break;
                        case SHOW_RESULT:
                            // check if state changed.
                            if(_gameData.state != CelloWarGameData.State.WAIT_FOR_OTHER) {
                                // check if you the one who finished
                                if(_pbWaiting.getVisibility() == View.INVISIBLE)

                                postInfo(getString(R.string.info_finish));
                                _pbWaiting.setVisibility(View.INVISIBLE);
                            }
                            _gameData = data;
                            break;
                    }

//                    _connectedClients.clear();
//                    for(Client client: session.getClientList()) {
//                        _connectedClients.add(client);
//                    }
//                    _adapter.notifyDataSetChanged();
                    break;
                //TODO
//                case RESPONSE_SMS:
//                    if(message.getId() == null)
//                        break;
//                    MessageResponseSms textMsg = (MessageResponseSms) message;
//                    postTextToActivity(textMsg.name, textMsg.text);
//                    break;
            }
        }
    }


}
