package vra.com.vra_emergency;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import vra.com.vra_emergency.Adapter.PlacesAdapter;
import vra.com.vra_emergency.Models.Location;
import vra.com.vra_emergency.Models.Places;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap mMap;
    ListView lv;
    private FusedLocationProviderClient mFusedLocationClient;
    private MarkerOptions marker_origin;


    private LocationCallback mLocationCallback;
    private Marker start_prec;
    private ArrayList<Places> list_places;
    private LatLng latLng_origin;
    private LatLng latlng_target;

    final long duration = 400;
    final Handler handler = new Handler();
    final long start = SystemClock.uptimeMillis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupActionBar();

        lv = (ListView) findViewById(R.id.lv_places);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(MainActivity.this, RouteActivity.class);
                i.putExtra("latlng", list_places.get(position));
                startActivity(i);
            }
        });




        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //Initialize map fragment
        SupportMapFragment supportMapFragment = ((SupportMapFragment) getSupportFragmentManager().
                findFragmentById(R.id.map));
        supportMapFragment.getMapAsync(this);

        //location callback for updating location
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (android.location.Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    // ...
                    Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                    try {
                        String addresses = geocoder.getFromLocation(location.getLatitude(),
                                location.getLongitude(), 1).get(0).getAddressLine(0);
                        Toast.makeText(MainActivity.this, addresses, Toast.LENGTH_SHORT).show();

                        latlng_target = new LatLng(location.getLatitude(), location.getLongitude());

                        if (latlng_target!=null && latLng_origin!=null){
                            final Interpolator interpolator = new LinearInterpolator();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    long elapsed = SystemClock.uptimeMillis() - start;
                                    if (elapsed > duration) {
                                        elapsed = duration;
                                    }
                                    float t = interpolator.getInterpolation((float) elapsed / duration);
                                    double lng = t * latlng_target.longitude + (1 - t) * latLng_origin.longitude;
                                    double lat = t * latlng_target.latitude + (1 - t) * latLng_origin.latitude;
                                    start_prec.setPosition(new LatLng(lat, lng));
                                    if (t < 1.0) {
                                        // Post again 10ms later.
                                        handler.postDelayed(this, 10);
                                    } else {
                                        // animation ended
                                    }
                                }
                            });
                        }



                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }

            ;
        };


    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();

    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    private void startLocationUpdates() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null /* Looper */);
    }


    private void getPlacesData(final LatLng latlng, String type) {
        String url = "https://maps.googleapis.com/maps/api/place/search/json?location=" +
                latlng.latitude + "," + latlng.longitude + "&rankby=distance&types=" + type +
                "&key=" + getString(R.string.google_map_v2_place_api);
        new AsyncTask<String, Void, ArrayList<Places>>() {
            @Override
            protected ArrayList<Places> doInBackground(String... strings) {
                list_places = new ArrayList<Places>();

                try {
                    OkHttpClient client = new OkHttpClient();


                    Request request = new Request.Builder()
                            .url(strings[0])
                            .build();

                    Response response = client.newCall(request).execute();
                    String res = response.body().string().replace("\n", "");
                    //res = res.replace(" ","");
                    JSONObject jsonObject = new JSONObject(res);

                    if (jsonObject.getString("status").equalsIgnoreCase("OK")) {
                        JSONArray jsonArray_result = jsonObject.getJSONArray("results");

                        for (int i = 0; i < jsonArray_result.length(); i++) {
                            JSONObject jsonObject_result = jsonArray_result.getJSONObject(i);

                            JSONObject jsonObject_geometry = jsonObject_result.
                                    getJSONObject("geometry");

                            JSONObject jsonObject_location = jsonObject_geometry.
                                    getJSONObject("location");

                            String name = jsonObject_result.getString("name");
                            String address = jsonObject_result.getString("vicinity");
                            String lat = jsonObject_location.getString("lat");
                            String lng = jsonObject_location.getString("lng");

                            LatLng latlng_destination = new LatLng(Double.parseDouble(lat),
                                    Double.parseDouble(lng));

                            double lat1 = latlng.latitude;
                            double lng1 = latlng.longitude;

                            double lat2 = latlng_destination.latitude;
                            double lng2 = latlng_destination.longitude;

                            float[] dist = new float[1];

                            android.location.Location.distanceBetween(lat1, lng1, lat2, lng2, dist);




                            list_places.add(new Places(name, latlng_destination, address, dist[0]));


                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }


                return list_places;
            }

            @Override
            protected void onPostExecute(ArrayList<Places> places) {
                super.onPostExecute(places);

                if (places != null && places.size() > 0) {

                    int height = 50;
                    int width = 45;
                    BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().
                            getDrawable(R.drawable.marker);
                    Bitmap b = bitmapdraw.getBitmap();
                    Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                    for (int i = 0; i < places.size(); i++) {
                        mMap.addMarker(new MarkerOptions().position(places.get(i).getLocation())
                                .title(places.get(i).getName()).icon(BitmapDescriptorFactory.
                                        fromBitmap(smallMarker)));
                    }

                    PlacesAdapter adapter = new PlacesAdapter(MainActivity.this, R.layout.activity_main, places);
                    lv.setAdapter(adapter);
                }
            }
        }.execute(url);
    }




    //actionbar setup
    private void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable
                (getResources().getColor(R.color.red)));
        TextView tv_title = toolbar.findViewById(R.id.title);
        tv_title.setText("EMERGENCY");


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        getCurrentLocation();


    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(MainActivity.this, new OnSuccessListener<android.location.Location>() {
                    @Override
                    public void onSuccess(android.location.Location location) {
                        if(location!=null){
                            try{
                                latLng_origin = new LatLng(location.getLatitude(), location.getLongitude());

                                Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());

                                String addresses = geocoder.getFromLocation(location.getLatitude(),
                                        location.getLongitude(), 1).get(0).getAddressLine(0);

                                marker_origin = new MarkerOptions().
                                        position(latLng_origin).title(addresses)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                                start_prec = mMap.addMarker(marker_origin);
                                //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng_origin, 16.0f));
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng_origin, 16.0f));


                                getPlacesData(latLng_origin, "restaurant");
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }

}
