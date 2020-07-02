package kr.babylab.receipt;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Handler;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ListActivity extends AppCompatActivity {

    private ImageView list_search_cancel_button;
    private int listPositino;

    private ListContactAdapter listcontactAdapter;
    ProgressBar progressBar1;
    //서버데이터 받아올 변
    private List<ListContact> listContacts;
    private List<String> idx;
    private List<String> reg_date;
    private List<String> client_name;
    private List<String> phone_number;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("영수증 조회");
        actionBar.setDisplayHomeAsUpEnabled(true);
        //
        listContacts = new ArrayList<>();
//        progressBar1 = findViewById(R.id.progressBar1);
//        progressBar1.setVisibility(View.VISIBLE);
        //data
        idx = new ArrayList<>();
        reg_date = new ArrayList<>();
        client_name = new ArrayList<>();
        phone_number = new ArrayList<>();
        //
//        cancel_button = (ImageView) findViewById(R.id.cancel_button);
//        cancel_button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getApplicationContext(), InDirectRegDialog.class);
//                startActivity(intent);
//            }
//        });

        getList();

    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.search, menu) ;
//
//        return true ;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.search_action_btn:
//                showListSearchDialog();
//                return true;
//
//
//            default:
//                // If we got here, the user's action was not recognized.
//                // Invoke the superclass to handle it.
//                return super.onOptionsItemSelected(item);
//
//        }
//    }

    private void getList(){
        OkHttpClient client = new OkHttpClient();
        String url = getString(R.string.web_services) + "/gift_card/";
//        url = "http://172.30.1.57/gift_card/";
        Request request = new Request.Builder()
                .url(url)
                .build();
//        Log.d("","-------------:"+request);

        client.newCall(request).enqueue(giftCardCallBack);


    }
    private Callback giftCardCallBack = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
//            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d("","-------------:실패"+e);
        }
        @Override
        public void onResponse(Call call, Response response) throws IOException {

            try{
                //서버에서 데이터 받아오
                JSONObject resJson = new JSONObject(response.body().string());
                JSONArray jsonArray = resJson.getJSONArray("data");

          Log.d("","—————————————  "+jsonArray);

                for (listPositino=0 ; listPositino <= jsonArray.length(); listPositino++){
                    ListContact listContact = new ListContact();
                    String date = jsonArray.getJSONObject(listPositino).getString("reg_date").split("T")[1].split("Z")[0];
                    String regEx = "(\\d{3})(\\d{3,4})(\\d{4})";
                    listContact.setIdx(jsonArray.getJSONObject(listPositino).getString("idx"));
                    listContact.setDate(date.split(":")[0]+":"+date.split(":")[1]);
                    listContact.setName(jsonArray.getJSONObject(listPositino).getString("client_name"));
                    listContact.setPhone(jsonArray.getJSONObject(listPositino).getString("phone_number").replaceAll(regEx, "$1-$2-$3"));
                    listContacts.add(listContact);
                    Log.d("","————————%%%%—  "+ listContact);
                }
//                reg_listResult.setAdapter(listadapter);
            } catch (JSONException e){
//
            }
                new Thread(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run() {
                            // 해당 작업을 처리함
                            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.reg_listResult);
                            recyclerView.setLayoutManager(new LinearLayoutManager(ListActivity.this));
                            listcontactAdapter = new ListContactAdapter(recyclerView, listContacts, ListActivity.this);
                            recyclerView.setAdapter(listcontactAdapter);
                            //list 추가하기 이벤트
//                            listcontactAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
//                                @Override
//                                public void onLoadMore() {
//                                    if (listContacts.size() <= 200) {
//                                        listContacts.add(null);
//                                        listcontactAdapter.notifyItemInserted(listContacts.size() - 1);
//                                        new Handler().postDelayed(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                listContacts.remove(listContacts.size() - 1);
//                                                listcontactAdapter.notifyItemRemoved(listContacts.size());
//
//                                                //Generating more data
//                                                int index = listContacts.size();
//                                                int end = index + 40;
//                                                for (int i = index; i < end; i++) {
//                                                    ListContact listContact = new ListContact();
//                                                    listContact.setDate("13:00");
//                                                    listContact.setName("정윤경");
//                                                    listContact.setPhone("010-5068-0738");
//                                                    listContacts.add(listContact);
//                                                }
//                                                listcontactAdapter.notifyDataSetChanged();
//                                                listcontactAdapter.setLoaded();
//                                            }
//                                        },300);
//                                    } else {
//                                        Toast.makeText(ListActivity.this, "Loading data completed", Toast.LENGTH_SHORT).show();
//                                    }
//                                }
//                            });
                        }
                    });
                }
            }).start();

        }


    };

    private void showListSearchDialog() {
        LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout dialog_list_search = (RelativeLayout) vi.inflate(R.layout.dialog_list_search, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialog_list_search);
        final AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        ImageView list_search_cancel_button = (ImageView)dialog_list_search.findViewById(R.id.list_search_cancel_button);
        list_search_cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();


    }
}