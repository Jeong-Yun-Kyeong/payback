package kr.babylab.receipt;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class IssuanceActivity extends AppCompatActivity {
    private Button issuance_of_gift_certificates;
    //승인번호 데이터
    List<String> business_nums;
    List<String> receipt_nums;
    List<String> imagePathResult;
    List<String> receipt_num_keys;
    //
    private String client_name;
    private String phone_number;
    private EditText gift_card_price;

    private String market_num;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issuance);
        //승인번호 데이터 받기
        business_nums = new ArrayList<>();
        receipt_nums = new ArrayList<>();
        imagePathResult = new ArrayList<>();
        receipt_num_keys = new ArrayList<>();
        Intent get_intent = getIntent();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();
        market_num = preferences.getString("market_num", null);
        
        business_nums  = (ArrayList<String>)get_intent.getSerializableExtra("business_nums");
        receipt_nums  = (ArrayList<String>)get_intent.getSerializableExtra("receipt_nums");
        imagePathResult = (ArrayList<String>)get_intent.getSerializableExtra("imagePathResult");

        client_name = getIntent().getStringExtra("client_name");
        phone_number = getIntent().getStringExtra("phone_number");
            System.out.println("----- 전에 받아온 데이터들 : "+business_nums+", "+receipt_nums+", "+client_name+", "+phone_number);
        //
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("상품권 발행");
        actionBar.setDisplayHomeAsUpEnabled(true);

        gift_card_price = (EditText) findViewById(R.id.gift_card_price);

        issuance_of_gift_certificates = (Button) findViewById(R.id.issuance_of_gift_certificates);
        issuance_of_gift_certificates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //모두 입력 확인
                if(gift_card_price.length() == 0 )
                {
                    Toast.makeText(getApplicationContext(),"금액을 입력해주세요",Toast.LENGTH_SHORT).show();
                }
                else{
                Log.d("","----------------------- : "+ receipt_nums +", " + client_name+", "+phone_number + ", " + gift_card_price.getText());
                setReceipt(receipt_nums, business_nums);
//                Intent intent = new Intent(getApplicationContext(),HomeActivity.class);
//
//                startActivity(intent);
            }}
        });
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    private void setReceipt(List<String> receipt_nums, List<String> business_nums){
        OkHttpClient client = new OkHttpClient();
        Log.d("","!!!!!!!!!!!!!!!!!!!!! receipt_nums : "+receipt_nums.size());

        for(int i=0;i<receipt_nums.size();i++){
            Log.d("","!!!!!!!!!!!!!!!!!!!!! dir_number : "+receipt_nums.get(i));
//            Log.d("","!!!!!!!!!!!!!!!!!!!!! dir_number : "+imagePathResult.get(i));
            //멀티파트폼에서 데이터 추가하게하
            RequestBody formBody;
//            if(imagePathResult.get(i) != null){
//
//
//                Log.d("", "!!!!!!!!!!!!!!!!!!!!! dir_number : " + imagePathResult.get(i));
//                formBody = new MultipartBody.Builder()
//                        .setType(MultipartBody.FORM)
//                        .addFormDataPart("receipt_num", receipt_nums.get(i))
////                        .addFormDataPart("receipt_image", imagePathResult.get(i), RequestBody.create(MultipartBody.FORM, new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/receipt",imagePathResult.get(i))))
//                        .build();
//            }else{
            System.out.println("----- 사업자 번호 : " + business_nums);
                formBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("receipt_num", receipt_nums.get(i))
                        .addFormDataPart("business_num", business_nums.get(i))
                        .build();
//            }


            String url = getString(R.string.web_services) + "/receipt/";
//            url = "http://172.30.1.57/receipt/";
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
            Log.d("","-------------:"+request);

            client.newCall(request).enqueue(receiptCallback);
        }
    }

    private Callback receiptCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
//            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d("","-------------:실패"+e);
        }
        @Override
        public void onResponse(Call call, okhttp3.Response response) throws IOException {
//            Log.d("","-------------: 데이터"+response.body().string());
            try{
                JSONObject resJson = new JSONObject(response.body().string());
                String idx = resJson.getString("idx");
                receipt_num_keys.add(idx);
                Log.d("","@@@@@@@@@@@ 승인번호 다 됬나 확인  : " + (receipt_num_keys.size() +", "+ receipt_nums.size()));
                if (receipt_num_keys.size() == receipt_nums.size()){
                    Log.d("","@@@@@@@@@@@ 승인번호 다 집어넣어서 키값 받음 : " + receipt_num_keys);
                    String receip_num = "";
                    for(int i = 0; i<receipt_num_keys.size();i++){
                        if(i == receipt_num_keys.size()-1){
                            receip_num += receipt_num_keys.get(i);
                        }else{
                            receip_num += receipt_num_keys.get(i) +',';
                        }
                    }
                    System.out.println(receip_num);
                    inputGiftCard(receip_num);
                }
            } catch(JSONException e){
                    System.out.println(e);
            }catch(IOException e){
                System.out.println(e);
            }
        }
    };

    private void inputGiftCard(String receip_num){
        OkHttpClient client = new OkHttpClient();
        //
        RequestBody formBody = new FormBody.Builder()
                .add("receipt_keys", receip_num)
                .add("gift_card_price", gift_card_price.getText().toString())
                .add("client_name", client_name)
                .add("phone_number", phone_number)
                .add("market_num", market_num)
//                .add("writer", receip_num)
                .build();
//        가져온 데이터들과 상품권 금액 추가하기
        //
        String url = getString(R.string.web_services) + "/gift_card/";
//        url = "http://172.30.1.57/gift_card/";
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        Log.d("","-------------:"+request);

        client.newCall(request).enqueue(giftCardCallback);
    }

    private Callback giftCardCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
//            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d("","-------------:실패"+e);
        }
        @Override
        public void onResponse(Call call, okhttp3.Response response) throws IOException {
//            Log.d("","-------------: 데이터"+response.body().string());
            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(intent);
        }
    };
}