package kr.babylab.receipt;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.googlecode.tesseract.android.TessBaseAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;



public class CameraActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private LinearLayout in_direct_reg;
    //
    private static final String TAG = "android_camera_example";
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int CAMERA_FACING = Camera.CameraInfo.CAMERA_FACING_BACK; // Camera.CameraInfo.CAMERA_FACING_FRONT
    //    private OcrTExt OCRTextView;
//    private OcrTExt OCRresult;
    private SurfaceView surfaceView;
    private CameraPreview mCameraPreview;
    private View mLayout;  // Snackbar 사용하기 위해서는 View가 필요합니다.
    // (참고로 Toast에서는 Context가 필요했습니다.)

    private TessBaseAPI tess;
    private String dataPath="";
    static String OCRTestData;
    //직접입력
    private TextView receipt_num_reg_btn;
    //private LinearLayout direct_reg_btn_layoout;
    //승인번호 및 사업자번호
     public static EditText business_num_input;
     public static EditText receipt_num_input;
    //
    private static int slideHeight = 0;

    private List<String> imagePathResult;

    public static String imagePath;
    //RecycleView
    private InputAdapter inputAdapter;
    private RecyclerView dataList;
    private List<String> business_nums;
    private List<String> receipt_nums;
    private List<Integer> images;
    //Head
    private String get_receipt_num;
    private String get_business_num;
    //입력 완료버튼
    private Button go_sms;
    //
    public static float staticPx;
    //
    private LinearLayout camera_slide_list;

    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //
        float dip = 50f;
        Resources r = getResources();
        float px = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip,
                r.getDisplayMetrics()
        );
        staticPx = px;
        //
        // 키보드 내릴때 사용하는 상수
        final InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//        int dpi = displayMetrics.densityDpi;
//
//        staticPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpi, this.getResources().getDisplayMetrics());

        // 화면 켜진 상태를 유지합니다.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_camera);
        //
        camera_slide_list = (LinearLayout) findViewById(R.id.camera_slide_list);
        camera_slide_list.setVisibility(View.GONE);
        //
//        direct_reg_btn_layoout.setVisibility(Button.INVISIBLE);
        //데이터 경로
        dataPath = getFilesDir()+"/tesseract/";
        //한글 & 영어 데이터 체크 인데 한글만
        checkFile(new File(dataPath + "tessdata/"),"kor");
        //문자 인식을 수행할 tess객체 생성
        String lang = "kor";
        tess = new TessBaseAPI();
        tess.init(dataPath,lang);
        //
        business_num_input = (EditText) findViewById(R.id.business_num_input);
        receipt_num_input = (EditText) findViewById(R.id.receipt_num_input);
        //recycleview
        dataList = findViewById(R.id.dataList);
        business_nums = new ArrayList<>();
        receipt_nums = new ArrayList<>();
        images = new ArrayList<>();
        imagePathResult = new ArrayList<>();

        inputAdapter = new InputAdapter(this,business_nums,receipt_nums,images);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,1,GridLayoutManager.VERTICAL,false);
        dataList.setLayoutManager(gridLayoutManager);
        dataList.setAdapter(inputAdapter);
        //
        actionBar = getSupportActionBar();
//        actionBar.setTitle(Html.fromHtml("<font color='#000000''>영수증 등록하기</font>"));
        actionBar.setTitle("영수증 등록하기");
        actionBar.setDisplayHomeAsUpEnabled(true);
//        actionBar.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        //
        mLayout = findViewById(R.id.camera_layout);
        surfaceView = findViewById(R.id.camera_preview_main);
        // 런타임 퍼미션 완료될때 까지 화면에서 보이지 않게 해야합니다.
        surfaceView.setVisibility(View.GONE);
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
            int writeExternalStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if ( cameraPermission == PackageManager.PERMISSION_GRANTED
                    && writeExternalStoragePermission == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            }else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {
                    Snackbar.make(mLayout, "이 앱을 실행하려면 카메라와 외부 저장소 접근 권한이 필요합니다.",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            ActivityCompat.requestPermissions( CameraActivity.this, REQUIRED_PERMISSIONS,
                                    PERMISSIONS_REQUEST_CODE);
                        }
                    }).show();
                } else {
                    // 2. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                    // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                    ActivityCompat.requestPermissions( this, REQUIRED_PERMISSIONS,
                            PERMISSIONS_REQUEST_CODE);
                }
            }
        } else {
            final Snackbar snackbar = Snackbar.make(mLayout, "디바이스가 카메라를 지원하지 않습니다.",
                    Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("확인", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackbar.dismiss();
                }
            });
            snackbar.show();
        }
        //
        // x버튼 선택
        ImageView camera_dialog_cancel_button = (ImageView) findViewById(R.id.camera_dialog_cancel_button);
        camera_dialog_cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 카메라 멈춤 필요
