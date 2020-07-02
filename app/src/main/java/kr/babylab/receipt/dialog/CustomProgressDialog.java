//package kr.babylab.receipt.dialog;
//
//import android.app.ProgressDialog;
//import android.content.Context;
//import android.os.Build;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import kr.babylab.receipt.R;
//
//public abstract class BaseActivity extends AppCompatActivity {
//
//    protected ProgressDialog mProgressDialog = null;
//
//    protected abstract Context getContext();
//
//    /**
//     * 프로그래스를 보여준다.
//     */
//    public void showProgressDialog() {
//        if (mProgressDialog == null) {
//            if (Build.VERSION_CODES.KITKAT < Build.VERSION.SDK_INT) {
//
////              R.style.ProgressDialogStyle은 커스텀으로 정의한 스타일임
//                mProgressDialog = new ProgressDialog(getNowContext(), R.style.ProgressDialogStyle);
//
//            } else {
//                mProgressDialog = new ProgressDialog(getNowContext());
//            }
//            mProgressDialog.setMessage(getString(R.string.progress_message));
//            mProgressDialog.setIndeterminate(true);
//            mProgressDialog.setCancelable(false);
//        }
//        mProgressDialog.show();
//    }
//
//    /**
//     * 프로그래스를 숨긴다.
//     */
//    public void hideProgressDialog() {
//        if (mProgressDialog != null && mProgressDialog.isShowing()) {
//            mProgressDialog.dismiss();
//        }
//    }
//
//}
//
