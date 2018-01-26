package com.alar.cellowar.client;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.alar.cellowar.R;
import com.alar.cellowar.client.controller.Settings;
import com.alar.cellowar.shared.datatypes.CelloWarGameData;
import com.alar.cellowar.shared.datatypes.Client;
import com.alar.cellowar.shared.datatypes.ConnectionStatus;
import com.alar.cellowar.shared.datatypes.ICallback;
import com.alar.cellowar.shared.datatypes.Session;
import com.alar.cellowar.shared.messaging.IMessage;
import com.alar.cellowar.shared.messaging.MessageRequestJoinPool;
import com.alar.cellowar.shared.messaging.MessageRequestSetMove;
import com.alar.cellowar.shared.messaging.MessageResponseSession;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by alexi on 1/26/2018.
 */

public class CelloWarActivity extends BaseActivity {

    public static final String INTENT_TAG_SUDOKU_GAME_DATA = "gameData";
    public static final String INTENT_TAG_SESSION_ID_TO_JOIN = "sessionToJoin";

    private CelloWarGameData _gameData;
    private Button _btnFinishMove;

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


        Serializable gameDataSerializable = getIntent().getSerializableExtra(INTENT_TAG_SUDOKU_GAME_DATA);
        Serializable sessionIdToJoin = getIntent().getSerializableExtra(INTENT_TAG_SESSION_ID_TO_JOIN);

        if(gameDataSerializable != null)
            _gameData = (CelloWarGameData) gameDataSerializable;
        if(sessionIdToJoin != null)
            _client.setCurrSessionId((UUID) sessionIdToJoin);

        _btnFinishMove = findViewById(R.id.btnFinishMove);
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
            }
        });

        //TODO should be, for debugging.
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

                    // what to do ??
//                    SudokuMove[] diff = SudokuGameData.getDiff(_gameData, session.getGameData());
//                    _gameData = new SudokuGameData(session.getGameData());
//                    for(SudokuMove sMove: diff){
//                        setSudokuValueToActivity(sMove.row, sMove.col, sMove.value);
//                    }

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
