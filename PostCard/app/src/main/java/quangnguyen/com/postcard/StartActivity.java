package quangnguyen.com.postcard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by Quang Nguyen on 16/05/2017.
 */
public class StartActivity extends Activity {
    private Button btnCapturePicture, btnSelectPicture;
    public static int CAMERA_PREVIEW_RESULT = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        btnCapturePicture = (Button) findViewById(R.id.btnCapturePicture);
        btnSelectPicture = (Button) findViewById(R.id.btnSelectPicture);

        /**
         * Capture image button click event
         */
        btnCapturePicture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // capture picture intent
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                intent.putExtra("function", "take_picture");
                startActivity(intent);
            }
        });

        /**
         * Select image button click event
         */
        btnSelectPicture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // select picture intent
//                Intent intent = new Intent(StartActivity.this, MainActivity.class);
//                intent.putExtra("function", "select_picture");
//                startActivity(intent);


            }
        });
    }


}
