package com.example.xiaobaiz.chat;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;

import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class ChatSignUp extends AppCompatActivity {
    private static final int PICK_FROM_CAMERA = 1;
    private static final int CROP_FROM_CAMERA = 2;
    private static final int PICK_FROM_FILE = 3;
    private final String fusername = "[a-zA-Z0-9]{6,16}",
            fpassword = "[a-zA-Z0-9]{6,16}",
            femail = "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
    private boolean vusername, vpassword, vemail, vphoto;
    private EditText username_et, password_et, email_et;
    private ImageView username_c_iv, password_c_iv, email_c_iv;
    private ImageView username_e_iv, password_e_iv, email_e_iv;
    private ImageButton uploadphoto;
    private Uri imgUri;
    private Button signup_btn;
    private TextView signup_s,signup_e;
    private Handler mHandler = new Handler();
    private String filePath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_sign_up);
        initView();
        SetEditTextEvent();
        //setUploadPhoto();
        signUpHandle();
    }

    private void signUpHandle() {
        signup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Log.i("Test","Sign Up");
                username_et.clearFocus();
                password_et.clearFocus();
                email_et.clearFocus();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (vusername && vpassword && vemail) {
                                //Log.i("Test","Sign Up Pass,Socket(192.168.56.1:5050)");
                                Socket socket = new Socket("192.168.56.1", 5050);
                               // Log.i("Test","Connect! Sent SignUp Requestion!");
                                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
                                bw.write("SignUp");
                                bw.newLine();
                                bw.flush();
                                Thread.sleep(500);

                                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                                JSONObject json_write = new JSONObject();
                                json_write.put("j_username",username_et.getText().toString());
                                json_write.put("j_password",password_et.getText().toString());
                                json_write.put("j_email",email_et.getText().toString());

                                oos.writeObject(json_write.toString());
                                oos.flush();

                                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF8"));
                                String tmp;
                                while ((tmp = br.readLine())==null){

                                }
                                System.out.println(tmp);
                                if(tmp.equals("success")){
                                    System.out.println("success");
                                    showFinalAlert(true);
                                }else{
                                    showFinalAlert(false);
                                }
                            }
                        } catch (Exception e) {}
                    }
                }).start();
            }
        });
    }
    private void showFinalAlert(final boolean btmp){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(btmp){
                    signup_s.setVisibility(View.VISIBLE);
                    signup_e.setVisibility(View.INVISIBLE);
                }else{
                    signup_e.setVisibility(View.VISIBLE);
                    signup_s.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
    private void setUploadPhoto() {

        uploadphoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username_et.clearFocus();
                password_et.clearFocus();
                email_et.clearFocus();
                //方式1，直接打开图库，只能选择图库的图片
                /*Intent i = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);*/
                //方式2，会先让用户选择接收到该请求的APP，可以从文件系统直接选取图片
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_FROM_FILE);
            }
        });
    }

    private void doCrop() {

        final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");
        List<ResolveInfo> list = getPackageManager().queryIntentActivities(
                intent, 0);
        int size = list.size();
        if (size == 0) {
            Toast.makeText(this, "can't find crop app", Toast.LENGTH_SHORT)
                    .show();
            return;
        } else {
            intent.setData(imgUri);
            intent.putExtra("outputX", 128);
            intent.putExtra("outputY", 128);
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("scale", true);
            intent.putExtra("return-data", true);

            for (ResolveInfo res : list) {
                final CropOption co = new CropOption();
                co.title = getPackageManager().getApplicationLabel(
                        res.activityInfo.applicationInfo);
                co.icon = getPackageManager().getApplicationIcon(
                        res.activityInfo.applicationInfo);
                co.appIntent = new Intent(intent);
                co.appIntent
                        .setComponent(new ComponentName(
                                res.activityInfo.packageName,
                                res.activityInfo.name));
                cropOptions.add(co);
            }

            CropOptionAdapter adapter = new CropOptionAdapter(
                    getApplicationContext(), cropOptions);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("choose a app");
            builder.setAdapter(adapter,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            startActivityForResult(
                                    cropOptions.get(item).appIntent,
                                    CROP_FROM_CAMERA);
                        }
                    });

            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {

                    if (imgUri != null) {
                        getContentResolver().delete(imgUri, null, null);
                        imgUri = null;
                    }
                }
            });

            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case PICK_FROM_CAMERA:
                doCrop();
                break;
            case PICK_FROM_FILE:
                imgUri = data.getData();
                doCrop();
                break;
            case CROP_FROM_CAMERA:
                if (null != data) {
                    setCropImg(data);
                }
                break;
        }
    }

    private void setCropImg(Intent picdata) {
        if (Environment.getExternalStorageState()//確定SD卡可讀寫
                .equals(Environment.MEDIA_MOUNTED)) {
            File sdFile = android.os.Environment.getExternalStorageDirectory();
            String path = sdFile.getPath() + File.separator + "Chat";
            //sdcard/
            File dirFile = new File(path);
            if (!dirFile.exists()) {//如果資料夾不存在
                dirFile.mkdir();//建立資料夾
            }
        }
        Bundle bundle = picdata.getExtras();
        if (null != bundle) {
            Bitmap mBitmap = bundle.getParcelable("data");
            uploadphoto.setImageBitmap(mBitmap);
            saveBitmap(Environment.getExternalStorageDirectory() + "/Chat" + "/crop_"
                    + System.currentTimeMillis() + ".png", mBitmap);
        }
    }

    private void saveBitmap(String fileName, Bitmap mBitmap) {
        filePath = fileName;
        File f = new File(fileName);
        FileOutputStream fOut = null;
        try {
            f.createNewFile();
            fOut = new FileOutputStream(f);
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            vphoto = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fOut.close();
                Toast.makeText(this, "save success", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void initView() {
        signup_s = (TextView)findViewById(R.id.signup_s_tv);
        signup_e = (TextView)findViewById(R.id.signup_e_tv);
        signup_btn = (Button) findViewById(R.id.signup_btn);
        uploadphoto = (ImageButton) findViewById(R.id.s_uploadphoto_ib);
        username_et = (EditText) findViewById(R.id.s_username_et);
        password_et = (EditText) findViewById(R.id.s_password_et);
        email_et = (EditText) findViewById(R.id.s_email_et);
        username_c_iv = (ImageView) findViewById(R.id.s_username_c_iv);
        password_c_iv = (ImageView) findViewById(R.id.s_password_c_iv);
        email_c_iv = (ImageView) findViewById(R.id.s_email_c_iv);
        username_e_iv = (ImageView) findViewById(R.id.s_username_e_iv);
        password_e_iv = (ImageView) findViewById(R.id.s_password_e_iv);
        email_e_iv = (ImageView) findViewById(R.id.s_email_e_iv);
    }

    private void SetEditTextEvent() {
        username_et.setOnFocusChangeListener(edittextListener);
        password_et.setOnFocusChangeListener(edittextListener);
        email_et.setOnFocusChangeListener(edittextListener);
    }

    //region edittextListener
    private View.OnFocusChangeListener edittextListener = new View.OnFocusChangeListener() {

        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            switch (view.getId()) {
                case R.id.s_username_et:
                    if (hasFocus) {
                        vusername = false;
                        username_c_iv.setVisibility(View.INVISIBLE);
                        username_e_iv.setVisibility(View.INVISIBLE);
                    } else {
                        if (username_et.getText().toString().matches(fusername)) {
                            vusername = true;
                            username_c_iv.setVisibility(View.VISIBLE);
                        } else {
                            vusername = false;
                            username_e_iv.setVisibility(View.VISIBLE);
                        }
                    }
                    break;
                case R.id.s_password_et:
                    if (hasFocus) {
                        vpassword = false;
                        password_c_iv.setVisibility(View.INVISIBLE);
                        password_e_iv.setVisibility(View.INVISIBLE);
                    } else {
                        if (password_et.getText().toString().matches(fpassword)) {
                            vpassword = true;
                            password_c_iv.setVisibility(View.VISIBLE);
                        } else {
                            vpassword = false;
                            password_e_iv.setVisibility(View.VISIBLE);
                        }
                    }
                    break;
                case R.id.s_email_et:
                    if (hasFocus) {
                        vemail = false;
                        email_c_iv.setVisibility(View.INVISIBLE);
                        email_e_iv.setVisibility(View.INVISIBLE);
                    } else {
                        if (email_et.getText().toString().matches(femail)) {
                            vemail = true;
                            email_c_iv.setVisibility(View.VISIBLE);
                        } else {
                            vemail = false;
                            email_e_iv.setVisibility(View.VISIBLE);
                        }
                    }
                    break;
            }
        }
    };
    //endregion
}
