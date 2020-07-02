package kr.babylab.receipt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class DirectRegActivity extends AppCompatActivity {
    // 입력완료 버튼
    private Button go_sms;
    // ??
    private String get_business_num;
    private String get_receipt_num;
    // 승인번호 및 사업자번호
    private EditText direct_reg_receipt;
    private EditText direct_reg_business;
    // 입력 버튼
    private TextView direct_reg_btn;
    // 남자 이미지
    private ImageView man_image;
    // 입력된 데이터 레이아웃
    private LinearLayout reg_layout;
    //RecycleView
    private InputAdapter inputAdapter;
    private RecyclerView dataList;
    private List<String> business_nums;
    private List<String> receipt_nums;
//    private List<String> imagePathResult;
    private List<Integer> delete_btns;
    //
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direct_reg);
        // recycleview
        dataList = findViewById(R.id.dataList);
        business_nums = new ArrayList<>();
        receipt_nums = new ArrayList<>();
        delete_btns = new ArrayList<>();
        inputAdapter = new InputAdapter(this,business_nums,receipt_nums,delete_btns);
        //

        //
        direct_reg_business = (EditText)findViewById(R.id.direct_reg_business);
        direct_reg_receipt = (EditText)findViewById(R.id.direct_reg_receipt);
        //
        man_image = (ImageView) findViewById(R.id.man_image);
        //
        reg_layout = (LinearLayout) findViewById(R.id.reg_layout);
        reg_layout.setVisibility(View.GONE);
        //
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,1,GridLayoutManager.VERTICAL,false);
        dataList.setLayoutManager(gridLayoutManager);
        dataList.setAdapter(inputAdapter);
        //
        if (business_nums.size() == 0){
            direct_reg_business.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if (hasFocus) {
                        man_image.setVisibility(View.GONE);
                    } else {
                        man_image.setVisibility(View.VISIBLE);
                    }
                }
            });
            direct_reg_receipt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if (hasFocus) {
                        man_image.setVisibility(View.GONE);
                    } else {
                        man_image.setVisibility(View.VISIBLE);
                    }
                }
            });
        }else{
            //
            man_image.setVisibility(View.GONE);
        }
        //
        controlActionBar();
