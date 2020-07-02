package kr.babylab.receipt.dialog;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import kr.babylab.receipt.R;

public class ListSearchDialog extends Activity {
    private ImageView cancel_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_list_search);

        cancel_button = (ImageView) findViewById(R.id.cancel_button);
        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
