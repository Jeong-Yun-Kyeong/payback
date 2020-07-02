package kr.babylab.receipt;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.media.ImageReader;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.googlecode.tesseract.android.TessBaseAPI;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import static android.hardware.Camera.Parameters.FLASH_MODE_OFF;
import static android.hardware.Camera.Parameters.FOCUS_MODE_AUTO;
import static android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
import static kr.babylab.receipt.CameraActivity.business_num_input;
import static kr.babylab.receipt.CameraActivity.imagePath;
import static kr.babylab.receipt.CameraActivity.receipt_num_input;


// 카메라에서 가져온 영상을 보여주는 카메라 프리뷰 클래스
class CameraPreview extends ViewGroup implements SurfaceHolder.Callback {
 //   private final String TAG = "CameraPreview";
    private int mCameraID;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Camera.CameraInfo mCameraInfo;
    private int mDisplayOrientation;
    private List<Size> mSupportedPreviewSizes;
    private Size mPreviewSize;
    private boolean isPreview = false;

    private AppCompatActivity mActivity;
    //
    private TessBaseAPI tess;
    private String dataPath;

    public CameraPreview(Context context, AppCompatActivity activity, int cameraID, SurfaceView surfaceView) {
        super(context);
        Log.d("@@@", "Preview");

        mActivity = activity;
        mCameraID = cameraID;
        mSurfaceView = surfaceView;

        mSurfaceView.setVisibility(View.VISIBLE);
        // SurfaceHolder.Callback를 등록하여 surface의 생성 및 해제 시점을 감지
        mHolder = mSurfaceView.getHolder();
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mHolder.addCallback(this);
    }
    //
    private static final String TAG = "AndroidCameraApi";
    private Button takePictureButton;
    private TextureView textureView;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private String cameraId;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest captureRequest;
    protected CaptureRequest.Builder captureRequestBuilder;
    private android.util.Size imageDimension;
    private ImageReader imageReader;
    private File file;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private boolean mFlashSupported;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 사이즈
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);
        // 적용
        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed && getChildCount() > 0) {
            final View child = getChildAt(0);

            final int width = r - l;
            final int height = b - t;

            int previewWidth = width;
            int previewHeight = height;
            if (mPreviewSize != null) {
                previewWidth = mPreviewSize.width;
                previewHeight = mPreviewSize.height;
            }

            // Center the child SurfaceView within the parent.
            if (width * previewHeight > height * previewWidth) {
                final int scaledChildWidth = previewWidth * height / previewHeight;
                child.layout((width - scaledChildWidth) / 2, 0,
                        (width + scaledChildWidth) / 2, height);
            } else {
                final int scaledChildHeight = previewHeight * width / previewWidth;
                child.layout(0, (height - scaledChildHeight) / 2,
                        width, (height + scaledChildHeight) / 2);
            }
        }
    }
    // Surface가 생성되었을 때 어디에 화면에 프리뷰를 출력할지 알려줘야 한다.
    public void surfaceCreated(SurfaceHolder holder) {

        // Open an instance of the camera
        try {
            mCamera = Camera.open(mCameraID); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            Log.e(TAG, "Camera " + mCameraID + " is not available: " + e.getMessage());
        }

        // retrieve camera's info.
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraID, cameraInfo);

        mCameraInfo = cameraInfo;
        mDisplayOrientation = mActivity.getWindowManager().getDefaultDisplay().getRotation();

        int orientation = calculatePreviewOrientation(mCameraInfo, mDisplayOrientation);
        mCamera.setDisplayOrientation(orientation);

        mSupportedPreviewSizes =  mCamera.getParameters().getSupportedPreviewSizes();
        requestLayout();

        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            //
//            mCamera.autoFocus(null);
//            Camera.Parameters parameters = mCamera.getParameters();
//            List<String> focusModes = parameters.getSupportedFocusModes();
//            if(focusModes.contains(FOCUS_MODE_CONTINUOUS_PICTURE)){
//                parameters.setFocusMode(FOCUS_MODE_CONTINUOUS_PICTURE);
//            } else if(focusModes.contains(FOCUS_MODE_AUTO)){
//                parameters.setFocusMode(FOCUS_MODE_AUTO);
//            }
//            mCamera.setParameters(parameters);
            //
            isPreview = true;
            Log.d(TAG, "Camera preview started.");
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }

    }


    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        // Release the camera for other applications.
        if (mCamera != null) {
            if (isPreview)
                mCamera.stopPreview();
//                mCamera.cancelAutoFocus();
            mCamera.release();
            mCamera = null;
            isPreview = false;
        }

    }


    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }



    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

