# GoogleMap-Marker-LocationUpdates

If your app can continuously track location, it can deliver more relevant information to the user. For example, if your app helps the user find their way while walking or driving, or if your app tracks the location of assets, it needs to get the location of the device at regular intervals. As well as the geographical location (latitude and longitude), you may want to give the user further information such as the bearing (horizontal direction of travel), altitude, or velocity of the device. This information, and more, is available in the Location object that your app can retrieve from the fused location provider.

# Create location services client

private FusedLocationProviderClient mFusedLocationClient;

// ..

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    // ...

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }
    
    
# Get the last known location

    mFusedLocationClient.getLastLocation()
        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    // Logic to handle location object
                }
            }
        });
