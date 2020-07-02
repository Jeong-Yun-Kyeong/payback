package kr.babylab.receipt;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private Button login_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hideActionBar();
        changeStatusBarColor();
        // 안드로이드 내부저장소 선언
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();
        //
        login_btn = (Button) findViewById(R.id.login_btn);
        clickLoginBtn();
    }
    private void clickLoginBtn(){
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText login_id = (EditText)findViewById(R.id.login_id);
                EditText login_pw = (EditText)findViewById(R.id.login_pw);
                if(login_id.getText().toString().equals("")){
                    Toast.makeText(MainActivity.this, "아이디를 입력해주세요.", Toast.LENGTH_SHORT).show();
                }else if(login_pw.getText().toString().equals("")){
                    Toast.makeText(MainActivity.this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                }else{
                    startLoginProcess(login_id.getText().toString(), login_pw.getText().toString());
                }
            }
        });
    }

    private void startLoginProcess(String id, String pw){
        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("username", id)
                .add("password", pw)
                .build();

        String url = getString(R.string.web_services) + "/login/";
//        url = "http://172.30.1.17/login/";

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        Log.d("","----- request confirm :"+request);

        client.newCall(request).enqueue(loginCallback);
    }

    private Callback loginCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
//            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d("","-------------:실패"+e);
        }
        @Override
        public void onResponse(Call call, okhttp3.Response response) throws IOException {
            Log.d("","-------------:성공");
            String receivedValue;
            try{
                JSONObject responseJson = new JSONObject(response.body().string());
                receivedValue = responseJson.getString("response");
                if(receivedValue.equals("true")){
                    doLogin(responseJson);
                }else{
                    doNotLogin();
                }
            } catch(JSONException error){
                System.out.println("----- json exception : " + error );
            }
        }
    };

    private void hideActionBar(){
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    private void changeStatusBarColor(){
        Window window = MainActivity.this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(MainActivity.this, R.color.splash));
    }

    private void doLogin(JSONObject responseJson) throws JSONException{
        System.out.println("----- login success" );
        editor.putString("username", responseJson.getString("username"));
        editor.putString("name", responseJson.getString("name"));
        editor.putString("market_num", responseJson.getString("market_num"));
        editor.putString("market_name", responseJson.getString("market_name"));
        editor.apply();

        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(intent);
    }

    private void doNotLogin(){
        System.out.println("----- login fail" );
        editor.putString("username", null);
        editor.apply();

        Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "로그인에 실패 했습니다.", Toast.LENGTH_SHORT).show();
            }
        }, 0);
    }
}
