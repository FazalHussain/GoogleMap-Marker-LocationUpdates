package vra.com.vra_emergency;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import vra.com.vra_emergency.Models.Places;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class RouteActivity extends AppCompatActivity implements OnMapReadyCallback {

    private FusedLocationProviderClient mFusedLocationClient;
    GoogleMap mMap;
    private Marker start_prec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_route1);

        //Initialize map fragment
        SupportMapFragment supportMapFragment = ((SupportMapFragment) getSupportFragmentManager().
                findFragmentById(R.id.map));
        supportMapFragment.getMapAsync(this);

        //Initialize fusedLocationClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
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
                .addOnSuccessListener(RouteActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(android.location.Location location) {
                        if(location!=null){
                            try{
                                LatLng latLng_origin = new LatLng(location.getLatitude(), location.getLongitude());

                                Geocoder geocoder = new Geocoder(RouteActivity.this, Locale.getDefault());

                                String addresses = geocoder.getFromLocation(location.getLatitude(),
                                        location.getLongitude(), 1).get(0).getAddressLine(0);

                                MarkerOptions marker_origin = new MarkerOptions().
                                        position(latLng_origin).title(addresses)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                                start_prec = mMap.addMarker(marker_origin);
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng_origin, 16.0f));
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng_origin, 16.0f));

                                if(getIntent().getSerializableExtra("latlng") != null){
                                    Places place = (Places) getIntent().getSerializableExtra("latlng");
                                    LatLng latlng_destination = place.getLocation();
                                    addresses = place.getName();
                                    MarkerOptions marker_destiantion = new MarkerOptions().
                                            position(latlng_destination).title(addresses)
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                                    mMap.addMarker(marker_destiantion);

                                    drawDirection(latLng_origin, latlng_destination);
                                }





                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }

    private void drawDirection(LatLng latLng_origin, LatLng latlng_destination) {
        try{

            List<LatLng> path = new ArrayList();

            //Execute Directions API request
            GeoApiContext context = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.google_map_v2_place_api))
                    .build();

            String origin = latLng_origin.latitude + "," + latLng_origin.longitude;
            String destination = latlng_destination.latitude + "," + latlng_destination.longitude;

            DirectionsApiRequest req = DirectionsApi.getDirections(context, origin, destination);

            DirectionsResult res = req.await();

            //Loop through legs and steps to get encoded polylines of each step
            if (res.routes != null && res.routes.length > 0) {
                DirectionsRoute route = res.routes[0];
                if (route.legs!=null){
                    for(int i =0; i<route.legs.length; i++ ){
                        DirectionsLeg legs = route.legs[i];

                        if(legs!=null){
                            for (int j=0; j<legs.steps.length; j++){
                                DirectionsStep steps = legs.steps[j];

                                if(steps.steps!=null && steps.steps.length > 0){
                                    for (int k=0; k<steps.steps.length; k++){
                                        DirectionsStep step1 = steps.steps[k];
                                        EncodedPolyline points1 = step1.polyline;

                                        if(points1 != null){
                                            //Decode polyline and add points to list of route coordinates

                                            List<com.google.maps.model.LatLng> cord1 = points1.decodePath();
                                            for (com.google.maps.model.LatLng coord1 : cord1){
                                                path.add(new LatLng(coord1.lat, coord1.lng));
                                            }
                                        }
                                    }

                                }else {
                                    EncodedPolyline points1 = steps.polyline;

                                    if(points1 != null){
                                        //Decode polyline and add points to list of route coordinates

                                        List<com.google.maps.model.LatLng> cord1 = points1.decodePath();
                                        for (com.google.maps.model.LatLng coord1 : cord1){
                                            path.add(new LatLng(coord1.lat, coord1.lng));
                                        }
                                    }
                                }
                            }
                        }

                    }
                }

            }

            if (path.size() > 0) {
                PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.BLUE).width(10);
                mMap.addPolyline(opts);
            }

        }catch (Exception e){
            e.printStackTrace();
        }


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        getCurrentLocation();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


}
