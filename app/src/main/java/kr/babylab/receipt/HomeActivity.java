package kr.babylab.receipt;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.preference.PreferenceManager;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;



public class HomeActivity extends AppCompatActivity {

    private LinearLayout write_img;
    private ImageView camera_img;
    private ImageView lookup_img;
    //
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    //signal(뒤로가기 방지)받기
    private long time= 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();
        //signal(뒤로가기 방지)받기
        //signal  = (int) get_intent.getSerializableExtra("signal");
        // 로그인 정보 없으면 로그인화면으로 이동 시키기
        String username = (String) preferences.getString("username", null);
        System.out.println("----- username : " + username);
        if(username == null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }else{
            hideActionBar();
            setContentView(R.layout.activity_home);
            loadUserInfo();
            clickButtons();
        }
    }

    private void hideActionBar(){
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    private void loadUserInfo(){
        TextView market_name = (TextView) findViewById(R.id.market_name);
        market_name.setText(preferences.getString("market_name", null));
        TextView name = (TextView) findViewById(R.id.name);
        name.setText(preferences.getString("name", null));
    }

    private void clickButtons(){
        clickDirectBtn();
        clickCameraBtn();
        clickReceiptListBtn();
        clickLogoutBtn();
    }

    private void clickDirectBtn(){
        write_img = (LinearLayout) findViewById(R.id.write_img);
        write_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DirectRegActivity.class);
                startActivity(intent);
            }
        });
    }

    private void clickCameraBtn(){
        camera_img = (ImageView) findViewById(R.id.camera_img);
        camera_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),CameraActivity.class);
                startActivity(intent);
            }
        });
    }

    private void clickReceiptListBtn(){
        lookup_img = (ImageView) findViewById(R.id.lookup_img);
        lookup_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),ListActivity.class);
                startActivity(intent);
            }
        });
    }

    private void clickLogoutBtn(){
        Button logout_btn = (Button) findViewById(R.id.logout_btn);
        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("","----- logout");
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                builder.setTitle("로그아웃").setMessage("로그아웃 하시겠습니까?");
                builder.setPositiveButton("네", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        editor.remove("username");
                        editor.apply();
                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                        startActivity(intent);
                    }
                });
                builder.setNegativeButton("아니오", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        return;
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(System.currentTimeMillis()-time>=2000){
            time=System.currentTimeMillis();
            Toast.makeText(getApplicationContext(),"뒤로 버튼을 한번 더 누르면 종료합니다.",Toast.LENGTH_SHORT).show();
        }else if(System.currentTimeMillis()-time<4000){
            moveTaskToBack(true);
        }
    }
}
