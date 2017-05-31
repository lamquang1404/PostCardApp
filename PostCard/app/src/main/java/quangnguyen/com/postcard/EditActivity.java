package quangnguyen.com.postcard;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.colorpicker.ColorPickerDialog;
import com.android.colorpicker.ColorPickerSwatch;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Quang Nguyen on 27/05/2017.
 */

public class EditActivity extends AppCompatActivity implements android.location.LocationListener{
    private EditText edtLocation;
    private Spinner spnFont;
    private ImageButton imgBtnColor, imgBtnSubmit;
    private TextView tvPreview;
    private int[] colors;
    public int color_selected;
    public String font_name, CityName, formattedDate;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double currentLatitude = 0;
    private double currentLongitude = 0;

    private LocationManager locationManager;
    private String provider;
    private LocationListener mLocationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        edtLocation = (EditText) findViewById(R.id.editTextLocation);
        spnFont = (Spinner) findViewById(R.id.spinnerFont);
        imgBtnColor = (ImageButton) findViewById(R.id.imageButtonColor);
        imgBtnSubmit = (ImageButton) findViewById(R.id.imageButtonSubmit);
        tvPreview = (TextView) findViewById(R.id.textViewPreview);
        colors = getResources().getIntArray(R.array.colors);
        color_selected = colors[0];
        font_name = "";
        CityName = "";
        formattedDate = getIntent().getStringExtra("Time");


        imgBtnColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ColorPickerDialog colorPickerDialog = new ColorPickerDialog();
                colorPickerDialog.initialize(
                        R.string.dialog_color, colors, color_selected, 4, colors.length);
                colorPickerDialog.show(getFragmentManager(), "haha");
                colorPickerDialog.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int color) {
                        colorPickerDialog.setSelectedColor(color);
                        color_selected = color;
                        tvPreview.setText(edtLocation.getText().toString());
                        tvPreview.setTextColor(color_selected);
                    }
                });

            }
        });
        String fonts[] = {"Atmostsphere", "Bunga Melati Putih", "Crochet Pattern", "Croissant Sandwich", "DroidSerif", "Skyrimouski"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(EditActivity.this,R.layout.support_simple_spinner_dropdown_item);
        adapter.addAll(fonts);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spnFont.setAdapter(adapter);
        spnFont.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                font_name = "fonts/" + spnFont.getSelectedItem().toString().trim() + ".ttf";
                Log.d("ololo", String.format("%s", font_name));
                Typeface custom_font = Typeface.createFromAsset(getAssets(), font_name);
                tvPreview.setText(edtLocation.getText().toString());
                tvPreview.setTypeface(custom_font);
                tvPreview.setTextSize(20);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        if (Build.VERSION.SDK_INT >= 23 &&(ContextCompat.checkSelfPermission(EditActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) &&
               (ContextCompat.checkSelfPermission(EditActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) !=  PackageManager.PERMISSION_GRANTED) { return ;}
        Location location = locationManager.getLastKnownLocation(provider);


        if (location != null) {
            Log.d("ololo", String.format("%s", "Provider " + provider + " has been selected."));
           // System.out.println("Provider " + provider + " has been selected.");
            onLocationChanged(location);
        } else {
//            latituteField.setText("Location not available");
//            longitudeField.setText("Location not available");
        }


        imgBtnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();
                Bundle bundle = new Bundle();
                bundle.putInt("Color",color_selected);
                bundle.putString("TextView",CityName+"\n"+formattedDate);
                bundle.putString("Font",font_name);
                intent.putExtra("Result", bundle);
                setResult(MainActivity.IMAGE_EDIT_OK, intent); // phương thức này sẽ trả kết quả cho Activity1
                finish(); // Đóng Activity hiện tại
            }
        });
    }





    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= 23 &&(ContextCompat.checkSelfPermission(EditActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(EditActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) !=  PackageManager.PERMISSION_GRANTED) { return ;}
        locationManager.requestLocationUpdates(provider, 400, 1, this);
        Log.d("ololo", String.format("%s", "location resume"));
    }


    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this );
        Log.d("ololo", String.format("%s", "location pause"));
    }

    @Override
    public void onLocationChanged(Location location) {
        //You had this as int. It is advised to have Lat/Loing as double.
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        Log.d("ololo", String.format("%s---%s", lat,lng));
        Geocoder geoCoder = new Geocoder(this, Locale.getDefault());

        try {
            List<Address> addresses = geoCoder.getFromLocation(lat, lng, 1);
            //String address = addresses.get(0).getSubAdminArea();
            String cityName = addresses.get(0).getLocality();
            CityName = cityName;
            edtLocation.setText(CityName +"\n" + formattedDate);
            //Log.d("ololo", String.format("%s---%s", cityName, address));
            //String stateName = addresses.get(0).getAdminArea();

        } catch (IOException e) {
            // Handle IOException
        } catch (NullPointerException e) {
            // Handle NullPointerException
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {


    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Enabled new provider " + provider,
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Disabled provider " + provider,
                Toast.LENGTH_SHORT).show();
    }

    /**
     * If connected get lat and long
     *
     */
//    @Override
//    public void onConnected(Bundle bundle) {
//        Log.d("ololo", String.format("%s", "on connect"));
//        Log.d("ololo", String.format("%s---%s", ContextCompat.checkSelfPermission(EditActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION), PackageManager.PERMISSION_GRANTED));
//        Log.d("ololo", String.format("%s---%s", ContextCompat.checkSelfPermission(EditActivity.this, Manifest.permission.ACCESS_FINE_LOCATION), PackageManager.PERMISSION_GRANTED));
//        if (Build.VERSION.SDK_INT >= 23 &&(ContextCompat.checkSelfPermission(EditActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) &&
//                (ContextCompat.checkSelfPermission(EditActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) !=  PackageManager.PERMISSION_GRANTED) { return ;}
//        else Log.d("ololo", String.format("%s", "on connect3"));
//            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//
//            if (location == null) {
//                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, EditActivity.this);
//                Log.d("ololo", String.format("%s", "on connect1"));
//
//            }
//                //If everything went fine lets get latitude and longitude
//                currentLatitude = location.getLatitude();
//                currentLongitude = location.getLongitude();
//                Geocoder geocoder = new Geocoder(EditActivity.this, Locale.getDefault());
//                Log.d("ololo", String.format("%s", "on connect2"));
//                //List<Address> addresses =geocoder.getFromLocation(latitude, longitude, 1);
//
//                try {
//                    List<Address> addresses = geocoder.getFromLocation(currentLatitude, currentLongitude, 1);
//                    String address = addresses.get(0).getSubAdminArea();
//                    String cityName = addresses.get(0).getLocality();
//                    edtLocation.setText(cityName.toString());
//                    Log.d("ololo", String.format("%s---%s", cityName, address));
////            String stateName = addresses.get(0).getAdminArea();
//
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                Toast.makeText(this, currentLatitude + " WORKS " + currentLongitude + "", Toast.LENGTH_LONG).show();
//
//
//
//    }



}