//                mCameraPreview.startCameraPreview();
//                showInDirectRegDialog();
                //초점
                business_num_input.setText("");
                receipt_num_input.setText("");
                // 하단에서 레이어 올라오게 하기
//                LinearLayout camera_slide_list = (LinearLayout) findViewById(R.id.camera_slide_list);
////                camera_slide_list.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
//                slideHeight=0;
                imm.hideSoftInputFromWindow(receipt_num_input.getWindowToken(), 0);
//                camera_slide_list.animate().translationY(slideHeight);
//                actionBar.show();
                camera_slide_list.setVisibility(View.GONE);


            }
        });
        // 추가하기버튼 선택
        LinearLayout reg_add_btn = (LinearLayout) findViewById(R.id.additional_shooting);
        reg_add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 카메라 멈춤 필요
//                mCameraPreview.startCamera가Preview();
//                showInDirectRegDialog();
                business_num_input.setText("");
                // 하단에서 레이어 올라오게 하기
                receipt_num_input.setText("");
//                LinearLayout camera_slide_list = (LinearLayout) findViewById(R.id.camera_slide_list);
////                camera_slide_list.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
//                slideHeight=0;
                imm.hideSoftInputFromWindow(receipt_num_input.getWindowToken(), 0);
//                camera_slide_list.animate().translationY(slideHeight);
//                actionBar.show();
                camera_slide_list.setVisibility(View.GONE);

            }
        });
        // 직접등록 버튼 선택
        in_direct_reg = (LinearLayout) findViewById(R.id.in_direct_reg);
        in_direct_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 카메라 멈춤 필요
//                mCameraPreview.stopCameraPreview();
//                if(dir_numbers == null){
//                    direct_reg_btn_layoout.setVisibility(Button.INVISIBLE);
//
//                }else{
//                    direct_reg_btn_layoout.setVisibility(Button.VISIBLE);
//                }
//                showInDirectRegDialog();
                // 하단에서 레이어 올라오게 하기
//                LinearLayout camera_slide_list = (LinearLayout) findViewById(R.id.camera_slide_list);
////                camera_slide_list.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
//
//                slideHeight = camera_slide_list.getHeight();
//                camera_slide_list.animate().setDuration(300).translationY(-slideHeight);
//                actionBar.hide();
                camera_slide_list.setVisibility(View.VISIBLE);
                //
            }
        });

        // 빈화면 클릭시 키보드 내리
        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("ooooooooooooook2");
                imm.hideSoftInputFromWindow(receipt_num_input.getWindowToken(), 0);
            }});

        //완료버튼
        go_sms = (Button) findViewById(R.id.input_complete);
        go_sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(receipt_nums.size()>=1){
                    Intent intent = new Intent(getApplicationContext(),SmsActivity.class);
                    intent.putExtra("business_nums", (Serializable) business_nums);
                    intent.putExtra("receipt_nums", (Serializable) receipt_nums);
                    intent.putExtra("imagePathResult", (Serializable) imagePathResult);
                    System.out.println(imagePathResult);
                    startActivity(intent);}
                else{
                    Toast.makeText(CameraActivity.this, "승인번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                }}
        });
        // 카메라 캡처 버튼 선택
        Button button = findViewById(R.id.camera_capture_btn);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mCameraPreview.takePicture(tess,dataPath);
