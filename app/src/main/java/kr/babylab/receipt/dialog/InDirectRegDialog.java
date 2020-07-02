package kr.babylab.receipt.dialog;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import kr.babylab.receipt.R;

public class InDirectRegDialog extends Activity {
    private ImageView cancel_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_in_direct_reg);

        cancel_button = (ImageView) findViewById(R.id.cancel_button);
        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
