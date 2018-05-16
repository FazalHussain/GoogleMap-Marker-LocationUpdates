package vra.com.vra_emergency.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import vra.com.vra_emergency.Models.Places;
import vra.com.vra_emergency.R;

/**
 * Created by fazal on 1/10/2018.
 */

public class PlacesAdapter extends ArrayAdapter<Places> {


    public PlacesAdapter(Context context, int resource, List<Places> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.place_row, null, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Places places = getItem(position);

        viewHolder.place_tv.setText(places.getName());
        String distance = String.format("%.2f", places.getDistance());
        viewHolder.distance_tv.setText(distance+" meters");

        return convertView;
    }

    public class ViewHolder{
        TextView place_tv;
        TextView distance_tv;

        public ViewHolder(View view){
            place_tv = view.findViewById(R.id.place);
            distance_tv = view.findViewById(R.id.distance);
        }
    }
}
