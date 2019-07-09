package com.hby.henu;

import androidx.appcompat.app.AppCompatActivity;
import okhttp3.*;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hby.henu.utils.MD5;
import com.hby.henu.model.ResponseItem;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Login extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Login";
    private static final String URL = "http://xk.henu.edu.cn";
    private static final int FAIL = 0;
    private static final int SUCCESS = 1;
    private static String _sessionid;
    private static Gson gson = new Gson();
    private EditText idET;
    private EditText passwdET;
    private EditText captchaET;
    private ImageView captchaIV;
    private Button loginBt;
    OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
    Handler captchaHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case SUCCESS:
                    byte[] Picture = (byte[]) msg.obj;
                    Bitmap bitmap = BitmapFactory.decodeByteArray(Picture, 0, Picture.length);
                    captchaIV.setImageBitmap(bitmap);
                    break;
                case FAIL:
                    Toast.makeText(Login.this, "验证码刷新失败", Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    });
    Handler responseHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case FAIL:
                    Toast.makeText(Login.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        idET = (EditText) findViewById(R.id.idET);
        passwdET = (EditText) findViewById(R.id.passwdET);
        captchaET = (EditText) findViewById(R.id.captchaET);
        captchaIV = (ImageView) findViewById(R.id.captchaIV);
        loginBt = (Button) findViewById(R.id.loginBt);

        captchaIV.setOnClickListener(this);
        loginBt.setOnClickListener(this);

        // 测试
        idET.setText("1710252305");
        passwdET.setText("A0000038D1A41C7");

        // 获取验证码图片
        setCaptcha();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.captchaIV:
                setCaptcha();
                break;
            case R.id.loginBt:
                String id = idET.getText().toString();
                String pw = passwdET.getText().toString();
                String captcha = captchaET.getText().toString();
                if (id.length() != 0 && pw.length() != 0 && captcha.length() != 0) {
                    id = Base64.encodeToString((id + ";;" + _sessionid).getBytes(), Base64.DEFAULT);
                    pw = MD5.MD5Encode(MD5.MD5Encode(pw) + MD5.MD5Encode(captcha.toLowerCase()));
                    int expression = 0;
                    for (int i = 0; i < pw.length(); ++i) {
                        expression |= charType(pw.charAt(i));
                    }
                    String length = pw.length() + "";
                    String userzh = pw.toLowerCase().trim().contains(id.toLowerCase().trim()) ? "1" : "0";
                    login(id, pw, captcha, expression + "", length, userzh);
                }
                break;
        }
    }

    public int charType(int num) {
        if (num >= 48 && num <= 57) {
            return 8;
        }
        if (num >= 97 && num <= 122) {
            return 4;
        }
        if (num >= 65 && num <= 90) {
            return 2;
        }
        return 1;
    }

    public void setCaptcha() {
        String time = new SimpleDateFormat("EEE   MMM   dd   yyyy   HH:mm:ss", Locale.US).format(new Date()).replace("   ", "%20");
        Request request = new Request.Builder()
                .url(URL + "/cas/genValidateCode?dateTime=" + time + "%20GMT+0800%20(%D6%D0%B9%FA%B1%EA%D7%BC%CA%B1%BC%E4)")
                .header("Host", "xk.henu.edu.cn")
                .header("Proxy-Connection", "keep-alive")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36")
                .header("Accept", "image/webp,image/apng,image/*,*/*;q=0.8")
                .header("Referer", "http://xk.henu.edu.cn/cas/login.action")
                .header("Accept-Encoding", "gzip, deflate")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7")
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String cookie = response.header("Set-Cookie").substring(11, 52);
                _sessionid = cookie;
                SharedPreferences.Editor editor = getSharedPreferences("cookies", Context.MODE_PRIVATE).edit();
                editor.putString("henu", "JSESSIONID=" + cookie);
                editor.apply();

                byte[] captcha = response.body().bytes();
                Message msgCaptcha = captchaHandler.obtainMessage();
                msgCaptcha.obj = captcha;
                msgCaptcha.what = SUCCESS;
                captchaHandler.sendMessage(msgCaptcha);
            }
        });
    }

    public void login(String id, String pw, String captcha, String expression, String length, String userzh) {
        RequestBody formBody = new FormBody.Builder()
                .add("_u" + captcha, id)
                .add("_p" + captcha, pw)
                .add("randnumber", captcha)
                .add("isPasswordPolicy", "1")
                .add("txt_mm_expression", expression)
                .add("txt_mm_length", length)
                .add("txt_mm_userzh", userzh)
                .build();
        Request request = new Request.Builder()
                .url(URL + "/cas/logon.action")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36")
                .header("Cookie", "JSESSIONID=" + _sessionid)
                .post(formBody)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ResponseItem res = gson.fromJson(response.body().string(), ResponseItem.class);
                if (!res.getStatus().equals("200")) {
                    Message msgCaptcha = responseHandler.obtainMessage();
                    msgCaptcha.obj = res.getMessage();
                    msgCaptcha.what = FAIL;
                    responseHandler.sendMessage(msgCaptcha);
                } else {
                    Intent Menu = new Intent(Login.this, Menu.class);
                    startActivity(Menu);
                    finish();
                }
            }
        });
    }
}
