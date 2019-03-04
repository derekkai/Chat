package com.example.xiaobaiz.chat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ChatLogin extends AppCompatActivity {
    private ProgressDialog progressdialog;
    private Handler mHandler = new Handler();
    private Button tosignup_btn,login_btn;
    private TextView login_e_tv;
    private EditText username_et,password_et;
    private boolean vusername,vpassword;
    private final String fusername = "[a-zA-Z0-9]{6,16}",
            fpassword = "[a-zA-Z0-9]{6,16}";
    protected static Socket socket;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_login);
        initView();
        toSignUpPage();
        logIn();
    }
    private void logIn(){
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(username_et.getText().toString().matches(fusername)
                        &&password_et.getText().toString().matches(fpassword)){
                    progressdialog = ProgressDialog.show(ChatLogin.this, "Log In", "Please wait...");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                socket = new Socket("192.168.56.1",5050);
                                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF8"));
                                bw.write("LogIn");
                                bw.newLine();
                                bw.flush();
                                JSONObject json_write = new JSONObject();
                                json_write.put("j_username",username_et.getText().toString());
                                json_write.put("j_password",password_et.getText().toString());

                                bw.write(json_write.toString());
                                bw.newLine();
                                Thread.sleep(100);
                                bw.flush();

                                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF8"));
                                String tmp = br.readLine();
                                if(tmp.equals("error")){
                                    showErrorAlert(true);
                                }else if(tmp.equals("success")){
                                    showErrorAlert(false);
                                    toMainPage();
                                }
                            }catch (Exception e){}
                            finally {progressdialog.dismiss();}
                        }
                    }).start();
                }else{
                    showErrorAlert(true);
                }
            }
        });
    }
    private void showErrorAlert(final boolean btmp){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(btmp){
                    login_e_tv.setVisibility(View.VISIBLE);
                } else {
                    login_e_tv.setVisibility(View.INVISIBLE);
                }

            }
        });
    }
    private void toSignUpPage() {
        tosignup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent().setClass(ChatLogin.this, ChatSignUp.class));
            }
        });
    }
    private void toMainPage(){
        startActivity(new Intent().setClass(ChatLogin.this, ChatMain.class));
    }
    private void initView() {
        login_e_tv = (TextView)findViewById(R.id.login_e_tv);
        username_et = (EditText) findViewById(R.id.l_username_et);
        password_et = (EditText) findViewById(R.id.l_password_et);
        tosignup_btn = (Button)findViewById(R.id.tosignup_btn);
        login_btn = (Button)findViewById(R.id.login_btn);
    }
}
