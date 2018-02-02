package com.alar.cellowar.client;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import com.alar.cellowar.shared.messaging.MessageRequestJoinPool;
import com.alar.cellowar.shared.messaging.MessageResponseClientList;
import com.alar.cellowar.shared.messaging.MessageResponseSession;
import com.google.gson.Gson;

import java.util.LinkedList;
import java.util.UUID;

import mehdi.sakout.fancybuttons.FancyButton;

/**
 * Created by alexi on 1/26/2018.
 */

public class MainActivity  extends BaseActivity{

    private FancyButton _fabSearch;

    private TextView _tvUsers = null;
    private ListView _lvUsers = null;
    private TextView _tvUsersOnlineTitle = null;
    private TextView _tvJoinPool = null;
    private TextView _tvMainTitle = null;
    private Button _btnSettings;
    private Button _btnAbout;

    private LinkedList<Client> _usersConnected = new LinkedList<>();
    private ClientsPlayingAdapter _adapter = null;
    private UUID _waitingForClientSearchResponse = null;
    private UUID _waitingForJoinPoolResponse = null;

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

        _tvUsersOnlineTitle = findViewById(R.id.tvUsersOnlineTitle);
        _fabSearch = findViewById(R.id.fabSearch);
        _tvUsers = findViewById(R.id.tvUsers);
        _lvUsers = findViewById(R.id.lvUsers);
        _tvJoinPool = findViewById(R.id.tvJoinPool);
        _tvMainTitle = findViewById(R.id.tvMainTitle);
        _btnSettings = findViewById(R.id.btnSettings);
        _btnAbout = findViewById(R.id.btnAbout);

        _tvUsers.setTypeface(Settings.getInstance().getFont());
        _tvUsersOnlineTitle.setTypeface(Settings.getInstance().getFont());
        _tvJoinPool.setTypeface(Settings.getInstance().getFontBold());
        _fabSearch.setCustomTextFont("fonts/segoeuisl.ttf");
        _tvMainTitle.setTypeface(Settings.getInstance().getFont());

        _adapter = new ClientsPlayingAdapter(this, _usersConnected);
        _lvUsers.setAdapter(_adapter);
        _client.setCurrSessionId(null);

        _fabSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLoadingFab(true);
                if(Settings.getInstance().getConnectivityState() == ConnectionStatus.CONNECTION_TIMED_OUT) {
                    _networkManager.resetServiceConnection();
                }

                _waitingForJoinPoolResponse = UUID.randomUUID();
                MessageRequestJoinPool msg = new MessageRequestJoinPool();
                msg.client = _client;
                msg.id = _waitingForJoinPoolResponse;
                _networkManager.sendMessage(msg);
            }
        });

        _btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // prompting the user for his username
                openSettingsPanelAndUpdateIt();
            }
        });
        _btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // showing about dialog
                openAboutPanel();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        _client.setCurrSessionId(null);
    }

    public void setLoadingFab(boolean isLoading) {
        if(isLoading) {
//            _fabSearch.setShowProgressBackground(true);
//            _fabSearch.setIndeterminate(true);
        } else {
//            _fabSearch.setShowProgressBackground(false);
//            _fabSearch.setIndeterminate(false);
        }
    }

    public void onClickRefreshUsers(View view) {
        setLoadingFab(true);
        if(Settings.getInstance().getConnectivityState() == ConnectionStatus.CONNECTION_TIMED_OUT) {
            _networkManager.resetServiceConnection();
        }

        _usersConnected.clear();
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
        else {
            _usersConnected.clear();
            for (Client client : list) {
                _usersConnected.add(client);

            }
            _adapter.notifyDataSetChanged();
        }
        if(!response.equals(getResources().getString(R.string.user_search_no_users)))
            response = "";
        else if(response.equals(""))
            response = getResources().getString(R.string.user_search_no_users);
        _tvUsers.setText(response);
    }

    public void openSettingsPanelAndUpdateIt() {
        LayoutInflater li = LayoutInflater.from(this);
        View v = li.inflate(R.layout.item_settings, null);
        final Client c = Settings.getInstance().getThisClient();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set your name:");

        builder.setView(v);
        final EditText et = v.findViewById(R.id.etName);
        et.setText(c.getName());
        et.setSelection(et.getText().length());

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Settings.getInstance().setClientName(et.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void openAboutPanel() {
        LayoutInflater li = LayoutInflater.from(this);
        View v = li.inflate(R.layout.item_about, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Credits");

        builder.setView(v);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });

        builder.show();
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
                    if(message.getId() == null)
                        break;
                    setUsersList((MessageResponseClientList) message);
                    break;
                case RESPONSE_SESSION:
                    MessageResponseSession sessionMessage = (MessageResponseSession)message;
                    if(sessionMessage.activeSession.getGameData() != null &&
                            sessionMessage.activeSession.getIsSearching() == false) {
//                         starting game.
                        _client.setCurrSessionId(sessionMessage.getId());
                        Intent i = new Intent(MainActivity.this, CelloWarActivity.class);
                        i.putExtra(CelloWarActivity.INTENT_TAG_CELLOWAR_GAME_DATA,
                                sessionMessage.activeSession.getGameData());
                        i.putExtra(CelloWarActivity.INTENT_TAG_SESSION_ID_TO_JOIN,
                                sessionMessage.activeSession.getSessionId());
                        i.putExtra(CelloWarActivity.INTENT_TAG_PLAYER_NUMBER,
                                sessionMessage.activeSession.getClientOrder().get(_client.getId()));
                        setLoadingFab(false);
                        startActivity(i);
                    } else if(sessionMessage.activeSession.getIsSearching()) {
                        // waiting for a game.
                        _client.setCurrSessionId(sessionMessage.activeSession.getSessionId());
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