//              // 카메라 멈춤
//                mCameraPreview.stopCameraPreview();
//                //새로운 뷰
//                LinearLayout camera_slide_list = (LinearLayout) findViewById(R.id.camera_slide_list);
//                slideHeight = camera_slide_list.getHeight();
//                camera_slide_list.animate().setDuration(300).translationY(-slideHeight);
//                actionBar.hide();
                camera_slide_list.setVisibility(View.VISIBLE);
                //
            }
        });
        //
//        receipt_num_reg_btn.setTextColor(getResources().getColor(R.color.cornflower));
        receipt_num_reg_btn = (TextView) findViewById(R.id.receipt_num_reg_btn);
        receipt_num_reg_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //입력값
                get_business_num = business_num_input.getText().toString();
                get_receipt_num = receipt_num_input.getText().toString();
                //
                System.out.println("----- bisinuss num : "+get_business_num);
                System.out.println("----- receipt num : "+get_receipt_num);
                doSearchBisinessNum();
            }
        });

    }

    private void doSearchBisinessNum(){
        if(get_business_num.length() != 12){
//            return false;
        }else{
            OkHttpClient client = new OkHttpClient();
            RequestBody formBody = new FormBody.Builder()
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
            //수정
//            return true;
        }
        //        if(get_business_num.length() != 12){
//            return false;
//        }else{
//            return true;
//        }
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
                                    Toast.makeText(CameraActivity.this, "등록된 사업장이 아닙니다.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).start();
                }else{
                    if(get_receipt_num.length() == 8 || get_receipt_num.length() == 9){
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
                                        Toast.makeText(CameraActivity.this, "승인번호는 8자리 입니다.", Toast.LENGTH_SHORT).show();
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

    public static float getPx(){
        float px = staticPx;
        return px;
    }


    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    void startCamera(){
        mCameraPreview = new CameraPreview(this, this, CAMERA_FACING, surfaceView );

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grandResults) {
        if ( requestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {
            boolean check_result = true;
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }
            if ( check_result ) {

                startCamera();

            }
            else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            finish();
                        }
                    }).show();
                }else {
                    Snackbar.make(mLayout, "설정(앱 정보)에서 퍼미션을 허용해야 합니다. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            finish();
                        }
                    }).show();
                }
            }
        }
    }
    //
    private void checkFile(File dir, String lang){
        //directory does not exist, but we can successfully create it
        if(!dir.exists()&&dir.mkdirs()){
            copyFiles(lang);
        }
        //The directory exists, but there is no data file in it
        if(dir.exists()){
            String datafilePath = dataPath + "/tessdata/" + lang + ".traineddata";
            File datafile = new File(datafilePath);
            if(!datafile.exists()){
                copyFiles(lang);
            }
        }
    }

    private void copyFiles(String lang){
        try{
            // location we want the file to be at
            String filepath = dataPath + "/tessdata/" + lang + ".traineddata";

            //get access go AssetManager
            AssetManager assetManager = getAssets();

            //open byte streams for reading/writing
            InputStream inStream = assetManager.open("tessdata/"+lang+".traineddata");
            OutputStream outStream = new FileOutputStream(filepath);

            //copy the file to the location specified by filepath
            byte[] buffer = new byte[1024];
            int read;
            while((read = inStream.read(buffer))!= -1){
                outStream.write(buffer,0,read);
            }
            outStream.flush();
            outStream.close();
            inStream.close();

        }catch(FileNotFoundException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public void duplicateInspection(String receiptNm){
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("receiptNum", receiptNm)
                .build();
        String url = getString(R.string.web_services) + "/duplicate/";
//        url = "http:/172.30.1.57/duplicate/";
        Request request = new Request.Builder()
//                .addHeader("x-api-key", RestTestCommon.API_KEY)
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
                                    Toast.makeText(CameraActivity.this, "이미 등록된 승인번호입니다.", Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(CameraActivity.this, "이미 등록된 승인번호입니다.", Toast.LENGTH_SHORT).show();
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
                                        Toast.makeText(CameraActivity.this, "10개까지 등록가능합니다", Toast.LENGTH_LONG).show();
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
                                        images.add(R.drawable.ic_x_12);
                                        business_num_input.setText("");
                                        receipt_num_input.setText("");
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
                                dataList.setVisibility(View.VISIBLE);
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
