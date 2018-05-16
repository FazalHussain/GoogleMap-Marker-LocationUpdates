package vra.com.vra_emergency.Models;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by fazal on 1/10/2018.
 */

public class Places implements Serializable {
    String name;
    transient LatLng location;
    String address;
    double distance;

    public Places(String name, LatLng location, String address, double distance) {
        this.name = name;
        this.location = location;
        this.address = address;
        this.distance = distance;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeDouble(location.latitude);
        out.writeDouble(location.longitude);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        location = new LatLng(in.readDouble(), in.readDouble());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}
