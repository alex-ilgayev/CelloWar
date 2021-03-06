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
import java.util.Set;
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

        _tvGameInfo.setTypeface(Settings.getInstance().getFont());

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

        // TODO: artium debug
        /*_gameData = new CelloWarGameData(); // TODO: artium debug
        _gameData.obst.add(new Obstacle(100.0f, 100.0f, 200.0f, 500.0f));
        _gameData.obst.add(new Obstacle(100.0f, 100.0f, 500.0f, 200.0f));
        _gameData.obst.add(new Obstacle(900.0f, 450.0f, 1100.0f, 650.0f));

        _gameData.obst.add(new Obstacle(500.0f, 500.0f, 800.0f, 1200.0f));


        _gameData.ants.add(new Antenna(200.0f, 50.0f, 50.0f, Antenna.AntennaType.TRANSMISSION ));
        _gameData.ants.add(new Antenna(400.0f, 50.0f, 350.0f, Antenna.AntennaType.TRANSMISSION ));
        _gameData.ants.add(new Antenna(350.0f, 650.0f, 650.0f, Antenna.AntennaType.ELECTONIC_WARFARE ));
        //_gameData.ants.add(new Antenna(300.0f, 1050.0f, 1050.0f, Antenna.AntennaType.TRANSMISSION ));
        //_gameData.ants.add(new Antenna(300.0f, 650.0f, 900.0f, Antenna.AntennaType.TRANSMISSION ));
        //_gameData.ants.add(new Antenna(300.0f, 600.0f, 1050.0f, Antenna.AntennaType.TRANSMISSION ));

        _gameData.ants.add(new Antenna(250.0f, 640.0f, 1050.0f, Antenna.AntennaType.TRANSMISSION ));
        _gameData.ants.add(new Antenna(250.0f, 620.0f, 1050.0f, Antenna.AntennaType.TRANSMISSION ));
*/
//        _gameData = new CelloWarGameData();
//        _gameData.setWH(100.0f, 100.0f);
//        _gameData.obst.add(new Obstacle(20.0f, 20.0f, 30.0f, 30.0f));
//        _gameData.obst.add(new Obstacle(70.0f, 70.0f, 80.0f, 80.0f));
//        _gameData.obst.add(new Obstacle(70.0f, 20.0f, 80.0f, 30.0f));
//        _gameData.obst.add(new Obstacle(20.0f, 70.0f, 30.0f, 80.0f));
//
//        _gameData.obst.add(new Obstacle(25.0f, 45.0f, 35.0f, 55.0f));
//        _gameData.obst.add(new Obstacle(65.0f, 45.0f, 75.0f, 55.0f));
//
//        _gameData.ants.add(new Antenna(30.0f, 10.0f, 10.0f, Antenna.AntennaType.TRANSMISSION ));
//        _gameData.ants.add(new Antenna(15.0f, 15.0f, 10.0f, Antenna.AntennaType.TRANSMISSION ));
//        _gameData.ants.add(new Antenna(15.0f, 20.0f, 10.0f, Antenna.AntennaType.TRANSMISSION ));
//        _gameData.ants.add(new Antenna(15.0f, 25.0f, 10.0f, Antenna.AntennaType.TRANSMISSION ));
//        _gameData.ants.add(new Antenna(15.0f, 10.0f, 15.0f, Antenna.AntennaType.TRANSMISSION ));
//        _gameData.ants.add(new Antenna(10.0f, 10.0f, 20.0f, Antenna.AntennaType.ELECTONIC_WARFARE ));
//        _gameData.ants.add(new Antenna(10.0f, 10.0f, 25.0f, Antenna.AntennaType.ELECTONIC_WARFARE ));

        _gameView.setMap(_gameData); // need to call after layout is created
    }

    public void postInfo(String text) {
        _tvGameInfo.setText(text);
    }

    public class EventHandlerToSudokuActivity implements ICallback {
        @Override
        public void receiveMessage(IMessage message) {
            Set<Integer> bases;

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
                                if(_pbWaiting.getVisibility() == View.INVISIBLE) {
                                    bases = _gameView.DetermineInterconnectedBases();
                                    if (bases.size() == 0) {
                                        postInfo(getString(R.string.info_finish1));
                                    } else if (bases.size() >= 2) {
                                        postInfo(getString(R.string.info_finish2));
                                    } else if (bases.contains(1)) {
                                        postInfo(getString(R.string.info_finish3));
                                    } else if (bases.contains(2)) {
                                        postInfo(getString(R.string.info_finish4));
                                    }

                                    //postInfo(getString(R.string.info_finish));
                                }
                                _pbWaiting.setVisibility(View.INVISIBLE);
                            }
                            _btnFinishMove.setVisibility(View.INVISIBLE);
                            _gameData = data;
                            _gameView.setMap(data);
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