//        imagePathResult = new ArrayList<>();
        //
        clickInputCompleteBtn();
        editTextEvent();
    }

    private void controlActionBar(){
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("직접 등록");
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void clickInputCompleteBtn(){
        go_sms = (Button) findViewById(R.id.go_sms);
        go_sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(receipt_nums.size() == 0){
                    Toast.makeText(DirectRegActivity.this, "사업자 번호 및 승인 번호를 입력하세요", Toast.LENGTH_SHORT).show();
                }else{
                    Intent intent = new Intent(getApplicationContext(),SmsActivity.class);
                    intent.putExtra("business_nums", (Serializable) business_nums);
                    intent.putExtra("receipt_nums", (Serializable) receipt_nums);
//                    intent.putExtra("imagePathResult", (Serializable) imagePathResult);
                    startActivity(intent);
                }}
        });
        go_sms.setVisibility(Button.GONE);

    }
    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
    //
    private void editTextEvent(){
        direct_reg_btn = (TextView) findViewById(R.id.direct_reg_btn);

        direct_reg_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //입력값
                get_business_num = direct_reg_business.getText().toString();
                get_receipt_num = direct_reg_receipt.getText().toString();
                System.out.println("----- bisinuss num : "+get_business_num);
                System.out.println("----- receipt num : "+get_receipt_num);
                // 사업자번호 조회 : 현재는 무조건 true
// <<<<<<< diddn
                doSearchBisinessNum();

//                if(work_place_presence){
//                    // 승인번호 조회 부분 들어가게 하기 없으면 등록 후 진행
//                    // 있으면 중단 후 toast
//                    if(receipt_nums.contains(get_receipt_num)){
//                        direct_reg_receipt.setText("");
//                        Toast.makeText(DirectRegActivity.this, "존재하는 승인번호입니다", Toast.LENGTH_SHORT).show();
//                    }else{
//                        // 중복검사 서버용
//                        if(get_receipt_num.length() == 8){
//                            duplicateInspection(get_receipt_num);
//                        }else{
//                            Toast.makeText(DirectRegActivity.this, "승인번호는 8자리 입니다.", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                }else{
//                    // toast 입력 초기화하고 해당 사업장이 없습니다.
//                    direct_reg_business.setText("");
//                    Toast.makeText(DirectRegActivity.this, "해당 사업장이 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
//                }
// =======
//                 Boolean work_place_presence = doSearchBisinessNum();
//                 if(work_place_presence){
//                     // 승인번호 조회 부분 들어가게 하기 없으면 등록 후 진행
//                     // 있으면 중단 후 toast
//                     if(receipt_nums.contains(get_receipt_num)){
//                         Toast.makeText(DirectRegActivity.this, "존재하는 승인번호입니다", Toast.LENGTH_SHORT).show();
//                     }else{
//                         // 중복검사 서버용
//                         if(get_receipt_num.length() == 8){
//                             duplicateInspection(get_receipt_num);
//                         }else{
//                             Toast.makeText(DirectRegActivity.this, "승인번호는 8자리 입니다.", Toast.LENGTH_SHORT).show();
//                         }
//                     }
//                 }else{
//                     // toast 입력 초기화하고 해당 사업장이 없습니다.
//                     Toast.makeText(DirectRegActivity.this, "해당 사업장이 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
//                 }
// >>>>>>> underMaster
            }
        });
    }

    private void doSearchBisinessNum(){
        if(get_business_num.length() != 12){
//            return false;
        }else{
            OkHttpClient client = new OkHttpClient();
            RequestBody formBody = new FormBody.Builder()
                    .add("market_num", preferences.getString("market_num", null))
                    .add("businessNum", get_business_num)
                    .build();
            String url = getString(R.string.web_services) + "/duplicate/business/";
//        url = "http:/172.30.1.57/duplicate/business/";
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
            Log.d("","-------------:"+request);

            client.newCall(request).enqueue(businessDuplicateCallback);
//            return true;
        }
    }

    private Callback businessDuplicateCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            Log.d("","-------------:실패"+e);
        }
        @Override
        public void onResponse(Call call, okhttp3.Response response) throws IOException {
            Log.d("","-------------:성공");
            String res = response.body().string();
            System.out.println("------------------------------------------------------ : "+response.body());
            try{
                JSONObject resJson = new JSONObject(res);
                System.out.println("----- : "+resJson);

                if(resJson.getString("message").equals("존재하는 상점이 아닙니다.")){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable(){
                                @Override
                                public void run() {
                                    // 해당 작업을 처리함
                                    Toast.makeText(DirectRegActivity.this, "등록된 사업장이 아닙니다.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).start();
                }else{
                    if(get_receipt_num.length() == 8){
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable(){
                                    @Override
                                    public void run() {
                                        // 해당 작업을 처리함
                                        duplicateInspection(get_receipt_num);
                                    }
                                });
                            }
                        }).start();
                    }else{
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable(){
                                    @Override
                                    public void run() {
                                        // 해당 작업을 처리함
                                        Toast.makeText(DirectRegActivity.this, "승인번호는 8자리 입니다.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).start();
                    }
                }
            } catch(JSONException e){
                System.out.println("jsonException"+e);

            }
        }
    };

    public void duplicateInspection(String receiptNm){
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("receiptNum", receiptNm)
                .build();
        String url = getString(R.string.web_services) + "/duplicate/";
//        url = "http:/172.30.1.57/duplicate/";
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        Log.d("","-------------:"+request);

        client.newCall(request).enqueue(duplicateCallback);
    }

    private Callback duplicateCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            Log.d("","-------------:실패"+e);
        }
        @Override
        public void onResponse(Call call, okhttp3.Response response) throws IOException {
            Log.d("","-------------:성공");
            String res = response.body().string();
            System.out.println("------------------------------------------------------ : "+response.body());
            try{
                JSONObject resJson = new JSONObject(res);
                if(receipt_nums.contains(get_receipt_num)) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable(){
                                @Override
                                public void run() {
                                    // recycleview내의 승인번호 중복검사
                                    Toast.makeText(DirectRegActivity.this, "이미 등록된 승인번호입니다.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).start();
                }
                else if(resJson.getString("message").equals("이미 등록 되어있습니다.")){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable(){
                                @Override
                                public void run() {
                                    // 서버내의 승인번호 중복검사
                                    Toast.makeText(DirectRegActivity.this, "이미 등록된 승인번호입니다.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).start();
                }else{

                    if(receipt_nums.size() >= 10){
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable(){
                                    @Override
                                    public void run() {
                                        // 10개 초과
                                        Toast.makeText(DirectRegActivity.this, "10개까지 등록가능합니다", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }).start();
                    }
                    else {

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable(){
                                @Override
                                public void run() {
                                    // 해당 작업을 처리함
                                    business_nums.add(get_business_num);
                                    receipt_nums.add(get_receipt_num);
//                    imagePathResult.add(imagePath);
                                    delete_btns.add(R.drawable.ic_x_12);
                                    direct_reg_business.setText("");
                                    direct_reg_receipt.setText("");
                                    inputAdapter.notifyDataSetChanged();
                                    go_sms.setVisibility(Button.VISIBLE);
                                }
                            });
                        }
                    }).start();
                }}

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable(){
                            @Override
                            public void run() {
                                reg_layout.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }).start();
            } catch(JSONException e){
                System.out.println("jsonException"+e);

            }
        }
    };
}
