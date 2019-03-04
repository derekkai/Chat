package com.example.xiaobaiz.chat;

import android.graphics.Color;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ChatMain extends AppCompatActivity {
    private ChatAdapter adapter;
    private ListView messagesContainer;
    private EditText message;
    private NavigationView navigation;
    private DrawerLayout mDrawerLayout;
    private Toolbar toolbar;
    private Socket socket;
    private TextView servermessage;
    private Handler mHandler = new Handler();
    private Button sent_message_btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_main);
        setNavigation();
        initView();
        initControl();
        talkToServer();
        sentMessage();
    }

    private void initControl() {
        adapter = new ChatAdapter(ChatMain.this, new ArrayList<ChatMessage>());
        messagesContainer.setAdapter(adapter);
    }

    private void sentMessage() {
        sent_message_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF8"));
                            String msg = message.getText().toString();
                            bw.write(msg);
                            bw.newLine();
                            bw.flush();

                            final ChatMessage chatMessage = new ChatMessage("Me",msg,DateFormat.getDateTimeInstance().format(new Date()),true);
                            mHandler.post(new Runnable() {
                                    @Override
                                public void run(){message.setText("");
                                        displayMessage(chatMessage);}});

                        }catch (Exception e){e.printStackTrace();}
                    }
                }).start();
            }
        });
    }

    private void displayMessage(ChatMessage chatMessage) {
        adapter.add(chatMessage);
        adapter.notifyDataSetChanged();
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
    }

    private void setNavigation() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_main);
        setSupportActionBar(toolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawerToggle.syncState();
        mDrawerLayout.setScrimColor(Color.parseColor("#777B7B7B"));
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        navigation = (NavigationView) findViewById(R.id.navigation);
        navigation.inflateHeaderView(R.layout.nav_header);
        navigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.navItem1:
                        Toast.makeText(ChatMain.this, "clic home button", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.navItem2:
                        break;
                    case R.id.navItem3:
                        break;
                }
                return false;
            }
        });
    }

    private void talkToServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = ChatLogin.socket;

                    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF8"));
                    //br.readLine();
                    String message = br.readLine();

                    showServerMessage(0,message,null);
                    while(socket.isConnected()){

                        if((message = br.readLine())!=null){
                            try{
                                JSONObject json_read = new JSONObject(message);
                                String tmp = json_read.get("j_type").toString();
                                if(tmp.equals("1")){
                                    ChatMessage chatMessage = new ChatMessage(json_read.get("j_username").toString(),json_read.get("j_message").toString(),
                                            DateFormat.getDateTimeInstance().format(new Date()),false);
                                   showServerMessage(1,null,chatMessage);
                                }else if(tmp.equals("2")){
                                    String username = json_read.get("j_username").toString();
                                    showServerMessage(2,username,null);
                                }
                            }catch (Exception e){}

                        }
                    }
                } catch (IOException e) { Log.i("1",e.toString());}
            }
        }).start();
    }

    private void showServerMessage(final int which , final String getMessage ,final ChatMessage chatMessage) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                switch (which){
                    case 0:
                        servermessage.setText(getMessage);//Hello
                        break;
                    case 1:
                        displayMessage(chatMessage);//chat
                        break;
                    case 2:
                        servermessage.setText(getMessage+" is online !");
                        break;
                }

            }
        });
    }

    private void initView() {
        messagesContainer = (ListView) findViewById(R.id.messagesContainer);
        servermessage = (TextView) findViewById(R.id.server_message);
        sent_message_btn = (Button)findViewById(R.id.sent_message_btn);
        message = (EditText)findViewById(R.id.message_et);
    }
}
