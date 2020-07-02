package kr.babylab.receipt;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Telephony;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.telephony.SmsManager;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kotlin.collections.ArrayDeque;


public class SmsActivity extends AppCompatActivity {
    private Button customer_certification;
    private String phoneNo;
    private String textsms;
    private EditText textPhoneNo;
    private EditText textName;
    private int send_sms_permission;
    private int phone_state_permission;
    private Button next;
    //승인번호 데이터
    List<String> business_nums;
    List<String> receipt_nums;
    List<String> imagePathResult;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);
        //승인번호 데이터 받기
        business_nums = new ArrayList<>();
        receipt_nums = new ArrayList<>();
        Intent get_intent = getIntent();
        business_nums  = (ArrayList<String>)get_intent.getSerializableExtra("business_nums");
        receipt_nums  = (ArrayList<String>)get_intent.getSerializableExtra("receipt_nums");
        imagePathResult  = (ArrayList<String>)get_intent.getSerializableExtra("imagePathResult");
        System.out.println("_&&&&&___"+receipt_nums);

        //
        if (ContextCompat.checkSelfPermission(SmsActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            //SEND_SMS 권한이 없다면,

            ActivityCompat.requestPermissions(SmsActivity.this, new String[]{Manifest.permission.SEND_SMS}, 5);
            //달라고 요청
        }
        //메세지 권한
        send_sms_permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
        phone_state_permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

        //
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("고객 정보 등록");
        actionBar.setDisplayHomeAsUpEnabled(true);
        //
        textPhoneNo = (EditText) findViewById(R.id.textPhone);
        textName = (EditText) findViewById(R.id.textName);
        //
        next = findViewById((R.id.next));
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNo = textPhoneNo.getText().toString();
                textsms = textName.getText().toString();
                //모두 입력 확인
                if(textsms.length() == 0){
                    Toast.makeText(getApplicationContext(), "이름을 입력해주세요", Toast.LENGTH_SHORT).show();
                }else {
                    if(phoneNo.length() >= 10 ){
                        Intent intent = new Intent(getApplicationContext(),IssuanceActivity.class);
                        //가져온 데이터와 고객명, 전화번호 추가해서 보내기
                        intent.putExtra("receipt_nums", (Serializable) receipt_nums);
                        intent.putExtra("business_nums", (Serializable) business_nums);
//                intent.putExtra("imagePathResult", (Serializable) imagePathResult);
                        intent.putExtra("client_name", textName.getText().toString());
                        intent.putExtra("phone_number", textPhoneNo.getText().toString());
                        Log.d("","------------------ : "+textName.getText() + ", " + textPhoneNo.getText());

                        startActivity(intent);
                    }else{
                        if(phoneNo.length()==0){
                            Toast.makeText(getApplicationContext(), "전화번호를 입력해주세요", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(getApplicationContext(), "전화번호를 확인해주세요", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
        //
        customer_certification = (Button) findViewById(R.id.customer_certification);
        customer_certification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNo = textPhoneNo.getText().toString();
                textsms = textName.getText().toString();
                System.out.println(phoneNo);
                System.out.println(textsms);

                //모두 입력 확인
                if(textsms.length() == 0){
                    Toast.makeText(getApplicationContext(), "이름을 입력해주세요", Toast.LENGTH_SHORT).show();
                }else {
                    if(phoneNo.length() >= 10 ){
                        Uri smsUri = Uri.parse("sms: "+phoneNo);
                        Intent sendIntent = new Intent(Intent.ACTION_SENDTO, smsUri);
                        sendIntent.putExtra("sms_body", textsms+"님 본인 소유 핸드폰 인증 확인 문자입니다.");
                        startActivity(sendIntent);
                    }else{
                        if(phoneNo.length()==0){
                            Toast.makeText(getApplicationContext(), "전화번호를 입력해주세요", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(getApplicationContext(), "전화번호를 확인해주세요", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
}

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}