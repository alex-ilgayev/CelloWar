package com.alar.cellowar.client;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alar.cellowar.R;
import com.alar.cellowar.client.controller.Settings;
import com.alar.cellowar.shared.datatypes.Client;
import com.alar.cellowar.shared.datatypes.ConnectionStatus;
import com.alar.cellowar.shared.datatypes.ICallback;
import com.alar.cellowar.shared.messaging.IMessage;
import com.alar.cellowar.shared.messaging.MessageInnerConnectionStatus;
import com.alar.cellowar.shared.messaging.MessageRequestAvailableClients;
import com.alar.cellowar.shared.messaging.MessageRequestJoin;
import com.alar.cellowar.shared.messaging.MessageRequestNewGame;
import com.alar.cellowar.shared.messaging.MessageResponseClientList;
import com.alar.cellowar.shared.messaging.MessageResponseSession;
import com.github.clans.fab.FloatingActionButton;

import java.util.LinkedList;
import java.util.UUID;

import static com.alar.cellowar.shared.messaging.MessageType.INNER_CONNECTION_STATUS;
import static com.alar.cellowar.shared.messaging.MessageType.RESPONSE_CLIENT_LIST;
import static com.alar.cellowar.shared.messaging.MessageType.RESPONSE_SESSION;

/**
 * Created by alexi on 1/26/2018.
 */

public class MainActivity  extends BaseActivity{
    private final String SUDOKU_TITLE = "Sudoku";
    private final String CROSSWORD_TITLE = "Crossword";

    private FloatingActionButton _fabSearch;
    private TextView _tvUsers = null;
    private ListView _lvUsers = null;
    private TextView _tvUsersOnlineTitle = null;
    private LinkedList<Client> _usersToJoin = new LinkedList<>();
    private ClientsPlayingAdapter _adapter = null;
    private UUID _waitingForClientSearchResponse = null;
    private UUID _waitingForJoinGameResponse = null;
    private UUID _waitingForNewGameResponse = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setupBaseActivityNetwork(new EventHandlerToMainActivity());

        RelativeLayout rlRootView = (RelativeLayout) findViewById( R.id.rlRootView);
        View view_child = getLayoutInflater().inflate(R.layout.activity_main, null);
        rlRootView.addView(view_child, 0);
        for(int i=1; i<rlRootView.getChildCount(); i++){
            rlRootView.getChildAt(i).bringToFront();
        }

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/segoeuisl.ttf");
        Typeface fontBold = Typeface.createFromAsset(getAssets(),"fonts/seguisb.ttf");
        Settings.getInstance().setFonts(font, fontBold);

        _tvUsersOnlineTitle = (TextView) findViewById(R.id.tvUsersOnlineTitle);
        _tvUsersOnlineTitle.setTypeface(Settings.getInstance().getFont());

        _tvUsers = (TextView) findViewById(R.id.tvUsers);
        _tvUsers.setTypeface(Settings.getInstance().getFont());

        _lvUsers = (ListView) findViewById(R.id.lvUsers);
        _adapter = new ClientsPlayingAdapter(this, _usersToJoin);
        _lvUsers.setAdapter(_adapter);
        _lvUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setLoadingFab(true);
                if(Settings.getInstance().getConnectivityState() == ConnectionStatus.CONNECTION_TIMED_OUT) {
                    _networkManager.resetServiceConnection();
                }