//        Camera.Parameters parameters = mCamera.getParameters();
//        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
//        mCamera.setParameters(parameters);

        mCamera.startPreview();
        mCamera.autoFocus(null);

        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            Log.d(TAG, "Preview surface does not exist");
            return;
        }


        // stop preview before making changes
        try {
            mCamera.stopPreview();
            Log.d(TAG, "Preview stopped.");
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }

        int orientation = calculatePreviewOrientation(mCameraInfo, mDisplayOrientation);
        mCamera.setDisplayOrientation(orientation);

        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
            mCamera.autoFocus(null);
            Camera.Parameters parameters = mCamera.getParameters();
            List<String> focusModes = parameters.getSupportedFocusModes();
            if(focusModes.contains(FOCUS_MODE_CONTINUOUS_PICTURE)){
                parameters.setFocusMode(FOCUS_MODE_CONTINUOUS_PICTURE);
            } else if(focusModes.contains(FOCUS_MODE_AUTO)){
                parameters.setFocusMode(FOCUS_MODE_AUTO);
            }
            mCamera.setParameters(parameters);
        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }

    }



    /**
     * 안드로이드 디바이스 방향에 맞는 카메라 프리뷰를 화면에 보여주기 위해 계산합니다.
     */
    public static int calculatePreviewOrientation(Camera.CameraInfo info, int rotation) {
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        return result;
    }


