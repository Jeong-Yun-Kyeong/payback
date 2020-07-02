package kr.babylab.receipt;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.util.Log;
import android.widget.TextView;
import android.view.View;
import android.widget.ProgressBar;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ListDetailActivity extends AppCompatActivity {
    private String idx;
    //RecycleView
    RecyclerView dataList;
    List<String> business_nums;
    List<String> receipt_nums;
    List<Integer> images;
    InputAdapter inputAdapter;
//    Adapter adapter;
    //
    ProgressBar progressBar;
    private static final int PAGE_START = 1;
    //SwipeRefreshLayout swipeRefreshLayout;
    //
    private boolean isLoading = false;
    private boolean isLastPage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("영수증 상세");
        actionBar.setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_list_detail);
//        progressBar = findViewById(R.id.main_progress);
//        progressBar.setVisibility(View.VISIBLE);


//        final ProgressDialog progressDialog = new ProgressDialog(ListDetailActivity.this);
////        progressDialog.setMessage("Loading..."); // Setting Message
////        progressDialog.setTitle("ProgressDialog"); // Setting Title
//        progressDialog.setProgressStyle(R.id.cp_pbar); // Progress Dialog Style Spinner
//        progressDialog.show(); // Display Progress Dialog
//        progressDialog.setCancelable(false);
//        new Thread(new Runnable() {
//            public void run() {
//                try {
//                    Thread.sleep(10000);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                progressDialog.dismiss();
//            }
//        }).start();

        //recycleview
        dataList = findViewById(R.id.dataList);
        business_nums = new ArrayList<>();
        receipt_nums = new ArrayList<>();
        images = new ArrayList<>();
        inputAdapter = new InputAdapter(this,business_nums,receipt_nums,images);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,1,GridLayoutManager.VERTICAL,false);
        dataList.setLayoutManager(gridLayoutManager);
        dataList.setAdapter(inputAdapter);
        //
        idx  = (String) getIntent().getStringExtra("idx");
        System.out.println(idx);
        //
        getDetailData();
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    public void getDetailData(){
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("idx", idx)
                .build();
        String url = getString(R.string.web_services) + "/gift_card/";
//        url = "http://172.30.1.57/gift_card/";
        Request request = new Request.Builder()
//                .addHeader("x-api-key", RestTestCommon.API_KEY)
                .url(url)
                .post(formBody)
                .build();
        Log.d("","-------------:"+request);

        client.newCall(request).enqueue(detailCallback);
    }

    private Callback detailCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
//            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d("","-------------:실패"+e);
        }
        @Override
        public void onResponse(Call call, okhttp3.Response response) throws IOException {
            Log.d("","-------------:성공");

//            Log.d("","-----------------------: "+response.body().string());
            final okhttp3.Response resp = response;
//            final JSONObject resJson = null;
//                //
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable(){
                            @Override
                            public void run() {
                                // 해당 작업을 처리함
                                try{
                                    JSONObject resJson = new JSONObject(resp.body().string());
                                    Log.d("", "-------------:data : " + resJson);
                                    TextView reg_date = (TextView) findViewById(R.id.reg_date);
                                    reg_date.setText(resJson.getString("reg_date").split("T")[0]+" "+resJson.getString("reg_date").split("T")[1].split("Z")[0]);
                                    TextView client_name = (TextView) findViewById(R.id.client_name);
                                    client_name.setText(resJson.getString("client_name"));
                                    TextView phone_number = (TextView) findViewById(R.id.phone_number);
                                    phone_number.setText(resJson.getString("phone_number"));
                                    TextView gift_card_price = (TextView) findViewById(R.id.gift_card_price);
                                    gift_card_price.setText(resJson.getString("gift_card_price"));
                                    //
                                    String receipt_keys = resJson.getString("receipt_keys");
                                    System.out.println("======================= : "+receipt_keys.split(","));
                                    String[] receipt_keys_array = (String[]) receipt_keys.split(",");
                                    System.out.println("======================= : "+receipt_keys_array);
                                    for( int i=0;i<receipt_keys_array.length;i++){
                                        System.out.println("======================= : "+receipt_keys_array[i]);
                                        getReceipt(receipt_keys_array[i]);
                                    }

                                }catch(JSONException e){

                                }catch(IOException e){

                                }catch(NetworkOnMainThreadException e){

                                }


                            }
                        });
                    }
                }).start();

////

        }
    };

    public void getReceipt(String receipt_key){
        System.out.println("//////////////////////////////: " +receipt_key);
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("idx", receipt_key)
                .build();
        String url = getString(R.string.web_services) + "/receipt/";
//        url = "http://172.30.1.57/receipt/";
        Request request = new Request.Builder()
//                .addHeader("x-api-key", RestTestCommon.API_KEY)
                .url(url)
                .post(formBody)
                .build();
        Log.d("","-------------:"+request);

        client.newCall(request).enqueue(receiptCallback);
    }
    private void doRefresh() {
      //  progressBar.setVisibility(View.VISIBLE);
        // TODO: Check if data is stale.
        //  Execute network request if cache is expired; otherwise do not update data.
        progressBar.setVisibility(View.GONE);
        inputAdapter.get(receipt_nums).clear();
        inputAdapter.notifyDataSetChanged();
        dataList.setAdapter(inputAdapter);

//        loadFirstPage();
//     //   swipeRefreshLayout.setRefreshing(false);
//    }
//    private void loadFirstPage() {
//
//               // List<string> dir_numbers = fetchResults(response);
//
//                adapter.add(dir_numbers);
//
//
            }


    private Callback receiptCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
//            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d("","-------------:실패"+e);
        }
        @Override
        public void onResponse(Call call, okhttp3.Response response) throws IOException {
            Log.d("","-------------:성공");
            final okhttp3.Response resp = response;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run() {
                            // 해당 작업을 처리함
                            try{
//                                dataList.setVisibility(View.GONE);
//                                System.out.println("-------------------// : // " + resp.body().string());
                                JSONObject resJson = new JSONObject(resp.body().string());
                                Log.d("", "-------------:data : " + resJson.getString("business_num"));
                                business_nums.add(resJson.getString("business_num"));
                                receipt_nums.add(resJson.getString("receipt_num"));
                                //progressBar.setVisibility(View.GONE);
                                images.add(R.drawable.detail_back);
                                dataList.setAdapter(inputAdapter);

//                                System.out.println("갑삽ㄱㅂ삽가"+dir_numbers);
//                                //loading
                                inputAdapter.notifyDataSetChanged();

//                                progressBar.setVisibility(View.GONE);
//                                dataList.setVisibility(View.VISIBLE);
//
//                                //doRefresh();
//                                TextView client_name = (TextView) findViewById(R.id.client_name);
//                                client_name.setText(resJson.getString("receipt_num"));
                            }catch(JSONException e){
                                Log.d("", "-------------:data : " + e);
                            }catch(IOException e){
                                Log.d("", "-------------:data : " + e);
                            }catch(NetworkOnMainThreadException e){
                                Log.d("", "-------------:data : " + e);
                            }


                        }
                    });
                }
            }).start();
        }
    };
}