                _waitingForJoinGameResponse = UUID.randomUUID();
                MessageRequestJoin msg = new MessageRequestJoin();
                msg.client = _client;
                msg.id = _waitingForJoinGameResponse;
                msg.sessionIdToJoin = _usersToJoin.get(position).getCurrSessionId();
                _networkManager.sendMessage(msg);
            }
        });
        _client.setCurrSessionId(null);

        _fabSearch = (FloatingActionButton) findViewById(R.id.fabSearch);
    }

    @Override
    protected void onResume() {
        super.onResume();
        _client.setCurrSessionId(null);
    }

    public void setLoadingFab(boolean isLoading) {
        if(isLoading) {
            _fabSearch.setShowProgressBackground(true);
            _fabSearch.setIndeterminate(true);
        } else {
            _fabSearch.setShowProgressBackground(false);
            _fabSearch.setIndeterminate(false);
        }
    }

    public void onClickStartSudokuGame(View v){
        setLoadingFab(true);
        if(Settings.getInstance().getConnectivityState() == ConnectionStatus.CONNECTION_TIMED_OUT) {
            _networkManager.resetServiceConnection();
        }

        _waitingForNewGameResponse = UUID.randomUUID();
        MessageRequestNewGame msg = new MessageRequestNewGame();
        msg.client = _client;
        msg.id = _waitingForNewGameResponse;
        _networkManager.sendMessage(msg);
    }

    public void onClickRefreshUsers(View view) {
        setLoadingFab(true);
        if(Settings.getInstance().getConnectivityState() == ConnectionStatus.CONNECTION_TIMED_OUT) {
            _networkManager.resetServiceConnection();
        }

        _usersToJoin.clear();
        _adapter.notifyDataSetChanged();
        _tvUsers.setText(R.string.user_search_searching);
        _waitingForClientSearchResponse = UUID.randomUUID();
        MessageRequestAvailableClients msg = new MessageRequestAvailableClients();
        msg.client = _client;
        msg.id = _waitingForClientSearchResponse;
        _networkManager.sendMessage(msg);
    }

    public void setUsersList(MessageResponseClientList msg){

        Client[] list = msg.clients;
        String response = "";

        if(list.length == 0)
            response = getResources().getString(R.string.user_search_no_users);
        else
            for(Client client: list) {
                _usersToJoin.add(client);
                _adapter.notifyDataSetChanged();
            }
        if(!response.equals(getResources().getString(R.string.user_search_no_users)))
            response = "";
        else if(response.equals(""))
            response = getResources().getString(R.string.user_search_no_users);
        _tvUsers.setText(response);
    }

    public class ClientsPlayingAdapter extends ArrayAdapter<Client> {
        public ClientsPlayingAdapter(Context context, LinkedList<Client> users) {
            super(context, 0, users);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Client user = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_client_playing, parent, false);
            }

            TextView tvName = (TextView) convertView.findViewById(R.id.tvClientName);

            tvName.setText(user.getName());
            tvName.setTypeface(Settings.getInstance().getFont());
            return convertView;
        }
    }

    public class EventHandlerToMainActivity implements ICallback {
        @Override
        public void receiveMessage(IMessage message) {
            switch(message.getMessageType()) {
                case RESPONSE_CLIENT_LIST:
                    if(message.getId() == null || !message.getId().equals(_waitingForClientSearchResponse))
                        break;
                    setUsersList((MessageResponseClientList) message);
                    setLoadingFab(false);
                    break;
                case RESPONSE_SESSION:
                    if(message.getId() == null ||
                            (!message.getId().equals(_waitingForNewGameResponse) &&
                                    !message.getId().equals(_waitingForJoinGameResponse)))
                        break;
                    MessageResponseSession sessionMessage = (MessageResponseSession)message;
                    if(sessionMessage.activeSession.getGameData() != null) {
                        // TODO
                        // starting game.
//                        Intent i = new Intent(MainActivity.this, SudokuGameActivity.class);
//                        i.putExtra(SudokuGameActivity.INTENT_TAG_SUDOKU_GAME_DATA,
//                                sessionMessage.activeSession.getGameData());
//                        i.putExtra(SudokuGameActivity.INTENT_TAG_SESSION_ID_TO_JOIN,
//                                sessionMessage.activeSession.getSessionId());
//                        setLoadingFab(false);
//                        startActivity(i);
                    }
                    break;
                case INNER_CONNECTION_STATUS:
                    MessageInnerConnectionStatus connStatus = (MessageInnerConnectionStatus)message;
                    if(connStatus.connStatus == ConnectionStatus.CONNECTION_TIMED_OUT)
                        setLoadingFab(false);
                    break;
            }
        }
    }
}