//    public void takePicture(TessBaseAPI tessData, String dataPathData){
//        tess = tessData;
//        dataPath = dataPathData;
//        mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
//    }

    public void stopCameraPreview(){
        mCamera.stopPreview();
//        mCamera.cancelAutoFocus();
//        Camera.Parameters parameters = mCamera.getParameters();
//        List<String> focusModes = parameters.getSupportedFocusModes();
//        focusModes.contains(FLASH_MODE_OFF);


    }
    public void startCameraPreview(){
        mCamera.startPreview();
//        mCamera.autoFocus(null);
//        Camera.Parameters parameters = mCamera.getParameters();
//        List<String> focusModes = parameters.getSupportedFocusModes();
//        if(focusModes.contains(FOCUS_MODE_CONTINUOUS_PICTURE)){
//            parameters.setFocusMode(FOCUS_MODE_CONTINUOUS_PICTURE);
//        } else if(focusModes.contains(FOCUS_MODE_AUTO)){
//            parameters.setFocusMode(FOCUS_MODE_AUTO);
//        }
//        mCamera.setParameters(parameters);
        final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(CameraDevice camera) {
                //This is called when the camera is open
                Log.e(TAG, "onOpened");
                cameraDevice = camera;
                createCameraPreview();
            }
            protected void createCameraPreview() {
                try {
                    SurfaceTexture texture = textureView.getSurfaceTexture();
                    assert texture != null;
                    texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
                    Surface surface = new Surface(texture);
                    captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                    captureRequestBuilder.addTarget(surface);
                    cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            //The camera is already closed
                            if (null == cameraDevice) {
                                return;
                            }
                            // When the session is ready, we start displaying the preview.
                            cameraCaptureSessions = cameraCaptureSession;
                            updatePreview();
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                         //   Toast.makeText(CameraPreview.this, "Configuration change", Toast.LENGTH_SHORT).show();
                        }
                    }, null);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onDisconnected(CameraDevice camera) {
                cameraDevice.close();
            }

            @Override
            public void onError(CameraDevice camera, int error) {
                cameraDevice.close();
                cameraDevice = null;
            }
        };
    }
    protected void updatePreview() {
        if (null == cameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    public void takePicture(TessBaseAPI tessData, String dataPathData){
        tess = tessData;
        dataPath = dataPathData;
        mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
    }
    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {

        public void onShutter() {
        }
    };

    Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
        }
    };

    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {

            //이미지의 너비와 높이 결정
            float w;
            float h;
            float imageWidth = camera.getParameters().getPictureSize().width;
            float imageHeight = camera.getParameters().getPictureSize().height;
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            float screenWidth = metrics.widthPixels;
            float screenHeight = metrics.heightPixels;
            System.out.println(imageWidth+", "+imageHeight);
            if(imageWidth > imageHeight){
                w = 1024;
                float val = imageWidth/1024;
                System.out.println(val);
                h = imageHeight/val;
            }else{
                h=1024;
                float val = imageHeight/1024;
                System.out.println(val);
                w = imageWidth/val;
            }
            System.out.println(w+", "+h);



            int orientation = calculatePreviewOrientation(mCameraInfo, mDisplayOrientation);

            //byte array를 bitmap으로 변환
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeByteArray( data, 0, data.length, options);

            //이미지를 디바이스 방향으로 회전
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);
            bitmap =  Bitmap.createBitmap(bitmap, 0, 0, (int)imageWidth, (int)imageHeight, matrix, true);
            bitmap = Bitmap.createScaledBitmap(bitmap,(int)(h), (int)(w), true);
            //bitmap을 byte array로 변환
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] currentData = stream.toByteArray();





            //파일로 저장
            new SaveImageTask().execute(currentData);

        }
    };

    private class SaveImageTask extends AsyncTask<byte[], Void, Void> {

        @Override
        protected Void doInBackground(byte[]... data) {
            FileOutputStream outStream = null;

            Log.d("","-------------: 저장소");
            try {

                File path = new File (Environment.getExternalStorageDirectory().getAbsolutePath() + "/receipt");
                if (!path.exists()) {
                    path.mkdirs();
                }

                String fileName = String.format("%d.jpg", System.currentTimeMillis());
                File outputFile = new File(path, fileName);

                System.out.println(path);
                outStream = new FileOutputStream(outputFile);
                outStream.write(data[0]);
                outStream.flush();
                outStream.close();
                mCamera.startPreview();
                //dp,px
                float px = CameraActivity.getPx();
                Log.d("","@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ : "+ px);
                //
                imagePath = fileName;
                //
                // todo:
//                final int PIC_CROP = 3;
//                Uri uri = Uri.fromFile(outputFile);
//                Intent intent = new Intent("com.android.camera.action.CROP");
//                intent.setDataAndType(uri,"%d.jpg");
//                intent.putExtra("aspectX",1);
//                intent.putExtra("aspecty",1);
//                intent.putExtra("outputX",400);
//                intent.putExtra("outputY",200);
//                intent.putExtra("scale",true);
//                intent.putExtra("return-data",true);
//                mActivity.startActivityForResult(intent,PIC_CROP);
//

                //todo:


                OkHttpClient client = new OkHttpClient();
                RequestBody formBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("image",fileName, RequestBody.create(MultipartBody.FORM, new File(path,fileName)))
                        .build();

                String url = getResources().getString(R.string.web_services) + "/ocr/";
//                url = "http://172.30.1.57/ocr/";

                Request request = new Request.Builder()
                        .url(url)
                        .post(formBody)
                        .build();
                Log.d("","-------------:"+request);
                client.newCall(request).enqueue(ocrCallBack);

                try {
                    mCamera.setPreviewDisplay(mHolder);
                    mCamera.startPreview();
                    Log.d(TAG, "Camera preview started.");
                } catch (Exception e) {
                    Log.d(TAG, "Error starting camera preview: " + e.getMessage());
                }


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    private Callback ocrCallBack = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
//            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d("","-------------:실패 "+e);
        }
        @Override
        public void onResponse(Call call, okhttp3.Response response) throws IOException {
            Log.d("","-------------:성공 "+ response);
            // todo: 받았어도 오류인경우 처리 필요
            //Log.d("",""+response.body().string());
            try {
                JSONObject resJson = new JSONObject(response.body().string());
                Log.d(TAG,"-------------------------- : "+resJson);
                final String receipt_num = resJson.getString("receipt_num");
                final String business_num = resJson.getString("com_num");
                final String business_num2 = resJson.getString("com_num2");
                Log.d(TAG,"-------------------------- : "+receipt_num +", " + business_num +", "+ business_num2);
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(receipt_num.equals("")){

                        }else{
                            System.out.println("----- receipt num");
                            receipt_num_input.setText(receipt_num);
                        }
                        if(business_num.equals("")){
                            if(business_num2.equals("")){

                            }else{
                                System.out.println("----- business num 2");
                                business_num_input.setText(business_num2);
                            }
                        }else{
                            System.out.println("—— business num");
                            business_num_input.setText(business_num);
                        }
                    }
                });
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        if(receipt_num.equals("")){
//
//                        }else{
//                            System.out.println("----- receipt num");
//                            receipt_num_input.setText(receipt_num);
//                        }
//
//                        if(business_num.equals("")){
//                            if(business_num2.equals("")){
//
//                            }else{
//                                System.out.println("----- business num 2 : " + business_num2);
//
//                                business_num_input.setText(business_num2);
//                            }
//                        }else{
//                            System.out.println("----- business num");
//                            business_num_input.setText(business_num);
//                        }
//                    }
//                }, 0);

//                    }
//                });
            }catch (JSONException e){

            }
        }
    };
}