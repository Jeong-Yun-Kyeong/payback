package kr.babylab.receipt.dialog;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Camera;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import kr.babylab.receipt.CameraActivity;
import kr.babylab.receipt.R;
import kr.babylab.receipt.UpdateReceiptNum;


public class CameraListDialog extends AppCompatActivity {


    public EditText ReceiptNum;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_camera_list);
        ReceiptNum = (EditText) findViewById(R.id.receipt_num_input);
        ReceiptNum.setText("test Text");

        EditText receipt_num_input = (EditText) findViewById(R.id.receipt_num_input);
        final TextView receipt_num_reg_btn = (TextView) findViewById(R.id.receipt_num_reg_btn);
        final LinearLayout direct_reg_btn_layoout = (LinearLayout) findViewById(R.id.direct_reg_btn_layoout);
        receipt_num_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) {
                if(s.length() == 8){
                    Log.d("","------------:8자리 만족");
                    receipt_num_reg_btn.setTextColor(getResources().getColor(R.color.cornflower));
                    receipt_num_reg_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //버튼 보이게 하기
                            direct_reg_btn_layoout.setVisibility(LinearLayout.VISIBLE);
                            //
                            Toast.makeText(CameraListDialog.this, ""+s, Toast.LENGTH_SHORT).show();
                            //
                            LinearLayout receipt_num_layout = (LinearLayout) findViewById(R.id.receipt_num_layout);
                            TextView view = new TextView(CameraListDialog.this);
                            view.setText(s);
                            receipt_num_layout.addView(view);
                        }
                    });
                }else{
                    receipt_num_reg_btn.setTextColor(getResources().getColor(R.color.black_38));
                    receipt_num_reg_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //
                            Toast.makeText(CameraListDialog.this, "숫자 8자리입니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
}
