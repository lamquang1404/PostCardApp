package quangnguyen.com.postcard;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    // Application request codes
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int IMAGE_EDIT_RESULT = 1;
    public static final int IMAGE_EDIT_OK = 2;
    // directory name to store captured images and videos
    private static final String IMAGE_DIRECTORY_NAME = "Hello Camera";

    private Uri fileUri; // file url to store image
    private Bitmap bmp_background, bmp_foreground, bmp_final;
    private String formattedDate;
    private RelativeLayout result;
    private ImageView imgPreview;
    private TextView textLocation;
    private static File mediaFile;
    private FloatingActionButton imgBtnEdit;
    private FloatingActionButton imgBtnShare;
    private OutputStream out;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent tmpintent = getIntent();
        result = (RelativeLayout) findViewById(R.id.ViewResult);
        imgPreview = (ImageView) findViewById(R.id.imgPreview);
        imgBtnEdit = (FloatingActionButton) findViewById(R.id.EditButton);
        imgBtnShare = (FloatingActionButton) findViewById(R.id.ShareButton);
        textLocation = (TextView) findViewById(R.id.TextLocation);
        out = null;

        // Checking camera availability
        if (!isDeviceSupportCamera()) {
            Toast.makeText(getApplicationContext(),
                    "Sorry! Your device doesn't support camera",
                    Toast.LENGTH_LONG).show();
            // will close the app if the device does't have camera
            finish();
        }
        Log.d("aloalo",tmpintent.getStringExtra("function"));
        if (tmpintent.getStringExtra("function").equals("take_picture")) {
            // capture picture
            captureImage();

        }
       // else if (intent.getStringExtra("function") == "select_picture") {
            // select picture
       // }

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        //formattedDate = df.format(c.getTime());
        formattedDate = df.format(new Date());
        imgBtnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditActivity.class);

                intent.putExtra("Time",formattedDate);
                startActivityForResult(intent, IMAGE_EDIT_RESULT);
            }
        });

        imgBtnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    out = new FileOutputStream(mediaFile);
                    bmp_final.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
                } catch (IOException e) {

                }
                Uri uri_final = Uri.fromFile(mediaFile);
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                shareIntent.putExtra(Intent.EXTRA_STREAM, uri_final);
                shareIntent.setType("image/jpeg");
                startActivity(Intent.createChooser(shareIntent, "Share"));
            }
        });


    }

    /**
     * Checking device has camera hardware or not
     */
    private boolean isDeviceSupportCamera() {
        if (getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /**
     * Capturing Camera Image will launch camera app request image capture
     */
    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        // start the image capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);

    }

    /**
     * Here we store the file url as it will be null after returning from camera
     * app
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on scren orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }

    /**
     * Receiving activity result method will be called after closing the camera
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // successfully captured the image
                // display it in image view
                previewCapturedImage();
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled Image capture
                Toast.makeText(getApplicationContext(),
                        "User cancelled image capture", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }
        }
        else if (requestCode == IMAGE_EDIT_RESULT){
            if (resultCode == IMAGE_EDIT_OK){
                Bundle bundle = data.getBundleExtra("Result");
                textLocation.setText(bundle.getString("TextView"));
                Typeface custom_font = Typeface.createFromAsset(getAssets(), bundle.getString("Font"));
                textLocation.setTypeface(custom_font);
                textLocation.setTextColor(bundle.getInt("Color"));
                bmp_final = Bitmap.createBitmap(result.getMeasuredWidth(), result.getMeasuredHeight(),Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bmp_final);
                result.draw(canvas);
            }
        }
    }

    /**
     * Display image from a path to ImageView
     */
    private void previewCapturedImage() {
        try {
            imgPreview.setVisibility(View.VISIBLE);

            // bimatp factory
            BitmapFactory.Options options = new BitmapFactory.Options();

            // downsizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 8;

//            final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(),
//                    options);
            bmp_background = BitmapFactory.decodeFile(fileUri.getPath(), options);

            imgPreview.setImageBitmap(bmp_background);
            Paint paint = new Paint();

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creating file uri to store image
     */
    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * return image
     */
    private static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());

        if (type == MEDIA_TYPE_IMAGE) {
           mediaFile = new File(mediaStorageDir.getPath() + File.separator
                   + "IMG_" + timeStamp + ".jpg");

        } else {
            return null;
        }

        return mediaFile;
    }
}
